package de.hbznrw.ygor.iet.connector

import java.util.ArrayList;

import org.apache.commons.csv.CSVRecord;

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.Query;
import de.hbznrw.ygor.iet.enums.Status;
import de.hbznrw.ygor.iet.formatadapter.OaiDcSruFormatAdapter
import de.hbznrw.ygor.iet.interfaces.*


/**
 * Controlling API calls using services.dnb.de/sru
 * 
 * @author kloberd
 *
 */
class SruConnector extends ConnectorAbstract {
	
	private String requestUrl       = "http://services.dnb.de/sru/zdb?version=1.1&operation=searchRetrieve"
	private String queryIdentifier  = 'query=iss='

	//
	
	SruConnector(BridgeInterface bridge) {
		super(bridge)
	}
	
	@Override
	Envelope getResult(Query query, String value) {
		
		try {
			String url = requestUrl + "&recordSchema=" + bridge.formatAdapter.formatIdentifier + "&" + queryIdentifier + value
			String response = new URL(url).getText()
			
			bridge.formatAdapter.response = new XmlSlurper().parseText(response)
			
		} catch(Exception e) {
			return bridge.formatAdapter.getEnvelopeWithStatus(Status.STATUS_ERROR)
		}
		
		return bridge.formatAdapter.getEnvelope(query)
	}
}
