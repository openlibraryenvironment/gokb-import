package de.hbznrw.ygor.iet

import de.hbznrw.ygor.iet.enums.Status;

/**
 * Class for wrapping API call results
 * 
 * @author kloberd
 *
 */
class Envelope {

	Status state
	ArrayList message
	
	Envelope(Status state, ArrayList message) {
		this.state = state
		this.message = message
	}
	
	/**
	 * Print containing state and message
	 */
	void printInfo() {
		println("state:   " + state)
		println("message: " + message)
	}
}
