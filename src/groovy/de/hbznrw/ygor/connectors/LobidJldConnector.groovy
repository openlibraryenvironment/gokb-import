package de.hbznrw.ygor.connectors

import groovy.json.JsonSlurper
import groovy.util.logging.Log4j
import de.hbznrw.ygor.processing.Envelope
import de.hbznrw.ygor.enums.*
import de.hbznrw.ygor.interfaces.*


/**
 * Controlling API calls using lobid.org
 */
@Deprecated
@Log4j
class LobidJldConnector extends AbstractConnector {
	
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
    String getAPIQuery(String issn, String queryIdentifier) {
        return requestUrl + queryIdentifier + issn
    }
    
	@Override
	def poll(String issn, String queryIdentifier) {
		
		try {
            String q = getAPIQuery(issn, queryIdentifier)
            
            log.info("polling(): " + q)
			String text = new URL(q).getText(requestProperties: [Accept: requestHeader])

			response = new JsonSlurper().parseText(text)
			
		} catch(Exception e) {
            log.error(e)
		}
        // TODO fix return value
	}
    
    @Override
    Envelope query(Query query) {
        try {
            getEnvelope(query)
        } catch(Exception e) {
            return getEnvelopeWithStatus(AbstractEnvelope.STATUS_ERROR)
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
            return getEnvelopeWithStatus(AbstractEnvelope.STATUS_NO_RESPONSE)
        
        switch(query){
            case Query.HBZID:
                return getHbzID()
                break;
        }
        
        getEnvelopeWithStatus(AbstractEnvelope.STATUS_UNKNOWN_REQUEST)
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
