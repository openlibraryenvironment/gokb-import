package ygor.integrators

import ygor.Record
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.source.AbstractSource

class ExternalIntegrationService {

    static void integrateWithExisting(Record item, Map<String, String> readData,
                                              MappingsContainer mappings, AbstractSource source){
        if (!item || !readData || !mappings || !source){
            // TODO: throw exception?
            return
        }
        for (Map.Entry<String, String> date : readData){
            FieldKeyMapping mapping = mappings.getMapping(date.key, MappingsContainer.ZDB)
            MultiField multiField = item.get(mapping.ygorKey)
            multiField.addValue(source, date.value)
        }
    }


    static void addToObserved(Record item, def observedZdbIds, def observedEissns, def observedPissns){
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
