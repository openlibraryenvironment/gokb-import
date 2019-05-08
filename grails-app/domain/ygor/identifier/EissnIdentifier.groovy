package ygor.identifier

import ygor.field.FieldKeyMapping

class EissnIdentifier extends AbstractIdentifier{

    FieldKeyMapping fieldKeyMapping

    static constraints = {
        fieldKeyMapping nullable : false

        identifier validator: {
            ISSN_PATTERN.matcher(identifier).matches()
        }
    }

    EissnIdentifier(String id, FieldKeyMapping fieldKeyMapping){
        identifier = id
        this.fieldKeyMapping = fieldKeyMapping
    }
}
