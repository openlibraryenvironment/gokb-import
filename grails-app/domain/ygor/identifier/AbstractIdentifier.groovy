package ygor.identifier

import java.util.regex.Pattern

class AbstractIdentifier {

    String identifier
    static Pattern issnPattern = Pattern.compile("[\\d]{4}-?[\\d]{3}[\\dX]")

    static constraints = {
        identifier nullable : false
    }

    String toString(){
        return identifier
    }
}
