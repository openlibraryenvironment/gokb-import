package de.hbznrw.ygor.iet.connector

import groovy.json.JsonSlurper

import java.util.ArrayList

import org.apache.commons.csv.CSVRecord

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.interfaces.*


/**
 * Controlling API calls using lobid.org
 * 
 * @author David Klober
 *
 */
class LobidConnector extends ConnectorAbstract {
	
	private String requestUrl       = "https://lobid.org/resource?"
	private String requestHeader    = "application/json, application/ld+json"
	private String queryIdentifier  = 'name='
    
    private String formatIdentifier = 'jld'
    private ArrayList response 
	
	LobidConnector(BridgeInterface bridge) {
		super(bridge)
	}
    
    // ConnectorInterface
	
	@Override
	Envelope getResult(Query query, String value) {
		
		try {
			String url  = requestUrl + queryIdentifier + value
			String text = new URL(url).getText(requestProperties: [Accept: requestHeader])

			response = new JsonSlurper().parseText(text)
			
		} catch(Exception e) {
			return getEnvelopeWithStatus(Status.STATUS_ERROR)
		}
		
		getEnvelope(query)
	}
    
    // FormatAdapterInterface

    @Override
    Envelope getEnvelope(Query query) {
        if(Query.HBZID == query) {
            return getHbzID()
        }
        else {
            return getEnvelopeWithStatus(Status.UNKNOWN_REQUEST)
        }
    }
    
    private Envelope getHbzID() {
        if(response == null) {
            return getEnvelopeWithStatus(Status.STATUS_NO_RESPONSE)
        }
                
        def result = []
        
        /*
         * Matching:
         *
         * {
         *   "@graph" : [ {
         *     "hbzId" : "HT015982448",
         *   }, {
         *   } ],
         * }
         */
        
        response."@graph"."hbzId".each{ i ->
            
            if(null != i) {
                i = i - [null]
                result << ((i.size() > 0) ? i.first() : i)
            }
        }
        return getEnvelopeWithMessage(result)
    }
}
