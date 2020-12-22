package ygor

class HomeController {

  def index() {
    SessionService.setSessionDuration(request, 600)
    render(view: '/index')
  }


  def checkAvailability() {
    render("Available.")
  }

}
