package de.hbznrw.ygor.processing

import javax.annotation.Nonnull

class YgorFeedback{

  YgorProcessingStatus ygorProcessingStatus
  String statusDescription
  Class reportingComponent
  Class dataComponent
  Map<String, String> errors
  Map<String, Object> processedData
  List<Exception> exceptions

  YgorFeedback(@Nonnull YgorProcessingStatus ygorProcessingStatus, String statusDescription,
               @Nonnull Class reportingComponent,
               Class dataComponent, Map<String, String> errors, Map<String, Object> processedData, List<Exception> exceptions){
    this.ygorProcessingStatus = ygorProcessingStatus
    this.statusDescription = statusDescription == null ? "" : statusDescription
    this.reportingComponent = reportingComponent
    this.dataComponent = dataComponent
    this.errors = errors == null ? new HashMap<String, String>() : errors
    this.processedData = processedData == null ? new HashMap<String, String>() : processedData
    this.exceptions = exceptions == null ? new ArrayList<Exception>() : exceptions
  }

  enum YgorProcessingStatus{
    ERROR,
    OK,
    PREPARATION,
    RUNNING,
    WAITING
  }

}
