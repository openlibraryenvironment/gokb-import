package ygor.identifier

import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer

import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.util.regex.Pattern

@SuppressWarnings('JpaObjectClassSignatureInspection')
class AbstractIdentifier {

  static mapWith = "none" // disable persisting into database

  String identifier
  FieldKeyMapping fieldKeyMapping
  String kbartKey
  String zdbKey
  String ezbKey

  static Pattern ISSN_PATTERN = Pattern.compile("[\\d]{4}-?[\\d]{3}[\\dX]")
  static Pattern ISBN10_SIMPLE_PATTERN = Pattern.compile("([\\d]{9}|[\\d-]{12})[\\dX]")
  static Pattern ISBN13_SIMPLE_PATTERN = Pattern.compile("[\\d-]{13,17}")

  static constraints = {
    identifier nullable: false
  }

  protected AbstractIdentifier(FieldKeyMapping fieldKeyMapping) {
    this.fieldKeyMapping = fieldKeyMapping
    kbartKey = getFirst(fieldKeyMapping.kbartKeys)
    zdbKey = getFirst(fieldKeyMapping.zdbKeys)
    ezbKey = getFirst(fieldKeyMapping.ezbKeys)
  }


  String toReducedString() {
    if (identifier == null){
      return ""
    }
    return identifier
  }


  String toString() {
    if (identifier == null){
      return ""
    }
    return this.getClass().getSimpleName().concat(" : ").concat(identifier)
  }


  static AbstractIdentifier fromString(string, MappingsContainer mappings) throws InstantiationException{
    try{
      String[] split = string.split(" : ")
      Class clazz = Class.forName("ygor.identifier.".concat(split[0]))
      String mappingName
      switch (clazz.getSimpleName()){
        case "ZdbIdentifier":
          mappingName = "zdbId"
          break
        case "EzbIdentifier":
          mappingName = "ezbId"
          break
        case "DoiIdentifier":
          mappingName = "doi"
          break
        case "OnlineIdentifier":
          mappingName = "onlineIdentifier"
          break
        case "PrintIdentifier":
          mappingName = "printIdentifier"
          break
        default:
          mappingName = null
      }
      if (mappingName != null){
        FieldKeyMapping fieldKeyMapping = mappings.getMapping(mappingName, MappingsContainer.YGOR)
        Constructor boa = clazz.getConstructor(String.class, FieldKeyMapping.class)
        return boa.newInstance(split[1], fieldKeyMapping)
      }
      // else
      return null
    }
    catch (Exception e){
      throw new InstantiationException("Could not create AbstractIdentifier for : ".concat(string))
    }
  }


  private String getFirst(List<String> list) {
    Iterator it = list.iterator()
    if (it.hasNext()) {
      return it.next()
    } else {
      return null
    }
  }


  @Override
  boolean equals(Object anotherIdentifier){
    if (!(anotherIdentifier instanceof AbstractIdentifier)){
      return false
    }
    if (anotherIdentifier.identifier != this.identifier){
      return false
    }
    if (anotherIdentifier.fieldKeyMapping != this.fieldKeyMapping){
      return false
    }
    return true
  }


  @Override
  int hashCode(){
    return Objects.hash(identifier, fieldKeyMapping)
  }

}
