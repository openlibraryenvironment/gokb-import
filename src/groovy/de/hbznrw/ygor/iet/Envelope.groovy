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
        this.state   = state
        this.message = message
        this.type    = Envelope.SIMPLE
    }
    
    Envelope(List states, HashMap messages) {
        this.states   = states
        this.messages = messages
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
