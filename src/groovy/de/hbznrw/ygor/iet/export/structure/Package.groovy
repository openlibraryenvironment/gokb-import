package de.hbznrw.ygor.iet.export.structure

import java.util.ArrayList

import de.hbznrw.ygor.iet.enums.*

class Package {
    
    Pod packageHeader = new Pod(new PackageHeader())
    HashMap tipps     = [:]
    
    Package() {        
        def cg = PackageStruct.getNewPackageHeaderCuratoryGroup()
        cg.curatoryGroup.v = "LAS:eR"
        cg.curatoryGroup.m = Status.HARDCODED
        packageHeader.v.curatoryGroups << cg
    }
}
