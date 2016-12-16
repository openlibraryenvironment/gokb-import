package de.hbznrw.ygor.iet.connector

import groovy.util.slurpersupport.GPathResult

import java.util.ArrayList

import org.apache.commons.csv.CSVRecord

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.interfaces.*


/**
 * Controlling API calls using http://ezb.uni-regensburg.de
 * 
 * @author David Klober
 *
 */
class EzbConnector extends ConnectorAbstract {
	
	private String requestUrl       = "http://rzblx1.uni-regensburg.de/ezeit/searchres.phtml?bibid=HBZ"
	private String queryIdentifier  = 'jq_type1=IS&jq_term1='
    
    private String formatIdentifier = 'xmloutput=1&xmlv=3'
    private GPathResult response
	
	EzbConnector(BridgeInterface bridge) {
		super(bridge)
	}
	
    // ConnectorInterface
    
	@Override
	Envelope getResult(Query query, String value) {
		
		try {
			String url  = requestUrl + "&" + formatIdentifier + "&" + queryIdentifier + value
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
        if(Query.EZBID == query) {
            return getEzbID()
        }
        else {
            return getEnvelopeWithStatus(Status.UNKNOWN_REQUEST)
        }
    }
    
    private Envelope getEzbID() {
        if(response == null) {
            return getEnvelopeWithStatus(Status.STATUS_NO_RESPONSE)
        }
                
        def result = []
        
        /*
         * Matching:
         *
         * <ezb_page>
         *   <ezb_alphabetical_list_searchresult>
         *     <alphabetical_order>
         *       <journals>
         *         <journal jourid="64800">
         */
        
        response.ezb_alphabetical_list_searchresult.alphabetical_order.journals.journal.each { journal ->
            result << journal.@'jourid'
        }

        return getEnvelopeWithMessage(result)
    }
}
