package ygor.identifier

import ygor.field.FieldKeyMapping

class PissnIdentifier extends AbstractIdentifier{

    FieldKeyMapping fieldKeyMapping

    static constraints = {
        fieldKeyMapping nullable : false

        identifier validator: {
            ISSN_PATTERN.matcher(identifier).matches()
        }
    }

    PissnIdentifier(String id, FieldKeyMapping fieldKeyMapping){
        identifier = id
        this.fieldKeyMapping = fieldKeyMapping
    }
}
