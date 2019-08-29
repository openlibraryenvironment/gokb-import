package ygor.identifier

import ygor.field.FieldKeyMapping

class DoiIdentifier extends AbstractIdentifier {

    static constraints = {
        fieldKeyMapping nullable : false

        // TODO: check Doi format
    }

    DoiIdentifier(String id, FieldKeyMapping fieldKeyMapping) {
        super(fieldKeyMapping)
        identifier = id
    }
}
