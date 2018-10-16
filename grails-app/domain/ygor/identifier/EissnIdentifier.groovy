package ygor.identifier

import ygor.field.FieldKeyMapping

class EissnIdentifier extends AbstractIdentifier{

    // static FieldKeyMapping FIELD_KEY_MAPPING = FieldKeyMapping.findByYgorKey("onlineIdentifier")

    static constraints = {
        // TODO: check EISSN format
    }

    EissnIdentifier(String identifier){
        this.identifier = identifier
    }
}
