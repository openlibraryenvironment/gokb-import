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
	Envelope poll(String value) {
		
		try {
			String url  = requestUrl + queryIdentifier + value
			String text = new URL(url).getText(requestProperties: [Accept: requestHeader])

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
