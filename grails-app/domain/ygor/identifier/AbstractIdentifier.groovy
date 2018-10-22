package ygor.identifier

import ygor.field.FieldKeyMapping

class AbstractIdentifier {

    String identifier

    static constraints = {
        identifier nullable : false
    }

    String toString(){
        return identifier
    }

    static Class byFieldKeyMapping(FieldKeyMapping fieldKeyMapping){
        if (fieldKeyMapping == ZdbIdentifier.fieldKeyMapping){
            return ZdbIdentifier.class
        }
        if (fieldKeyMapping == PissnIdentifier.fieldKeyMapping){
            return PissnIdentifier.class
        }
        if (fieldKeyMapping == EissnIdentifier.fieldKeyMapping){
            return EissnIdentifier.class
        }
        null
    }
}
