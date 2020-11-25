package de.hbznrw.ygor.tools

import java.text.SimpleDateFormat
import groovy.time.TimeCategory

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class DateToolkit {

  static String getDateMinusOneMinute(String date) {
    try {
      def sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")
      Date d = sdf.parse(date)
      use(TimeCategory) {
        d = d - 1.minute
        return sdf.format(d)
      }
    }
    catch (Exception e) {
      return date
    }
  }


  static LocalDateTime fromString(String dateString) throws IllegalArgumentException, DateTimeParseException{
    try{
      return fromString(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }
    catch(IllegalArgumentException | DateTimeParseException e){
      return fromString(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
    }
  }


  static LocalDateTime fromString(String dateString, DateTimeFormatter formatter){
    return LocalDateTime.parse(dateString, formatter)
  }


  static String formatDate(LocalDateTime localDateTime){
    return formatDate(localDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
  }


  static String formatDate(LocalDateTime localDateTime, DateTimeFormatter formatter){
    return localDateTime.format(formatter)
  }
}
