package de.hbznrw.ygor.iet.export.structure

import de.hbznrw.ygor.iet.enums.*

class TippTitle {
    
    Pod name        = new Pod("")
    Pod type        = new Pod(FixedValues.tipp_title_type, Status.HARDCODED)
    
    ArrayList<Identifier> identifiers = []
}