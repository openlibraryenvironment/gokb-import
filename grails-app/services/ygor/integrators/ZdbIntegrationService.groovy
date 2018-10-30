package ygor.integrators

import de.hbznrw.ygor.connectors.KbartConnector
import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.ZdbReader
import grails.transaction.Transactional
import ygor.Record
import ygor.field.MappingsContainer

@Transactional
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
                for (Record item in dataContainer.recordsPerZdbId) {
                    if (item.zdbId && !observedZdbIds.contains(item.zdbId)){
                        readData = reader.readItemData(owner.zdbKeyMapping, item.zdbId)
                        integrateWithExisting(item, readData)
                        addToObserved(item, observedZdbIds, observedEissns, observedPissns)
                    }
                }
            }
            else if (key == KbartConnector.KBART_HEADER_ONLINE_IDENTIFIER){
                for (Record item in dataContainer.recordsPerEissn) {
                    if (item.eissn && !observedEissns.contains(item.eissn)){
                        readData = reader.readItemData(owner.eissnKeyMapping, item.eissn)
                        integrateWithExisting(item, readData)
                        addToObserved(item, observedZdbIds, observedEissns, observedPissns)
                    }
                }
            }
            else if (key == KbartConnector.KBART_HEADER_PRINT_IDENTIFIER){
                for (Record item in dataContainer.recordsPerPissn) {
                    if (item.pissn && !observedPissns.contains(item.pissn)){
                        readData = reader.readItemData(owner.pissnKeyMapping, item.pissn)
                        integrateWithExisting(item, readData)
                        addToObserved(item, observedZdbIds, observedEissns, observedPissns)
                    }
                }
            }
        }
    }

    private void integrateWithExisting(Record item, Map<String, String> readData){
        // TODO
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
