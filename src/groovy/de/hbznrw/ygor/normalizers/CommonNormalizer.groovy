package de.hbznrw.ygor.normalizers

class CommonNormalizer {

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
