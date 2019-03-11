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
    static final KBART_HEADER_DOI_IDENTIFIER  = "doi_identifier"

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
        'publication_title' // used in case of more specific ZDB queries
        /*
        'zdb_id',
        'print_identifier',
        'online_identifier'
        */
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
                return getTippUrl()
                break
            case Query.KBART_TIPP_COVERAGE:
                return getTippCoverageAsFatEnvelope()
                break
            case Query.KBART_TIPP_ACCESS:
                return getTippAccessDatesAsFatEnvelope()
                break
            case Query.KBART_PISSN:
                return getPrintIdentifier()
                break
            case Query.KBART_EISSN:
                return getOnlineIdentifier()
                break
            case Query.KBART_EISBN:
                return getOnlineIdentifier()
                break
            case Query.KBART_PUBLISHER:
                return getPublisher()
                break
            case Query.KBART_DOI:
                return getDOI()
                break
            case Query.KBART_TITLE:
                return getTitle()
                break
            case Query.KBART_TITLE_ID:
                return getTitleID()
                break
            case Query.KBART_DATE_MONOGRAPH_PUBLISHED_ONLINE:
                return getDateMonographPublishedOnline()
                break
            case Query.KBART_DATE_MONOGRAPH_PUBLISHED_PRINT:
                return getDateMonographPublishedPrint()
                break
            case Query.KBART_MONOGRAPH_EDITION:
                return getMonographEdition()
                break
            case Query.KBART_MONOGRAPH_VOLUME:
                return getMonographVolume()
                break
            case Query.KBART_FIRST_AUTHOR:
                return getFirstAuthor()
                break
            case Query.KBART_FIRST_EDITOR:
                return getFirstEditor()
                break
        }
        getEnvelopeWithStatus(AbstractEnvelope.STATUS_UNKNOWN_REQUEST)
    }
    
    private Envelope getTippUrl() {
        def result = []
        result << getValue("title_url")

        getEnvelopeWithMessage(result)
    }

    private Envelope getTitle() {
        def result = []
        result << getValue("publication_title")

        getEnvelopeWithMessage(result)
    }
    private Envelope getTitleID() {
        def result = []
        result << getValue("title_id")

        getEnvelopeWithMessage(result)
    }
    private Envelope getDateMonographPublishedPrint() {
        def result = []
        result << getValue("date_monograph_published_print")

        getEnvelopeWithMessage(result)
    }

    private Envelope getDateMonographPublishedOnline() {
        def result = []
        result << getValue("date_monograph_published_online")

        getEnvelopeWithMessage(result)
    }
    private Envelope getMonographEdition () {
        def result = []
        result << getValue("monograph_edition")

        getEnvelopeWithMessage(result)
    }
    private Envelope getMonographVolume () {
        def result = []
        result << getValue("monograph_volume")

        getEnvelopeWithMessage(result)
    }
    private Envelope getFirstEditor () {
        def result = []
        result << getValue("first_editor")

        getEnvelopeWithMessage(result)
    }
    private Envelope getFirstAuthor() {
        def result = []
        result << getValue("first_author")

        getEnvelopeWithMessage(result)
    }
    private Envelope getPrintIdentifier() {
        def result = []
        result << getValue("print_identifier")

        getEnvelopeWithMessage(result)
    }

    private Envelope getOnlineIdentifier() {
        def result = []
        result << getValue("online_identifier")

        getEnvelopeWithMessage(result)
    }

    private Envelope getPublisher() {
        def result = []
        result << getValue("publisher_name")

        getEnvelopeWithMessage(result)
    }

    private Envelope getDOI() {
        def result = []
        result << getValue("doi_identifier")

        getEnvelopeWithMessage(result)
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
