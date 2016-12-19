package de.hbznrw.ygor.iet.interfaces

import java.util.ArrayList

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.Query
import de.hbznrw.ygor.iet.enums.Status

/**
 * Abstract class for defining API endpoints
 * and defining format specific queries
 *
 * @author David Klober
 *
 */
abstract class ConnectorAbstract implements ConnectorInterface {

	protected String requestUrl      = "set-in-extending-class"
	protected String requestHeader
	protected String queryIdentifier = "set-in-extending-class"
		
    static String formatIdentifier   = 'set-in-extending-class'
 
	protected BridgeInterface bridge
	
	//
	
	ConnectorAbstract(BridgeInterface bridge) {
		this.bridge = bridge
	}
	
    Envelope poll() {
        // TODO
    }
    
    Envelope query(Query query) {  
        getEnvelopeWithStatus(Status.UNKNOWN_REQUEST)
    }
    
    Envelope getEnvelopeWithMessage(ArrayList message) {
        def state = Status.RESULT_OK
        switch(message.size()) {
            case 0:
                state = Status.RESULT_NO_MATCH
            break;
            case {it > 1}:
                state = Status.RESULT_MULTIPLE_MATCHES
            break;
        }
        return new Envelope(state, message)
    }
    
    Envelope getEnvelopeWithStatus(Status state) {
        return new Envelope(state, [])
    }
}
