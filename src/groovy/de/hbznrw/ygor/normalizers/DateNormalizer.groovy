package de.hbznrw.ygor.normalizers

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.regex.Matcher
import java.util.regex.Pattern

class DateNormalizer {

  static final String START_DATE = "StartDate"
  static final String END_DATE = "EndDate"
  static final Pattern BRACKET_PATTERN = Pattern.compile("^\\[(.*)]\$")
  static final Pattern DATE_SPAN_PATTERN = Pattern.compile(
      "^([\\d]{4}-[\\d]{4})|" +
      "([\\d]{4}-[\\d]{2}-[\\d]{4}-[\\d]{2})|" +
      "([\\d]{4}(-[\\d]{2}){2}-[\\d]{4}(-[\\d]{2}){2})|" +
      "([\\d]{2}\\.[\\d]{4}-[\\d]{2}\\.[\\d]{4})|" +
      "(([\\d]{2}\\.){2}[\\d]{4}-([\\d]{2}\\.){2}[\\d]{4})\$")
  static final Pattern DATE_SPAN_GROUPER =
      Pattern.compile("(([\\d]{2}\\.){0,2}[\\d]{4}(-[\\d]{2}){0,2})-(([\\d]{2}\\.){0,2}[\\d]{4}(-[\\d]{2}){0,2})")

  static SimpleDateFormat YYYY = new SimpleDateFormat("yyyy")
  static SimpleDateFormat YYYY_MM = new SimpleDateFormat("yyyy-MM")
  static SimpleDateFormat YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd")
  static SimpleDateFormat MM_YYYY = new SimpleDateFormat("MM.yyyy")
  static SimpleDateFormat DD_MM_YYYY = new SimpleDateFormat("dd.MM.yyyy")
  static List<SimpleDateFormat> KNOWN_DATE_FORMATS = new ArrayList()
  static{
    // the order is important
    KNOWN_DATE_FORMATS.add(YYYY_MM_DD)
    KNOWN_DATE_FORMATS.add(DD_MM_YYYY)
    KNOWN_DATE_FORMATS.add(YYYY_MM)
    KNOWN_DATE_FORMATS.add(MM_YYYY)
    KNOWN_DATE_FORMATS.add(YYYY)
  }
  static SimpleDateFormat TARGET_FORMAT = YYYY_MM_DD
  static DateTimeFormatter TARGET_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  static String normalizeDate(String str, String dateType) {
    if (!str) {
      return str
    }

    str = removeBlanksAndBrackets(str)
    str = pickPartFromDateSpan(str, dateType)
    str = completeEndDate(str, dateType)
    str = TARGET_FORMAT.format(formatDateTime(str))
    str
  }


  static String completeEndDate(String str, String dateType){
    if (dateType.equals(END_DATE)){
      if (str.matches("^[\\d]{4}\$")){
        return str.concat("-12-31")
      }
      if (str.matches("^[\\d]{4}-[\\d]{2}\$")){
        YearMonth yearMonth = new YearMonth( str.substring(0,4), Month.of(Integer.valueOf(str.substring(5))))
        LocalDate endOfMonth = yearMonth.atEndOfMonth()
        return TARGET_FORMATTER.format(endOfMonth)
      }
    }
    str
  }


  static Date formatDateTime(String date){
    for (SimpleDateFormat pattern : KNOWN_DATE_FORMATS) {
      try {
        return new Date(pattern.parse(date).getTime())
      }
      catch (ParseException pe) {}
    }
    return null
  }


  private static String pickPartFromDateSpan(String str, String dateType){
    // Take only start part or end part of something like "01.01.2000-31.12.2000"
    if (str.matches(DATE_SPAN_PATTERN)){
      Matcher dateSpanGrouperMatcher = DATE_SPAN_GROUPER.matcher(str)
      dateSpanGrouperMatcher.matches()
      if (dateType.equals(START_DATE)){
        str = dateSpanGrouperMatcher.group(1)
      }
      else if (dateType.equals(END_DATE)){
        str = dateSpanGrouperMatcher.group(4)
      }
    }
    str
  }


  private static String removeBlanksAndBrackets(String str){
    str = str.replaceAll(" ", "")
    def bracketMatcher = BRACKET_PATTERN.matcher(str)
    if (bracketMatcher.find()){
      str = bracketMatcher.group(1)
    }
    str
  }
}
