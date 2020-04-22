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
  ZdbReader zdbReader

  ZdbIntegrationService(MappingsContainer mappingsContainer) {
    super(mappingsContainer)
    zdbReader = new ZdbReader()
  }


  def integrate(MultipleProcessingThread owner, DataContainer dataContainer) {
    if (status != IntegrationStatus.INTERRUPTING){
      super.integrate()
      zdbIdMapping = mappingsContainer.getMapping("zdbId", MappingsContainer.YGOR)
      processStart = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS").format(new Date())
      List<FieldKeyMapping> idMappings = [owner.zdbKeyMapping, owner.issnKeyMapping, owner.eissnKeyMapping]
      for (String recId in dataContainer.records){
        Record record = Record.load(dataContainer.enrichmentFolder, dataContainer.resultHash, recId, dataContainer.mappingsContainer)
        if (status == IntegrationStatus.INTERRUPTING){
          status = IntegrationStatus.STOPPED
          return
        }
        if (isApiCallMedium(record)){
          integrateRecord(owner, record, idMappings)
        }
        for (Record linkedRecord in getLinkedRecords(record, owner)){
          dataContainer.addRecord(linkedRecord)
          linkedRecord.save(dataContainer.enrichmentFolder, dataContainer.resultHash)
        }
        record.save(dataContainer.enrichmentFolder, dataContainer.resultHash)
        owner.increaseProgress()
      }
    }
    status = IntegrationStatus.IDLE
  }


  private void integrateRecord(MultipleProcessingThread owner, Record record, List<FieldKeyMapping> idMappings){
    if (!(record.multiFields.get("publicationType").getFirstPrioValue().toLowerCase().equals("serial"))){
      // Don't integrate monographs (or any other type)
      return
    }
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
          newLinkedRecord.publicationType = record.publicationType
          result.add(newLinkedRecord)
        }
      }
      else{
        existing.publicationType = record.publicationType
        result.add(existing)
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
        readData = zdbReader.readItemData(queryString)
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
