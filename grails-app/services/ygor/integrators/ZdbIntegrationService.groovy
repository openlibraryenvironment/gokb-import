package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.ZdbReader
import org.apache.commons.lang.StringUtils
import ygor.Record
import ygor.field.Field
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.identifier.AbstractIdentifier
import ygor.identifier.ZdbIdentifier

import java.text.SimpleDateFormat

class ZdbIntegrationService extends ExternalIntegrationService {

  FieldKeyMapping zdbIdMapping
  String processStart

  ZdbIntegrationService(MappingsContainer mappingsContainer) {
    super(mappingsContainer)
  }


  def integrate(MultipleProcessingThread owner, DataContainer dataContainer) {
    zdbIdMapping = mappingsContainer.getMapping("zdbId", MappingsContainer.YGOR)
    processStart = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS").format(new Date())
    List<FieldKeyMapping> idMappings = [owner.zdbKeyMapping, owner.pissnKeyMapping, owner.eissnKeyMapping]
    List<Record> linkedRecords = []
    for (Record record in dataContainer.records.values()) {
      if (isApiCallMedium(record)) {
        integrateRecord(owner, record, idMappings)
      }
      linkedRecords.addAll(getLinkedRecords(record, owner))
      owner.increaseProgress()
    }
    for (Record linkedRecord in linkedRecords){
      dataContainer.addRecord(linkedRecord)
    }
  }


  private void integrateRecord(MultipleProcessingThread owner, Record record, List<FieldKeyMapping> idMappings){
    Map<String, String> zdbMatch = getBestMatch(owner, record)
    if (zdbMatch){
      record.zdbIntegrationDate = processStart
      // collect all identifiers (zdb_id, online_identifier, print_identifier) from the record
      for (idMapping in idMappings){
        for (zdbKey in idMapping.zdbKeys){
          if (zdbMatch[zdbKey]){
            Class clazz = owner.identifierByKey[idMapping]
            def identifier = clazz.newInstance(["identifier": zdbMatch[zdbKey]])
            record.addIdentifier(identifier)
          }
        }
      }
      integrateWithExisting(record, zdbMatch, mappingsContainer, MappingsContainer.ZDB)
    }
  }


  private List<Record> getLinkedRecords(Record record, MultipleProcessingThread owner){
    List<Record> result = []
    // check for historyEvents
    List<Field> historyEventFields = record.multiFields.get("historyEventIdentifier").getFields(MappingsContainer.ZDB)
    for (Field historyEventField in historyEventFields){
      ZdbIdentifier zdbIdentifier = new ZdbIdentifier(historyEventField.value, zdbIdMapping)
      Record existing = owner.enrichment.dataContainer.getRecord(zdbIdentifier)
      if (existing == null){
        Record newLinkedRecord = new Record([] << zdbIdentifier, owner.enrichment.mappingsContainer)
        integrateRecord(owner, newLinkedRecord, [zdbIdMapping])
        result.add(newLinkedRecord)
      }
    }
    result
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
        String queryString = ZdbReader.getAPIQuery(id.identifier, mapping.kbartKeys.getAt(0))
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
