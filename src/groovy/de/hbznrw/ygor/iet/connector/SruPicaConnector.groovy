package de.hbznrw.ygor.iet.connector

import groovy.util.slurpersupport.GPathResult

import java.util.ArrayList

import org.apache.commons.csv.CSVRecord

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.interfaces.*
import de.hbznrw.ygor.iet.export.*


/**
 * Controlling API calls using services.dnb.de/sru
 * 
 * @author David Klober
 *
 */
class SruPicaConnector extends ConnectorAbstract {
	
	private String requestUrl       = "http://sru.gbv.de/gvk?version=1.2&operation=searchRetrieve&maximumRecords=10"
	private String queryIdentifier  = 'query=pica.iss%3D'
    
    private String formatIdentifier = 'picaxml'
    private GPathResult response
    private obvRecords              = []
    
	SruPicaConnector(BridgeInterface bridge) {
		super(bridge)
	}
    
    
    // ConnectorInterface
    
    // TODO fix return value
    
    @Override
    Envelope poll(String issn) {
        try {
            String url  = requestUrl + "&recordSchema=" + formatIdentifier + "&" + queryIdentifier + issn
            String text = new URL(url).getText()
            
            response = new XmlSlurper().parseText(text)
            
            obvRecords = []
            response.records.record.each { r ->
                def df = r.recordData.record.find{it.'@tag' == '002@'}
                def sf = df.subfield.find{it.'@code' == '0'}
                obvRecords << r
            }
            
        } catch(Exception e) {
            return getEnvelopeWithStatus(Status.STATUS_ERROR)
        }
        
        getEnvelopeWithStatus(Status.STATUS_OK)
    }
    
	@Override
	Envelope query(Query query) {
		try {
            getEnvelope(query)
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
    Envelope getEnvelope(Query query) {
        if(response == null)
            return getEnvelopeWithStatus(Status.STATUS_NO_RESPONSE)
            
        switch(query){
            case Query.ZDBID:
                return resultOnly('006Z', '0')
                break;
            case Query.GBVGVKPPN:
                return resultOnly('003@', '0')
                break;
            case Query.GBVEISSN:
                return resultOnly('005A', '0')
                break;
            case Query.GBVPISSN:
                return resultOnly('005P', '0')
                break;
            case Query.GBVTITLE:
                return getTitle()
                break;
            case Query.GBVPUBLISHER:
                return getPublisher()
                break;
        }
        
        getEnvelopeWithStatus(Status.UNKNOWN_REQUEST)
    }
    
    private String getPicaValue(Object record, String tag, String code) {
        def df = record.datafield.find{it.'@tag' == tag}
        def sf = df.subfield.find{it.'@code' == code}

        println " .. getPicaValue(" +  tag + "" + code + ") = " + sf
        return sf ? sf.text() : null
    }  
    
    private Envelope resultOnly(String tag, String code) {
        def result = []
        
        obvRecords.each { record ->
            result << getPicaValue(record.recordData.record, tag, code)
        }
        getEnvelopeWithMessage(result.minus(null).unique())
    }

    private Envelope getTitle() {
        def result = []
        
        // correction
        obvRecords.each { record ->
            result << getPicaValue(record.recordData.record, '025@', 'a')
        }
        // or .. main title
        if(result.minus(null).isEmpty()) {
            obvRecords.each { record ->
                result << getPicaValue(record.recordData.record,'021A', 'a')
            }
        }
        getEnvelopeWithMessage(result.minus(null).unique())
    }

    private Envelope getPublisher() {
        def resultPublisher     = []
        def resultPublisherDate = []
        
        obvRecords.each { record ->
            resultPublisher << getPicaValue(record.recordData.record, '033A', 'n')
            
            def date1 = getPicaValue(record.recordData.record, '011@', 'a')
            def date2 = getPicaValue(record.recordData.record, '031N', 'j')
            date1     = date1 ? date1 : date2
            date1     = date1 ? date1.split("/")[0] : null
            
            resultPublisherDate << date1
        }
        
        getEnvelopeWithComplexMessage([
            'name':     resultPublisher.minus(null), 
            'startDate':resultPublisherDate.minus(null)
        ])
    }
}
