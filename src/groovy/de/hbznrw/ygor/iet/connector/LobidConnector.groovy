package de.hbznrw.ygor.iet.connector

import groovy.json.JsonSlurper

import java.util.ArrayList;

import org.apache.commons.csv.CSVRecord;

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.Query;
import de.hbznrw.ygor.iet.enums.Status;
import de.hbznrw.ygor.iet.formatadapter.OaiDcSruFormatAdapter
import de.hbznrw.ygor.iet.interfaces.*


/**
 * Controlling API calls using lobid.org
 * 
 * @author kloberd
 *
 */
class LobidConnector extends ConnectorAbstract {
	
	private String requestUrl      = "https://lobid.org/resource?"
	private String requestHeader   = "application/json, application/ld+json"
	private String queryIdentifier = 'name='
	
	//
	
	LobidConnector(BridgeInterface bridge) {
		super(bridge)
	}
	
	@Override
	Envelope getResult(Query query, String value) {
		
		try {
			String url = requestUrl + queryIdentifier + value
			String response = new URL(url).getText(requestProperties: [Accept: requestHeader])

			bridge.formatAdapter.response = new JsonSlurper().parseText(response)
			
		} catch(Exception e) {
			return bridge.formatAdapter.getEnvelopeWithStatus(Status.STATUS_ERROR)
		}
		
		return bridge.formatAdapter.getEnvelope(query)
	}
}
