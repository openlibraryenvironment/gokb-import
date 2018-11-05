package ygor

import groovy.json.JsonBuilder
import ygor.field.MultiField
import ygor.identifier.AbstractIdentifier
import ygor.identifier.EissnIdentifier
import ygor.identifier.PissnIdentifier
import ygor.identifier.ZdbIdentifier

class Record {

    ZdbIdentifier   zdbId
    EissnIdentifier eissn
    PissnIdentifier pissn
    Map multiFields
    static hasMany = [multiFields : MultiField]

    static constraints = {
    }

    Record(ArrayList<AbstractIdentifier> ids){
        for (id in ids){
            addIdentifier(id)
        }
        multiFields = [:]
    }

    void addIdentifier(AbstractIdentifier identifier){
        if (identifier instanceof ZdbIdentifier){
            if (zdbId){
                throw new IllegalArgumentException("ZDB id ".concat(zdbId).concat(" already given for record"))
            }
            zdbId = identifier
        }
        else if (identifier instanceof EissnIdentifier){
            if (eissn){
                throw new IllegalArgumentException("EISSN ".concat(eissn).concat(" already given for record"))
            }
            eissn = identifier
        }
        else if (identifier instanceof PissnIdentifier){
            if (pissn){
                throw new IllegalArgumentException("PISSN ".concat(pissn).concat(" already given for record"))
            }
            pissn = identifier
        }
    }

    void addMultiField(MultiField multiField){
        multiFields.put(multiField.ygorFieldKey, multiField)
    }

    JsonBuilder toJson(){
        // TODO
    }

}
