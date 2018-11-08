package ygor.integrators

import de.hbznrw.ygor.connectors.KbartConnector
import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.EzbReader
import de.hbznrw.ygor.readers.ZdbReader
import grails.transaction.Transactional
import ygor.Record
import ygor.field.MappingsContainer
import ygor.identifier.EissnIdentifier
import ygor.identifier.PissnIdentifier
import ygor.identifier.ZdbIdentifier
import ygor.source.SourceContainer

@Transactional
class EzbIntegrationService extends ExternalIntegrationService{

    def integrate(MultipleProcessingThread owner, DataContainer dataContainer,
                  MappingsContainer mappingsContainer) {
        Set<String> observedZdbIds = new HashSet<>()
        Set<String> observedEissns = new HashSet<>()
        Set<String> observedPissns = new HashSet<>()

        // owner.setProgressTotal(1) TODO: value?
        EzbReader reader = owner.ezbReader

        Map<String, String> readData = null
        for (String key in owner.KEY_ORDER) {
            if (key == KbartConnector.KBART_HEADER_ZDB_ID){
                for (Map.Entry<ZdbIdentifier, Record> item in dataContainer.recordsPerZdbId) {
                    if (item.key && !observedZdbIds.contains(item.value.zdbId)){
                        readData = reader.readItemData(owner.zdbKeyMapping, item.key.identifier)
                        integrateWithExisting(item.value, readData, owner.mappingsContainer, SourceContainer.ezbSource)
                        addToObserved(item.value, observedZdbIds, observedEissns, observedPissns)
                    }
                }
            }
            else if (key == KbartConnector.KBART_HEADER_ONLINE_IDENTIFIER){
                for (Map.Entry<EissnIdentifier, Record> item in dataContainer.recordsPerEissn) {
                    if (item.key && !observedEissns.contains(item.value.eissn)){
                        readData = reader.readItemData(owner.eissnKeyMapping, item.key.identifier)
                        integrateWithExisting(item.value, readData, owner.mappingsContainer, SourceContainer.ezbSource)
                        addToObserved(item.value, observedZdbIds, observedEissns, observedPissns)
                    }
                }
            }
            else if (key == KbartConnector.KBART_HEADER_PRINT_IDENTIFIER){
                for (Map.Entry<PissnIdentifier, Record> item in dataContainer.recordsPerPissn) {
                    if (item.key && !observedPissns.contains(item.value.pissn)){
                        readData = reader.readItemData(owner.pissnKeyMapping, item.key.identifier)
                        integrateWithExisting(item.value, readData, owner.mappingsContainer, SourceContainer.ezbSource)
                        addToObserved(item.value, observedZdbIds, observedEissns, observedPissns)
                    }
                }
            }
        }
    }
}
