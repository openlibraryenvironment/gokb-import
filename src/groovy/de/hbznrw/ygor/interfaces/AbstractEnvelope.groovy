package de.hbznrw.ygor.interfaces
/**
 * Class for wrapping API call results
 */

abstract class AbstractEnvelope {

    static final TYPE_SIMPLE  = 'simple'
    static final TYPE_COMPLEX = 'complex'
    
    
    static final STATUS_UNKNOWN_REQUEST = "UNKNOWN_REQUEST"

    // communication state
    
    static final STATUS_OK              = "STATUS_OK"
    static final STATUS_ERROR           = "STATUS_ERROR"
    static final STATUS_NO_RESPONSE     = "STATUS_NO_RESPONSE"
    
    // response state
    
    static final RESULT_OK                  = "RESULT_OK"
    static final RESULT_NO_MATCH            = "RESULT_NO_MATCH"
    static final RESULT_MULTIPLE_MATCHES    = "RESULT_MULTIPLE_MATCHES"
}
