package de.hbznrw.ygor.iet.connector

import groovy.util.slurpersupport.GPathResult

import java.util.ArrayList

import org.apache.commons.csv.CSVRecord

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.interfaces.*
import de.hbznrw.ygor.iet.export.*


/**
 * Controlling API calls using sru.gbv.de
 * 
 * @author David Klober
 *
 */
class SruPicaConnector extends ConnectorAbstract {
	
	private String requestUrl       = "http://sru.gbv.de/gvk?version=1.2&operation=searchRetrieve&maximumRecords=10"
	private String queryIdentifier  = 'query=pica.iss%3D'
    private String queryOnlyJournals = "%20and%20(pica.mak=Obvz%20or%20pica.mak=Obv)"
    private String queryOrder       = "sortKeys=year,,1"
    
    private String formatIdentifier = 'picaxml'
    private GPathResult response
    
    public picaRecords   = []
    public currentRecord = null
    
	SruPicaConnector(BridgeInterface bridge) {
		super(bridge)
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
            String text = new URL(getAPIQuery(identifier)).getText()
            response = new XmlSlurper().parseText(text)
            
            picaRecords = []
            response.records.record.each { r ->
                def test = r.recordData.record.datafield.findAll{it.'@tag' == '016H'}.subfield.findAll{it.'@code' == '0'}
                if("Elektronische Ressource".equals(test?.text()))
                    picaRecords << r
            }
            
        } catch(Exception e) {
            return getEnvelopeWithStatus(Status.STATUS_ERROR)
        }
        
        getEnvelopeWithStatus(Status.STATUS_OK)
    }
    
	@Override
	Envelope query(Object record, Query query) {
		try {
            getEnvelope(record, query)
		} catch(Exception e) {
			return getEnvelopeWithStatus(Status.STATUS_ERROR)
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
            return getEnvelopeWithStatus(Status.STATUS_NO_RESPONSE)
        
        currentRecord = record   
        if(currentRecord == null)
            return getEnvelopeWithStatus(Status.STATUS_ERROR)
            
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
                return getPublisherAsFatEnvelope()
                break;
            case Query.GBV_PUBLISHED_FROM:
                return getFirstResultOnly('011@', 'a')
                break;
            case Query.GBV_PUBLISHED_TO:
                return getFirstResultOnly('011@', 'b')
                break;
            case Query.GBV_TIPP_URL:
                return getAllTippURL()
                break;
            case Query.GBV_TIPP_COVERAGE:
                return getTippCoverageAsFatEnvelope()
                break;
            case Query.GBV_HISTORY_EVENTS:
                return getHistoryEventAsFatEnvelope()
                break;
        }
        
        getEnvelopeWithStatus(Status.UNKNOWN_REQUEST)
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
    
    private Envelope getPublisherAsFatEnvelope() {
        def result          = []
        def resultStartDate = []
        def resultEndDate   = []
        def resultName      = []
        def resultStatus    = []
        
        currentRecord.recordData.record.datafield.findAll{it.'@tag' == '033A'}.each { df ->
            def n = df.subfield.find{it.'@code' == 'n'}.text() // TODO or use p here ?
            def h = df.subfield.find{it.'@code' == 'h'}.text()
            
            resultName      << n ? n : null
            resultStartDate << h ? h : null
            resultEndDate   << h ? h : null
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
        
        // TODO:
        def result                = []
        def resultType            = []
        def resultTitle           = []
        def resultIdentifierValue = []
        def resultIdentifierType  = []
        
        currentRecord.recordData.record.datafield.findAll{it.'@tag' == '039E'}.each { df ->
            def c  = df.subfield.find{it.'@code' == 'c'}.text()
            def a  = df.subfield.find{it.'@code' == 'a'}.text()
            def C  = df.subfield.find{it.'@code' == 'C'}.text()
            def f6 = df.subfield.find{it.'@code' == '6'}.text()
            
            resultType            << c ? c : null
            resultTitle           << a ? a : null
            resultIdentifierType  << C ? C : null
            resultIdentifierValue << f6?f6 : null
        }
        
        log.debug("getPicaValues(039Ec) = " + resultType)
        log.debug("getPicaValues(039Ea) = " + resultTitle)
        log.debug("getPicaValues(039EC) = " + resultIdentifierType)
        log.debug("getPicaValues(039E6) = " + resultIdentifierValue)
        
        // TODO refactor this
        
        result << getEnvelopeWithComplexMessage([
            'type':            resultType,
            'title':           resultTitle,
            'identifierType':  resultIdentifierType,
            'identifierValue': resultIdentifierValue
        ])
       
        getEnvelopeWithMessage(result)
    }
    
    private Envelope getAllTippURL() {
        def result = []
        
        result += getAllPicaValues(currentRecord.recordData.record, '009P', 'a') // TODO
        getEnvelopeWithMessage(result.minus(null).unique())
    }

    private Envelope getTippCoverageAsFatEnvelope() { 
        def result              = []     
        def resultCoverageNote  = []
        def resultEmbargo       = []
        def resultEndDate       = []
        def resultEndIssue      = []
        def resultEndVolume     = []
        def resultStartDate     = []
        def resultStartIssue    = []
        def resultStartVolume   = []
        
        currentRecord.recordData.record.datafield.findAll{it.'@tag' == '009P'}.each { df ->
            def x = df.subfield.find{it.'@code' == 'x'}.text()
            def z = df.subfield.find{it.'@code' == 'z'}.text()

            resultCoverageNote  << z ? z : null
            resultEmbargo       << null
            resultEndDate       << x ? x : null
            resultEndIssue      << null
            resultEndVolume     << x ? x : null
            resultStartDate     << x ? x : null
            resultStartIssue    << null
            resultStartVolume   << x ? x : null
        }
        
        log.debug("getPicaValues(009Px) = " + resultStartDate)
        log.debug("getPicaValues(009Pz) = " + resultCoverageNote)
        
        // TODO refactor this
        
        result << getEnvelopeWithComplexMessage([
            'coverageNote': resultCoverageNote,
            'embargo':      resultEmbargo,
            'endDate':      resultEndDate,
            'endIssue':     resultEndIssue,
            'endVolume':    resultEndVolume,
            'startDate':    resultStartDate,
            'startIssue':   resultStartIssue,
            'startVolume':  resultStartVolume
        ])
        
        getEnvelopeWithMessage(result)
    }
}
