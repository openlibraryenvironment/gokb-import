package de.hbznrw.ygor.normalizers


import groovy.util.logging.Log4j
import ygor.identifier.OnlineIdentifier

import java.util.regex.Pattern

@Log4j
class IdentifierNormalizer {


  static String normIdentifier(String str, String type, String namespace) {
    if (!str) {
      return str
    }
    str = StringNormalizer.normalizeString(str, false)

    if (!(type in ["titleId", "onlineIdentifier", "printIdentifier", "gokbUuid"])) {
      str = str.replaceAll(/[\/-]+/, "")
    }
    if (type in ["onlineIdentifier", "printIdentifier"]) {
      if (str.length() == 8) {
        str = new StringBuilder(str).insert(4, "-").toString()
      }
      if (str.endsWith("x")) {
        str = str.replaceAll("x\$", "X")
      }
    }
    else if (type.equals("zdbId")) {
      str = new StringBuilder(str.replaceAll("x", "X")).insert(Math.abs(str.length() - 1).toInteger(), "-").toString()
    }
    else if (type.equals("ezbId")) {
      // TODO
    }
    else if (type.equals("titleId")) {
      if (namespace.equals("doi")) {
        str = Pattern.compile("^https?://(dx\\.)?doi.org/").matcher(str).replaceAll("")
      }
    }
    else if (type.equals("inID_" + namespace)) {
      str = namespace ? str : ''
    }
    str
  }


  static def fixIdentifier(String id, Class clazz) {
    if (clazz.equals(OnlineIdentifier.class)) {
      return fixEISSN(id)
    }
    id
  }


  static def fixEISSN(String id) {
    // set the last character to upper case if it is "x" and if the rest of the eissn is valid
    if (id.matches("[\\d]{4}-[\\d]{3}x")) {
      id = id.replace("x", "X")
      log.info("Set \"x\" to upper case in " + id)
    }
    id
  }

}
