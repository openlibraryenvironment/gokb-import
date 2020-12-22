package ygor

import grails.transaction.Transactional

@Transactional
class SessionService {

  static void setSessionDuration(def request, int seconds) {
    request.getSession(true).setMaxInactiveInterval(seconds)
  }

}
