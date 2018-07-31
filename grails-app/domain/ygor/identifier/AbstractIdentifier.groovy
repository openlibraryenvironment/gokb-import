package ygor.identifier

import ygor.field.FieldKeyMapping

class AbstractIdentifier {

    String identifier

    static constraints = {
        identifier nullable : false
    }

    String toString(){return identifier}

    static Class byFieldKeyMapping(FieldKeyMapping fieldKeyMapping){
        if (fieldKeyMapping == FieldKeyMapping.findByYgorKey("zdbId")){
            return ZdbIdentifier.class
        }
        if (fieldKeyMapping == FieldKeyMapping.findByYgorKey("printIdentifier")){
            return PissnIdentifier.class
        }
        if (fieldKeyMapping == FieldKeyMapping.findByYgorKey("onlineIdentifier")){
            return EissnIdentifier.class
        }
        null
    }
}
