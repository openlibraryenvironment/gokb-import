package de.hbznrw.ygor.normalizers

import org.apache.commons.lang.StringUtils

import java.util.regex.Matcher
import java.util.regex.Pattern

class AmountNormalizer {

  final static Pattern CURRENCY_PRE_PATTERN =
      Pattern.compile("(^(EUR|GBP|USD))(.*)", Pattern.CASE_INSENSITIVE)
  final static Pattern CURRENCY_POST_PATTERN =
      Pattern.compile("(.*)((EUR|GBP|USD)\$)", Pattern.CASE_INSENSITIVE)
  final static Pattern AMOUNT_PATTERN =
      Pattern.compile("[\\d]+([\\.,][\\d]{2})?")


  static String normalizeAmount(String str) {
    if (StringUtils.isEmpty(str)){
      return ""
    }
    // remove eventual currency
    Matcher matcher = CURRENCY_PRE_PATTERN.matcher(str)
    if (matcher.matches()) {
      str = matcher.group(3)
    }
    matcher = CURRENCY_POST_PATTERN.matcher(str)
    if (matcher.matches()) {
      str = matcher.group(1)
    }
    //
    str = str.trim()
    // check amount format
    matcher = AMOUNT_PATTERN.matcher(str)
    if (matcher.matches()) {
      return str.replace(",", ".")
    }
    // else: by now, we don't accept any irregularities in numbers
    return ""
  }

}
