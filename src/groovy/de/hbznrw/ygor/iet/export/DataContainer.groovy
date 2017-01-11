package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.export.structure.Package

class DataContainer {

    Meta info
    Pod  pkg
    Pod  titles
    
    DataContainer() {

        info = new Meta(
            type:   "TODO",
            ygor:   "TODO",
            date:   new Date().format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone('GMT+1')),
            api:    [],
            stats:  [:]
        )
        pkg      = new Pod(new Package())
        titles   = new Pod([:]) // list
    }
}