package de.hbznrw.ygor.connectors

import groovy.util.logging.Log4j
import de.hbznrw.ygor.processing.Envelope
import de.hbznrw.ygor.enums.*
import de.hbznrw.ygor.interfaces.*
import de.hbznrw.ygor.bridges.*


/**
 * Controlling API calls to a kbart file
 */
@Log4j
class KbartConnector extends AbstractConnector {

    static final KBART_HEADER_ZDB_ID            = "zdb_id"
    static final KBART_HEADER_ONLINE_IDENTIFIER = "online_identifier"
    static final KBART_HEADER_PRINT_IDENTIFIER  = "print_identifier"
    static final KBART_HEADER_DOI_IDENTIFIER    = "doi_identifier"

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
        'publication_title', // used in case of more specific ZDB queries
        'title_id'
    ]

    public optionalKbartKeys = [
        'access_start_date',
        'access_end_date'
    ]

	KbartConnector(BridgeInterface bridge) {
		super(bridge)
	}
	
    // ConnectorInterface
    
    @Override
    String getAPIQuery(String identifier, String queryIdentifier) {
        return requestUrl
    }
    
	@Override
	def poll(String identifier, String queryIdentifier, def publicationTitle) {
		
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
            log.error(e)
		}
        // TODO fix return value
	}
         
    @Override
    Envelope query(Query query) {
        try {
            getEnvelope(query)
        } catch(Exception e) {
            return getEnvelopeWithStatus(AbstractEnvelope.STATUS_ERROR)
        }
    }

    @Override
    Envelope getEnvelope(Query query) {
        if(response == null)
            return getEnvelopeWithStatus(AbstractEnvelope.STATUS_NO_RESPONSE)

        switch(query){
            case Query.KBART_TIPP_URL:
                return getEnvelopeWithMessage("title_url")
            case Query.KBART_TIPP_COVERAGE:
                return getTippCoverageAsFatEnvelope()
            case Query.KBART_TIPP_ACCESS:
                return getTippAccessDatesAsFatEnvelope()
            case Query.KBART_PISBN:
                return getEnvelopeWithMessage("print_identifier")
            case Query.KBART_PISSN:
                return getEnvelopeWithMessage("print_identifier")
            case Query.KBART_EISSN:
                return getEnvelopeWithMessage("online_identifier")
            case Query.KBART_EISBN:
                return getEnvelopeWithMessage("online_identifier")
            case Query.KBART_PUBLISHER:
                return getEnvelopeWithMessage("publisher_name")
            case Query.KBART_DOI:
                return getEnvelopeWithMessage("doi_identifier")
            case Query.KBART_TITLE:
                return getEnvelopeWithMessage("publication_title")
            case Query.KBART_TITLE_ID:
                return getEnvelopeWithMessage("title_id")
            case Query.KBART_DATE_MONOGRAPH_PUBLISHED_ONLINE:
                return getEnvelopeWithMessage("date_monograph_published_online")
            case Query.KBART_DATE_MONOGRAPH_PUBLISHED_PRINT:
                return getEnvelopeWithMessage("date_monograph_published_print")
            case Query.KBART_MONOGRAPH_EDITION:
                return getEnvelopeWithMessage("monograph_edition")
            case Query.KBART_MONOGRAPH_VOLUME:
                return getEnvelopeWithMessage("monograph_volume")
            case Query.KBART_FIRST_AUTHOR:
                return getEnvelopeWithMessage("first_author")
            case Query.KBART_FIRST_EDITOR:
                return getEnvelopeWithMessage("first_editor")
        }
        getEnvelopeWithStatus(AbstractEnvelope.STATUS_UNKNOWN_REQUEST)
    }

    private Envelope getEnvelopeWithMessage(String fieldName) {
        def result = []
        result << getValue(fieldName)
        return getEnvelopeWithMessage(result)
    }

    private Envelope getTippAccessDatesAsFatEnvelope() {
        getEnvelopeWithComplexMessage([
            'accessStartDate':      ([] << getValue('access_start_date')),
            'accessEndDate':        ([] << getValue('access_end_date'))
        ])
    }

    private Envelope getTippCoverageAsFatEnvelope() {
        getEnvelopeWithComplexMessage([
            'startDate':        ([] << getValue('date_first_issue_online')),
            'endDate':          ([] << getValue('date_last_issue_online')),
            'startIssue':       ([] << getValue('num_first_issue_online')),
            'endIssue':         ([] << getValue('num_last_issue_online')),
            'startVolume':      ([] << getValue('num_first_vol_online')),
            'endVolume':        ([] << getValue('num_last_vol_online')),
            'coverageNote':     ([] << getValue('notes')),
            'coverageDepth':    ([] << getValue('coverage_depth')),
            'embargo':          ([] << getValue('embargo_info'))
        ])
    }
    
    private getValue(String key) {
        def hm = response.find{ it.key == key }
        hm?.value
    }
}
