package ygor

import com.sun.javaws.exceptions.InvalidArgumentException
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

    Record(AbstractIdentifier... ids){
        for (id in ids){
            addIdentifier(id)
        }
        multiFields = [:]
    }

    void addIdentifier(AbstractIdentifier id){
        if (id instanceof ZdbIdentifier){
            if (zdbId){
                throw new InvalidArgumentException("ZDB id ".concat(zdbId).concat(" already given for record"))
            }
            zdbId = id
        }
        else if (id instanceof EissnIdentifier){
            if (eissn){
                throw new InvalidArgumentException("EISSN ".concat(eissn).concat(" already given for record"))
            }
            eissn = id
        }
        else if (id instanceof PissnIdentifier){
            if (pissn){
                throw new InvalidArgumentException("PISSN ".concat(pissn).concat(" already given for record"))
            }
            pissn = id
        }
    }

    MultiField putMultifield(MultiField multiField){
        multiFields.put(multiField.ygorFieldKey, multiField)
    }

    MultiField getMultifield(String ygorFieldKey){
        multiFields.get(ygorFieldKey)
    }

}
