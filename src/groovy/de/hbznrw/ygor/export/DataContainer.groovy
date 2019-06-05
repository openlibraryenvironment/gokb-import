package de.hbznrw.ygor.export

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import de.hbznrw.ygor.export.structure.Meta
import de.hbznrw.ygor.export.structure.Package
import ygor.Record
import ygor.field.MappingsContainer
import ygor.identifier.AbstractIdentifier
import ygor.identifier.EissnIdentifier
import ygor.identifier.PissnIdentifier
import ygor.identifier.ZdbIdentifier

class DataContainer {

    static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance

    Meta        info    // TODO: use or delete
    Package     pkg
    ObjectNode  packageHeader
    Set<Record> records
    ArrayNode   titles
    ArrayNode   tipps


    DataContainer() {
        info = new Meta(
            file:   "TODO",
            type:   "TODO",
            ygor:   "TODO",
            date:   new Date().format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone('GMT+1')),
            api:    [],
            stats:  [:],
            stash:  [:],
            namespace_title_id: ""
        )
        pkg = new Package()

        records = []
        titles = new ArrayNode(NODE_FACTORY)
        tipps = new ArrayNode(NODE_FACTORY)
    }


    def putRecord(Record record){
        if (record.zdbId || record.pissn || record .eissn){
            records.add(record)
        }
    }


    static DataContainer fromJson(ArrayNode dataContainerNode, MappingsContainer mappings){
        DataContainer result = new DataContainer()
        Iterator it = dataContainerNode.iterator()
        while (it.hasNext()){
            result.records << Record.fromJson(it.next(), mappings)
        }
        result
    }
}
