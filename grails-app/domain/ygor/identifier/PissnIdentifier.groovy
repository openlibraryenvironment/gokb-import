package ygor.identifier

import ygor.field.FieldKeyMapping

class PissnIdentifier extends AbstractIdentifier{

    FieldKeyMapping fieldKeyMapping

    static constraints = {
        // TODO: check PISSN format
    }

    PissnIdentifier(String id, FieldKeyMapping fieldKeyMapping){
        identifier = id
        this.fieldKeyMapping = fieldKeyMapping
    }
}
