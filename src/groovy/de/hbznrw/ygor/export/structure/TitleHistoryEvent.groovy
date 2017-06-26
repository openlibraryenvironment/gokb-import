package de.hbznrw.ygor.export.structure

class TitleHistoryEvent {
    
    Pod date = new Pod()
    
    ArrayList<TitleHistoryEventGeneric> from = []
    ArrayList<TitleHistoryEventGeneric> to   = []
}

class TitleHistoryEventGeneric {
    
    Pod title = new Pod()
    
    ArrayList<Identifier> identifiers = []
}