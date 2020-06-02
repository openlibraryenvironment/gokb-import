package de.hbznrw.ygor.tools

import org.codehaus.groovy.grails.web.util.WebUtils
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

class SessionToolkit {

  static HttpSession getSession() {
    HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
    HttpSession session = request.session
    session
  }
}
