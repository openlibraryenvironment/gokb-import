package de.hbznrw.ygor.readers

import groovy.util.logging.Log4j


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

  final static XmlParser XMLPARSER = new XmlParser()

  ZdbReader(){}


  @Override
  List<Map<String, List<String>>> readItemData(String queryString){
    List<Map<String, List<String>>> result = new ArrayList<>()
    log.info("query ZDB: " + queryString)
    String text = new URL(queryString).getText()
    def xml = XMLPARSER.parseText(text)
    List<Node> records = xml.get("records")
    for (Node record in records){
      if (record.record.size() == 0){
        // no records in API response
        continue
      }
      NodeList tags = record.record.recordData[0].get("ppxml:record")[0].get("ppxml:global")[0].children()
      Map<String, List<String>> recordMap = getDataFromTagNodes(tags)
      if (!recordMap.isEmpty()){
        result.add(recordMap)
      }
      tags = recordMap = null
    }
    xml = records = null
    System.gc()
    result
  }


  private Map<String, List<String>> getDataFromTagNodes(NodeList tags){
    Map<String, List<String>> result = [:]
    for (Node tag in tags){
      String tagId = tag.attributes()."id"
      def subfields = tag.children()
      for (Node subfield in subfields){
        String subfieldId = subfield.attributes()."id"
        String key = tagId.concat(":").concat(subfieldId)
        String value = subfield.value()[0]
        List<String> existingValues = result.get(key)
        if (existingValues == null){
          existingValues = new ArrayList<String>()
        }
        existingValues.add(value)
        result.put(key, existingValues)
      }
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
