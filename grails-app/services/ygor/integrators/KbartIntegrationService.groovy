package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.KbartReader
import de.hbznrw.ygor.readers.KbartReaderConfiguration
import ygor.Record
import ygor.field.Field
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.AbstractIdentifier

class KbartIntegrationService {

  private MappingsContainer mappingsContainer


  KbartIntegrationService(MappingsContainer mappingsContainer) {
    this.mappingsContainer = mappingsContainer
  }


  def integrate(MultipleProcessingThread owner, DataContainer data,
                KbartReaderConfiguration kbartReaderConfiguration) {
    KbartReader reader = owner.kbartReader.setConfiguration(kbartReaderConfiguration)
    List<FieldKeyMapping> idMappings = [owner.zdbKeyMapping, owner.pissnKeyMapping, owner.eissnKeyMapping]
    List<AbstractIdentifier> identifiers
    TreeMap<String, String> item = reader.readItemData(null, null)
    while (item != null) {
      // collect all identifiers (zdb_id, online_identifier, print_identifier) from the record
      identifiers = []
      for (idMapping in idMappings) {
        for (key in idMapping.kbartKeys) {
          if (item[key]) {
            Class clazz = owner.identifierByKey[idMapping]
            def identifier = clazz.newInstance(["identifier": item[key]])
            identifiers.add(identifier)
          }
        }
      }
      Record record = new Record(identifiers, mappingsContainer)

      // fill record with all non-identifier fields
      item.each { key, value ->
        def fieldKeyMapping = mappingsContainer.getMapping(key, MappingsContainer.KBART)
        if (fieldKeyMapping == null) {
          fieldKeyMapping = new FieldKeyMapping(false,
            [(MappingsContainer.YGOR) : key,
             (MappingsContainer.KBART): key,
             (MappingsContainer.ZDB)  : "",
             (MappingsContainer.EZB)  : ""])
        }
        MultiField multiField = new MultiField(fieldKeyMapping)
        multiField.addField(new Field(MappingsContainer.KBART, key, value))
        record.addMultiField(multiField)
      }
      data.addRecord(record)
      owner.increaseProgress()
      item = reader.readItemData(null, null)
    }
    return
  }
}
