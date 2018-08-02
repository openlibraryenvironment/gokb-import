package ygor.identifier

import ygor.field.FieldKeyMapping

class AbstractIdentifier {

    String identifier

    static constraints = {
        identifier nullable : false
    }

    String toString(){return identifier}

    static Class byFieldKeyMapping(FieldKeyMapping fieldKeyMapping){
        if (fieldKeyMapping == ZdbIdentifier.FIELD_KEY_MAPPING){
            return ZdbIdentifier.class
        }
        if (fieldKeyMapping == PissnIdentifier.FIELD_KEY_MAPPING){
            return PissnIdentifier.class
        }
        if (fieldKeyMapping == EissnIdentifier.FIELD_KEY_MAPPING){
            return EissnIdentifier.class
        }
        null
    }
}
