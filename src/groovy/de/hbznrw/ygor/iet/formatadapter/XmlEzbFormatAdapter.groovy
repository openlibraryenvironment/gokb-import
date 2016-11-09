package de.hbznrw.ygor.iet.formatadapter

import java.util.ArrayList
import org.apache.commons.csv.CSVRecord

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.interfaces.*
import groovy.util.slurpersupport.GPathResult


/**
 * Class for processing API calls using the xml format
 * 
 * @author David Klober
 *
 */
class XmlEzbFormatAdapter extends de.hbznrw.ygor.iet.interfaces.FormatAdapterAbstract {

	static formatIdentifier = 'xmloutput=1&xmlv=3'
	
	private GPathResult response = null
			
	//
	
	XmlEzbFormatAdapter(BridgeInterface bridge) {
		super(bridge)
	}

	@Override
	Envelope getEnvelope(Query query) {
		if(Query.EZBID == query) {
			return getEzbID()
		}
		else {
			return getEnvelopeWithStatus(Status.UNKNOWN_REQUEST)
		}
	}
	
	private Envelope getEzbID() {
		if(response == null) {
			return getEnvelopeWithStatus(Status.STATUS_NO_RESPONSE)
		}
				
		def result = []
		
		/*
		 * Matching:
		 * 
		 * <ezb_page>
		 *   <ezb_alphabetical_list_searchresult> 
		 *     <alphabetical_order> 
		 *       <journals>
		 *         <journal jourid="64800">
		 */
		
		response.ezb_alphabetical_list_searchresult.alphabetical_order.journals.journal.each { journal ->
			result << journal.@'jourid'
		}

		return getEnvelopeWithMessage(result)
	}
}
