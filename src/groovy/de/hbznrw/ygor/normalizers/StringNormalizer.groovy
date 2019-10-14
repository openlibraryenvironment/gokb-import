package de.hbznrw.ygor.normalizers


class StringNormalizer {

  static String normalizeString(String orgValue, boolean isTitleString) {
    String result = CommonNormalizer.removeSpaces(orgValue)
    if (isTitleString) {
      result = normalizeStringTitle(result)
    }
    result
  }


  /**
   * Removes special chars. So far: removes "@" occuring after " " and in front of word character.
   * Returns null if null given.
   * Returns "" if empty string given
   *
   * @param str
   * @return
   */
  static String normalizeStringTitle(String str) {
    if (!str) {
      return str
    }
    str = str.replaceAll(" @(\\w)", ' $1')
  }

}
