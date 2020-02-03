package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.ZdbReader
import org.apache.commons.lang.StringUtils
import ygor.Record
import ygor.field.Field
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField
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
    List<FieldKeyMapping> idMappings = [owner.zdbKeyMapping, owner.issnKeyMapping, owner.eissnKeyMapping]
    List<Record> existingRecords = []
    existingRecords.addAll(dataContainer.records.values())
    for (Record record in existingRecords) {
      if (isApiCallMedium(record)) {
        integrateRecord(owner, record, idMappings)
      }
      for (Record linkedRecord in getLinkedRecords(record, owner)){
        dataContainer.addRecord(linkedRecord)
      }
      owner.increaseProgress()
    }
  }


  private void integrateRecord(MultipleProcessingThread owner, Record record, List<FieldKeyMapping> idMappings){
    Map<String, String> zdbMatch = getBestMatch(owner, record)
    if (zdbMatch != null && !zdbMatch.isEmpty()){
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
        if (newLinkedRecord.zdbIntegrationDate != null){
          // If there is no zdbIntegrationDate, the integration didn't happen, most probably because there was
          // no match. In this case, we would not add an empty record stub to the result list.
          copyValueToLinkedRecord("publicationType", record, newLinkedRecord)
          result.add(newLinkedRecord)
        }
      }
      else{
        copyValueToLinkedRecord("publicationType", record, existing)
        result.add(existing)
      }
    }
    result
  }


  void copyValueToLinkedRecord(String multiFieldName, Record from, Record to){
    MultiField fromField = from.multiFields.get(multiFieldName)
    MultiField toField = to.multiFields.get(multiFieldName)
    toField.revised = fromField.getFirstPrioValue()
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
        else{
          return new HashMap<String, String>()
        }
      }
    }
    return filterBestMatch(owner, record, readData, 0, MappingsContainer.ZDB, "zdbKeys")
  }
}
