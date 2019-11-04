package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.EzbReader
import org.apache.commons.lang.StringUtils
import ygor.Record
import ygor.field.MappingsContainer
import ygor.identifier.AbstractIdentifier

import java.text.SimpleDateFormat

class EzbIntegrationService extends ExternalIntegrationService {

  EzbIntegrationService(MappingsContainer mappingsContainer) {
    super(mappingsContainer)
  }


  def integrate(MultipleProcessingThread owner, DataContainer dataContainer) {
    String processStart = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS").format(new Date())
    for (Record record in dataContainer.records.values()) {
      if (isApiCallMedium(record)) {
        Map<String, String> ezbMatch = getBestMatch(owner, record)
        if (!ezbMatch.isEmpty()) {
          record.ezbIntegrationDate = processStart
          integrateWithExisting(record, ezbMatch, mappingsContainer, MappingsContainer.EZB)
        }
      }
      owner.increaseProgress()
    }
  }


  private Map<String, String> getBestMatch(MultipleProcessingThread owner, Record record) {
    List<Map<String, String>> readData = new ArrayList<>()
    for (String key in owner.KEY_ORDER) {
      AbstractIdentifier id = record."${key}"
      if (id && !StringUtils.isEmpty(id.identifier)) {
        String queryString = EzbReader.getAPIQuery(id.identifier, owner.zdbKeyMapping.kbartKeys)
        readData = owner.ezbReader.readItemData(queryString)
        if (!readData.isEmpty()) {
          record.ezbIntegrationUrl = queryString
          break
        }
      }
    }
    return filterBestMatch(owner, record, readData, 0, MappingsContainer.EZB, "ezbKeys")
  }
}
