package de.hbznrw.ygor.iet

import de.hbznrw.ygor.iet.bridge.*
import de.hbznrw.ygor.iet.export.structure.*
import groovy.util.logging.Log4j

@Log4j
class Stash {
    
    HashMap values

    Stash(){  
        values = [:]
        values[ZdbBridge.IDENTIFIER]    = [:]
        values[TitleStruct.ISSN]        = [:]
        values[KbartBridge.IDENTIFIER]  = [:]
    }
    
    def put(Object key, Object value){
        values[key] << value
    }
    
    def get(String key){
        values[key]
    }
}
