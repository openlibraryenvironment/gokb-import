package de.hbznrw.ygor.export

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import de.hbznrw.ygor.export.structure.Meta
import de.hbznrw.ygor.export.structure.Package
import de.hbznrw.ygor.tools.JsonToolkit
import de.hbznrw.ygor.tools.RecordFileFilter
import ygor.Record
import ygor.field.MappingsContainer
import ygor.identifier.AbstractIdentifier
import ygor.identifier.DoiIdentifier
import ygor.identifier.EzbIdentifier
import ygor.identifier.OnlineIdentifier
import ygor.identifier.PrintIdentifier
import ygor.identifier.ZdbIdentifier

class DataContainer {

  static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance

  Meta info
  Package pkg
  String  pkgId
  String  pkgIdNamespace
  String  isil
  ObjectNode packageHeader
  Map<String, Record> records
  Map<AbstractIdentifier, Set<Record>> recordsPerId
  ArrayNode titles
  ArrayNode tipps
  String curatoryGroup
  File sessionFolder
  String resultHash


  DataContainer(File sessionFolder, String resultHash) {
    if (!sessionFolder.isDirectory()){
      throw new IOException("Could not read from record directory.")
    }
    this.sessionFolder = sessionFolder
    this.resultHash = resultHash
    info = new Meta(
        date: new Date().format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone('GMT+1')),
        api: [],
        stats: [:],
        stash: [:],
        namespace_title_id: ""
    )
    pkg = new Package()

    records = [:]
    recordsPerId = [:]
    titles = new ArrayNode(NODE_FACTORY)
    tipps = new ArrayNode(NODE_FACTORY)
  }


  def addRecord(Record record) {
    records.put(record.uid, record)
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


  static DataContainer fromJson(File sessionFolder, String resultHash, MappingsContainer mappings) throws IOException{
    DataContainer result = new DataContainer(sessionFolder, resultHash)
    for (File file : sessionFolder.listFiles(new RecordFileFilter(resultHash))) {
      Record rec = Record.fromJson(JsonToolkit.jsonNodeFromFile(file), mappings)
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
    if (identifier instanceof OnlineIdentifier){
      for (Record record in records.values()){
        if (identifier.identifier.equals(record.onlineIdentifier?.identifier)){
          return record
        }
      }
    }
    if (identifier instanceof PrintIdentifier){
      for (Record record in records.values()){
        if (identifier.identifier.equals(record.printIdentifier?.identifier)){
          return record
        }
      }
    }
    return null
  }


  void markDuplicateIds(){
    this.sortAllRecordsPerId()
    for (def idRecs in recordsPerId){
      if (idRecs.value.size() > 1){
        for (Record rec in idRecs.value){
          rec.addDuplicates(idRecs.key, idRecs.value)
        }
      }
    }
  }


  void sortAllRecordsPerId(){
    for (Record rec in records.values()){
      if (rec.zdbId.identifier){
        addRecordToIdSortation(rec.zdbId, rec)
      }
      if (rec.ezbId.identifier){
        addRecordToIdSortation(rec.ezbId, rec)
      }
      if (rec.doiId.identifier){
        addRecordToIdSortation(rec.doiId, rec)
      }
      if (rec.onlineIdentifier.identifier){
        addRecordToIdSortation(rec.onlineIdentifier, rec)
      }
      if (rec.printIdentifier.identifier){
        addRecordToIdSortation(rec.printIdentifier, rec)
      }
    }
  }


  void addRecordToIdSortation(AbstractIdentifier id, Record record){
    Set<Record> recordList = recordsPerId.get(id)
    if (recordList == null){
      recordList = new HashSet<>()
      recordsPerId.put(id, recordList)
    }
    recordList.add(record)
  }
}
