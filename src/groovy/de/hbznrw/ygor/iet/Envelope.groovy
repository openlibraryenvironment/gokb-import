package de.hbznrw.ygor.iet

import de.hbznrw.ygor.iet.enums.Status

/**
 * Class for wrapping API call results
 * 
 * @author David Klober
 *
 */
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
        this.message = message                  // String | []
        this.type    = Envelope.SIMPLE
    }
    
    Envelope(List states, HashMap messages) {
        this.states   = states                  // [Status.CONST1, Status.CONST2, Status.CONST3]
        this.messages = messages                // [a1:[b11, b12, b13, b14], a2:[b21, b22, b23, b24], a3:[b31, b32, b33, b34]]
        this.type     = Envelope.COMPLEX
    }

	/**
	 * Print containing state and message
	 */
	void printInfo() {
        if(this.type == Envelope.SIMPLE) {
            println("state:   " + state)
            println("message: " + message)
        }
        if(this.type == Envelope.COMPLEX) {
            println("states:   " + states)
            println("messages: " + messages)
        }
	}
}
