package de.hbznrw.ygor.iet.connector

import groovy.json.JsonSlurper
import groovy.util.logging.Log4j
import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.interfaces.*


/**
 * Controlling API calls using lobid.org
 * 
 * @author David Klober
 *
 */
@Log4j
class LobidJldConnector extends ConnectorAbstract {
	
	private String requestUrl       = "https://lobid.org/resource?"
	private String requestHeader    = "application/json, application/ld+json"
	private String queryIdentifier  = 'name='
    
    private String formatIdentifier = 'jld'
    private ArrayList response 
	
	LobidJldConnector(BridgeInterface bridge) {
		super(bridge)
	}
    
    // ConnectorInterface
	
    @Override
    String getAPIQuery(String issn) {
        return requestUrl + queryIdentifier + issn
    }
    
	@Override
	Envelope poll(String issn) {
		
		try {
            String q = getAPIQuery(issn)
            
            log.info("polling(): " + q)
			String text = new URL(q).getText(requestProperties: [Accept: requestHeader])

			response = new JsonSlurper().parseText(text)
			
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
    
    // {
    //   "@graph" : [ {
    //     "hbzId" : "HT015982448",
    //   }, {
    //   } ],
    // }

    @Override
    Envelope getEnvelope(Query query) {
        if(response == null)
            return getEnvelopeWithStatus(Status.STATUS_NO_RESPONSE)
        
        switch(query){
            case Query.HBZID:
                return getHbzID()
                break;
        }
        
        getEnvelopeWithStatus(Status.UNKNOWN_REQUEST)
    }
    
    private Envelope getHbzID() {
        def result = []
        
        response."@graph"."hbzId".each{ i ->
            
            if(null != i) {
                i = i - [null]
                result << ((i.size() > 0) ? i.first() : i)
            }
        }
        getEnvelopeWithMessage(result)
    }
}
