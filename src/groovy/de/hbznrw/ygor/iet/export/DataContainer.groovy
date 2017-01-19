package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.export.structure.*

class DataContainer {

    Meta    info
    Package pkg
    HashMap titles
    
    DataContainer() {

        info = new Meta(
            type:   "TODO",
            ygor:   "TODO",
            date:   new Date().format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone('GMT+1')),
            api:    [],
            stats:  [:]
        )
        pkg      = new Package()
        titles   = [:]
    }
}