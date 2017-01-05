package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.enums.*

class DataContainer {

    Info    info
    Package pkg
    def     titles
    
    DataContainer() {

        info = new Info(
            type:   "TODO",
            ygor:   "TODO",
            date:   new Date().format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone('GMT+1'))
        )
        pkg      = new MV(new Package())
        titles   = new MV([]) // list
    }
}