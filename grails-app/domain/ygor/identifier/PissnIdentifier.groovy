package ygor.identifier

import ygor.field.FieldKeyMapping

class PissnIdentifier extends AbstractIdentifier{

    static constraints = {
        fieldKeyMapping nullable : false

        identifier validator: {
            ISSN_PATTERN.matcher(identifier).matches()
        }
    }

    PissnIdentifier(String id, FieldKeyMapping fieldKeyMapping){
        super(fieldKeyMapping)
        identifier = id
    }
}
