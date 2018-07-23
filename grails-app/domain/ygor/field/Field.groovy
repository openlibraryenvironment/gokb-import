package ygor.field

import ygor.source.SourceInterface

class Field {

    SourceInterface source
    String key
    String value

    static constraints = {
        source nullable : false
        key    nullable : false
        value  nullable : false
    }

    Field(SourceInterface source, String key, String value){
        this.source = source
        this.key = key
        this.value = value
    }

}
