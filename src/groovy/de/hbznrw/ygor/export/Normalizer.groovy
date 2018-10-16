package de.hbznrw.ygor.export

import de.hbznrw.ygor.bridges.EzbBridge
import de.hbznrw.ygor.bridges.ZdbBridge
import de.hbznrw.ygor.export.structure.PackageHeaderNominalPlatform
import de.hbznrw.ygor.export.structure.TitleStruct
import de.hbznrw.ygor.tools.UrlToolkit
import groovy.util.logging.Log4j

import java.time.LocalDate
// Trying to normalize values, but may return crap

@Log4j
class Normalizer {

    final static IS_START_DATE  = "IS_START_DATE"
    final static IS_END_DATE    = "IS_END_DATE"
    
    /**
     * Removes double spaces. Removes leading and ending spaces.
     * Returns null if null given.
     * Returns "" if empty string given
     * 
     * @param str
     * @return
     */
    static String normString(String str) {
        if(!str)
            return str
        
        str = str.trim().replaceAll(/\s+/," ").replaceAll(/\s*:\s*/,": ").replaceAll(/\s*,\s*/,", ")
    }

    /**
     * Removes special chars. So far: removes "@" occuring after " " and in front of word character.
     * Returns null if null given.
     * Returns "" if empty string given
     *
     * @param str
     * @return
     */
    static String normStringTitle(String str) {
        if(!str)
            return str

        str = str.replaceAll(" @(\\w)", ' $1')
    }

    /**
     * Concatenates list elements with "|" as delimiter.
     * Returns null if null given. 
     * Returns "" if empty list given
     *
     * @param list
     * @return
     */
    static String normString(ArrayList list) {
        if(null == list)
            return null
        
        def result = []
        list.each{ e ->
            result << Normalizer.normString(e.toString())
        }
        result.join("|")
    }
    
    /**
     * Returns null if null given.
     * Returns "" if empty string given.  
     * 
     * @param type
     * @param str
     * @return
     */
    static String normIdentifier(String str, Object type) {
        if(!str)
            return str
            
        str = Normalizer.normString(str)
        if(!(type.equals(TitleStruct.DOI))) {
            str = str.replaceAll(/[\/-]+/, "")
        }
        
        if(type.equals(TitleStruct.EISSN) || type.equals(TitleStruct.PISSN)){
            if(str.length() == 8)
                str = new StringBuilder(str).insert(4, "-").toString();
        }
        else if(type.equals(ZdbBridge.IDENTIFIER)){
            str = new StringBuilder(str).insert(Math.abs(str.length()-1).toInteger(), "-").toString();
        }
        else if(type.equals(EzbBridge.IDENTIFIER)){
            // TODO
        }
        else if(type.equals(TitleStruct.DOI)){
            // TODO: DOI Normalizier
        }

        str
    }
    
    /**
     * Concatenates list elements with "|" as delimiter.
     * Returns null if null given.
     * Returns "" if empty list given
     *
     * @param type
     * @param list
     * @return
     */
    static String normIdentifier(ArrayList list, Object type) {
        if(null == list)
            return null
            
        def result = []
        list.each{ e ->
            result << Normalizer.normIdentifier(e.toString(), type)
        }
        result.join("|")
    }

    /**
     *
     * @param str
     * @return
     */
    static String normInteger(String str) {
        if(!str)
            return str

        str = Normalizer.normString(str)
        str = str.replaceAll(/[\/-]+/,"")

        str.toInteger()
    }

    /**
     *
     * @param list
     * @param type
     * @return
     */
    static String normInteger(ArrayList list, Object type) {
        if(null == list)
            return null

        def result = []
        list.each{ e ->
            result << Normalizer.normInteger(e.toString(), type)
        }
        result.join("|")
    }

    /**
     * Returns null if null given
     * 
     * @param str
     * @param dateType DataMapper.IS_START_DATE|DataMapper.IS_END_DATE
     * @return "YYYY-01-01 00:00:00.000"|"YYYY-12-31 00:00:00.000"
     */
    static String normDate(String str, Object dateType) {
        if(!str)
            return str
            
        str = Normalizer.normString(str)
        
        if(str.contains("-")){
            def tmp = str.split("-")
            
            if(dateType.equals(Normalizer.IS_START_DATE)){
                if(tmp.size() > 1){
                    str = tmp[0]
                }
            }
            else if(dateType.equals(Normalizer.IS_END_DATE)){
                if(tmp.size() > 1){
                    str = tmp[0]
                }
            }  
        }
        
        def strList = Normalizer.parseDate(str, dateType)
        
        if(4 == strList[0].size()) {
            str = strList[0]
            
            if(strList[1]){
                def y = Integer.parseInt(strList[0])
                def m = Integer.parseInt(strList[1])
                if(m >= 1 && m <= 12) {
                    LocalDate date = LocalDate.of(y, m, 1)
                    def mm = String.format('%02d', m)
                    def dd = String.format('%02d', date.lengthOfMonth())
                    
                    if(dateType.equals(Normalizer.IS_START_DATE)){
                        str += ("-" + mm + "-01 00:00:00.000")
                    }
                    else if(dateType.equals(Normalizer.IS_END_DATE)){
                        str += ("-" + mm + "-" + dd + " 23:59:59.000")
                    }
                }
                else {
                    str = ''
                }
            }
            else {
                if(dateType.equals(Normalizer.IS_START_DATE)){
                    str += "-01-01 00:00:00.000"
                }
                else if(dateType.equals(Normalizer.IS_END_DATE)){
                    str += "-12-31 23:59:59.000"
                }
            }
        }
        else {
            str = ''
        }
        
        str
    }
    
