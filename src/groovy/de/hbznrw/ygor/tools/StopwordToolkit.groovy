package de.hbznrw.ygor.tools


/**
 * This is a first, rough, quick and very dirty implementation, to be expanded agile.
 */
class StopwordToolkit{

  private static List<String> STOPWORDS = [
      // english
      "and",
      "or",
      // german
      "und",
      "oder"
  ]

  static boolean isStopword(String word){
    return word in STOPWORDS
  }

}
