package ygor

import groovy.util.logging.Log4j

@Log4j
class AutoKbartUrlUpdateJob {

  def execute(context){
    log.debug("Start AutoKbartUrlUpdateJob.")
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

    for (File update in updateConfigurations){
      log.debug("Check AutoKbartUrlUpdateJob: ".concat(update.absolutePath))
      // TODO ...
    }
  }
}