    /**
     * Returns null if null given.
     * Returns "" if empty list given
     * 
     * @param list
     * @param dateType DataMapper.IS_START_DATE|DataMapper.IS_END_DATE
     * @return
     */
    static String normDate(ArrayList list, Object dateType) {
        if(null == list)
            return null
            
        def result = []
        list.each{ e ->
            result << Normalizer.normDate(e, dateType)
        }
        result.join("|")
    }
    
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
    static String normCoverageVolume(String str, Object dateType) {
        str = Normalizer.normString(str)

        if(str){
            if(str.contains("-")){
                def tmp = str.split("-")
                
                if(dateType.equals(Normalizer.IS_START_DATE)){
                    str = Normalizer.parseCoverageVolume(tmp[0])
                }
                else if(dateType.equals(Normalizer.IS_END_DATE)){
                    if(tmp.size() > 1){
                        str = Normalizer.parseCoverageVolume(tmp[1])
                    }
                    else {
                        str = ""
                    }
                }
            }
            else {
                str = Normalizer.parseCoverageVolume(str)
            }
            
        }
        
        Normalizer.normString(str)
    }
        
    /**
     * Returns an url (or only the url authority) including protocol.
     * Adding http:// if none given
     * 
     * @param str
     * @param onlyAuthority
     * @return
     */
    static String normURL(String str, boolean onlyAuthority) {    
        if(!str)
            return str
        
        if(onlyAuthority){
            return UrlToolkit.getURLAuthorityWithProtocol(str)
        }
        else {
            return UrlToolkit.getURLWithProtocol(str)
        }
    }


    /**
     * Returns an url (or only the url authority) including protocol.
     * Adding http:// if none given
     *
     * @param phnp
     * @param onlyAuthority
     * @return
     */
    static String normURL(PackageHeaderNominalPlatform phnp, boolean onlyAuthority) {
        if(!phnp?.url)
            return phnp

        if(onlyAuthority){
            return UrlToolkit.getURLAuthorityWithProtocol(phnp.url)
        }
        else {
            return UrlToolkit.getURLWithProtocol(phnp.url)
        }
    }




    /**
     * Concatenates list elements with "|" as delimiter.
     * Returns null if null given.
     * Returns "" if empty list given
     * 
     * @param list
     * @return
     */
    static String normURL(ArrayList list, boolean onlyAuthority) {
        if(null == list)
            return null
            
        def result = []
        list.each{ e ->
            result << Normalizer.normURL(e, onlyAuthority)
        }
        result.join("|")
    }
    
