package de.hbznrw.ygor.iet.export.structure

import java.util.ArrayList

import de.hbznrw.ygor.iet.enums.*

class PackageHeader {
    
    Pod breakable        = new Pod("No",  Status.HARDCODED)
    Pod consistent       = new Pod("Yes", Status.HARDCODED)
    Pod fixed            = new Pod("Yes", Status.HARDCODED)
    Pod global           = new Pod("Consortium", Status.HARDCODED)
    Pod listStatus       = new Pod("Checked",    Status.HARDCODED)
    Pod listVerifiedDate = new Pod("")
    Pod listVerifier     = new Pod("")
    Pod name             = new Pod("")
    Pod nominalPlatform  = new Pod("")
    Pod nominalProvider  = new Pod("")
    Pod paymentType      = new Pod("")
    Pod scope            = new Pod("")
    Pod userListVerifier = new Pod("")
    
    Pod source = new Pod(PackageStruct.getNewPackageHeaderSource())
    
    def additionalProperties = [] // TODO
    ArrayList<PackageHeaderCuratoryGroup> curatoryGroups = []
    def variantNames         = [] // TODO
}
