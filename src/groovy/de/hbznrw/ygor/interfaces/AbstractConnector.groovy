package de.hbznrw.ygor.interfaces

import groovy.util.logging.Log4j
import de.hbznrw.ygor.processing.Envelope
import de.hbznrw.ygor.enums.Query
import de.hbznrw.ygor.enums.Status

/**
 * Abstract class for defining API endpoints
 * and defining format specific queries
 *
 * @author David Klober
 *
 */

@Log4j
abstract class AbstractConnector implements ConnectorInterface {

	protected String requestUrl       = "set-in-extending-class"
	protected String requestHeader
	protected String queryIdentifier  = "set-in-extending-class"
		
    protected String formatIdentifier = 'set-in-extending-class'
 
	protected BridgeInterface bridge
	
	//
	
	AbstractConnector(BridgeInterface bridge) {
		this.bridge = bridge
	}
	
    String getAPIQuery(String identifier, String queryIdentifier) {
        log.info(" -- getAPIQuery(String identifier, String queryIdentifier) not implemented --")
    }
    def poll(String identifier, String queryIdentifier) {
        log.info(" -- poll(String identifier, String queryIdentifier) not implemented --")
    }
    
    Envelope query(Query query) {
        getEnvelopeWithStatus(AbstractEnvelope.STATUS_UNKNOWN_REQUEST)
    }
    
    Envelope query(Object record, Query query) {  
        getEnvelopeWithStatus(AbstractEnvelope.STATUS_UNKNOWN_REQUEST)
    }
    
    Envelope getEnvelope(Query query) {
        log.info(" -- getEnvelope(Query query) not implemented --")
    }
    
    Envelope getEnvelope(Object record, Query query) { 
        log.info(" -- getEnvelope(Object record, Query query) not implemented --")
    }
    
    Envelope getEnvelopeWithMessage(ArrayList message) {
        def state = AbstractEnvelope.RESULT_OK
        
        switch(message.size()) {
            case 0:
                state = AbstractEnvelope.RESULT_NO_MATCH
            break;
            case {it > 1}:
                state = AbstractEnvelope.RESULT_MULTIPLE_MATCHES
            break;
        }
        new Envelope(state, message)
    }
    
    Envelope getEnvelopeWithComplexMessage(HashMap messages) {
        def states = []
        if(messages.isEmpty())
            states = [Status.UNDEFINED]
        
        // missing values filled with null
        // see: de.hbznrw.ygor.processing.Envelope
            
        for(item in messages) {
            def tmp = item.value.minus(null) // TODO CHECK, or use e.g. Status.EMPTY_SLOT
            switch(tmp.size()) {
                case 0:
                    states << /*item.key + '_' + */ AbstractEnvelope.RESULT_NO_MATCH
                break;
                case 1:
                    states << /*item.key + '_' + */ AbstractEnvelope.RESULT_OK
                    break;
                case {it > 1}:
                    states << /*item.key + '_' + */ AbstractEnvelope.RESULT_MULTIPLE_MATCHES
                break;
            }
        }
        new Envelope(states, messages)
    }
    
    Envelope getEnvelopeWithStatus(Object state) {
        new Envelope(state, [])
    }
}
