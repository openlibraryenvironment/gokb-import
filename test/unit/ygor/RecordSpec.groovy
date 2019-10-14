package ygor

import grails.test.mixin.TestFor
import spock.lang.Specification
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.AbstractIdentifier
import ygor.identifier.EissnIdentifier
import ygor.identifier.ZdbIdentifier

@TestFor(Record)
class RecordSpec extends Specification {

  MappingsContainer container

  def setup() {
    container = new MappingsContainer()
  }

  def cleanup() {
  }

  void "create Record"() {
    given: "a mappings container and a list of identifiers"
    ZdbIdentifier zdbId = new ZdbIdentifier("12345", container.getMapping("zdbId", MappingsContainer.YGOR))
    EissnIdentifier eissn = new EissnIdentifier("12345678", container.getMapping("onlineIdentifier", MappingsContainer.YGOR))
    ArrayList<AbstractIdentifier> ids = [zdbId, eissn]

    when: "a record is created"
    Record record = new Record(ids, container)

    then: "the record's multifields are empty"
    for (MultiField mf in record.multiFields.values()) {
      mf.getPrioValue() == null
    }
    and: "the record's identifiers match the given ones"
    record.onlineIdentifier.identifier == "12345678"
    record.zdbId.identifier == "12345"
    record.printIdentifier == null
  }
}
