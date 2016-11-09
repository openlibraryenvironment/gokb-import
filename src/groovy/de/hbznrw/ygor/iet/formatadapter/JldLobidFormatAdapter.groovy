package de.hbznrw.ygor.iet.formatadapter

import java.util.ArrayList
import org.apache.commons.csv.CSVRecord
import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.interfaces.*
import groovy.util.slurpersupport.GPathResult


/**
 * Class for processing API calls using the JLD format
 * 
 * @author David Klober
 *
 */
class JldLobidFormatAdapter extends de.hbznrw.ygor.iet.interfaces.FormatAdapterAbstract {

	static formatIdentifier = 'jld'
	
	private ArrayList response = null
			
	//
	
	JldLobidFormatAdapter(BridgeInterface bridge) {
		super(bridge)
	}
	
	@Override
	Envelope getEnvelope(Query query) {
		if(Query.HBZID == query) {
			return getHbzID()
		}
		else {
			return getEnvelopeWithStatus(Status.UNKNOWN_REQUEST)
		}
	}
	
	private Envelope getHbzID() {
		if(response == null) {
			return getEnvelopeWithStatus(Status.STATUS_NO_RESPONSE)
		}
				
		def result = []
		
		/*
		 * Matching:
		 * 
		 * {
		 *   "@graph" : [ {
		 *     "hbzId" : "HT015982448",
		 *   }, { 
		 *   } ],
		 * }
		 */
		
		response."@graph"."hbzId".each{ i ->
			
			if(null != i) {
				i = i - [null]
				result << ((i.size() > 0) ? i.first() : i)
			}
		}
		return getEnvelopeWithMessage(result)
	}
}
