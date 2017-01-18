package de.hbznrw.ygor.iet.export.structure

import de.hbznrw.ygor.iet.enums.*

class Pod {
    
    def m  = Status.UNDEFINED
    def v
    
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