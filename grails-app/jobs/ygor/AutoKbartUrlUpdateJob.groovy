package ygor

import groovy.util.logging.Log4j

@Log4j
class AutoKbartUrlUpdateJob {

  static def schedule(ConfigObject configObject, Map map){
    return false
  }

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
        AutoUpdateService.processUpdateConfiguration(updateFile)
      }
      catch(Exception e){
        log.error("Exception occurred while trying to auto-update : ".concat(updateFile.absolutePath))
        e.printStackTrace()
      }
    }
  }
}
