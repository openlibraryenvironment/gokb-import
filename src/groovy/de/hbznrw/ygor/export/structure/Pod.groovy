package de.hbznrw.ygor.export.structure

import de.hbznrw.ygor.enums.*

class Pod {
    
    def m  = Status.UNDEFINED   // meta
    def v                       // value
    def org                     // orgValue
    
    Pod() {        
    }
    
    Pod(def value, Status meta) {
        this.m = meta
        this.v = value
    }
    
    Pod(Status meta) {
        this.m = meta
    }
    
    Pod(Object value) {
        this.v = value
    }
}