package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.ZdbReader
import org.apache.commons.lang.StringUtils
import ygor.Record
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.identifier.AbstractIdentifier

import java.text.SimpleDateFormat

class ZdbIntegrationService extends ExternalIntegrationService {

  ZdbIntegrationService(MappingsContainer mappingsContainer) {
    super(mappingsContainer)
  }


  def integrate(MultipleProcessingThread owner, DataContainer dataContainer) {
    String processStart = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS").format(new Date())
    List<FieldKeyMapping> idMappings = [owner.zdbKeyMapping, owner.pissnKeyMapping, owner.eissnKeyMapping]
    for (Record record in dataContainer.records.values()) {
      if (isApiCallMedium(record)) {
        Map<String, String> zdbMatch = getBestMatch(owner, record)
        if (zdbMatch) {
          record.zdbIntegrationDate = processStart
          // collect all identifiers (zdb_id, online_identifier, print_identifier) from the record
          for (idMapping in idMappings) {
            for (zdbKey in idMapping.zdbKeys) {
              if (zdbMatch[zdbKey]) {
                Class clazz = owner.identifierByKey[idMapping]
                def identifier = clazz.newInstance(["identifier": zdbMatch[zdbKey]])
                record.addIdentifier(identifier)
              }
            }
          }
          integrateWithExisting(record, zdbMatch, mappingsContainer, MappingsContainer.ZDB)
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
        FieldKeyMapping mapping = mappingsContainer.getMapping(key, MappingsContainer.YGOR)
        if (mapping == null) {
          continue
        }
        String queryString = ZdbReader.getAPIQuery(id.identifier, mapping.kbartKeys)
        readData = owner.zdbReader.readItemData(queryString)
        if (!readData.isEmpty()) {
          record.zdbIntegrationUrl = queryString
          break
        }
      }
    }
    return filterBestMatch(owner, record, readData, 0, MappingsContainer.ZDB, "zdbKeys")
  }
}
