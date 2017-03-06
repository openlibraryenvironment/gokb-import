package de.hbznrw.ygor.iet.export.structure

import de.hbznrw.ygor.iet.enums.*

class TitleHistoryEvent {
    
    Pod date = new Pod()
    
    ArrayList<TitleHistoryEventGeneric> from = []
    ArrayList<TitleHistoryEventGeneric> to   = []
}

class TitleHistoryEventGeneric {
    
    Pod title = new Pod()
    
    ArrayList<Identifier> identifiers = []
}