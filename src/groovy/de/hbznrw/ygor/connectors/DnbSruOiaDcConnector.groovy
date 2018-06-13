package de.hbznrw.ygor.connectors

import groovy.util.logging.Log4j
import groovy.util.slurpersupport.GPathResult
import de.hbznrw.ygor.processing.Envelope
import de.hbznrw.ygor.enums.*
import de.hbznrw.ygor.interfaces.*


/**
 * Controlling API calls using services.dnb.de/sru
 */
@Deprecated
@Log4j
class DnbSruOiaDcConnector extends AbstractConnector {
	
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

    @Override
    def poll(String issn) {
        try {
            String q = getAPIQuery(issn)
            
            log.info("polling(): " + q)
            String text = new URL(q).getText()
            
            response = new XmlSlurper().parseText(text)
            
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
            
    // FormatAdapterInterface

    // <records>
    //   <record>
    //     <recordData>
    // ..
       
    @Override
    Envelope getEnvelope(Query query) {
        if(response == null)
            return getEnvelopeWithStatus(AbstractEnvelope.STATUS_NO_RESPONSE)
            
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
        
        getEnvelopeWithStatus(AbstractEnvelope.STATUS_UNKNOWN_REQUEST)
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
