package de.hbznrw.ygor.readers

abstract class AbstractReader {

  abstract List<Map<String, String>> readItemData(String queryString)

}
