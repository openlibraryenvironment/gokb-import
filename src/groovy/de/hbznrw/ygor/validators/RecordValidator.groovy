package de.hbznrw.ygor.validators

import de.hbznrw.ygor.enums.Status
import ygor.Record
import ygor.field.MultiField

class RecordValidator {

  static validateCoverage(Record record) {
    // startDate
    // endDate
    // startVolume
    // endVolume

    MultiField startDate = record.getMultiField("dateFirstIssueOnline")
    MultiField endDate = record.getMultiField("dateLastIssueOnline")
    MultiField startVolume = record.getMultiField("numFirstVolOnline")
    MultiField endVolume = record.getMultiField("numLastIssueOnline")

    // remove due to inconsistency in data length
    if (!(startDate.getPrioValues().size() == endDate.getPrioValues().size()
          == startVolume.getPrioValues().size() == endVolume.getPrioValues().size())){
      record.addValidation("coverage", Status.REMOVE_FLAG)
    }

    // remove due to parsing or data error
    if (startDate.getFirstPrioValue() == endDate.getFirstPrioValue() &&
        startVolume.getFirstPrioValue() == endVolume.getFirstPrioValue() &&
        startDate.getFirstPrioValue() == startVolume.getFirstPrioValue()) {
      record.addValidation("coverage", Status.REMOVE_FLAG)
    }
    else {
      record.addValidation("coverage", Status.UNDEFINED)
    }

  }

  static validateHistoryEvent(Record record) {
    // date
    // from
    // to

    MultiField historyEvents = record.getMultiField("historyEvents")
    /* TODO
    if (historyEvents.size == 0){
        if (historyEvents.status == Status.UNDEFINED || historyEvents.status == Status.DATE_IS_MISSING){
            record.addValidation("historyEvents", Status.REMOVE_FLAG)
        }
    }
    else{
        record.addValidation("historyEvents", Status.HISTORYEVENT_IS_UNDEF)
    }
    */
  }

  static validatePublisherHistory(Record record) {
    // startDate
    // endDate
    // status
    // name

    MultiField startDate = record.getMultiField("dateFirstIssueOnline")
    MultiField endDate = record.getMultiField("dateLastIssueOnline")
    MultiField startVolume = record.getMultiField("numFirstVolOnline")
    MultiField endVolume = record.getMultiField("numLastIssueOnline")

    MultiField publisherHistory = record.getMultiField("publisherHistory")
    record.addValidation("publisherHistory", Status.UNDEFINED)
  }

}
