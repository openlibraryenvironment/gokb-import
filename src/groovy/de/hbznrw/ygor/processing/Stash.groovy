package de.hbznrw.ygor.processing

import de.hbznrw.ygor.bridges.*
import de.hbznrw.ygor.connectors.KbartConnector
import de.hbznrw.ygor.export.structure.TitleStruct
import groovy.util.logging.Log4j

@Log4j
class Stash {
    
    static final PROCESSED_KBART_ENTRIES = 'processedKbartEntries'
    static final IGNORED_KBART_ENTRIES   = 'ignoredKbartEntries'

    HashMap values

    Stash(){  
        values = [:]

        // api enrichment
        values[KbartConnector.KBART_HEADER_ZDB_ID]            = [:]
        values[KbartConnector.KBART_HEADER_ONLINE_IDENTIFIER] = [:]
        values[KbartConnector.KBART_HEADER_PRINT_IDENTIFIER]  = [:]

        // file enrichment
        values[KbartBridge.IDENTIFIER]      = [:]

        // meta
        values[Stash.PROCESSED_KBART_ENTRIES] = 0
        values[Stash.IGNORED_KBART_ENTRIES]   = []
    }
    
    def put(Object key, Object value){
        values[key] << value
    }
    
    def get(String key){
        values[key]
    }

    def getKeyByValue(String key, String value){
        def result = []
        def map = values[key]

        map?.keySet().each { k ->
            if (map.get(k) == value) {
                result << k
            }
        }
        if (result.size() > 1) {
            println "WARNING: getKeyByValue(" + key + ", " + value + ") ->> multiple matches"
        }

        result.size() == 1 ? result.get(0) : null
    }
}
