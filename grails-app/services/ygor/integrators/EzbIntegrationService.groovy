package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.processing.YgorFeedback
import de.hbznrw.ygor.readers.EzbReader
import org.apache.commons.lang.StringUtils
import ygor.Record
import ygor.field.MappingsContainer
import ygor.identifier.AbstractIdentifier

import java.text.SimpleDateFormat

class EzbIntegrationService extends ExternalIntegrationService {

  EzbReader ezbReader
  YgorFeedback ygorFeedback

  EzbIntegrationService(MappingsContainer mappingsContainer, YgorFeedback ygorFeedback) {
    super(mappingsContainer)
    ezbReader = new EzbReader()
    this.ygorFeedback = ygorFeedback
  }


  def integrate(MultipleProcessingThread owner, DataContainer dataContainer) {
    if (status != ExternalIntegrationService.IntegrationStatus.INTERRUPTING){
      super.integrate()
      String processStart = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS").format(new Date())
      for (String recId in dataContainer.records){
        Record record = Record.load(dataContainer.enrichmentFolder, dataContainer.resultHash, recId, mappingsContainer)
        if (record.publicationType.toLowerCase() != "serial"){
          continue
        }
        if (status == ExternalIntegrationService.IntegrationStatus.INTERRUPTING){
          status = ExternalIntegrationService.IntegrationStatus.STOPPED
          return
        }
        if (isApiCallMedium(record)){
          Map<String, String> ezbMatch = getBestMatch(owner, record)
          if (!ezbMatch.isEmpty()){
            record.ezbIntegrationDate = processStart
            integrateWithExisting(record, ezbMatch, mappingsContainer, MappingsContainer.EZB)
          }
        }
        record.save(dataContainer.enrichmentFolder, dataContainer.resultHash)
        owner.increaseProgress()
      }
    }
    status = ExternalIntegrationService.IntegrationStatus.IDLE
  }


  private Map<String, List<String>> getBestMatch(MultipleProcessingThread owner, Record record) {
    List<Map<String, List<String>>> readData = new ArrayList<>()
    for (String key in owner.KEY_ORDER) {
      AbstractIdentifier id = record."${key}"
      if (id && !StringUtils.isEmpty(id.identifier)) {
        String queryString = EzbReader.getAPIQuery(id.identifier, owner.zdbKeyMapping.kbartKeys)
        readData = ezbReader.readItemData(queryString)
        if (!readData.isEmpty()) {
          record.ezbIntegrationUrl = queryString
          break
        }
      }
    }
    return filterBestMatch(owner, record, readData, 0, MappingsContainer.EZB, "ezbKeys")
  }
}
