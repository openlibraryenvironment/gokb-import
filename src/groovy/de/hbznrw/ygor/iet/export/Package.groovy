package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.enums.*

class Package {
    
    PackageHeader packageHeader
    def tipps
    
    Package() {
        packageHeader = new PackageHeader()
        tipps         = []
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
    
    def additionalProperties = []
    def curatoryGroups       = []
    def source               = []
    def variantNames         = []

    PackageHeader() {
        source = PackageStruct.getNewPackageSource()
    }
}


class PackageStruct {
    
    static getNewPackageIdentifier() {
        return new PackageIdentifier()
    }
    
    static getNewPackageTipp() {
        return new PackageTipp()
    }
    
    static getNewPackageVariantName() {
        return new PackageVariantName()
    }
    
    static getNewPackageCuratoryGroups() {
        return new PackageCuratoryGroups()
    }
    
    static getNewPackageSource() {
        return new PackageSource()
    }
}

class PackageIdentifier {
    String _meta = Status.UNDEFINED
    
    String type  = ""
    String value = ""
}

class PackageTipp {
    String _meta       = Status.UNDEFINED
    
    String accessEnd
    String accessStart 
    
    String medium      = ""
    String status      = ""   
    String url         = ""
    
    def coverage       = []
    def platform       = []
    def title          = []
}

class PackageVariantName {
    String _meta       = Status.UNDEFINED
    
    String variantName = ""
}

class PackageCuratoryGroups {
    String _meta         = Status.UNDEFINED
    
    String curatoryGroup = ""
}

class PackageSource {
    String _meta    = Status.UNDEFINED
    
    String name     = ""
    String normname = ""
    String url      = ""
}


class PackageTippStruct {
    
    static getNewPackageTippCoverage() {
        return new PackageTippCoverage()
    }
    
    static getNewPackageTippPlatform() {
        return new PackageTippPlatform()
    }
    
    static getNewPackageTippTitle() {
        return new PackageTippTitle()
    }
}

class PackageTippCoverage {
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

class PackageTippPlatform {
    String _meta      = Status.UNDEFINED

    String name       = ""
    String primaryUrl = ""
}

class PackageTippTitle {
    String _meta    = Status.UNDEFINED
    
    def identifiers = []
    String name     = ""
    String type     = ""
}

   
class PackageTippTitleStruct {
    
    static getNewPackageTippTitleIdentifier() {
        return new PackageTippTitleIdentifier()
    }
}

class PackageTippTitleIdentifier {
    String _meta = Status.UNDEFINED
    
    String type  = ""
    String value = ""
}

