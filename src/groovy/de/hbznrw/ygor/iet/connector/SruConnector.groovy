package de.hbznrw.ygor.iet.connector

import groovy.util.slurpersupport.GPathResult

import java.util.ArrayList

import org.apache.commons.csv.CSVRecord

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.interfaces.*


/**
 * Controlling API calls using services.dnb.de/sru
 * 
 * @author David Klober
 *
 */
class SruConnector extends ConnectorAbstract {
	
	private String requestUrl       = "http://services.dnb.de/sru/zdb?version=1.1&operation=searchRetrieve"
	private String queryIdentifier  = 'query=iss='
    
    private String formatIdentifier = 'oai_dc'
    private GPathResult response

	SruConnector(BridgeInterface bridge) {
		super(bridge)
	}
    
    
    // ConnectorInterface
    
    @Override
    Envelope poll(String issn) {
        try {
            String url  = requestUrl + "&recordSchema=" + formatIdentifier + "&" + queryIdentifier + issn
            String text = new URL(url).getText()
            
            response = new XmlSlurper().parseText(text)
            
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
            
    // FormatAdapterInterface

    // <records>
    //   <record>
    //     <recordData>
    // ..
       
    @Override
    Envelope getEnvelope(Query query) {
        if(response == null)
            return getEnvelopeWithStatus(Status.STATUS_NO_RESPONSE)
            
        switch(query){
            case Query.ZDBID:
                return getZdbID()
                break;
            case Query.TITLE:
                return getTitle()
                break;
        }
        
        getEnvelopeWithStatus(Status.UNKNOWN_REQUEST)
    }
    
    // <dc>
    //   <identifier xsi:type="dnb:ZDBID">2530653-4
    //   <type>Online-Ressource
    
    private Envelope getZdbID() {
        def result = []
        
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
        getEnvelopeWithMessage(result)
    }
    
    // <dc>
    //   <title>Blah
    
    private Envelope getTitle() {
        def result = []
        
        response.records.record.each { record ->
            record.recordData.dc.title.each { i ->
                result << i
            }
        }
        getEnvelopeWithMessage(result)
    }
}
