package de.hbznrw.ygor.tools

import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils
import org.apache.commons.validator.routines.UrlValidator

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Matcher
import java.util.regex.Pattern

@Log4j
class UrlToolkit {

  static final String DATESTAMP_PLACEHOLDER = "{YYYY_MM_DD}"
  static final String DATESTAMP_REGEX = "(.*[\\W_])([\\d]{4}-[\\d]{2}-[\\d]{2})([\\W_].*)"
  static final String DATESTAMP_PLACEHOLDER_REGEX = "(.*[\\W_])(\\{?YYYY_MM_DD}?)([\\W_].*)"
  static final Pattern DATESTAMP_REGEX_PATTERN = Pattern.compile(DATESTAMP_REGEX)
  static final Pattern DATESTAMP_PLACEHOLDER_REGEX_PATTERN = Pattern.compile(DATESTAMP_PLACEHOLDER_REGEX)


  static boolean urlExists(URL url){
    HttpURLConnection huc = (HttpURLConnection) url.openConnection()
    return HttpURLConnection.HTTP_OK.equals(huc.getResponseCode())
  }


  static def getURLWithProtocol(String str) {

    def url = UrlToolkit.buildUrl(str)
    if (url) {
      return url
    }
    else if (str) {
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
    }
    catch (Exception e) {
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


  static boolean containsDateStamp(String url){
    if (StringUtils.isEmpty(url)){
      return false
    }
    if (url.matches(DATESTAMP_REGEX)){
      return true
    }
    return false
  }


  static boolean containsDateStampPlaceholder(String url){
    if (StringUtils.isEmpty(url)){
      return false
    }
    if (url.contains(DATESTAMP_PLACEHOLDER)){
      return true
    }
    return false
  }


  static List<URL> getUpdateUrlList(String url, String from){
    return getUpdateUrlList(url, from, LocalDate.now())
  }


  static List<URL> getUpdateUrlList(String url, String from, LocalDate lastDate){
    List<URL> result = new ArrayList<>()
    Matcher urlMatcher = DATESTAMP_REGEX_PATTERN.matcher(url)
    if (!urlMatcher.matches()){
      urlMatcher = DATESTAMP_PLACEHOLDER_REGEX_PATTERN.matcher(url)
    }
    if (urlMatcher.matches()){
      String prefix = urlMatcher.group(1)
      String appendix = urlMatcher.group(3)
      LocalDateTime fromDateTime = DateToolkit.fromString(from)
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      fromDateTime.format(dateFormatter)
      UrlValidator urlValidator = new UrlValidator()
      for (LocalDate date = fromDateTime; date.isBefore(lastDate); date = date.plusDays(1)){
        String urlString = prefix.concat(date.format(dateFormatter)).concat(appendix)
        if (urlValidator.isValid(urlString)){
          result.add(new URL(urlString))
        }
      }
    }
    return result
  }

}
