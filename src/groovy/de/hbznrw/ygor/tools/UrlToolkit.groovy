package de.hbznrw.ygor.tools

import groovy.util.logging.Log4j

@Log4j
class UrlToolkit {

  static final HARD_CHECK = 'HARD_CHECK'
  static final ONLY_HIGHEST_LEVEL_DOMAIN = 'ONLY_HIGHEST_LEVEL_DOMAIN'

  static def getURL(String str) {
    UrlToolkit.buildUrl(str)
  }

  static def getURLWithProtocol(String str) {

    def url = UrlToolkit.buildUrl(str)
    if (url) {
      return url
    } else if (str) {
      return UrlToolkit.buildUrl('http://' + str)
    }
    null
  }

  static def getURLAuthority(String str) {

    def url = UrlToolkit.getURLWithProtocol(str)
    if (url) {
      return url.getAuthority()
    }
    null
  }

  static def getURLAuthorityWithProtocol(String str) {

    def url = UrlToolkit.getURLWithProtocol(str)
    if (url) {
      return url.getProtocol() + '://' + url.getAuthority()

    }
    null
  }


  static def buildUrl(String str) {
    def url
    try {
      url = new URL(str)
    } catch (Exception e) {
      log.error(e.getMessage())
      log.error(e.getStackTrace())
    }
    url
  }

  static sortOutBadSyntaxUrl(String url) {
    def u = UrlToolkit.getURLWithProtocol(url)
    if (u) return url
    null
  }

  static sortOutBadSyntaxUrl(ArrayList urls) {
    def result = []
    urls.each { e ->
      result << UrlToolkit.sortOutBadSyntaxUrl(e)
    }
    result.minus(null).minus("").join("|")
  }
}
