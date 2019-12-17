package de.hbznrw.ygor.readers

import groovy.util.logging.Log4j
import groovy.util.slurpersupport.GPathResult


@Log4j
class ZdbReader extends AbstractReader {

  static final IDENTIFIER = 'zdb'

  final static Map QUERY_IDS = [:]
  static {
    QUERY_IDS.put(KbartReader.KBART_HEADER_ZDB_ID, "query=dnb.zdbid%3D")
    QUERY_IDS.put(KbartReader.KBART_HEADER_ONLINE_IDENTIFIER, "query=dnb.iss%3D")
    QUERY_IDS.put(KbartReader.KBART_HEADER_PRINT_IDENTIFIER, "query=dnb.iss%3D")
  }
  final static String REQUEST_URL = "http://services.dnb.de/sru/zdb?version=1.1&operation=searchRetrieve&maximumRecords=10"
  final static String QUERY_ONLY_JOURNALS = "%20and%20dnb.frm=O"
  final static String FORMAT_IDENTIFIER = 'PicaPlus-xml'

  ZdbReader(){}


  @Override
  List<Map<String, List<String>>> readItemData(String queryString) {
    List<Map<String, List<String>>> result = new ArrayList<>()
    try {
      log.info("query ZDB: " + queryString)
      String text = new URL(queryString).getText()
      def records = new XmlSlurper().parseText(text).depthFirst().findAll { it.name() == 'records' }
      if (records) {
        for (GPathResult record in records) {
          Map<String, List<String>> recordMap = new TreeMap<String, String>()
          def subfields = record.depthFirst().findAll { it.name() == 'subf' }
          int count = 0
          int size = 0
          List<String> readSubFieldsInThisTag
          for (def subfield in subfields){
            if (subfield.parent().parent().name() == "global") {
              if (count == 0){
                size = subfield.parent().children().size()
                readSubFieldsInThisTag = []
              }
              def key = subfield.parent().attributes().get("id").concat(":").concat(subfield.attributes().get("id"))
              if (!readSubFieldsInThisTag.contains(key)){
                def value = subfield.localText()[0]
                List<String> values = recordMap.get(key)
                if (values == null){
                  values = []
                }
                values << value
                recordMap.put(key, values)
                readSubFieldsInThisTag.add(key)
              }
              if (++count == size){
                count = 0
              }
            }
          }
          if (!recordMap.isEmpty()) {
            result.add(recordMap)
          }
        }
      }
    }
    catch (Exception e) {
      log.error(e)
    }
    result
  }


  private def convertToMap(nodes) {
    nodes.children().collectEntries {
      [it.name(), it.childNodes() ? convertToMap(it) : it.text()]
    }
  }


  static String getAPIQuery(String identifier, String queryIdentifier) {
    return REQUEST_URL +
        "&recordSchema=" + FORMAT_IDENTIFIER +
        "&" + QUERY_IDS.get(queryIdentifier) + identifier + QUERY_ONLY_JOURNALS
  }
}
