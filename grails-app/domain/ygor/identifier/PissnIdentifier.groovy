package ygor.identifier

import ygor.field.FieldKeyMapping

class PissnIdentifier extends AbstractIdentifier{

    static FieldKeyMapping FIELD_KEY_MAPPING

    static constraints = {
        // TODO: check PISSN format
    }

    static void initialize(){
        FIELD_KEY_MAPPING = FieldKeyMapping.findByYgorKey("printIdentifier")
    }

    PissnIdentifier(String identifier){
        this.identifier = identifier
    }
}
