package de.hbznrw.ygor.iet.export.structure

import de.hbznrw.ygor.iet.enums.*

class Title {
    
    def _meta            = [:]
        
    Pod imprint          = new Pod("")
    Pod issuer           = new Pod("")
    Pod medium           = new Pod("Journal", Status.HARDCODED)
    Pod type             = new Pod("Serial", Status.HARDCODED)
    Pod name             = new Pod("")
    Pod publishedFrom    = new Pod("")
    Pod publishedTo      = new Pod("")
    
    ArrayList<Identifier> identifiers                  = []
    ArrayList<TitlePublisherHistory> publisher_history = []
    ArrayList<TitleHistoryEvent> history_events        = []
    
    Pod shortcode        = new Pod("")
    Pod status           = new Pod("Current", Status.HARDCODED)
    
    Pod OAStatus         = new Pod("")
    Pod continuingSeries = new Pod("")
    Pod defaultAccessURL = new Pod("")
    Pod editStatus       = new Pod("In Progress", Status.HARDCODED)
}
