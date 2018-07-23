package ygor.field

import ygor.source.AbstractSource


class Field {

    AbstractSource source
    String key
    String value

    static constraints = {
        source nullable : false
        key    nullable : false
        value  nullable : false
    }

    Field(AbstractSource source, String key, String value){
        this.source = source
        this.key = key
        this.value = value
    }

}
