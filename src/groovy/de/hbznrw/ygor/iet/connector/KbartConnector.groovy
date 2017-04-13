package de.hbznrw.ygor.iet.connector

import groovy.util.logging.Log4j
import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.interfaces.*
import de.hbznrw.ygor.iet.bridge.*


/**
 * Controlling API calls to a kbart file
 * 
 * @author David Klober
 *
 */
@Log4j
class KbartConnector extends ConnectorAbstract {
	
	private String requestUrl       = 'kbart-file'
    private HashMap response
	
    public kbartKeys = [      
        'date_first_issue_online',
        'date_last_issue_online',
        'num_first_vol_online',
        'num_last_vol_online',
        'num_first_issue_online',
        'num_last_issue_online',
        'title_url',
        'embargo_info',
        'coverage_depth',
        'notes',
        /*
        'publication_title',
        'ZDB-ID',
        'print_identifier',
        'online_identifier'
        */
    ]
    
	KbartConnector(BridgeInterface bridge) {
		super(bridge)
	}
	
    // ConnectorInterface
    
    @Override
    String getAPIQuery(String identifier) {
        return requestUrl
    }
    
	@Override
	Envelope poll(String identifier) {
		
        log.info("polling(): " + identifier)
        
		try {
            def stash = bridge.processor.getStash()
            def kbart = stash.get(KbartBridge.IDENTIFIER)

            kbart.any{ k, v ->
                if(k == identifier){
                    response = v
                    true
                }
            }
		} catch(Exception e) {
			return getEnvelopeWithStatus(Status.STATUS_ERROR)
		}
	}
         
    @Override
    Envelope query(Query query) {
        try {
            getEnvelope(query)
        } catch(Exception e) {
            return getEnvelopeWithStatus(Status.STATUS_ERROR)
        }
    }
    

    @Override
    Envelope getEnvelope(Query query) {
        if(response == null)
            return getEnvelopeWithStatus(Status.STATUS_NO_RESPONSE)

        switch(query){
            case Query.KBART_TIPP_URL:
                return getTippUrl()
                break
            case Query.KBART_TIPP_COVERAGE:
                return getTippCoverageAsFatEnvelope()
                break
        }

        getEnvelopeWithStatus(Status.UNKNOWN_REQUEST)
    }
    
    private Envelope getTippUrl() {
        def result = []
        result << getValue("title_url")
        
        getEnvelopeWithMessage(result)
    }
    
    private Envelope getTippCoverageAsFatEnvelope() {
        getEnvelopeWithComplexMessage([
            'startDate':    ([] << getValue('date_first_issue_online')),
            'endDate':      ([] << getValue('date_last_issue_online')),
            'startIssue':   ([] << getValue('num_first_issue_online')),
            'endIssue':     ([] << getValue('num_last_issue_online')),
            'startVolume':  ([] << getValue('num_first_vol_online')),
            'endVolume':    ([] << getValue('num_last_vol_online')),
            'coverageNote': ([] << getValue('notes')),
            'embargo':      ([] << getValue('embargo_info'))
        ])
    }
    
    private getValue(String key) {
        def hm = response.find{ it.key == key }
        hm?.value
    }
}
