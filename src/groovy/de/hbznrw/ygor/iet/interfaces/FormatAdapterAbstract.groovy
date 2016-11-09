package de.hbznrw.ygor.iet.interfaces

import java.util.ArrayList
import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.Status

/**
 * Abstract class for defining format specific queries
 * 
 * @author David Klober
 *
 */
abstract class FormatAdapterAbstract implements FormatAdapterInterface {

	static formatIdentifier             = 'set-in-extending-class'
	
	protected BridgeInterface bridge
	
	//
	
	FormatAdapterAbstract(BridgeInterface bridge) {
		this.bridge = bridge
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
