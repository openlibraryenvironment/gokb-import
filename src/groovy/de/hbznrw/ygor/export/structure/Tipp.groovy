package de.hbznrw.ygor.export.structure

class Tipp {
    
    def _meta       = [:]
    
    String accessEndDate    = ""
    String accessStartDate  = ""
    
    String medium           = FixedValues.tipp_medium
    String status           = FixedValues.tipp_status
    String url              = ""
    
    TippTitle title         = PackageStruct.getNewTippTitle()
    TippPlatform platform   = PackageStruct.getNewTippPlatform()
    TippCoverage coverage   = PackageStruct.getNewTippCoverage()
}
