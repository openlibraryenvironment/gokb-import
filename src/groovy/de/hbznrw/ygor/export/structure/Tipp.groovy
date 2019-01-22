package de.hbznrw.ygor.export.structure

class Tipp {
    
    def _meta       = [:]
    
    Pod accessEndDate   = new Pod("")
    Pod accessStartDate = new Pod("")
    
    Pod medium      = new Pod(FixedValues.tipp_medium, Status.HARDCODED)
    Pod status      = new Pod(FixedValues.tipp_status, Status.HARDCODED)  
    Pod url         = new Pod("")
    
    Pod title       = new Pod(PackageStruct.getNewTippTitle())
    Pod platform    = new Pod(PackageStruct.getNewTippPlatform())
    
    def coverage    = []
}
