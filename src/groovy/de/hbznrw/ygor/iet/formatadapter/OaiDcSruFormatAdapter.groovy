package de.hbznrw.ygor.iet.formatadapter

import java.util.ArrayList
import org.apache.commons.csv.CSVRecord
import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.interfaces.*
import groovy.util.slurpersupport.GPathResult


/**
 * Class for processing API calls using the OAI_DC format
 * 
 * @author David Klober
 *
 */
class OaiDcSruFormatAdapter extends de.hbznrw.ygor.iet.interfaces.FormatAdapterAbstract {

	static formatIdentifier = 'oai_dc'
	
	private GPathResult response = null
			
	//
	
	OaiDcSruFormatAdapter(BridgeInterface bridge) {
		super(bridge)
	}

	@Override
	Envelope getEnvelope(Query query) {
		if(Query.ZDBID == query) {
			return getZdbID()
		}
		else {
			return getEnvelopeWithStatus(Status.UNKNOWN_REQUEST)
		}
	}
	
	private Envelope getZdbID() {
		if(response == null) {
			return getEnvelopeWithStatus(Status.STATUS_NO_RESPONSE)
		}
				
		def result = []
		
		/*
		 * Matching:
		 * 
		 * <records>
		 *   <record> 
		 *     <recordData> 
		 *       <dc>
		 *         <identifier xsi:type="dnb:ZDBID">2530653-4
		 *         <type>Online-Ressource
		 */
		
		response.records.record.each { record ->
			record.recordData.dc.type.findAll { type ->
				type.text() == "Online-Ressource"
			}.each { type ->
				type.parent().identifier.findAll { i ->
					i.@'xsi:type' == 'dnb:ZDBID'
				}.each { i ->
					result << i
				}
			}
		}

		return getEnvelopeWithMessage(result)
	}
}
