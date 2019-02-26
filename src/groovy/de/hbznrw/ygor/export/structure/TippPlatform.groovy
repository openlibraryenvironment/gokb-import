package de.hbznrw.ygor.export.structure

class TippPlatform {
    
    String name       = ""
    String primaryUrl = ""

    static TippPlatform fromPackageHeaderNominalPlatform(PackageHeaderNominalPlatform phnp){
        TippPlatform result = new TippPlatform()
        result.name = phnp.name
        result.primaryUrl = phnp.url
        result
    }
}
