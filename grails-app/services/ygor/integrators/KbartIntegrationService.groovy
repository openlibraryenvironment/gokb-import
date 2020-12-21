package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.normalizers.DateNormalizer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.KbartReader
import de.hbznrw.ygor.readers.KbartReaderConfiguration
import de.hbznrw.ygor.tools.DateToolkit
import ygor.Record
import ygor.field.Field
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.AbstractIdentifier

import java.time.LocalDate

class KbartIntegrationService {

  private MappingsContainer mappingsContainer


  KbartIntegrationService(MappingsContainer mappingsContainer) {
    this.mappingsContainer = mappingsContainer
  }


  def integrate(MultipleProcessingThread owner, DataContainer dataContainer,
                KbartReaderConfiguration kbartReaderConfiguration) {
    KbartReader reader = owner.kbartReader.setConfiguration(kbartReaderConfiguration)
    List<FieldKeyMapping> idMappings = [owner.zdbKeyMapping, owner.issnKeyMapping, owner.eissnKeyMapping]
    List<AbstractIdentifier> identifiers
    LocalDate lastUpdate = null
    if (owner.enrichment.isUpdate){
      lastUpdate = LocalDate.parse(DateNormalizer.getDateString(owner.enrichment.lastProcessingDate))
    }
    // addOnly is to be set if there is at least one KBart line containing a valid date stamp
    boolean addOnly = false
    TreeMap<String, String> item = reader.readItemData(lastUpdate)
    while (item != null) {
      // collect all identifiers (zdb_id, online_identifier, print_identifier) from the record
      log.debug("Integrating KBart record ${item.toString()}")
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
      record.publicationType = record.multiFields.get("publicationType").getFirstPrioValue().toLowerCase()
      dataContainer.addRecord(record)
      log.debug("... added record ${record.displayTitle} to data container.")
      record.save(dataContainer.enrichmentFolder, dataContainer.resultHash)
      owner.increaseProgress()
      if (!addOnly){
        if (null != DateToolkit.getAsLocalDate(item.get("last_changed"))){
          addOnly = owner.enrichment.addOnly = true
        }
      }
      item = reader.readItemData(lastUpdate)
    }
    return
  }
}
