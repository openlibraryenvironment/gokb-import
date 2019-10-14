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

    // remove due to parsing or data error
    if (startDate.getPrioValue() == endDate.getPrioValue() &&
        startVolume.getPrioValue() == endVolume.getPrioValue() &&
        startDate.getPrioValue() == startVolume.getPrioValue()) {
      record.addValidation("coverage", Status.STRUCTVALIDATOR_REMOVE_FLAG)
    } else {
      record.addValidation("coverage", Status.STRUCTVALIDATOR_COVERAGE_IS_UNDEF)
    }

  }

  static validateHistoryEvent(Record record) {
    // date
    // from
    // to

    MultiField historyEvents = record.getMultiField("historyEvents")
    /* TODO
    if (historyEvents.size == 0){
        if (historyEvents.status == Status.UNDEFINED || historyEvents.status == Status.VALIDATOR_DATE_IS_MISSING){
            record.addValidation("historyEvents", Status.STRUCTVALIDATOR_REMOVE_FLAG)
        }
    }
    else{
        record.addValidation("historyEvents", Status.STRUCTVALIDATOR_HISTORYEVENT_IS_UNDEF)
    }
    */
  }

  static validatePublisherHistory(Record record) {
    // startDate
    // endDate
    // status
    // name

    MultiField publisherHistory = record.getMultiField("publisherHistory")
    record.addValidation("publisherHistory", Status.STRUCTVALIDATOR_PUBLISHERHISTORY_IS_UNDEF)
  }

}
