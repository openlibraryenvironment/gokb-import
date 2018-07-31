package ygor.identifier

import ygor.field.FieldKeyMapping

class ZdbIdentifier extends AbstractIdentifier{

    static FieldKeyMapping FIELD_KEY_MAPPING = FieldKeyMapping.findByYgorKey("zdb_id")

    static constraints = {
        // TODO: check ZdbID format
    }

    ZdbIdentifier(String id){
        this.identifier = id
    }
}
