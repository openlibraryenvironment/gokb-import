package de.hbznrw.ygor.export.structure

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
    
    static Identifier getNewTippTitleIdentifier() {
        return new Identifier()
    }
}

