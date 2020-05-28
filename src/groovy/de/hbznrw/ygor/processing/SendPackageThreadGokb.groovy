package de.hbznrw.ygor.processing

import de.hbznrw.ygor.export.GokbExporter
import groovy.util.logging.Log4j
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import ygor.Enrichment

import javax.annotation.Nonnull
import java.util.regex.Matcher
import java.util.regex.Pattern

@Log4j
class SendPackageThreadGokb extends UploadThreadGokb{

  def grailsApplication
  final static Pattern INT_FROM_MESSAGE_REGEX = Pattern.compile("with (\\d+) TIPPs")
  String gokbJobId
  Map gokbStatusResponse

  SendPackageThreadGokb(def grailsApplication, @Nonnull Enrichment enrichment, @Nonnull String uri,
                        @Nonnull String user, @Nonnull String password){
    this.grailsApplication = grailsApplication
    this.enrichment = enrichment
    this.uri = uri
    this.user = user
    this.password = password
    total += enrichment.yellowRecords?.size()
    total += enrichment.greenRecords?.size()
    result = []
    gokbStatusResponse = [:]
  }


  @Override
  void run(){
    def json = enrichment.getAsFile(Enrichment.FileType.PACKAGE, true)
    log.info("exportFile: " + enrichment.resultHash + " -> " + uri)
    String body = json.getText()
    result << GokbExporter.sendText(uri, body, user, password)
  }


  void updateCount(){
    String jobId = getJobId()
    String message = getGokbResponseValue(jobId, "job_result.message")
    Matcher matcher = INT_FROM_MESSAGE_REGEX.matcher(message)
    if (matcher.find()){
      Integer foundInt = Integer.valueOf(matcher.group(1))
      if (foundInt != null){
        count = foundInt
      }
    }
  }


  private String getJobId(){
    if (gokbJobId == null && result != null){
      gokbJobId = result[0]?.get("info")?.get("job_id")
    }
    return gokbJobId
  }


  @Override
  String getGokbResponseValue(String jobId, String responseKey){
    gokbStatusResponse = getGokbStatusResponse(jobId)
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
    if (user == null || password == null){
      return null
    }
    def uri = grailsApplication.config.gokbApi.xrJobInfo.toString().concat("/").concat(jobId)
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
        results.put(String.valueOf(i), error.toString())
      }
    }
    return results
  }
}