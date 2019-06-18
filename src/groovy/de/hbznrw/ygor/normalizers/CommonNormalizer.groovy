package de.hbznrw.ygor.normalizers

class CommonNormalizer {


    static String normalize(String value, String type){
        switch (type) {
            case "String":
                return StringNormalizer.normalizeString(value)
            case "Number":
                return NumberNormalizer.normalizeInteger(value)
            case "ID":
                return IdentifierNormalizer.normIdentifier(value)
            case "URL":
                return UrlNormalizer.normURL(value)
            case DateNormalizer.START_DATE:
                return DateNormalizer.normalizeDate(value, DateNormalizer.START_DATE)
            case DateNormalizer.END_DATE:
                return DateNormalizer.normalizeDate(value, DateNormalizer.END_DATE)
            case "ISBN":
                // return validateISBN(value) // TODO
            default:
                return StringNormalizer.normalizeString(value)
        }
    }

    static String removeText(String str){
        if(!str) {
            return str
        }
        str = str.replace('Vol.', '').replace('Vol', '')
                 .replace('Nr.', '').replace('Nr', '')
                 .replace('Verlag;', '').replace('Verlag', '')
                 .replace('Agentur;', '').replace('Agentur', '')
                 .replace('Archivierung;', '').replace('Archivierung', '')
                 .replace('Digitalisierung;', '').replace('Digitalisierung', '')
        str
    }


    /**
     * Removes double spaces. Removes leading and ending spaces.
     * Returns null if null given.
     * Returns "" if empty string given
     *
     * @param str
     * @return
     */
    static String removeSpaces(String str) {
        if(!str)
            return str
        return str.trim().replaceAll(/\s+/," ").replaceAll(/\s*:\s*/,": ").replaceAll(/\s*,\s*/,", ")
    }

}
