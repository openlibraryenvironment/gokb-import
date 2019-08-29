package de.hbznrw.ygor.normalizers

import java.util.regex.Matcher
import java.util.regex.Pattern

class NumberNormalizer {

    final static Pattern NUMBER_PATTERN =
            Pattern.compile("([\\d]*)((st|nd|rd|th) )?((revised|and|expanded) ?)* ?(edition)?")

    /**
     * "3rd revised and expanded edition" --> "3" // and similar cases, see Regex in
     */
    static def extractNumbers(ArrayList<String> strings) {
        if(!strings || strings.isEmpty()) {
            return strings
        }
        StringBuilder result = new StringBuilder()
        for (str in strings) {
            if (str && str.length() > 0) {
                Matcher matcher = NUMBER_PATTERN.matcher(str)
                if (result.length() > 0) {
                    result.append("|")
                }
                if (matcher.matches()) {
                    result.append(matcher.group(1))
                } else {
                    result.append(str)
                }
            }
        }
        result.toString()
    }


    static String normalizeInteger(String str) {
        if(!str)
            return str
        String result = new String(StringNormalizer.normalizeString(str, false))
        result = result.replaceAll(/[\/-]+/,"")
        if (!(result.matches("^[\\d]+\$"))){
            throw new NumberFormatException(str.concat(" does not match simple digit RegEx"))
        }
        result
    }

}
