package de.hbznrw.ygor.processing

import de.hbznrw.ygor.export.GokbExporter
import grails.util.Holders
import groovy.util.logging.Log4j
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import ygor.AutoUpdateService
import ygor.Enrichment

import javax.annotation.Nonnull
import java.util.regex.Matcher
import java.util.regex.Pattern

@Log4j
class SendPackageThreadGokb extends UploadThreadGokb{

  final static Pattern INT_FROM_MESSAGE_REGEX = Pattern.compile("with (\\d+) TIPPs")
  String gokbJobId
  Map gokbStatusResponse
  boolean integrateWithTitleData
  boolean isUpdate

  SendPackageThreadGokb(@Nonnull Enrichment enrichment, @Nonnull String uri, @Nonnull String user,
                        @Nonnull String password, boolean integrateWithTitleData){
    this.enrichment = enrichment
    this.uri = uri
    this.user = user
    this.password = password
    total += enrichment.yellowRecords?.size()
    total += enrichment.greenRecords?.size()
    result = []
    gokbStatusResponse = [:]
    this.locale = enrichment.locale
    this.integrateWithTitleData = integrateWithTitleData
    this.isUpdate = enrichment.isUpdate
  }

  SendPackageThreadGokb(@Nonnull Enrichment enrichment, @Nonnull String uri, boolean integrateWithTitleData){
    this.enrichment = enrichment
    this.uri = uri
    total += enrichment.yellowRecords?.size()
    total += enrichment.greenRecords?.size()
    result = []
    gokbStatusResponse = [:]
    this.locale = enrichment.locale
    this.isUpdate = enrichment.isUpdate
    this.integrateWithTitleData = integrateWithTitleData
  }


  @Override
  void run(){
    def json
    if (integrateWithTitleData){
      json = enrichment.getAsFile(Enrichment.FileType.PACKAGE_WITH_TITLEDATA, true)
    }
    else{
      json = enrichment.getAsFile(Enrichment.FileType.PACKAGE, true)
    }
    log.info("exportFile: " + enrichment.resultHash + " -> " + uri)
    if (isUpdate){
      result << GokbExporter.sendUpdate(uri.concat("/").concat(enrichment.dataContainer?.pkgHeader?.uuid), json.getText(), locale)
    }
    else{
      result << GokbExporter.sendText(uri, json.getText(), user, password, locale)
    }
  }


  void updateCount(){
    String message = getGokbResponseValue("job_result.message", true)
    if (message != null){
      // get count from finished process
      Matcher matcher = INT_FROM_MESSAGE_REGEX.matcher(message)
      if (matcher.find()){
        Integer foundInt = Integer.valueOf(matcher.group(1))
        if (foundInt != null){
          count = foundInt
        }
      }
      String token = getGokbResponseValue("job_result.updateToken", false)
      String uuid = getGokbResponseValue("job_result.uuid", false)
      if (token != null && uuid != null){
        enrichment.dataContainer?.pkgHeader?.token = token
        enrichment.dataContainer?.pkgHeader?.uuid = uuid
        if (enrichment.autoUpdate == true){
          AutoUpdateService.addEnrichmentJob(enrichment)
        }
      }
    }
    else{
      String error = getGokbResponseValue("error", false)
      if (error != null){
        count = total
      }
      else{
        // get count from ongoing process
        String countString = getGokbResponseValue("progress", false)
        if (countString != null){
          count = Double.valueOf(countString) / 100.0 * total
        }
      }
    }
  }


  boolean isInterrupted(){
    String message = getGokbResponseValue("job_result.message", true)
    if (message != null && message.contains("tipps have not been loaded because of validation errors")){
      return true
    }
    message = getGokbResponseValue("result", false)
    if (message != null && message.contains("error")){
      return true
    }
    // else
    return false
  }


  private String getJobId(){
    if (gokbJobId == null && result != null && result.size() > 0 && result[0].get("info") != null){
      gokbJobId = result[0].get("info")?.get("job_id")
    }
    return gokbJobId
  }


  @Override
  String getGokbResponseValue(String responseKey, boolean updateResponse){
    def jobId = getJobId()
    if (jobId == null){
      return null
    }
    if (updateResponse){
      gokbStatusResponse = getGokbStatusResponse(jobId)
    }
    String[] path = responseKey.split("\\.")
    def response = gokbStatusResponse
    for (String subField in path){
      response = response.get(subField)
      if (response == null){
        break
      }
    }
    return response
  }


  protected Map getGokbStatusResponse(String jobId){
    if (user == null || password == null || jobId == null){
      return null
    }
    def uri = Holders.config.gokbApi.xrJobInfo.toString().concat("/").concat(jobId)
    def http = new HTTPBuilder(uri)
    Map<String, Object> result = new HashMap<>()
    http.auth.basic user, password
    http.request(Method.GET, ContentType.JSON){ req ->
      response.success = { response, resultMap ->
        if (response.headers.'Content-Type' == 'application/json;charset=UTF-8'){
          if (response.status < 400){
            if (resultMap.result.equals("ERROR")){
              result.put('responseStatus', 'error')
              result.putAll(resultMap)
            }
            else{
              result.put('responseStatus', 'ok')
              result.putAll(resultMap)
            }
          }
          else{
            result.put('responseStatus', 'warning')
            result.putAll(resultMap)
          }
        }
        else{
          result.put('responseStatus', 'authenticationError')
        }
      }
      response.failure = { response, resultMap ->
        if (response.headers.'Content-Type' == 'application/json;charset=UTF-8'){
          result.put('responseStatus', 'error')
          result.putAll(resultMap)
        }
        else{
          result.put('responseStatus', 'authenticationError')
        }
      }
      response.'401'= {resp ->
        result.put('responseStatus', 'authenticationError')
      }
    }
    result
  }


  @Override
  Map getResultsTable(){
    Map results = [:]
    results.put("listDocuments.gokb.response.type", "listDocuments.gokb.response.package")
    results.put("listDocuments.gokb.response.status", gokbStatusResponse.get("result"))
    Map jobResult = gokbStatusResponse.get("job_result")
    if (jobResult != null){
      results.put("listDocuments.gokb.response.message", jobResult.get("message"))
      results.put("listDocuments.gokb.response.ok", String.valueOf(count))
      results.put("listDocuments.gokb.response.error", jobResult.get("errors")?.size())
      int i=1
      for (def error in jobResult.get("errors")){
        results.put(String.valueOf(i++), error.toString())
      }
    }
    if ("ERROR" == gokbStatusResponse.get("result")){
      results.put("listDocuments.gokb.response.message", gokbStatusResponse.get("message"))
    }
    if ("ERROR" == gokbStatusResponse.get("job_result")?.get("result")){
      results.put("listDocuments.gokb.response.message", gokbStatusResponse.get("job_result").get("message"))
      results.put("listDocuments.gokb.response.status", "ERROR")
    }
    return results
  }
}