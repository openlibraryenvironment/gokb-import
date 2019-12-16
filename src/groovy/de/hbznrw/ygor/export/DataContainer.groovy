package de.hbznrw.ygor.export

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import de.hbznrw.ygor.export.structure.Meta
import de.hbznrw.ygor.export.structure.Package
import ygor.Record
import ygor.field.MappingsContainer
import ygor.identifier.AbstractIdentifier
import ygor.identifier.DoiIdentifier
import ygor.identifier.EissnIdentifier
import ygor.identifier.EzbIdentifier
import ygor.identifier.PissnIdentifier
import ygor.identifier.ZdbIdentifier

class DataContainer {

  static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance

  Meta info    // TODO: use or delete
  Package pkg
  ObjectNode packageHeader
  Map<String, Record> records
  ArrayNode titles
  ArrayNode tipps


  DataContainer() {
    info = new Meta(
        date: new Date().format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone('GMT+1')),
        api: [],
        stats: [:],
        stash: [:],
        namespace_title_id: ""
    )
    pkg = new Package()

    records = [:]
    titles = new ArrayNode(NODE_FACTORY)
    tipps = new ArrayNode(NODE_FACTORY)
  }


  def addRecord(Record record) {
    if (record.zdbId || record.printIdentifier || record.onlineIdentifier) {
      records.put(record.uid, record)
    }
  }


  Record getRecord(def id) {
    if (id instanceof AbstractIdentifier){
      return getRecordFromIdentifier(id)
    }
    try {
      if (id instanceof String && UUID.fromString(id)){
        return records.get(id)
      }
    }
    catch(IllegalArgumentException iae) {
      return null
    }
    return null
  }


  void validateRecords() {
    for (Record record in records.values()) {
      record.validate(info.namespace_title_id)
    }
  }


  static DataContainer fromJson(ArrayNode dataContainerNode, MappingsContainer mappings) {
    DataContainer result = new DataContainer()
    Iterator it = dataContainerNode.iterator()
    while (it.hasNext()) {
      Record rec = Record.fromJson(it.next(), mappings)
      result.records.put(rec.uid, rec)
    }
    result
  }


  private Record getRecordFromIdentifier(AbstractIdentifier identifier){
    if (identifier instanceof ZdbIdentifier){
      for (Record record in records.values()){
        if (identifier.identifier.equals(record.zdbId?.identifier)){
          return record
        }
      }
    }
    if (identifier instanceof EzbIdentifier){
      for (Record record in records.values()){
        if (identifier.identifier.equals(record.ezbId?.identifier)){
          return record
        }
      }
    }
    if (identifier instanceof DoiIdentifier){
      for (Record record in records.values()){
        if (identifier.identifier.equals(record.doiId?.identifier)){
          return record
        }
      }
    }
    if (identifier instanceof EissnIdentifier){
      for (Record record in records.values()){
        if (identifier.identifier.equals(record.onlineIdentifier?.identifier)){
          return record
        }
      }
    }
    if (identifier instanceof PissnIdentifier){
      for (Record record in records.values()){
        if (identifier.identifier.equals(record.printIdentifier?.identifier)){
          return record
        }
      }
    }
    return null
  }
}
