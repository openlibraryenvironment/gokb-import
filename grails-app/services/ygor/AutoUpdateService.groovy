package ygor

import de.hbznrw.ygor.processing.YgorFeedback
import de.hbznrw.ygor.tools.JsonToolkit
import de.hbznrw.ygor.tools.UrlToolkit
import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils

// TODO: delete
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


  static void processUpdateConfiguration(File updateFile, YgorFeedback ygorFeedback) throws Exception{
    Enrichment enrichment = Enrichment.fromJsonFile(updateFile, false)
    List<URL> updateUrls = getUpdateUrls(enrichment)
    for (URL updateUrl in updateUrls){
      if (UrlToolkit.urlExists(updateUrl)){
        enrichment.updateUrl = updateUrl
        log.info("Start automatic update for : ".concat(updateFile.absolutePath).concat(" with URL : ").concat(updateUrl.toExternalForm()))
        processUpdate(enrichment, ygorFeedback)
      }
    }
  }


  static List<URL> getUpdateUrls(Enrichment enrichment){
    if (enrichment == null){
      return new ArrayList<URL>()
    }
    return getUpdateUrls(enrichment.originUrl, enrichment.lastProcessingDate, null)
  }


  static List<URL> getUpdateUrls(String url, String lastProcessingDate, String packageCreationDate){
    if (StringUtils.isEmpty(lastProcessingDate)){
      lastProcessingDate = packageCreationDate
    }
    if (StringUtils.isEmpty(url)){
      return new ArrayList<URL>()
    }
    if (UrlToolkit.containsDateStamp(url) || UrlToolkit.containsDateStampPlaceholder(url)){
      return UrlToolkit.getUpdateUrlList(url, lastProcessingDate)
    }
    else{
      log.debug("No dates in URL ..")
      return Arrays.asList(new URL(url))
    }
  }


  // obsolete. TODO: delete
  static void processUpdate(File updateConfiguration, YgorFeedback ygorFeedback) throws Exception{
    log.info("Start automatic update for : ".concat(updateConfiguration.absolutePath))
    Enrichment enrichment = Enrichment.fromRawJson(JsonToolkit.jsonNodeFromFile(updateConfiguration), false)
    enrichment.isUpdate = true
    enrichment.needsPreciseClassification = false
    processUpdate(enrichment, ygorFeedback)
  }


  static void processUpdate(Enrichment enrichment, YgorFeedback ygorFeedback) throws Exception{
    enrichment.isUpdate = true
    enrichment.needsPreciseClassification = false
    UploadJob uploadJob = ENRICHMENT_SERVICE.buildCompleteUpdateProcess(enrichment, ygorFeedback)
    ENRICHMENT_CONTROLLER.watchUpload(uploadJob, Enrichment.FileType.PACKAGE_WITH_TITLEDATA, enrichment.resultName)
  }
}
