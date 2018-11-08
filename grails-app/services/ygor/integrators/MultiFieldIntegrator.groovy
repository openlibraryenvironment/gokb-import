package ygor.integrators

import ygor.Record
import ygor.field.MultiField

class MultiFieldIntegrator {

    static Record integrate(MultiField field, String source, String value){
        if (!field || !source || !value){
            // TODO throw Exception?
            return field
        }
        // non-conditional integration - previously contained data will be overwritten
        field
    }
}
