package de.hbznrw.ygor.iet.interfaces

import de.hbznrw.ygor.iet.Envelope;
import de.hbznrw.ygor.iet.enums.Query;
import de.hbznrw.ygor.iet.enums.Status;

/**
 * Abstract class for defining API endpoints
 *
 * @author kloberd
 *
 */
abstract class ConnectorAbstract implements ConnectorInterface {

	protected String requestUrl      = "set-in-extending-class"
	protected String requestHeader
	protected String queryIdentifier = "set-in-extending-class"
		
	protected BridgeInterface bridge
	
	//
	
	ConnectorAbstract(BridgeInterface bridge) {
		this.bridge = bridge
	}
	
	Envelope getResult(Query query, String value) {
		bridge.formatAdapter.getEnvelopeWithStatus(Status.UNKNOWN_REQUEST)
	}
}
