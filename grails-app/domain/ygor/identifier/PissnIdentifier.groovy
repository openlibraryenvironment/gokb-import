package ygor.identifier

import ygor.field.FieldKeyMapping

class PissnIdentifier extends AbstractIdentifier{

    // static FieldKeyMapping FIELD_KEY_MAPPING = FieldKeyMapping.findByYgorKey("printIdentifier")

    static constraints = {
        // TODO: check PISSN format
    }

    PissnIdentifier(String identifier){
        this.identifier = identifier
    }
}
