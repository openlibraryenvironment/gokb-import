package de.hbznrw.ygor.iet.connector

import groovy.util.slurpersupport.GPathResult

import java.util.ArrayList

import org.apache.commons.csv.CSVRecord

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.interfaces.*


/**
 * Controlling API calls using services.dnb.de/sru
 * 
 * @author David Klober
 *
 */
class SruConnector extends ConnectorAbstract {
	
	private String requestUrl       = "http://services.dnb.de/sru/zdb?version=1.1&operation=searchRetrieve"
	private String queryIdentifier  = 'query=iss='
    
    private String formatIdentifier = 'oai_dc'
    private GPathResult response    = null

	SruConnector(BridgeInterface bridge) {
		super(bridge)
	}
    
    
    // ConnectorInterface
    
	@Override
	Envelope getResult(Query query, String value) {
		
		try {
			String url  = requestUrl + "&recordSchema=" + formatIdentifier + "&" + queryIdentifier + value
			String text = new URL(url).getText()
			
			response = new XmlSlurper().parseText(text)
			
		} catch(Exception e) {
			return getEnvelopeWithStatus(Status.STATUS_ERROR)
		}
		
		getEnvelope(query)
	}
            
    // FormatAdapterInterface

    @Override
    Envelope getEnvelope(Query query) {
        if(Query.ZDBID == query) {
            return getZdbID()
        }
        else {
            return getEnvelopeWithStatus(Status.UNKNOWN_REQUEST)
        }
    }
    
    private Envelope getZdbID() {
        if(response == null) {
            return getEnvelopeWithStatus(Status.STATUS_NO_RESPONSE)
        }
                
        def result = []
        
        /*
         * Matching:
         *
         * <records>
         *   <record>
         *     <recordData>
         *       <dc>
         *         <identifier xsi:type="dnb:ZDBID">2530653-4
         *         <type>Online-Ressource
         */
        
        response.records.record.each { record ->
            record.recordData.dc.type.findAll { type ->
                type.text() == "Online-Ressource"
            }.each { type ->
                type.parent().identifier.findAll { i ->
                    i.@'xsi:type' == 'dnb:ZDBID'
                }.each { i ->
                    result << i
                }
            }
        }

        return getEnvelopeWithMessage(result)
    }
}
