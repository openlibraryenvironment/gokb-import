package ygor

import de.hbznrw.ygor.tools.JsonToolkit
import de.hbznrw.ygor.tools.UrlToolkit
import groovy.util.logging.Log4j

@Log4j
class AutoUpdateService {

  static EnrichmentController ENRICHMENT_CONTROLLER = new EnrichmentController()
  static EnrichmentService ENRICHMENT_SERVICE = new EnrichmentService()

  static void addEnrichmentJob(Enrichment enrichment){
    String fileName = enrichment.originPathName.concat("_").concat(UUID.randomUUID().toString())
    FileWriter fileWriter = new FileWriter(grails.util.Holders.grailsApplication.config.ygor.autoUpdateJobsLocation.concat(fileName))
    fileWriter.write(enrichment.asJson(false))
    fileWriter.close()
  }


  static void processUpdateConfiguration(File updateFile) throws Exception{
    Enrichment enrichment = Enrichment.fromJsonFile(updateFile, false)
    List<URL> updateUrls = getUpdateUrls(enrichment)
    for (URL updateUrl in updateUrls){
      if (UrlToolkit.urlExists(updateUrl)){
        enrichment.updateUrl = updateUrl
        log.info("Start automatic update for : ".concat(updateFile.absolutePath).concat(" with URL : ").concat(updateUrl))
        processUpdate(enrichment)
      }
    }
  }


  static List<URL> getUpdateUrls(Enrichment enrichment){
    String url = enrichment.originUrl
    String lastProcessingDate = enrichment.lastProcessingDate
    if (UrlToolkit.containsDateStamp(url) || UrlToolkit.containsDateStampPlaceholder(url)){
      return UrlToolkit.getUpdateUrlList(url, lastProcessingDate)
    }
    else{
      return Arrays.asList(new URL(url))
    }
  }


  static void processUpdate(File updateConfiguration) throws Exception{
    log.info("Start automatic update for : ".concat(updateConfiguration.absolutePath))
    Enrichment enrichment = Enrichment.fromRawJson(JsonToolkit.jsonNodeFromFile(updateConfiguration), false)
    processUpdate(enrichment)
  }


  static void processUpdate(Enrichment enrichment) throws Exception{
    UploadJob uploadJob = ENRICHMENT_SERVICE.buildCompleteUpdateProcess(enrichment)
    ENRICHMENT_CONTROLLER.watchUpload(uploadJob, Enrichment.FileType.PACKAGE_WITH_TITLEDATA, enrichment.resultName)
  }
}
