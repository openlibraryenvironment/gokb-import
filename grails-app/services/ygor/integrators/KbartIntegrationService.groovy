package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.KbartReader
import de.hbznrw.ygor.readers.KbartReaderConfiguration
import grails.converters.JSON
import ygor.Record
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.AbstractIdentifier
import ygor.source.KbartSource

class KbartIntegrationService {

    static def integrate(MultipleProcessingThread owner, DataContainer data,
                         MappingsContainer container, KbartReaderConfiguration kbartReaderConfiguration){

        owner.setProgressTotal(1)
        KbartReader reader = owner.kbartReader.setConfiguration(kbartReaderConfiguration)
        List<FieldKeyMapping> idMappings = [owner.zdbKeyMapping, owner.pissnKeyMapping, owner.eissnKeyMapping]
        List<AbstractIdentifier> identifiers

        JSON item = reader.readItemData(null, null)
        while (item != null){
            // collect all identifiers (zdb_id, online_identifier, print_identifier) from the record
            identifiers = []
            for (idMapping in idMappings) {
                if (item.$(idMapping.kbartKey)){
                    Class clazz = AbstractIdentifier.byFieldKeyMapping(idMapping)
                    identifiers.add(clazz.newInstance(["identifier" : item.$(idMapping.kbartKey)]))
                }
            }
            Record record = new Record(identifiers)

            // fill record with all non-identifier fields
            def fields = item.getData()
            fields.items.each{ key, value ->
                def fieldKeyMapping = FieldKeyMapping.findByKbartKey(key)
                if (null == AbstractIdentifier.byFieldKeyMapping(fieldKeyMapping)){
                    MultiField multiField = new MultiField(fieldKeyMapping)
                    multiField.addValue(KbartSource, value)
                    record.addToMultiFields(multiField)
                }
            }
            data.putRecord(record)
            item = reader.readItemData(null, null)
        }
    }

}
