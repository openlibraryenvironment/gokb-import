import grails.util.Environment


class BootStrap {

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
  }

  def destroy = {
    log.info('I\'ll leave')
  }
}
