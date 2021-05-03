package de.hbznrw.ygor.normalizers

import org.apache.commons.lang.StringUtils


class StringNormalizer {

  static String normalizeString(String orgValue, boolean isTitleString) {
    String result = CommonNormalizer.removeSpaces(orgValue)
    if (isTitleString) {
      result = normalizeStringTitle(result)
    }
    result
  }


  static String normalizeAbbreviation(String orgValue) {
    return CommonNormalizer.removeSpaces(orgValue)
  }


  /**
   * Removes special chars. So far: removes "@" occuring after " " and in front of word character.
   * Returns null if null given.
   * Returns "" if empty string given
   * @param str
   * @return
   */
  static String normalizeStringTitle(String str) {
    if (!str) {
      return str
    }
    str = str.replaceAll(" @(\\w)", ' $1')
  }


  /**
   * Some ZDB values are put into brackets to be marked as unsafe. In some cases, we would want these values to be
   * processed and hence remove the brackets.
   * @param zdbValue The possibly unsafe ZDB value.
   * @return The parameter given minus the brackets, if existing. The parameter itself in any other case.
   */
  static String getSafeVersionOfZdbValue(String zdbValue){
    if (!StringUtils.isEmpty(zdbValue) && zdbValue.startsWith("[") && zdbValue.endsWith("]")){
      return zdbValue.substring([1, zdbValue.length()-1])
    }
    // else
    return zdbValue
  }

}
