package de.hbznrw.ygor.interfaces

import de.hbznrw.ygor.processing.Envelope
import de.hbznrw.ygor.enums.Query

interface ConnectorInterface {
    
    /**
     * 
     * @param identifier
     * @return
     */
    String getAPIQuery(String identifier, String queryIdentifier)
    
    /**
     * Storing polling response
     * 
     * @param identifier
     * @param queryIdentifier
     * @param publicationTitle
     * @return
     */
    def poll(String identifier, String queryIdentifier, def publicationTitle)
    
    /**
     * 
     * @param query
     * @return
     */
    Envelope query(Query query)
    
    /**
     *
     * @param record
     * @param query
     * @return
     */
    Envelope query(Object record, Query query)
    
    /**
     * @param query
     * @return Envelope depending on query
     */
    Envelope getEnvelope(Query query)
    
    /**
     * @param record
     * @param query
     * @return Envelope depending on query
     */
    Envelope getEnvelope(Object record, Query query)
    
    /**
     * @param status
     * @return Envelope with given status and message
     */
    Envelope getEnvelopeWithStatus(Object state)
    
    /**
     * @param result
     * @return Envelope with status and given message
     */
    Envelope getEnvelopeWithMessage(ArrayList message)
}
