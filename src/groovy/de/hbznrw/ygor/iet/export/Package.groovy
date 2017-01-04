package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.enums.*

class Package {
    
    PackageHeader packageHeader = new PackageHeader()
    def tipps                   = [] // list
    
    Package() {
    }
}

class PackageStruct {
    
    // Package
    
    static Tipp getNewTipp() {
        return new Tipp()
    }
    
    static Tipp getNewTipp(String issn) {
        return new Tipp(issn)
    }
    
    // PackageHeader
    
    static PackageHeaderVariantName getNewPackageHeaderVariantName() {
        return new PackageHeaderVariantName()
    }
    
    static PackageHeaderCuratoryGroups getNewPackageHeaderCuratoryGroups() {
        return new PackageHeaderCuratoryGroups()
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
    
    String breakable        = ""
    String consistent       = ""
    String fixed            = ""
    String global           = ""
    String listStatus       = ""
    String listVerifiedDate = ""
    String listVerifier     = ""
    String name             = ""
    String nominalPlatform  = ""
    String nominalProvider  = ""
    String paymentType      = ""
    String scope            = ""
    String userListVerifier = ""
    
    PackageHeaderSource source = PackageStruct.getNewPackageHeaderSource()
    
    def additionalProperties = [] // list
    def curatoryGroups       = [] // list
    def variantNames         = [] // list

    PackageHeader() {
    }
}

class PackageHeaderIdentifier {
    
    String _meta = Status.UNDEFINED   
    String type  = ""
    String value = ""
}

class PackageHeaderVariantName {
    
    String _meta         = Status.UNDEFINED    
    String variantName   = ""
}

class PackageHeaderCuratoryGroups {
    
    String _meta         = Status.UNDEFINED  
    String curatoryGroup = ""
}

class PackageHeaderSource {
    
    String _meta    = Status.UNDEFINED   
    String name     = ""
    String normname = ""
    String url      = ""
}

class Tipp {
    
    String _meta       = Status.UNDEFINED
    
    String accessEnd
    String accessStart 
    
    String medium      = ""
    String status      = ""   
    String url         = ""
    
    def title          = PackageStruct.getNewTippTitle()
    def platform       = PackageStruct.getNewTippPlatform()
    def coverage       = [] // list
    
    Tipp() {    
    }
    
    Tipp(String issn) {
        
        def tmp     = PackageStruct.getNewTippTitleIdentifier()
        tmp.type    = TitleStruct.ISSN
        tmp.value   = issn
        tmp._meta   = Status.UNDEFINED
        title.identifiers << tmp
    }
}

class TippCoverage {
    
    String _meta            = Status.UNDEFINED   
    String coverageDepth    = ""
    String coverageNote     = ""
    String embargo          = ""
    String endDate          = ""
    String endIssue         = ""
    String endVolume        = ""
    String startDate        = ""
    String startIssue       = ""
    String startVolume      = ""
}

class TippPlatform {
    
    String _meta      = Status.UNDEFINED
    String name       = ""
    String primaryUrl = ""
}

class TippTitle {
    
    String _meta    = Status.UNDEFINED  
    def identifiers = [] // list
    String name     = ""
    String type     = "Serial"
}

class TippTitleIdentifier {
    
    String _meta    = Status.UNDEFINED
    String type     = ""
    String value    = ""
}

