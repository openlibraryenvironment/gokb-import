package de.hbznrw.ygor.iet.connector

import groovy.util.slurpersupport.GPathResult
import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.interfaces.*


/**
 * Controlling API calls using http://ezb.uni-regensburg.de
 * 
 * @author David Klober
 *
 */
class EzbXmlConnector extends ConnectorAbstract {
	
	private String requestUrl       = "http://rzblx1.uni-regensburg.de/ezeit/searchres.phtml?bibid=HBZ"
	private String queryIdentifier  = 'jq_type1=ZD&jq_term1='
    
    private String formatIdentifier = 'xmloutput=1&xmlv=3'
    private GPathResult response
	
	EzbXmlConnector(BridgeInterface bridge) {
		super(bridge)
	}
	
    // ConnectorInterface
    
    @Override
    String getAPIQuery(String identifier) {
        return requestUrl + "&" + formatIdentifier + "&" + queryIdentifier + identifier
    }
    
	@Override
	Envelope poll(String identifier) {
		
		try {
			String text = new URL(getAPIQuery(identifier)).getText()
			response = new XmlSlurper().parseText(text)
			
		} catch(Exception e) {
			return getEnvelopeWithStatus(Status.STATUS_ERROR)
		}
	}
         
    @Override
    Envelope query(Query query) {
        try {
            getEnvelope(query)
        } catch(Exception e) {
            return getEnvelopeWithStatus(Status.STATUS_ERROR)
        }
    }
    
    // FormatAdapterInterface
    
    // <ezb_page>
    //   <ezb_alphabetical_list_searchresult>
    //     <alphabetical_order>
    //       <journals>
    //         <journal jourid="64800">
    
    @Override
    Envelope getEnvelope(Query query) {
        if(response == null)
            return getEnvelopeWithStatus(Status.STATUS_NO_RESPONSE)
        
        switch(query){
            case Query.EZBID:
                return getEzbID()
                break;
        }
        
        getEnvelopeWithStatus(Status.UNKNOWN_REQUEST)
    }
    
    private Envelope getEzbID() {
        def result = []
        
        response.ezb_alphabetical_list_searchresult.alphabetical_order.journals.journal.each { journal ->
            result << journal.@'jourid'
        }
        getEnvelopeWithMessage(result)
    }
}