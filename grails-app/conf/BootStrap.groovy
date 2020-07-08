import grails.util.Environment
import ygor.AutoKbartUrlUpdateJob

class BootStrap {

  def grailsApplication

  def init = { servletContext ->

    log.info('''

  ▓██   ██▓  ▄████  ▒█████   ██▀███  
   ▒██  ██▒ ██▒ ▀█▒▒██▒  ██▒▓██ ▒ ██▒
    ▒██ ██░▒██░▄▄▄░▒██░  ██▒▓██ ░▄█ ▒
    ░ ▐██▓░░▓█  ██▓▒██   ██░▒██▀▀█▄  
    ░ ██▒▓░░▒▓███▀▒░ ████▓▒░░██▓ ▒██▒
     ██▒ ▒  ░▒   ▒ ░ ▒░▒░▒░ ░ ▒▓ ░▒▓░
   ▓██ ░░   ░   ░   ░ ▒  ░    ░ ░ ░
   ░░                     
                       Yes, Master?
			''')
    log.info('Environment: ' + Environment.current)

    Map<String, String> params = new HashMap<>()
    params.put("updatesContainer", grailsApplication.config.ygor.autoUpdateJobsLocation)
    AutoKbartUrlUpdateJob.schedule(
        grailsApplication.config.ygor.autoUpdateJobsInterval, params)
  }

  def destroy = {
    log.info('I\'ll leave')
  }
}
