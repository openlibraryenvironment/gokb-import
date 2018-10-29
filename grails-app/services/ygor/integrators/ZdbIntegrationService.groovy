package ygor.integrators

import de.hbznrw.ygor.connectors.KbartConnector
import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.KbartReaderConfiguration
import de.hbznrw.ygor.readers.ZdbReader
import grails.transaction.Transactional
import org.slf4j.Logger
import ygor.Record
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.AbstractIdentifier
import ygor.identifier.ZdbIdentifier

@Transactional
class ZdbIntegrationService {

    def integrate(MultipleProcessingThread owner, DataContainer dataContainer,
                         MappingsContainer mappingsContainer) {
        // owner.setProgressTotal(1) TODO: value?
        ZdbReader reader = owner.zdbReader

        for (Record item in dataContainer.recordsPerZdbId) {
            for (String key in owner.KEY_ORDER) {
                if (key == KbartConnector.KBART_HEADER_ZDB_ID){

                }
            }
        }

        for (ZdbIdentifier zdbId : dataContainer.recordsPerZdbId.keySet()){

        }
    }

    def integrateKbart(MultipleProcessingThread owner, DataContainer data,
                  MappingsContainer container, KbartReaderConfiguration kbartReaderConfiguration) {


        List<FieldKeyMapping> idMappings = [owner.zdbKeyMapping, owner.pissnKeyMapping, owner.eissnKeyMapping]
        List<AbstractIdentifier> identifiers

        // JsonOutput items = reader.readItems()
        // items.each { item ->
        Map<String, String> item = reader.readItemData(null, null)
        while (item != null) {
            // collect all identifiers (zdb_id, online_identifier, print_identifier) from the record

            // fill record with all non-identifier fields
            item.each { key, value ->
                def fieldKeyMapping = container.getMapping(key, MappingsContainer.KBART)
                if (fieldKeyMapping == null) {
                    fieldKeyMapping = new FieldKeyMapping(false,
                            [(MappingsContainer.YGOR) : value,
                             (MappingsContainer.KBART): value,
                             (MappingsContainer.ZDB)  : "",
                             (MappingsContainer.EZB)  : ""])
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
