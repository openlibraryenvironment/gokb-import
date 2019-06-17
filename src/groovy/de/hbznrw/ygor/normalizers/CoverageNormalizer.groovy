package de.hbznrw.ygor.normalizers

class CoverageNormalizer {

    /**
     *
     * "18.2005 - 27.2014"         -> "18" or "27"
     * "Verlag; 18.2005 - 27.2014" -> "18" or "27"
     * "27.2014"                   -> "18"
     *
     * @param str
     * @param dateType DataMapper.IS_START_DATE|DataMapper.IS_END_DATE
     * @return
     */
    static String normalizeCoverageVolume(String str, String dateType){
        str = StringNormalizer.normalizeString(str)

        if (str){
            if (str.contains("-")){
                def tmp = str.split("-")

                if (dateType.equals("StartDate")){
                    str = parseCoverageVolume(tmp[0])
                }
                else if (dateType.equals("EndDate")){
                    if (tmp.size() > 1){
                        str = parseCoverageVolume(tmp[1])
                    }
                    else {
                        str = ""
                    }
                }
            }
            else {
                str = parseCoverageVolume(str)
            }
        }
        return StringNormalizer.normalizeString(str)
    }


    static String parseCoverageVolume(String str) {
        if(!str) {
            return str
        }
        str = de.hbznrw.ygor.normalizers.CommonNormalizer.removeText(str)
        str = str.replaceAll(/\s+/,"").trim()

        // 4.2010,2 -> [4.2010,2, 4, 0, 2]
        def matches1 = (str =~ /(\d)+[.](\d){4},(\d)+/)

        // 4.2010 -> [4.2010, 4, 0]
        def matches2 = (str =~ /(\d)+[.](\d){4}/)

        if(matches1){
            str = str.split("[.]")[0]
        }
        else if(matches2){
            str = str.split("[.]")[0]
        }
        else {
            str = ""
        }
        str
    }
}
