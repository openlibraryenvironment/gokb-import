package ygor.integrators

import de.hbznrw.ygor.processing.MultipleProcessingThread
import org.apache.commons.lang.StringUtils
import ygor.Record
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField

class ExternalIntegrationService {

    MappingsContainer mappingsContainer
    final String mediumTypeKey

    protected ExternalIntegrationService(MappingsContainer mappingsContainer) {
        this.mappingsContainer = mappingsContainer
        mediumTypeKey = mappingsContainer.ygorMappings.get("medium").ygorKey
    }

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

    /**
     * @return the Map<String, String> matching best to the given Record.
     * Return an empty Map<String, String> if no singular best match could be determined.
     */
    protected Map<String, String> getBestMatchingData(MultipleProcessingThread owner, Record record,
                                                     List<Map<String, String>> readData, int keyOrderCount,
                                                     String containerProperty, String keyMappingProperty){
        if (readData.size() == 1){
            return readData.get(0)
        }
        String key = owner.KEY_ORDER.get(keyOrderCount)
        if (key){
            List<Map<String, String>> narrowedResult = new ArrayList<>()
            for (Map<String, String> readItem in readData){
                FieldKeyMapping fieldKeyMapping = mappingsContainer.getMapping(key, containerProperty)
                if (fieldKeyMapping) {
                    for (String zdbKey in fieldKeyMapping."${keyMappingProperty}") {
                        if (record.getMultiField(fieldKeyMapping.ygorKey).getPrioValue()
                                .equals(readItem.get(zdbKey))) {
                            narrowedResult.add(readItem)
                        }
                    }
                }
                else{
                    break
                }
            }
            if (narrowedResult.size() > 0){
                return getBestMatchingData(owner, record, narrowedResult, keyOrderCount+1,
                                           containerProperty, keyMappingProperty)
            }
            if (narrowedResult.size() == 0){
                return new HashMap<String, String>()
            }
        }
        // else
        return new HashMap<String, String>()
    }


    // The record needs to be processed in external APIs if and only if it is a journal, or if no value for
    // the medium type is given, meaning that API processing is default.
    protected boolean isApiCallMedium(Record record){
        String medium = record.multiFields.get(mediumTypeKey).getPrioValue()
        return (medium.equals("Journal") || StringUtils.isEmpty(medium))
    }

}
