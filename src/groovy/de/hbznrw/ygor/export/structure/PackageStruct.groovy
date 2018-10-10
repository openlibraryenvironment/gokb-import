package de.hbznrw.ygor.export.structure

class PackageStruct {
    
    // Package
    
    static Tipp getNewTipp() {
        return new Tipp()
    }

    // PackageHeader
    
    static PackageHeaderSource getNewPackageHeaderSource() {
        return new PackageHeaderSource()
    }

    static PackageHeaderNominalPlatform getNewPackageHeaderNominalPlatform() {
        return new PackageHeaderNominalPlatform()
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
    
    static Identifier getNewTippTitleIdentifier() {
        return new Identifier()
    }
}

