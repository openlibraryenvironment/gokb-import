package ygor

import de.hbznrw.ygor.tools.UrlToolkit
import groovy.util.logging.Log4j

@Log4j
class AutoKbartUrlUpdateJob {

  def execute(context){
    log.info("Start AutoKbartUrlUpdateJob.")
    File updatesContainer = new File(context.jobDataMap.get("updatesContainer"))
    if (!updatesContainer.exists()){
      log.debug("Trying to create updates directory.")
      updatesContainer.mkdirs()
      if (!updatesContainer.exists()){
        log.error("Could not create updates directory. Please create manually.")
        return
      }
    }
    File[] updateConfigurations = updatesContainer.listFiles()
    for (File updateFile in updateConfigurations){
      try{
        Enrichment enrichment = Enrichment.fromFilename(updateFile.getAbsolutePath())
        List<URL> updateUrls = AutoUpdateService.getUpdateUrls(updateFile)
        for (URL updateUrl in updateUrls){
          if (UrlToolkit.urlExists(updateUrl)){
            enrichment.updateUrl = updateUrl
            log.info("Start automatic update for : ".concat(updateFile.absolutePath).concat(" with URL : ").concat(updateUrl))
            AutoUpdateService.processUpdate(enrichment)
          }
        }
      }
      catch (Exception e){
        log.error("Exception occurred while trying to auto-update : ".concat(updateFile.absolutePath))
        e.printStackTrace()
      }
    }
  }
}
