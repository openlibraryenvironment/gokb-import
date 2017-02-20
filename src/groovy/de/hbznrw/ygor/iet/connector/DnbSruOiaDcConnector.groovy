package de.hbznrw.ygor.iet.connector

import groovy.util.slurpersupport.GPathResult
import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.interfaces.*


/**
 * Controlling API calls using services.dnb.de/sru
 * 
 * @author David Klober
 *
 */
class DnbSruOiaDcConnector extends ConnectorAbstract {
	
	private String requestUrl       = "http://services.dnb.de/sru/zdb?version=1.1&operation=searchRetrieve"
	private String queryIdentifier  = 'query=iss='
    
    private String formatIdentifier = 'oai_dc'
    private GPathResult response

	DnbSruOiaDcConnector(BridgeInterface bridge) {
		super(bridge)
	}
    
    
    // ConnectorInterface
       
    @Override
    String getAPIQuery(String issn) {
        return requestUrl + "&recordSchema=" + formatIdentifier + "&" + queryIdentifier + issn
    }
    
    // TODO fix return value
    
    @Override
    Envelope poll(String issn) {
        try {
            String text = new URL(getAPIQuery(issn)).getText()
            
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
            case Query.ZDB_TITLE:
                return getTitle()
                break;
            case Query.ZDB_PUBLISHER:
                return getPublisher()
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
                    def s = i.toString()
                    result << s
                }
            }
        }
        getEnvelopeWithMessage(result)
    }
    
    // <dc>
    //   <title>Blah [Elektronische Ressource] etc etc
    //   <type>Online-Ressource
    
    private Envelope getTitle() {
        def result = []
        
        response.records.record.each { record ->
            record.recordData.dc.type.findAll { type ->
                type.text() == "Online-Ressource"
            }.each { type ->
                type.parent().title.each { t ->                
                    def s = t.toString()
                    result << s.replace("[Elektronische Ressource]", "")
                }
            }
        }
        getEnvelopeWithMessage(result)
    }
    
    // <dc>
    //   <publisher>American Association of Pharmaceutical Scientists
    //   <type>Online-Ressource
    
    // <dc>
    //   <date>2008
    //   <type>Online-Ressource
    
    private Envelope getPublisher() {
        def resultPublisher     = []
        def resultPublisherDate = []
        
        response.records.record.each { record ->
            record.recordData.dc.type.findAll { type ->
                type.text() == "Online-Ressource"
            }.each { type ->
                type.parent().publisher.each { p ->
                    resultPublisher << p.toString()
                }
                type.parent().date.each { d ->
                    resultPublisherDate << d.toString() + '-01-01'
                }
            }
        }
        
        getEnvelopeWithComplexMessage([
            'name':     resultPublisher, 
            'startDate':resultPublisherDate
        ])
    }
       
}
