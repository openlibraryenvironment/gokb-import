package de.hbznrw.ygor.processing

import de.hbznrw.ygor.export.GokbExporter
import grails.converters.JSON
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

  final static Pattern INT_FROM_MESSAGE_REGEX = Pattern.compile("with (\\d+) TIPPs")
  String gokbJobId

  SendPackageThreadGokb(@Nonnull Enrichment enrichment, @Nonnull String uri,
                        @Nonnull String user, @Nonnull String password){
    this.enrichment = enrichment
    this.uri = uri
    this.user = user
    this.password = password
    total += enrichment.yellowRecords?.size()
    total += enrichment.greenRecords?.size()
    result = []
  }


  @Override
  void run(){
    def json = enrichment.getAsFile(Enrichment.FileType.PACKAGE, true)
    log.info("exportFile: " + enrichment.resultHash + " -> " + uri)
    String body = json.getText()
    result << GokbExporter.sendText(uri, body, user, password)
  }


  private String getJobId(ArrayList response){
    for (def responseItem in response){
      for (def value in responseItem.values()){
        gokbJobId = String.valueOf(value.get("job_id"))
      }
    }
  }


  @Override
  def getJobInfo(Map<String, Object> infoMap){
    def http = new HTTPBuilder(uri)
    http.auth.basic user, password

    http.request(Method.GET, ContentType.JSON){ req ->
      response.success = { response, resultMap ->
        if (response.headers.'Content-Type' == 'application/json;charset=UTF-8'){
          if (response.status < 400){
            if (resultMap.result.equals("ERROR")){
              infoMap.put('error', resultMap.message)
            }
            else{
              infoMap.putAll(getResponseSorted(resultMap))
            }
          }
          else{
            infoMap.put('warning': resultMap)
          }
        }
        else{
          infoMap.putAll(handleAuthenticationError(response))
        }
      }
      response.failure = { response, resultMap ->
        log.error("GOKb server response: ${response.statusLine}")
        if (response.headers.'Content-Type' == 'application/json;charset=UTF-8'){
          infoMap.put('error': resultMap)
        }
        else{
          infoMap.putAll(handleAuthenticationError(response))
        }
      }
      response.'401' = { resp ->
        infoMap.putAll(handleAuthenticationError(resp))
      }
    }
    return infoMap as JSON
  }


  protected Map getResponseSortedDetails(){
    Map sortedDetails = [:]
    def jobResult = result.get("job_result")
    String message = jobResult?.get("message")
    if (message != null){
      sortedDetails.put("response_message", message)
    }
    int error = jobResult?.get("errors") != null ? jobResult?.get("errors")?.size() : 0
    int ok = jobResult?.get("results") != null ? jobResult?.get("results")?.size() : 0
    if (ok == 0){
      // package update --> get "OK" information from message string
      Matcher matcher = INT_FROM_MESSAGE_REGEX.matcher(message)
      if (matcher.find()){
        ok = Integer.valueOf(matcher.group(1))
      }
    }
    sortedDetails.put("response_ok", ok.toString())
    sortedDetails.put("response_error", error.toString())
    sortedDetails
  }


  private Map handleAuthenticationError(response){
    log.error("GOKb server response: ${response.statusLine}")
    return ['error': ['message': "Authentication error!", 'result': "ERROR"]]
  }


  @Override
  int getCount(){
    // TODO --> get "size" of result
  }

}