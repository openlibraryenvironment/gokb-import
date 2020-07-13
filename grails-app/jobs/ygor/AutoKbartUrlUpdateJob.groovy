package ygor

import groovy.util.logging.Log4j

import java.time.LocalTime

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
    File updateSessionFolder = new File(updatesContainer.absolutePath.concat(LocalTime.now().toString()))

    File[] updateConfigurations = updatesContainer.listFiles()
    for (File updateFile in updateConfigurations){
      if (AutoUpdateService.urlHasBeenUpdated(updateFile)){
        log.info("Start automatic update for : ".concat(updateFile.absolutePath))
        try{
          AutoUpdateService.processUpdate(updateFile, updateSessionFolder)
        }
        catch (Exception e){
          log.error("Exception occurred while trying to auto-update : ".concat(updateFile.absolutePath))
          e.printStackTrace()
        }
      }
      else{
        log.info("No URL update found for : ".concat(updateFile.absolutePath))
      }
    }
  }
}
