package ygor.identifier

import ygor.field.FieldKeyMapping

class ZdbIdentifier extends AbstractIdentifier{

    static FieldKeyMapping FIELD_KEY_MAPPING

    static constraints = {
        // TODO: check ZdbID format
    }

    static void initialize(){
        FIELD_KEY_MAPPING = FieldKeyMapping.findByYgorKey("zdbId")
    }

    ZdbIdentifier(String id){
        this.identifier = id
    }
}
