package de.hbznrw.ygor.iet.export.structure

import de.hbznrw.ygor.iet.enums.*

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
