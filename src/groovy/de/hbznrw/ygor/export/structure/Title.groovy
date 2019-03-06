package de.hbznrw.ygor.export.structure

import de.hbznrw.ygor.enums.*

class Title {
    
    def _meta            = [:]
        
    Pod imprint          = new Pod("")
    Pod issuer           = new Pod("")
    Pod medium           = new Pod(FixedValues.title_medium, Status.HARDCODED)
    Pod type             = new Pod(FixedValues.title_type, Status.HARDCODED)
    Pod name             = new Pod("")
    Pod publishedFrom    = new Pod("")
    Pod publishedTo      = new Pod("")
    
    ArrayList<Identifier> identifiers                  = []
    ArrayList<TitlePublisherHistory> publisher_history = []
    ArrayList<TitleHistoryEvent> historyEvents         = []
    
    Pod shortcode        = new Pod("")
    Pod status           = new Pod(FixedValues.title_status, Status.HARDCODED)
    
    Pod OAStatus         = new Pod("")
    Pod continuingSeries = new Pod("")
    Pod defaultAccessURL = new Pod("")
    Pod editStatus       = new Pod(FixedValues.title_editStatus, Status.HARDCODED)

    Pod monographEdition = new Pod("")
    Pod volumeNumber     = new Pod("")
    Pod firstEditor      = new Pod("")
    Pod firstAutor       = new Pod("")
    Pod dateMonographPublishedPrint      = new Pod("")
    Pod dateMonographPublishedOnline      = new Pod("")

}
