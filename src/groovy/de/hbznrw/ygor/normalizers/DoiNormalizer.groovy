package de.hbznrw.ygor.normalizers

class DoiNormalizer {

  static String normalizeDoi(String orgValue) {
    String result = CommonNormalizer.removeSpaces(orgValue)
    if (result.startsWith("10.")) {
      result = "https://doi.org/".concat(result)
    }
    result
  }
}
