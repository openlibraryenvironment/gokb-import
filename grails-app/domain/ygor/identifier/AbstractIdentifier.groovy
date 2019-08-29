package ygor.identifier

import ygor.field.FieldKeyMapping

import java.util.regex.Pattern

class AbstractIdentifier {

    String identifier
    FieldKeyMapping fieldKeyMapping
    String kbartKey
    String zdbKey
    String ezbKey

    static Pattern ISSN_PATTERN = Pattern.compile("[\\d]{4}-?[\\d]{3}[\\dX]")
    static Pattern ISBN10_SIMPLE_PATTERN = Pattern.compile("([\\d]{9}|[\\d-]{12})[\\dX]")
    static Pattern ISBN13_SIMPLE_PATTERN = Pattern.compile("[\\d-]{17}")

    static constraints = {
        identifier nullable : false
    }

    protected AbstractIdentifier(FieldKeyMapping fieldKeyMapping){
        this.fieldKeyMapping = fieldKeyMapping
        kbartKey = getFirst(fieldKeyMapping.kbartKeys)
        zdbKey = getFirst(fieldKeyMapping.zdbKeys)
        ezbKey = getFirst(fieldKeyMapping.ezbKeys)
    }

    String toString(){
        return identifier
    }


    private String getFirst(List<String> list){
        Iterator it = list.iterator()
        if (it.hasNext()){
            return it.next()
        }
        else{
            return null
        }
    }

}
