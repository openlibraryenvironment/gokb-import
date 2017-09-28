package de.hbznrw.ygor.connectors

import groovy.util.logging.Log4j
import groovy.util.slurpersupport.GPathResult
import de.hbznrw.ygor.processing.Envelope
import de.hbznrw.ygor.enums.*
import de.hbznrw.ygor.interfaces.*

/**
 * Controlling API calls using sru.gbv.de/zdbdb
 */
@Log4j
class ZdbdbSruPicaConnector extends AbstractConnector {
	
    static final QUERY_PICA_ISS = "query=pica.iss%3D"
    static final QUERY_PICA_ZDB = "query=pica.yyy%3D"
    
	private String requestUrl       = "http://sru.gbv.de/zdbdb?version=1.2&operation=searchRetrieve&maximumRecords=10"
	private String queryIdentifier
    private String queryOnlyJournals = "%20and%20pica.mat=O*"
    private String queryOrder       = "sortKeys=year,,1"
    
    private String formatIdentifier = 'picaxml'
    private GPathResult response
    
    private picaRecords   = []
    private currentRecord = null
    
	ZdbdbSruPicaConnector(BridgeInterface bridge, String queryIdentifier) {
		super(bridge)
        this.queryIdentifier = queryIdentifier
	}
        
    // ConnectorInterface
    
    @Override
    String getAPIQuery(String identifier) {
        return requestUrl + "&recordSchema=" + formatIdentifier + "&" + queryIdentifier + identifier + queryOnlyJournals + "&" + queryOrder
    }
    
    // TODO fix return value
    
    @Override
    Envelope poll(String identifier) {
        try {
            String q = getAPIQuery(identifier)
            
            log.info("polling(): " + q)
            String text = new URL(q).getText()
            
            response = new XmlSlurper().parseText(text)
            
            picaRecords = []
            response.records.record.each { r ->
                //def test = r.recordData.record.datafield.findAll{it.'@tag' == '016H'}.subfield.findAll{it.'@code' == '0'}
                //if("Elektronische Ressource".equals(test?.text()))
                    picaRecords << r
            }
            
        } catch(Exception e) {
            return getEnvelopeWithStatus(AbstractEnvelope.STATUS_ERROR)
        }
        
        getEnvelopeWithStatus(AbstractEnvelope.STATUS_OK)
    }
    
	@Override
	Envelope query(Object record, Query query) {
		try {
            getEnvelope(record, query)
		} catch(Exception e) {
			return getEnvelopeWithStatus(AbstractEnvelope.STATUS_ERROR)
		}
	}
            
    // FormatAdapterInterface

    // <zs:searchRetrieveResponse>
    //   <zs:records>
    //     <zs:record>          <------------------ x times
    //       <zs:recordData>
    //         <record>
    //           <datafield tag="039D">
    //             <subfield code="c">Online-Ausg.</subfield>
    //             <subfield code="a">International journal of accounting and information management</subfield>
    //             <subfield code="C">ZDB</subfield>
    //             <subfield code="6">24063605</subfield>
    //           </datafield>
       
    @Override
    Envelope getEnvelope(Object record, Query query) {
        
        if(response == null)
            return getEnvelopeWithStatus(AbstractEnvelope.STATUS_NO_RESPONSE)
        
        currentRecord = record   
        if(currentRecord == null)
            return getEnvelopeWithStatus(AbstractEnvelope.STATUS_ERROR)
            
        switch(query){
            case Query.ZDBID:
                return getFirstResultOnly('006Z', '0')
                break;
            case Query.GBV_GVKPPN:
                return getFirstResultOnly('003@', '0')
                break;
            case Query.GBV_EISSN:
                return getFirstResultOnly('005A', '0')
                break;
            case Query.GBV_PISSN:
                return getFirstResultOnly('005P', '0')
                break;
            case Query.GBV_TITLE:
                return getTitle()
                break;
            case Query.GBV_PUBLISHER:
                return getPublisherHistoryAsFatEnvelope()
                break;
            case Query.GBV_PUBLISHED_FROM:
                return getFirstResultOnly('011@', 'a')
                break;
            case Query.GBV_PUBLISHED_TO:
                return getFirstResultOnly('011@', 'b')
                break;
            case Query.GBV_HISTORY_EVENTS:
                return getHistoryEventAsFatEnvelope()
                break;
        }
        
        getEnvelopeWithStatus(AbstractEnvelope.STATUS_UNKNOWN_REQUEST)
    }
    
