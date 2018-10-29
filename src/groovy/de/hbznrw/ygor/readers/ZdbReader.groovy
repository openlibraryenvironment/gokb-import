package de.hbznrw.ygor.readers

import de.hbznrw.ygor.interfaces.AbstractEnvelope
import groovy.util.logging.Log4j
import groovy.util.slurpersupport.GPathResult
import ygor.field.FieldKeyMapping

@Log4j
class ZdbReader extends AbstractReader{

    final static Map QUERY_IDS = [:]
    static{
        QUERY_IDS.put(de.hbznrw.ygor.connectors.KbartConnector.KBART_HEADER_ZDB_ID, "query=dnb.zdbid%3D")
        QUERY_IDS.put(de.hbznrw.ygor.connectors.KbartConnector.KBART_HEADER_ONLINE_IDENTIFIER, "query=dnb.iss%3D")
        QUERY_IDS.put(de.hbznrw.ygor.connectors.KbartConnector.KBART_HEADER_PRINT_IDENTIFIER, "query=dnb.iss%3D")
    }
    final static String REQUEST_URL         = "http://services.dnb.de/sru/zdb?version=1.1&operation=searchRetrieve&maximumRecords=10"
    final static String QUERY_ONLY_JOURNALS = "%20and%20dnb.frm=O"
    final static String FORMAT_IDENTIFIER   = 'PicaPlus-xml'

    ZdbReader(){

    }


    @Override
    Map<String, String> readItemData(FieldKeyMapping fieldKeyMapping, String identifier) {
        GPathResult response
        try {
            String queryString = getAPIQuery(identifier, fieldKeyMapping.zdbKey)
            log.info("query ZDB: " + queryString)
            String text = new URL(queryString).getText()
            response = new XmlSlurper().parseText(text)
            log.info("read ZDB: " + response)
        } catch(Exception e) {
            log.error(e)
        }
    }


    private String getAPIQuery(String identifier, String queryIdentifier) {
        return REQUEST_URL +
                "&recordSchema=" + FORMAT_IDENTIFIER +
                "&" + QUERY_IDS.get(queryIdentifier) + identifier + QUERY_ONLY_JOURNALS
    }
}
