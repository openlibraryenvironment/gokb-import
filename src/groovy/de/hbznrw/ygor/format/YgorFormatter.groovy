package de.hbznrw.ygor.format

interface YgorFormatter {

  String formatDate(String date)

  String formatStartDate(String date)

  String formatEndDate(String date)

  String formatId(String id)

  String formatNumber(String number)

  String formatString(String string)

  String formatUrl(String url)

}
