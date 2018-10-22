package ygor.identifier

import ygor.field.FieldKeyMapping

class EissnIdentifier extends AbstractIdentifier{

    FieldKeyMapping fieldKeyMapping

    static constraints = {
        // TODO: check EISSN format
    }

    EissnIdentifier(String id, FieldKeyMapping fieldKeyMapping){
        identifier = id
        this.fieldKeyMapping = fieldKeyMapping
    }
}
