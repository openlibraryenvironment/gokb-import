package de.hbznrw.ygor.iet.export.structure

import de.hbznrw.ygor.iet.enums.*

class Package {
    
    Pod packageHeader = new Pod(new PackageHeader())
    HashMap tipps     = [:]
    
    Package() {
        packageHeader.v.curatoryGroups << new Pod("LAS:eR", Status.HARDCODED)
    }
}
