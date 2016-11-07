import grails.util.Environment


class BootStrap {

    def init = { servletContext ->
		
			println('''

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
			println('Environment: ' + Environment.current)
    }
	
    def destroy = {
		println('I\'ll leave')
    }
}
