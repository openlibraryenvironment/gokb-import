package ygor.field

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.hbznrw.ygor.enums.Status
import org.apache.commons.lang.StringUtils

class FieldKeyMapping {

  static mapWith = "none" // disable persisting into database

  final static ObjectMapper MAPPER = new ObjectMapper()
  String ygorKey
  String displayName
  List<String> kbartKeys = new ArrayList()
  List<String> zdbKeys = new ArrayList()
  List<String> ezbKeys = new ArrayList()
  String type
  Integer lengthMin = null
  Integer lengthMax = null
  boolean isMultiValueCapable
  Set<String> kb
  String val = ""
  Set<String> allowedValues = new HashSet<>()
  boolean valIsFix
  boolean keepIfEmpty
  List<String> sourcePrio = MappingsContainer.DEFAULT_SOURCE_PRIO
  Map<String, Map<String, Status>> flags = [:]

  static constraints = {
    ygorKey nullable: false
    type nullable: false
    kb nullable: false
  }

  static String[] VALID_FLAG_TYPES = ["valid", "invalid", "missing", "undefined"]

  static hasMany = [kbartKeys    : String,
                    zdbKeys      : String,
                    ezbKeys      : String,
                    kb           : String,
                    sourcePrio   : String,
                    allowedValues: String]

  FieldKeyMapping() {
    // add explicit default constructor
  }

  FieldKeyMapping(boolean dontUseDefaultConstructor, def mappings) {
    if (mappings == null || !(mappings instanceof Map<?, ?>)) {
      throw IllegalArgumentException("Illegal mapping argument given for FieldKeyMapping configuration: "
          .concat(mappings))
    }
    parseMapping(mappings)
  }

  private void parseMapping(Map<?, ?> mappings) {
    for (mapping in mappings) {
      switch (mapping.key) {
        case MappingsContainer.YGOR:
          ygorKey = mapping.value
          break
        case MappingsContainer.KBART:
          putToKeys(mapping.value, kbartKeys)
          break
        case MappingsContainer.ZDB:
          putToKeys(mapping.value, zdbKeys)
          break
        case MappingsContainer.EZB:
          putToKeys(mapping.value, ezbKeys)
          break
        case MappingsContainer.TYPE:
          type = mapping.value
          break
        case "lengthmin":
          lengthMin = mapping.value
          break
        case "lengthmax":
          lengthMax = mapping.value
          break
        case MappingsContainer.TYPE:
          type = mapping.value
          break
        case MappingsContainer.MULTI_VALUE:
          isMultiValueCapable = Boolean.valueOf(mapping.value)
          break
        case MappingsContainer.KB:
          kb = new HashSet<>()
          if (mapping.value instanceof Collection<?>) {
            kb.addAll(mapping.value)
          }
          else if (!StringUtils.isEmpty(mapping.value.toString())) {
            kb.add(mapping.value)
          }
          break
        case "display":
          displayName = mapping.value
          break
        case "fixed":
          val = mapping.value
          valIsFix = true
          break
        case "in":
          parseMapping(mapping.value)
          break
        case "out":
          parseMapping(mapping.value)
          break
        case "value":
          if (!(mapping.value instanceof String)) {
            parseMapping(mapping.value)
          }
          else {
            val = mapping.value
          }
          break
        case "allowedValues":
          if (mapping.value instanceof Collection<?>) {
            allowedValues.addAll(mapping.value)
          }
          else if (!StringUtils.isEmpty(mapping.value.toString())) {
            allowedValues.add(mapping.value)
          }
          break
        case "keepIfEmpty":
          keepIfEmpty = Boolean.valueOf(mapping.value)
          break
        case "default":
          val = mapping.value
          valIsFix = false
          break
        case "flags":
          parseMapping(mapping.value)
          break
        case "valid":
        case "VALID":
          addFlag(mapping)
          break
        case "invalid":
        case "INVALID":
          addFlag(mapping)
          break
        case "missing":
        case "MISSING":
          addFlag(mapping)
          break
        case "undefined":
        case "UNDEFINED":
          addFlag(mapping)
          break
        default:
          continue
      }
    }
  }


  private void addFlag(def mapping){
    if (mapping.key in VALID_FLAG_TYPES){
      for (def entry in mapping.value){
        HashMap<String, Status> existing = flags.get(mapping.key)
        if (existing == null){
          existing = [:]
        }
        for (def pair in entry){
          existing.put(pair.key, Status.valueOf(pair.value.toUpperCase()))
        }
        flags.put(mapping.key, existing)
      }
    }
  }


