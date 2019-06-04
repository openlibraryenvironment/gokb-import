package de.hbznrw.ygor.readers

import groovy.util.logging.Log4j
import groovy.util.slurpersupport.GPathResult
import ygor.field.FieldKeyMapping

@Log4j
class EzbReader extends AbstractReader{

    final static Map QUERY_IDS = [:]
    static{
        QUERY_IDS.put(de.hbznrw.ygor.connectors.KbartConnector.KBART_HEADER_ZDB_ID, "jq_type1=ZD&jq_term1=")
        QUERY_IDS.put(de.hbznrw.ygor.connectors.KbartConnector.KBART_HEADER_ONLINE_IDENTIFIER, "jq_type1=IS&jq_term1=")
        QUERY_IDS.put(de.hbznrw.ygor.connectors.KbartConnector.KBART_HEADER_PRINT_IDENTIFIER, "jq_type1=IS&jq_term1=")
    }
    final static String REQUEST_URL         = "http://rzblx1.uni-regensburg.de/ezeit/searchres.phtml?bibid=HBZ"
    final static String FORMAT_IDENTIFIER   = "xmloutput=1&xmlv=3"

    EzbReader(){

    }


    @Override
    List<Map<String, String>> readItemData(FieldKeyMapping fieldKeyMapping, String identifier) {
        List<Map<String, String>> result = new ArrayList<>()
        try {
            String queryString = getAPIQuery(identifier, fieldKeyMapping.kbartKeys)
            log.info("query EZB: " + queryString)
            String text = new URL(queryString).getText()
            def records = new XmlSlurper().parseText(text).depthFirst().findAll {it.name() == 'title'}
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
        catch(Exception e){
            log.error(e)
        }
        result
    }


    private def convertToMap(nodes) {
        nodes.children().collectEntries {
            [ it.name(), it.childNodes() ? convertToMap(it) : it.text() ]
        }
    }



    private String getAPIQuery(String identifier, Set<String> queryIdentifier) {
        return REQUEST_URL +
                "&" + FORMAT_IDENTIFIER +
                "&" + QUERY_IDS.get(queryIdentifier.getAt(0)) + identifier
    }
}
