package de.hbznrw.ygor.export.structure

import de.hbznrw.ygor.enums.*

class PackageHeader {
    
    Pod breakable        = new Pod(FixedValues.packageHeader_breakable,  Status.HARDCODED)
    Pod consistent       = new Pod(FixedValues.packageHeader_consistent, Status.HARDCODED)
    Pod fixed            = new Pod(FixedValues.packageHeader_fixed,      Status.HARDCODED)
    Pod global           = new Pod(FixedValues.packageHeader_global,     Status.HARDCODED)
    Pod listStatus       = new Pod(FixedValues.packageHeader_listStatus, Status.HARDCODED)
    Pod listVerifiedDate = new Pod("")
    Pod listVerifier     = new Pod("")
    Pod name             = new Pod("")
    Pod nominalProvider  = new Pod("")
    Pod paymentType      = new Pod("")
    Pod scope            = new Pod("")
    Pod userListVerifier = new Pod("")
    
    Pod source = new Pod(PackageStruct.getNewPackageHeaderSource())
    PackageHeaderNominalPlatform nominalPlatform = PackageStruct.getNewPackageHeaderNominalPlatform()
    
    def additionalProperties = [] // TODO
    ArrayList curatoryGroups = []
    Pod isil             = new Pod("")
}
