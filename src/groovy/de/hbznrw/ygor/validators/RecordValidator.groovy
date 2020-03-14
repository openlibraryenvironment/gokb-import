package de.hbznrw.ygor.validators

import de.hbznrw.ygor.enums.Status
import de.hbznrw.ygor.normalizers.DateNormalizer
import ygor.Record
import ygor.RecordFlag
import ygor.field.MultiField

class RecordValidator {

  static validateCoverage(Record record) {

    MultiField startDate = record.getMultiField("dateFirstIssueOnline")
    MultiField endDate = record.getMultiField("dateLastIssueOnline")
    MultiField startVolume = record.getMultiField("numFirstVolOnline")
    MultiField endVolume = record.getMultiField("numLastIssueOnline")

    // remove due to inconsistency in data length
    if (!(startDate.getPrioValues().size() == endDate.getPrioValues().size()
          == startVolume.getPrioValues().size() == endVolume.getPrioValues().size())){
      record.addValidation("coverage", Status.REMOVE_FLAG)
      // this has no effect currently
    }


    // remove due data error
    Date startDateTime = DateNormalizer.formatDateTime(startDate.getFirstPrioValue())
    Date endDateTime = DateNormalizer.formatDateTime(endDate.getFirstPrioValue())
    if (startDateTime != null && endDateTime != null && startDateTime > endDateTime) {
      RecordFlag flag = record.getFlagWithErrorCode(RecordFlag.ErrorCode.ISSUE_ONLINE_DATES_ORDER)
      if (flag == null){
        flag = new RecordFlag(Status.INVALID, "${endDate.keyMapping.ygorKey} ${endDate.getFirstPrioValue()} %s",
            'record.date.order', endDate.keyMapping, RecordFlag.ErrorCode.ISSUE_ONLINE_DATES_ORDER)
      }
      flag.setColour(RecordFlag.Colour.RED)
      record.putFlag(flag)
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
