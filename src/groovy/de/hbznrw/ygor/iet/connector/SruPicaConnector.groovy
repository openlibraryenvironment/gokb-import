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
    private picaRecords             = []
    
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
            
            println url
            
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
                return getAllPublisher()
                break;
            case Query.GBV_PUBLISHED_FROM:
                return getFirstResultOnly('011@', 'a')
                break;
            case Query.GBV_PUBLISHED_TO:
                return getFirstResultOnly('011@', 'b')
                break;
            case Query.GBV_TIPP_URL:
                return getAllTippUrl()
                break;
        }
        
        getEnvelopeWithStatus(Status.UNKNOWN_REQUEST)
    }
    
    private Envelope getFirstResultOnly(String tag, String code) {
        def result = []
        
        picaRecords.each { record ->
            result << getFirstPicaValue(record.recordData.record, tag, code)
        }
        getEnvelopeWithMessage(result.minus(null).unique())
    }
    
    private String getFirstPicaValue(Object record, String tag, String code) {
        def df = record.datafield.find{it.'@tag' == tag}
        def sf = df.subfield.find{it.'@code' == code}

        println " .. getPicaValue(" +  tag + "" + code + ") = " + sf
        return sf ? sf.text() : null
    }  
    
    private ArrayList getAllPicaValues(Object record, String tag, String code) {
        def result = []
        def sf = record.datafield.findAll{it.'@tag' == tag}.subfield.findAll{it.'@code' == code}
        
        sf.each { f ->
            result << f.text()      
        }
        
        println " .. getPicaValues(" +  tag + "" + code + ") = " + result
        result
    }
    
    private Envelope getTitle() {
        def result = []
        
        // correction
        picaRecords.each { record ->
            result << getFirstPicaValue(record.recordData.record, '025@', 'a')
        }
        // or .. main title
        if(result.minus(null).isEmpty()) {
            picaRecords.each { record ->
                result << getFirstPicaValue(record.recordData.record,'021A', 'a')
            }
        }
        getEnvelopeWithMessage(result.minus(null).unique())
    }
    
    private Envelope getAllPublisher() {
        def resultName = []
        
        picaRecords.each { record ->
            resultName += getAllPicaValues(record.recordData.record, '033A', 'n') // TODO
        }
        
        getEnvelopeWithComplexMessage([
            'name':      resultName,
            'startDate': '',
            'endDate':   '',
            'status':    '',
        ])
        
        //getEnvelopeWithMessage(result.minus(null))
    }
    
    private Envelope getAllTippUrl() {
        def result = []
        
        picaRecords.each { record ->
            result += getAllPicaValues(record.recordData.record, '009P', 'a') // TODO
        }
        getEnvelopeWithMessage(result.minus(null).unique())
    }
}
