package ygor.identifier

import ygor.field.FieldKeyMapping

class ZdbIdentifier extends AbstractIdentifier{

    FieldKeyMapping fieldKeyMapping

    static constraints = {
        // TODO: check ZdbID format
    }

    ZdbIdentifier(String id, FieldKeyMapping fieldKeyMapping){
        identifier = id
        this.fieldKeyMapping = fieldKeyMapping
    }
}
