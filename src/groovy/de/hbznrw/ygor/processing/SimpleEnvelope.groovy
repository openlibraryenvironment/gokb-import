package de.hbznrw.ygor.processing

import de.hbznrw.ygor.interfaces.AbstractEnvelope
import groovy.util.logging.Log4j
import de.hbznrw.ygor.enums.Status

@Log4j
class SimpleEnvelope extends AbstractEnvelope {

    String    type
	Status    state
	ArrayList message = []
	    
    SimpleEnvelope(){
        this.type = SimpleEnvelope.TYPE_SIMPLE
    }
    
    SimpleEnvelope(Status state, ArrayList message) {
        this.type    = SimpleEnvelope.TYPE_SIMPLE
        this.state   = state                    // Status.CONST
        this.message = message                  // String | [] | [Envelope,Envelope,Envelope]
    }

	String toString() {      
        log.debug("| type:    " + type)
        log.debug("| state:   " + state)
        log.debug("| message: " + message)
	}
}
