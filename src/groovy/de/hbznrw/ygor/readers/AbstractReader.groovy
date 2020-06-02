package de.hbznrw.ygor.readers

abstract class AbstractReader {

  /**
   * @param queryString
   * @return a list of maps for each record. Within these maps, the mapping must be fieldname:List<fieldvalue>
   */
  abstract List<Map<String, List<String>>> readItemData(String queryString)

}
