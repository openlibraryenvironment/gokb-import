package de.hbznrw.ygor.export

import com.fasterxml.jackson.databind.node.ObjectNode
import de.hbznrw.ygor.export.structure.Meta
import de.hbznrw.ygor.export.structure.Package
import ygor.Record
import ygor.identifier.EissnIdentifier
import ygor.identifier.PissnIdentifier
import ygor.identifier.ZdbIdentifier

class DataContainer {

    Meta    info    // TODO: use or delete
    Package pkg
    ObjectNode packageHeader

    Map<ZdbIdentifier, Record>   recordsPerZdbId = [:]
    Map<EissnIdentifier, Record> recordsPerEissn = [:]
    Map<PissnIdentifier, Record> recordsPerPissn = [:]
    Set<Record> records = []
    List titles = []
    List tipps = []
    
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
    }

    def putRecord(Record record){
        if (record.zdbId){
            recordsPerZdbId.put(record.zdbId, record)
        }
        if (record.eissn){
            recordsPerEissn.put(record.eissn, record)
        }
        if (record.pissn){
            recordsPerPissn.put(record.pissn, record)
        }
        if (record.zdbId || record.pissn || record .eissn){
            records.add(record)
        }
    }
}
