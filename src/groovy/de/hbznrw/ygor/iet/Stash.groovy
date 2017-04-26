package de.hbznrw.ygor.iet

import de.hbznrw.ygor.bridges.*
import de.hbznrw.ygor.iet.export.structure.*
import groovy.util.logging.Log4j

@Log4j
class Stash {
    
    static final IGNORED_KBART_ENTRIES = 'ignoredKbartEntries'
    
    HashMap values

    Stash(){  
        values = [:]
        values[ZdbBridge.IDENTIFIER]        = [:]
        values[TitleStruct.ISSN]            = [:]
        values[KbartBridge.IDENTIFIER]      = [:]
        values[Stash.IGNORED_KBART_ENTRIES] = []
    }
    
    def put(Object key, Object value){
        values[key] << value
    }
    
    def get(String key){
        values[key]
    }
}
