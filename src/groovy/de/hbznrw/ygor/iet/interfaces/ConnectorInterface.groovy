package de.hbznrw.ygor.iet.interfaces

import java.util.ArrayList

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.Query
import de.hbznrw.ygor.iet.enums.Status


interface ConnectorInterface {
    
    /**
     * 
     * @param issn
     * @return
     */
    String getAPIQuery(String issn)
    
    /**
     * 
     * @param issn
     * @return
     */
    Envelope poll(String issn)
    
    /**
     *
     * @param query
     * @return
     */
    Envelope query(Query query)
    
    /**
     * @return Envelope depending on query
     */
    Envelope getEnvelope(Query query)
    
    /**
     * @param status
     * @return Envelope with given status and message
     */
    Envelope getEnvelopeWithStatus(Status state)
    
    /**
     * @param result
     * @return Envelope with status and given message
     */
    Envelope getEnvelopeWithMessage(ArrayList message)
}
