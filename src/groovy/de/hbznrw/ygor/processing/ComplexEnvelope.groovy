package de.hbznrw.ygor.processing

import de.hbznrw.ygor.interfaces.AbstractEnvelope
import groovy.util.logging.Log4j

@Log4j
class ComplexEnvelope extends AbstractEnvelope {

    String    type
    ArrayList states   = []
    HashMap   messages = [:]
    
    ComplexEnvelope(){
        this.type = SimpleEnvelope.TYPE_COMPLEX
    }
    
    ComplexEnvelope(List states, HashMap messages) {
        this.type     = SimpleEnvelope.TYPE_COMPLEX
        this.states   = states                  // [Status.CONST1, Status.CONST2, Status.CONST3]
        this.messages = messages                // [a1:[b11, null, b13, b14], a2:[null, null, b23, b24], a3:[b31, b32, b33, null]]
    }

	String toString() {      
        log.debug("| type:     " + type)
        log.debug("| states:   " + states)
        log.debug("| messages: " + messages) 
	}
}
