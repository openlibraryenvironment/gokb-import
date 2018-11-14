package ygor.integrators

import ygor.Record
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField

class ExternalIntegrationService {

    static void integrateWithExisting(Record item, Map<String, String> readData,
                                              MappingsContainer mappings, String source){
        if (!item || !readData || !mappings || !source){
            // TODO: throw exception?
            return
        }
        for (Map.Entry<String, String> date : readData){
            FieldKeyMapping mapping = mappings.getMapping(date.key, source)
            if (mapping) {
                MultiField multiField = item.getMultiField(mapping.ygorKey)
                if (multiField != null){
                    multiField.addField(source, date.key, date.value)
                }
                else {
                    multiField = new MultiField(mapping)
                    multiField.addField(source, date.key, date.value)
                    item.addMultiField(multiField)
                }
            }
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
