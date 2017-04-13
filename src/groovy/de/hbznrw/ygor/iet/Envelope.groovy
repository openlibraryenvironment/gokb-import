package de.hbznrw.ygor.iet

import groovy.util.logging.Log4j
import de.hbznrw.ygor.iet.enums.Status

/**
 * Class for wrapping API call results
 * 
 * @author David Klober
 *
 */

@Log4j
class Envelope {

    static final SIMPLE  = 'simple'
    static final COMPLEX = 'complex'
    
    String    type
	Status    state
	ArrayList message   = []
	
    ArrayList states    = []
    HashMap   messages  = [:]
    
    Envelope(){
    }
    
    Envelope(Status state, ArrayList message) {
        this.state   = state                    // Status.CONST
        this.message = message                  // String | [] | [Envelope,Envelope,Envelope]
        this.type    = Envelope.SIMPLE
    }
    
    Envelope(List states, HashMap messages) {
        this.states   = states                  // [Status.CONST1, Status.CONST2, Status.CONST3]
        this.messages = messages                // [a1:[b11, null, b13, b14], a2:[null, null, b23, b24], a3:[b31, b32, b33, null]]
        this.type     = Envelope.COMPLEX
    }

	String toString() {      
        log.debug("| type:     " + type)
        log.debug("| state:    " + state)
        log.debug("| message:  " + message)
        log.debug("| states:   " + states)
        log.debug("| messages: " + messages) 
	}
}
