package de.hbznrw.ygor.processing

import groovy.util.logging.Log4j

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
	Object    state
	ArrayList message   = []
	
    ArrayList states    = []
    HashMap   messages  = [:]
    
    Envelope(){
    }
    
    Envelope(Object state, ArrayList message) {
        this.state   = state                    // String
        this.message = message                  // String | [] | [Envelope,Envelope,Envelope]
        this.type    = Envelope.SIMPLE
    }
    
    Envelope(List states, HashMap messages) {
        this.states   = states                  // [String1, String2, String3]
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
