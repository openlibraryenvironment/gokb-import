package ygor

import groovy.json.JsonBuilder
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.AbstractIdentifier
import ygor.identifier.EissnIdentifier
import ygor.identifier.EzbIdentifier
import ygor.identifier.PissnIdentifier
import ygor.identifier.ZdbIdentifier

class Record {

    String          uid
    ZdbIdentifier   zdbId
    EzbIdentifier   ezbId
    EissnIdentifier eissn
    PissnIdentifier pissn
    Map multiFields
    static hasMany = [multiFields : MultiField]

    static constraints = {
    }

    Record(List<AbstractIdentifier> ids, MappingsContainer container){
        uid = UUID.randomUUID().toString()
        for (id in ids){
            addIdentifier(id)
        }
        multiFields = [:]
        for (def ygorMapping in container.ygorMappings){
            multiFields.put(ygorMapping.key, new MultiField(ygorMapping.value))
        }
    }

    void addIdentifier(AbstractIdentifier identifier){
        if (identifier instanceof ZdbIdentifier){
            if (zdbId){
                throw new IllegalArgumentException("ZDB id ".concat(zdbId).concat(" already given for record"))
            }
            zdbId = identifier
        }
        else if (identifier instanceof EzbIdentifier){
            if (ezbId){
                throw new IllegalArgumentException("EZB id ".concat(ezbId).concat(" already given for record"))
            }
            ezbId = identifier
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

    MultiField getMultiField(def ygorFieldKey){
        multiFields.get(ygorFieldKey)
    }

    void validate(String namespace){
        multiFields.each{k,v -> v.validate(namespace)}
    }

    JsonBuilder toJson(){
        // TODO
    }

}
