package ygor.integrators

import de.hbznrw.ygor.connectors.KbartConnector
import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.ZdbReader
import ygor.Record
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.EissnIdentifier
import ygor.identifier.PissnIdentifier
import ygor.identifier.ZdbIdentifier
import ygor.source.AbstractSource

class ZdbIntegrationService {

    def integrate(MultipleProcessingThread owner, DataContainer dataContainer,
                         MappingsContainer mappingsContainer) {

        Set<String> observedZdbIds = new HashSet<>()
        Set<String> observedEissns = new HashSet<>()
        Set<String> observedPissns = new HashSet<>()

        // owner.setProgressTotal(1) TODO: value?
        ZdbReader reader = owner.zdbReader

        Map<String, String> readData = null
        for (String key in owner.KEY_ORDER) {
            if (key == KbartConnector.KBART_HEADER_ZDB_ID){
                for (Map.Entry<ZdbIdentifier, Record> item in dataContainer.recordsPerZdbId) {
                    if (item.key && !observedZdbIds.contains(item.value.zdbId)){
                        readData = reader.readItemData(owner.zdbKeyMapping, item.key.identifier)
                        integrateWithExisting(item.value, readData)
                        addToObserved(item.value, observedZdbIds, observedEissns, observedPissns)
                    }
                }
            }
            else if (key == KbartConnector.KBART_HEADER_ONLINE_IDENTIFIER){
                for (Map.Entry<EissnIdentifier, Record> item in dataContainer.recordsPerEissn) {
                    if (item.key && !observedEissns.contains(item.value.eissn)){
                        readData = reader.readItemData(owner.eissnKeyMapping, item.key.identifier)
                        integrateWithExisting(item.value, readData)
                        addToObserved(item.value, observedZdbIds, observedEissns, observedPissns)
                    }
                }
            }
            else if (key == KbartConnector.KBART_HEADER_PRINT_IDENTIFIER){
                for (Map.Entry<PissnIdentifier, Record> item in dataContainer.recordsPerPissn) {
                    if (item.key && !observedPissns.contains(item.value.pissn)){
                        readData = reader.readItemData(owner.pissnKeyMapping, item.key.identifier)
                        integrateWithExisting(item.value, readData)
                        addToObserved(item.value, observedZdbIds, observedEissns, observedPissns)
                    }
                }
            }
        }
    }

    private void integrateWithExisting(Record item, Map<String, String> readData,
                                       FieldKeyMapping mapping, AbstractSource source){
        if (!item || !readData || !mapping || !source){
            // TODO: throw exception?
            return
        }
        // TODO: get from readData
        String value = ""

        MultiField multiField = item.get(mapping.ygorKey)
        multiField.addValue(source, value)
    }


    private void addToObserved(Record item, def observedZdbIds, def observedEissns, def observedPissns){
        if (item.zdbId){
            observedZdbIds.add(item.zdbId)
        }
        if (item.eissn){
            observedEissns.add(item.eissn)
        }
        if (item.pissn){
            observedPissns.add(item.pissn)
        }
    }
}
