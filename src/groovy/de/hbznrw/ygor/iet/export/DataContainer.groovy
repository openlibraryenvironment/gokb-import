package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.enums.*

class DataContainer {

    Info info
    Pod   pkg
    Pod   titles
    
    DataContainer() {

        info = new Info(
            type:   "TODO",
            ygor:   "TODO",
            date:   new Date().format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone('GMT+1')),
            api:    []
        )
        pkg      = new Pod(new Package())
        titles   = new Pod([:]) // list
    }
}