package de.hbznrw.ygor.iet.export

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

class PackageStruct {
    
    // Package
    
    static Tipp getNewTipp() {
        return new Tipp()
    }

    // PackageHeader
    
    static PackageHeaderVariantName getNewPackageHeaderVariantName() {
        return new PackageHeaderVariantName()
    }
    
    static PackageHeaderCuratoryGroup getNewPackageHeaderCuratoryGroup() {
        return new PackageHeaderCuratoryGroup()
    }
    
    static PackageHeaderSource getNewPackageHeaderSource() {
        return new PackageHeaderSource()
    }
    
    // Tipps
    
    static TippCoverage getNewTippCoverage() {
        return new TippCoverage()
    }
    
    static TippPlatform getNewTippPlatform() {
        return new TippPlatform()
    }
    
    static TippTitle getNewTippTitle() {
        return new TippTitle()
    }
    
    // TippTitle
    
    static TippTitleIdentifier getNewTippTitleIdentifier() {
        return new TippTitleIdentifier()
    }
}

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

class PackageHeaderIdentifier {
      
    Pod type  = new Pod("")
    Pod value = new Pod("")
}

class PackageHeaderVariantName {
       
    Pod variantName = new Pod("")
}

class PackageHeaderCuratoryGroup {
     
    Pod curatoryGroup = new Pod("")
}

class PackageHeaderSource {
      
    Pod name     = new Pod("")
    Pod normname = new Pod("")
    Pod url      = new Pod("")
}