    Object getPicaRecords() {
        picaRecords
    }
    
    private Envelope getFirstResultOnly(String tag, String code) {
        def result = []
        
        result << getFirstPicaValue(currentRecord.recordData.record, tag, code)
        getEnvelopeWithMessage(result.minus(null).unique())
    }
    
    private String getFirstPicaValue(Object record, String tag, String code) {
        def df = record.datafield.find{it.'@tag' == tag}
        def sf = df.subfield.find{it.'@code' == code}

        log.debug("getPicaValue(" +  tag + "" + code + ") = " + sf)
        return sf ? sf.text() : null
    }  
    
    private ArrayList getAllPicaValues(Object record, String tag, String code) {
        def result = []
        def sf = record.datafield.findAll{it.'@tag' == tag}.subfield.findAll{it.'@code' == code}
        
        sf.each { f ->
            result << f.text()      
        }
        
        log.debug("getPicaValues(" +  tag + "" + code + ") = " + result)
        result
    }
    
    private Envelope getTitle() {
        def result = []
        
        // correction
        result << getFirstPicaValue(currentRecord.recordData.record, '025@', 'a')

        // or .. main title
        if(result.minus(null).isEmpty()) {
            result << getFirstPicaValue(currentRecord.recordData.record,'021A', 'a')
        }
        getEnvelopeWithMessage(result.minus(null).unique())
    }
    
    private Envelope getPublisherHistoryAsFatEnvelope() {
        def result          = []
        def resultStartDate = []
        def resultEndDate   = []
        def resultName      = []
        def resultStatus    = []
        
        currentRecord.recordData.record.datafield.findAll{it.'@tag' == '033A'}.each { df ->
            def n = df.subfield.find{it.'@code' == 'n'}.text() // TODO or use p here ?
            def h = df.subfield.find{it.'@code' == 'h'}.text()
            
            resultName      << (n ? n : null)
            resultStartDate << (h ? h : '')
            resultEndDate   << (h ? h : '')
            resultStatus    << null
        }
        log.debug("getPicaValues(033An) = " + resultName)
        log.debug("getPicaValues(033Ah) = " + resultStartDate)
        
        // TODO refactor this
        
        result << getEnvelopeWithComplexMessage([
            'name':      resultName,
            'startDate': resultStartDate,
            'endDate':   resultEndDate,
            'status':    resultStatus
        ])
       
        getEnvelopeWithMessage(result)
    } 
    
    private Envelope getHistoryEventAsFatEnvelope() {
        
        def result                = []
        def resultType            = []
        def resultTitle           = []
        def resultIdentifierValue = []
        def resultIdentifierType  = []
        def resultDate            = []

        currentRecord.recordData.record.datafield.findAll{it.'@tag' == '039E'}.each { df ->

            def b  = df.subfield.find{it.'@code' == 'b'}.text() // s=später, f=früher
            def Y  = df.subfield.find{it.'@code' == 'Y'}.text() // default (Y/D)
            def D  = df.subfield.find{it.'@code' == 'D'}.text() // falls in der ZDB ein übergeordneter Titel existiert (Y/D)
            def H  = df.subfield.find{it.'@code' == 'H'}.text()
            def C  = df.subfield.find{it.'@code' == 'C'}.text() // ID-Typ
            def f0 = df.subfield.find{it.'@code' == '0'}.text()

            resultType            <<  (b ? b : null)
            resultTitle           <<  (D ? D : (Y ? Y : null))
            resultIdentifierType  <<  (C ? C : 'zdb') // default
            resultIdentifierValue <<  (f0 ? f0 : null)
            resultDate            <<  (H ? H : null)
        }
        
        // zdbdb
        log.debug("getPicaValues(039Eb) = "     + resultType)
        log.debug("getPicaValues(039E(D|Y)) = " + resultTitle)
        log.debug("getPicaValues(039EC) = "     + resultIdentifierType)
        log.debug("getPicaValues(039E0) = "     + resultIdentifierValue)
        log.debug("getPicaValues(039EH) = "     + resultDate)    
        
        result << getEnvelopeWithComplexMessage([
            'type':            resultType,
            'title':           resultTitle,
            'identifierType':  resultIdentifierType,
            'identifierValue': resultIdentifierValue,
            'date':            resultDate
        ])
       
        getEnvelopeWithMessage(result)
    }
}
