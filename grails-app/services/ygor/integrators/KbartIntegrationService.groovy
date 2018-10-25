package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.KbartReader
import de.hbznrw.ygor.readers.KbartReaderConfiguration
import ygor.Record
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.AbstractIdentifier
import ygor.source.KbartSource

class KbartIntegrationService {

    static def SOURCE = new KbartSource()

    def integrate(MultipleProcessingThread owner, DataContainer data,
                         MappingsContainer container, KbartReaderConfiguration kbartReaderConfiguration) {

        owner.setProgressTotal(1)
        KbartReader reader = owner.kbartReader.setConfiguration(kbartReaderConfiguration)
        List<FieldKeyMapping> idMappings = [owner.zdbKeyMapping, owner.pissnKeyMapping, owner.eissnKeyMapping]
        List<AbstractIdentifier> identifiers

        // JsonOutput items = reader.readItems()
        // items.each { item ->
        Map<String, String> item = reader.readItemData(null, null)
        while (item != null) {
            // collect all identifiers (zdb_id, online_identifier, print_identifier) from the record
            identifiers = []
            for (idMapping in idMappings) {
                if (item.(idMapping.kbartKey)) {
                    Class clazz = owner.identifierByKey[idMapping]
                    def identifier = clazz.newInstance(["identifier": item[idMapping.kbartKey]])
                    identifiers.add(identifier)
                }
            }
            Record record = new Record(identifiers)

            // fill record with all non-identifier fields
            item.each { key, value ->
                def fieldKeyMapping = container.getMapping(key, MappingsContainer.KBART)
                if (fieldKeyMapping == null){
                    fieldKeyMapping = new FieldKeyMapping(false,
                            [(MappingsContainer.YGOR) : value,
                             (MappingsContainer.KBART) : value,
                             (MappingsContainer.ZDB) : "",
                             (MappingsContainer.EZB) : ""])
                }
                if (null == owner.identifierByKey[fieldKeyMapping]) {
                    MultiField multiField = new MultiField(fieldKeyMapping)
                    multiField.addValue(SOURCE, value)
                    record.addMultiField(multiField)
                }
            }
            data.putRecord(record)
            item = reader.readItemData(null, null)
        }
    }
}
