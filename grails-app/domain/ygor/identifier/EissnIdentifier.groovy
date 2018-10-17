package ygor.identifier

import ygor.field.FieldKeyMapping

class EissnIdentifier extends AbstractIdentifier{

    static FieldKeyMapping FIELD_KEY_MAPPING

    static constraints = {
        // TODO: check EISSN format
    }

    static void initialize(){
        FIELD_KEY_MAPPING = FieldKeyMapping.findByYgorKey("onlineIdentifier")
    }

    EissnIdentifier(String identifier){
        this.identifier = identifier
    }
}
