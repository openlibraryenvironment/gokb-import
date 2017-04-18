package de.hbznrw.ygor.iet.export.structure

import de.hbznrw.ygor.iet.enums.*

class TitlePublisherHistory {
    
    Pod endDate   = new Pod()
    Pod name      = new Pod("")
    Pod startDate = new Pod()
    Pod status    = new Pod(FixedValues.title_publisher_history_status, Status.HARDCODED)
}
