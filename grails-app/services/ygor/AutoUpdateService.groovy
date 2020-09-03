package ygor

import de.hbznrw.ygor.readers.KbartFromUrlReader
import de.hbznrw.ygor.tools.JsonToolkit

import java.nio.charset.Charset

class AutoUpdateService {

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
    KbartFromUrlReader kbartReader = new KbartFromUrlReader(new URL(enrichment.originUrl) ,
        null, Charset.forName("UTF-8"), updateSessionFolder)
    String kbartFileName = KbartFromUrlReader.urlStringToFileString(enrichment.originUrl)
    enrichment.originPathName = kbartFileName
    def options = [
        'ygorVersion': grails.util.Holders.grailsApplication.config.ygor.version,
        'ygorType'   : grails.util.Holders.grailsApplication.config.ygor.type
    ]
    enrichment.process(options, kbartReader)
  }
}
