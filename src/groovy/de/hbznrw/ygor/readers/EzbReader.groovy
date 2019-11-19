package de.hbznrw.ygor.readers

import groovy.util.logging.Log4j
import groovy.util.slurpersupport.GPathResult

@Log4j
class EzbReader extends AbstractReader {

  static final IDENTIFIER = 'ezb'

  final static Map QUERY_IDS = [:]
  static {
    QUERY_IDS.put("zdb_id", "jq_type1=ZD&jq_term1=")
    QUERY_IDS.put("online_identifier", "jq_type1=IS&jq_term1=")
    QUERY_IDS.put("print_identifier", "jq_type1=IS&jq_term1=")
  }
  final static String REQUEST_URL = "http://rzblx1.uni-regensburg.de/ezeit/searchres.phtml?bibid=HBZ"
  final static String FORMAT_IDENTIFIER = "xmloutput=1&xmlv=3"

  EzbReader() {

  }


  @Override
  List<Map<String, String>> readItemData(String queryString) {
    List<Map<String, String>> result = new ArrayList<>()
    try {
      log.info("query EZB: " + queryString)
      String text = new URL(queryString).getText()
      def records = new XmlSlurper().parseText(text).depthFirst().findAll { it.name() == 'title' }
      if (records) {
        for (GPathResult record in records) {
          Map<String, String> recordMap = new HashMap<>()
          String name = records.get(0).localText()[0]
          String ezbId = records.get(0).parent().attributes().get("jourid")
          recordMap.put("jourid", ezbId)
          recordMap.put("title", name)
          result.add(recordMap)
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


  static String getAPIQuery(String identifier, List<String> queryIdentifier) {
    return REQUEST_URL +
        "&" + FORMAT_IDENTIFIER +
        "&" + QUERY_IDS.get(queryIdentifier.getAt(0)) + identifier
  }
}