  Status getFlag(String validity, String publicationType){
    return flags.get(validity)?.get(publicationType)
  }


  private void putToKeys(def value, Collection keys) {
    if (value instanceof Collection<?>) {
      keys.addAll(value)
    } else if (!StringUtils.isEmpty(value.toString())) {
      keys.add(value)
    }
  }


  /**
   * @param source One of {MappingsContainer.@value YGOR}, { MappingsContainer.@value KBART},
   *{MappingsContainer.@value ZDB}, {MappingsContainer.@value EZB} or
   *{MappingsContainer.@value TYPE}* @return The values of the given source.
   */
  Collection get(String source) {
    if (source == MappingsContainer.YGOR) {
      [ygorKey]
    } else if (source == MappingsContainer.KBART) {
      kbartKeys
    } else if (source == MappingsContainer.ZDB) {
      zdbKeys
    } else if (source == MappingsContainer.EZB) {
      ezbKeys
    } else if (source == MappingsContainer.TYPE) {
      [type]
    }
  }


  void setSourcePrio(List<String> sourcePrio) {
    if (!sourcePrio || sourcePrio.size() != MappingsContainer.DEFAULT_SOURCE_PRIO.size()) {
      throw IllegalArgumentException("Illegal static list of sources given for MultiField configuration: "
          .concat(sourcePrio))
    }
    for (necessaryKey in [MappingsContainer.ZDB, MappingsContainer.KBART, MappingsContainer.EZB]) {
      boolean found = false
      for (givenSource in sourcePrio) {
        if (givenSource == necessaryKey) {
          found = true
        }
      }
      if (!found) {
        throw NoSuchElementException("Missing ".concat(necessaryKey)
            .concat(" in given MultiField configuration: ".concat(sourcePrio)))
      }
    }
    this.sourcePrio = sourcePrio
  }


  void asJson(JsonGenerator jsonGenerator) {
    jsonGenerator.writeStartObject()
    jsonGenerator.writeStringField(MappingsContainer.YGOR, ygorKey)
    jsonGenerator.writeStringField("displayName", String.valueOf(displayName))
    jsonGenerator.writeStringField(MappingsContainer.TYPE, type)
    jsonGenerator.writeStringField("value", val)
    jsonGenerator.writeStringField("valueIsFix", String.valueOf(valIsFix))

    jsonGenerator.writeFieldName(MappingsContainer.KBART)
    jsonGenerator.writeStartArray()
    for (String kk in kbartKeys) {
      jsonGenerator.writeString(kk)
    }
    jsonGenerator.writeEndArray()

    jsonGenerator.writeFieldName(MappingsContainer.ZDB)
    jsonGenerator.writeStartArray()
    for (String zk in zdbKeys) {
      jsonGenerator.writeString(zk)
    }
    jsonGenerator.writeEndArray()

    jsonGenerator.writeFieldName(MappingsContainer.EZB)
    jsonGenerator.writeStartArray()
    for (String ek in ezbKeys) {
      jsonGenerator.writeString(ek)
    }
    jsonGenerator.writeEndArray()

    jsonGenerator.writeFieldName(MappingsContainer.KB)
    jsonGenerator.writeStartArray()
    for (String gf in kb) {
      jsonGenerator.writeString(gf)
    }
    jsonGenerator.writeEndArray()

    jsonGenerator.writeFieldName("sourcePrio")
    jsonGenerator.writeStartArray()
    for (String sp in sourcePrio) {
      jsonGenerator.writeString(sp)
    }
    jsonGenerator.writeEndArray()

    jsonGenerator.writeFieldName(MappingsContainer.FLAGS)
    jsonGenerator.writeStartObject()
    for (def flag in flags){
      jsonGenerator.writeFieldName(flag.key)
      jsonGenerator.writeStartObject()
      for (pair in flag.value){
        jsonGenerator.writeStringField(pair.key, pair.value.toString())
      }
      jsonGenerator.writeEndObject()
    }
    jsonGenerator.writeEndObject()

    jsonGenerator.writeFieldName("allowedValues")
    jsonGenerator.writeStartArray()
    for (def val in allowedValues){
      jsonGenerator.writeString(val)
    }
    jsonGenerator.writeEndArray()

    jsonGenerator.writeEndObject()
  }


  static FieldKeyMapping fromJson(JsonNode jsonNode) {
    Map<String, Object> map = MAPPER.convertValue(jsonNode, Map.class)
    return new FieldKeyMapping(false, map)
  }
}
