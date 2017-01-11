package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.enums.*

class Tipp {
    
    Pod accessEnd   = new Pod()
    Pod accessStart = new Pod()
    
    Pod medium      = new Pod("")
    Pod status      = new Pod("")  
    Pod url         = new Pod("")
    
    Pod title       = new Pod(PackageStruct.getNewTippTitle())
    Pod platform    = new Pod(PackageStruct.getNewTippPlatform())
    Pod coverage    = new Pod([]) // list
}

class TippCoverage {
     
    Pod coverageDepth = new Pod("Fulltext", Status.HARDCODED)
    Pod coverageNote  = new Pod("")
    Pod embargo       = new Pod("")
    Pod endDate       = new Pod("")
    Pod endIssue      = new Pod("")
    Pod endVolume     = new Pod("")
    Pod startDate     = new Pod("")
    Pod startIssue    = new Pod("")
    Pod startVolume   = new Pod("")
}

class TippPlatform {
    
    Pod name       = new Pod("")
    Pod primaryUrl = new Pod("")
}

class TippTitle {
    
    Pod name        = new Pod("")
    Pod type        = new Pod("Serial", Status.HARDCODED)
    Pod identifiers = new Pod([]) // list
}

class TippTitleIdentifier {

    Pod type        = new Pod("")
    Pod value       = new Pod("")
}

