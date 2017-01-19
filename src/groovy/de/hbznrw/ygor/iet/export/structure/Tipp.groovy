package de.hbznrw.ygor.iet.export.structure

import de.hbznrw.ygor.iet.enums.*

class Tipp {
    
    Pod accessEnd   = new Pod()
    Pod accessStart = new Pod()
    
    Pod medium      = new Pod("")
    Pod status      = new Pod("")  
    Pod url         = new Pod("")
    
    Pod title       = new Pod(PackageStruct.getNewTippTitle())
    Pod platform    = new Pod(PackageStruct.getNewTippPlatform())
    
    def coverage    = []
}
