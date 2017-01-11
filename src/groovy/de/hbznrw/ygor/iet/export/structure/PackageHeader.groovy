package de.hbznrw.ygor.iet.export.structure

import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.export.Pod

class PackageHeader {
    
    Pod breakable        = new Pod("")
    Pod consistent       = new Pod("")
    Pod fixed            = new Pod("")
    Pod global           = new Pod("")
    Pod listStatus       = new Pod("")
    Pod listVerifiedDate = new Pod("")
    Pod listVerifier     = new Pod("")
    Pod name             = new Pod("")
    Pod nominalPlatform  = new Pod("")
    Pod nominalProvider  = new Pod("")
    Pod paymentType      = new Pod("")
    Pod scope            = new Pod("")
    Pod userListVerifier = new Pod("")
    
    Pod source = new Pod(PackageStruct.getNewPackageHeaderSource())
    
    Pod additionalProperties = new Pod([]) // list
    Pod curatoryGroups       = new Pod([]) // list
    Pod variantNames         = new Pod([]) // list
}
