package ygor.field


class Field {

    String source
    String key
    String value

    static constraints = {
        source nullable : false
        key    nullable : false
        value  nullable : false
    }

    Field(String source, String key, String value){
        this.source = source
        this.key = key
        this.value = value
    }

}
