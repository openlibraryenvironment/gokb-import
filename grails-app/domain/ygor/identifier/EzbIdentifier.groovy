package ygor.identifier

import ygor.field.FieldKeyMapping

class EzbIdentifier extends AbstractIdentifier{

    static constraints = {
    }

    EzbIdentifier(String id, FieldKeyMapping fieldKeyMapping){
        super(fieldKeyMapping)
        identifier = id
    }
}


