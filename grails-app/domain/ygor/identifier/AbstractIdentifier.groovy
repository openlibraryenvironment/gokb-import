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
        kbartKey = fieldKeyMapping.kbartKeys.iterator().next()
        zdbKey = fieldKeyMapping.zdbKeys.iterator().next()
        ezbKey = fieldKeyMapping.ezbKeys.iterator().next()
    }

    String toString(){
        return identifier
    }


}
