package de.hbznrw.ygor.iet.export.structure

import de.hbznrw.ygor.iet.enums.*

class Package {
    
    Pod packageHeader = new Pod(new PackageHeader())
    Pod tipps         = new Pod([:]) // list
    
    Package() {        
        def tmp = PackageStruct.getNewPackageHeaderCuratoryGroup()
        tmp.curatoryGroup.v = new Pod("LAS:eR", Status.HARDCODED)
        packageHeader.v.curatoryGroups.v << new Pod(tmp)
    }
}
