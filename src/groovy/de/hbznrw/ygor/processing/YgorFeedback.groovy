package de.hbznrw.ygor.processing

import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.exception.ExceptionUtils

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


  @Override
  String toString(){
    String result = "Ygor processing status: " + ygorProcessingStatus.toString() + " |"
    if (!StringUtils.isEmpty(statusDescription)){
      result += (" Processing description: " + statusDescription + " |")
    }
    if (reportingComponent){
      result += (" Reporting component: " + reportingComponent.getName() + " |")
    }
    if (dataComponent){
      result += (" Data component: " + dataComponent.getName() + " |")
    }
    if (!errors.isEmpty()){
      result += "Errors:"
      for (def error in errors){
        result += (" " + error.key + " = " + error.value + ".")
      }
      result += " |"
    }
    if (!processedData.isEmpty()){
      result += "Processed data:"
      for (def date in processedData){
        result += (" " + date.key + " = " + date.value + ".")
      }
      result += " |"
    }
    if (!exceptions.isEmpty()){
      result += "Exceptions:"
      for (def exception in exceptions){
        result += (" " + exception.toString() + " : " + exception.message + " ... with Stacktrace : " + ExceptionUtils.getStackTrace(exception) + ".")
      }
    }
    return result
  }

}