    static List parseDate(String str, Object dateType) {       
        if(!str)
            return ['', null]

        str = Normalizer.removeText(str)
        str = str.replaceAll(/\s+/,'').trim()
        
        // remove coverage volume
        def matches00 = (str =~ /^(\d)+[.](\d){2,4}/)
        if(matches00){
            str = str.split("[.]")[1]
        }
        
        // remove brackets and more
        if(dateType.equals(Normalizer.IS_START_DATE)){
            if(str.startsWith("[")){
                str = str.replaceFirst("\\[", '')
            }
            if(str.endsWith("-")){
                str = str.take(str.length() - 1)
            }
        }
        else if(dateType.equals(Normalizer.IS_END_DATE)){
            if(str.endsWith("]")){
                str = str.take(str.length() - 1)
            }
            if(str.startsWith("-")){
                str = str.replaceFirst("-", '')
            }
        }
        
        // 2001-2002        -> [2001-2002, 1, null, null, -, 2, null, null]
        // 2001/2002        -> [2001/2002, 1, null, null, /, 2, null, null]
        // 2015,8-2016      -> [2015,8/2016, 5, ,8, 8, -, 6, null, null]
        // 2015,8/2016      -> [2015,8/2016, 5, ,8, 8, /, 6, null, null]
        // 2013-2015,3      -> [2013-2015,3, 3, null, null, -, 5, ,3, 3]
        // 2010/2018,3      -> [2010-2018,3, 0, null, null, /, 8, ,3, 3]
        // 1999,5-2005,11   -> [1999,5-2005,11, 9, ,5, 5, -, 5, ,11, 1]
        // 2001,4/2002,5    -> [2001,4/2002,5, 1, ,4, 4, /, 2, ,5, 5]
        
        def matches001 = (str =~ /^(\d){4}(,(\d)+)?(\/|-)(\d){4}(,(\d)+)?$/)
        if(matches001){
            def tmp1 = str.split(matches001[0][4])

            if(dateType.equals(Normalizer.IS_START_DATE)){
                if(tmp1[0].contains(",")){
                    def tmp2 = tmp1[0].split(",")
                    return [tmp2[0], tmp2[1]]
                }
                return [tmp1[0], null]
            }
            else if(dateType.equals(Normalizer.IS_END_DATE)){
                if(tmp1[1].contains(",")){
                    def tmp2 = tmp1[1].split(",")
                    return [tmp2[0], tmp2[1]]
                }
                return [tmp1[1], null]
            }
            return [null, null]
        }
        
        // 2022/23          -> [2022/23, 2, null, null, /, 3, null, null]
        // 2022,5-23        -> [2022,5/23, 2, ,5, 5, -, 3, null, null]
        // 2022/23,11       -> [2022/23,11, 2, null, null, /, 3, ,11, 1]
        // 2022,5/23,11     -> [2022,5/23,11, 2, ,5, 5, /, 3, ,11, 1]
        
        def matches002 = (str =~ /^(\d){4}(,(\d)+)?(\/|-)(\d){2}(,(\d)+)?$/)
        if(matches002){
            def tmp1 = str.split(matches002[0][4])

            if(dateType.equals(Normalizer.IS_START_DATE)){
                if(tmp1[0].contains(",")){
                    def tmp2 = tmp1[0].split(",")
                    return [tmp2[0], tmp2[1]]
                }
                return [tmp1[0], null]
            }
            else if(dateType.equals(Normalizer.IS_END_DATE)){
                if(tmp1[1].contains(",")){
                    def tmp2 = tmp1[1].split(",")
                    return [tmp1[0].take(2) + tmp2[0], tmp2[1]]
                }
                return [tmp1[0].take(2) + tmp1[1], null]
            }
            return [null, null]
        }

        // 22/23          -> [22/23, 2, null, null, /, 3, null, null]
        // 22,5-23        -> [22,5/23, 2, ,5, 5, -, 3, null, null]
        // 22/23,11       -> [22/23,11, 2, null, null, /, 3, ,11, 1]
        // 22,5/23,11     -> [22,5/23,11, 2, ,5, 5, /, 3, ,11, 1]
        
        def matches003 = (str =~ /^(\d){2}(,(\d)+)?(\/|-)(\d){2}(,(\d)+)?$/)
        if(matches003){
            def tmp1 = str.split(matches003[0][4])

            if(dateType.equals(Normalizer.IS_START_DATE)){
                if(tmp1[0].contains(",")){
                    def tmp2 = tmp1[0].split(",")
                    return ['20' + tmp2[0], tmp2[1]]
                }
                return ['20' + tmp1[0], null]
            }
            else if(dateType.equals(Normalizer.IS_END_DATE)){
                if(tmp1[1].contains(",")){
                    def tmp2 = tmp1[1].split(",")
                    return ['20' + tmp2[0], tmp2[1]]
                }
                return ['20' + tmp1[1], null]
            }
            return [null, null]
        }
        

        // 2010,2
        def matches02 = (str =~ /^(\d){4},(\d)+$/)
        if(matches02){
            def tmp = str.split(',')
            return [tmp[0], tmp[1]]
        }
        
        // 2010
        def matches03 = (str =~ /^(\d){4}$/)
        if(matches03){
            return [str, null]
        }
        
        // 05
        def matches04 = (str =~ /^(\d){2}$/)
        if(matches04){
            return ['20' + str, null]
        }
        
        return ['', null]
    }
    
    static String parseCoverageVolume(String str) {       
        if(!str)
            return str
        
        str = Normalizer.removeText(str) 
        str = str.replaceAll(/\s+/,'').trim()
        
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
            str = ''
        }

        str
    }
    
    static String removeText(String str){       
        if(!str)
            return str
            
        str = str.replace('Vol.', '').replace('Vol', '')
        str = str.replace('Nr.', '').replace('Nr', '')
        str = str.replace('Verlag;', '').replace('Verlag', '')
        str = str.replace('Agentur;', '').replace('Agentur', '')
        str = str.replace('Archivierung;', '').replace('Archivierung', '')
        str = str.replace('Digitalisierung;', '').replace('Digitalisierung', '')
        
        str
    }
}
