package ygor

import de.hbznrw.ygor.tools.JsonToolkit

class AutoUpdateService {

  static EnrichmentController ENRICHMENT_CONTROLLER = new EnrichmentController()
  static EnrichmentService ENRICHMENT_SERVICE = new EnrichmentService()

  static void addEnrichmentJob(Enrichment enrichment){
    String fileName = enrichment.originPathName.concat("_").concat(UUID.randomUUID().toString())
    FileWriter fileWriter = new FileWriter(grails.util.Holders.grailsApplication.config.ygor.autoUpdateJobsLocation.concat(fileName))
    fileWriter.write(enrichment.asJson(false))
    fileWriter.close()
  }

  static boolean urlHasBeenUpdated(File checkForUpdate){
    return true // DUMMY --> TODO
  }

  static void processUpdate(File updateConfiguration, File updateSessionFolder) throws Exception{
    Enrichment enrichment = Enrichment.fromRawJson(JsonToolkit.jsonNodeFromFile(updateConfiguration), false)
    UploadJob uploadJob = ENRICHMENT_SERVICE.processCompleteUpdate(enrichment)
    ENRICHMENT_CONTROLLER.watchUpload(uploadJob, Enrichment.FileType.PACKAGE_WITH_TITLEDATA, enrichment.resultName)
  }
}
