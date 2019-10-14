package de.hbznrw.ygor.tools

import java.security.MessageDigest
import java.nio.file.Paths
import org.codehaus.groovy.runtime.DateGroovyMethods
import org.springframework.core.io.Resource
import grails.util.Holders

class FileToolkit {

  static String getDateTimePrefixedFileName(filename) {
    def p1 = Paths.get(filename).getParent()
    def p2 = DateGroovyMethods.format(new Date(), 'yyyyMMdd-HHmm-')
    def p3 = Paths.get(filename).getFileName()

    if (null != p1)
      return p1.toString() + File.separator + p2 + p3.toString()

    p2 + p3.toString()
  }

  static String getMD5Hash(String s) {
    MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8")).encodeHex().toString()
  }

  static Resource getResourceByClassPath(String path) {
    Holders.getGrailsApplication().parentContext.getResource('classpath:' + path)
  }
}
