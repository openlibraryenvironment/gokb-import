package de.hbznrw.ygor.export

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import de.hbznrw.ygor.export.structure.Meta
import de.hbznrw.ygor.export.structure.Package
import ygor.Record
import ygor.field.MappingsContainer

class DataContainer {

    static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance

    Meta        info    // TODO: use or delete
    Package     pkg
    ObjectNode  packageHeader
    Map<String, Record> records
    ArrayNode   titles
    ArrayNode   tipps


    DataContainer() {
        info = new Meta(
            date:   new Date().format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone('GMT+1')),
            api:    [],
            stats:  [:],
            stash:  [:],
            namespace_title_id: ""
        )
        pkg = new Package()

        records = [:]
        titles = new ArrayNode(NODE_FACTORY)
        tipps = new ArrayNode(NODE_FACTORY)
    }


    def addRecord(Record record){
        if (record.zdbId || record.printIdentifier || record.onlineIdentifier){
            records.put(record.uid, record)
        }
    }


    Record getRecord(String uid){
        return records.get(uid);
    }


    void validateRecords(){
        for (Record record in records.values()){
            record.validate(info.namespace_title_id)
        }
    }


    static DataContainer fromJson(ArrayNode dataContainerNode, MappingsContainer mappings){
        DataContainer result = new DataContainer()
        Iterator it = dataContainerNode.iterator()
        while (it.hasNext()){
            Record rec = Record.fromJson(it.next(), mappings)
            result.records.put(rec.uid, rec)
        }
        result
    }
}
