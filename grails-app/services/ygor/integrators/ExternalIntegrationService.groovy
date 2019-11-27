package ygor.integrators

import de.hbznrw.ygor.processing.MultipleProcessingThread
import org.apache.commons.lang.StringUtils
import ygor.Record
import ygor.field.Field
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

  static void integrateWithExisting(Record item, Map<String, List<String>> readData,
                                    MappingsContainer mappings, String source) {
    if (!item || !readData || !mappings || !source) {
      // TODO: throw exception?
      return
    }
    for (Map.Entry<String, List<String>> date : readData) {
      FieldKeyMapping mapping = mappings.getMapping(date.key, source)
      if (mapping) {
        MultiField multiField = item.getMultiField(mapping.ygorKey)
        if (multiField == null) {
          multiField = new MultiField(mapping)
          item.addMultiField(multiField)
        }
        for (String singleValue in date.value){
          multiField.addField(new Field(source, date.key, singleValue))
        }
      }
    }
  }

  /**
   * @return the Map<String, String> matching best to the given Record.
   * Return an empty Map<String, String> if no singular best match could be determined.
   */
  protected Map<String, String> filterBestMatch(MultipleProcessingThread owner, Record record,
                                                List<Map<String, List<String>>> readData, int keyOrderCount,
                                                String containerProperty, String keyMappingProperty) {
    if (readData.size() == 1) {
      return readData.get(0)
    }
    String key = owner.KEY_ORDER.get(keyOrderCount)
    if (key) {
      List<Map<String, List<String>>> narrowedResult = new ArrayList<>()
      for (Map<String, List<String>> readItem in readData) {
        FieldKeyMapping fieldKeyMapping = mappingsContainer.getMapping(key, containerProperty)
        if (fieldKeyMapping) {
          for (String zdbKey in fieldKeyMapping."${keyMappingProperty}") {
            if (record.getMultiField(fieldKeyMapping.ygorKey).getFirstPrioValue() in (readItem.get(zdbKey))) {
              narrowedResult.add(readItem)
            }
          }
        }
        else {
          break
        }
      }
      if (narrowedResult.size() > 0) {
        return filterBestMatch(owner, record, narrowedResult, keyOrderCount + 1,
          containerProperty, keyMappingProperty)
      }
      if (narrowedResult.size() == 0) {
        return new HashMap<String, String>()
      }
    }
    // else
    // is reached if no single "best" match could be determined
    return new HashMap<String, String>()
  }

  // The record needs to be processed in external APIs if and only if it is a journal, or if no value for
  // the medium type is given, meaning that API processing is default.
  protected boolean isApiCallMedium(Record record) {
    String medium = record.multiFields.get(mediumTypeKey).getFirstPrioValue()
    return (medium.equals("Journal") || StringUtils.isEmpty(medium))
  }
}
