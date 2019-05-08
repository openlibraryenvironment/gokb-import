package ygor.identifier

import java.util.regex.Pattern

class AbstractIdentifier {

    String identifier
    static Pattern ISSN_PATTERN = Pattern.compile("[\\d]{4}-?[\\d]{3}[\\dX]")
    static Pattern ISBN10_SIMPLE_PATTERN = Pattern.compile("([\\d]{9}|[\\d-]{12})[\\dX]")
    static Pattern ISBN13_SIMPLE_PATTERN = Pattern.compile("[\\d-]{17}")

    static constraints = {
        identifier nullable : false
    }

    String toString(){
        return identifier
    }
}
