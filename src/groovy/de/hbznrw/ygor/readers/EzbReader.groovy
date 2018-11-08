package de.hbznrw.ygor.readers

import groovy.util.logging.Log4j
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
    Map<String, String> readItemData(FieldKeyMapping fieldKeyMapping, String identifier) {
        Map<String, String> result = new HashMap<>()
        try {
            String queryString = getAPIQuery(identifier, fieldKeyMapping.kbartKey)
            log.info("query EZB: " + queryString)
            String text = new URL(queryString).getText()
            def records = new XmlSlurper().parseText(text).depthFirst().findAll {it.name() == 'records'}
            if (records?.size() == 1){
                def subfields = new XmlSlurper().parseText(text).depthFirst().findAll {it.name() == 'subf'}
                subfields.each { subfield ->
                    if (subfield.parent().parent().name() == "global"){
                        def attribute = subfield.attributes().get("id")
                        def parentAttribute = subfield.parent().attributes().get("id")
                        def value = subfield.localText()[0]
                        result.put(parentAttribute.concat(":").concat(attribute), value)
                    }
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



    private String getAPIQuery(String identifier, String queryIdentifier) {
        return REQUEST_URL +
                "&" + FORMAT_IDENTIFIER +
                "&" + QUERY_IDS.get(queryIdentifier) + identifier
    }
}
