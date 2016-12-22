package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.enums.*

class DataContainer {

    Meta    meta
    Package pkg
    def     titles
    
    DataContainer() {

        meta = new Meta(
            type:   "alpha",
            ygor:   "0.3",
            date:   new Date().format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone('GMT+1'))
        )
        pkg      = new Package()
        titles   = []
    }
}

class Meta {
    
    String type = ""
    String ygor = ""
    String date = ""
}