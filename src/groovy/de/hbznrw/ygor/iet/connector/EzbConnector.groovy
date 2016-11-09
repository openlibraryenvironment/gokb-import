package de.hbznrw.ygor.iet.connector

import java.util.ArrayList
import org.apache.commons.csv.CSVRecord
import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.formatadapter.OaiDcSruFormatAdapter
import de.hbznrw.ygor.iet.interfaces.*


/**
 * Controlling API calls using http://ezb.uni-regensburg.de
 * 
 * @author David Klober
 *
 */
class EzbConnector extends ConnectorAbstract {
	
	private String requestUrl       = "http://rzblx1.uni-regensburg.de/ezeit/searchres.phtml?bibid=HBZ"
	private String queryIdentifier  = 'jq_type1=IS&jq_term1='

	//
	
	EzbConnector(BridgeInterface bridge) {
		super(bridge)
	}
	
	@Override
	Envelope getResult(Query query, String value) {
		
		try {
			String url = requestUrl + "&" + bridge.formatAdapter.formatIdentifier + "&" + queryIdentifier + value
			String response = new URL(url).getText()

			bridge.formatAdapter.response = new XmlSlurper().parseText(response)
			
		} catch(Exception e) {
			return bridge.formatAdapter.getEnvelopeWithStatus(Status.STATUS_ERROR)
		}
		
		return bridge.formatAdapter.getEnvelope(query)
	}
}
