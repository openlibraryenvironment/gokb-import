package de.hbznrw.ygor.iet.export

import groovy.util.logging.Log4j
import java.time.LocalDate
import org.springframework.util.StringUtils

import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.export.structure.TitleStruct
import de.hbznrw.ygor.iet.bridge.*

@Log4j
class Normalizer {

    final static IS_START_DATE  = "IS_START_DATE"
    final static IS_END_DATE    = "IS_END_DATE"
    
    
    
    /**
     * Contatenates list elements with "|" as delimiter
     * 
     * @param list
     * @return  
     */
    static String normString(ArrayList list) {
        list ? Normalizer.normString(list.join("|")) : ""
    }
    
    /**
     * Removes double spaces. Removes leading and ending spaces
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
     * Concatenates list elements with "|" as delimiter
     *
     * @param type
     * @param list
     * @return
     */
    static String normIdentifier(ArrayList list, Object type) {
        def result = []
        list.each{ e ->
            result << Normalizer.normIdentifier(e.toString(), type)
        }
        result ? result.join("|") : ""
    }
    
    /**
     * eissn/pissn: "12345678"  -> "1234-5678"
     * eissn/pissn: "1234567"   -> "1234567"
     * eissn/pissn: "123456789" -> "123456789"
     * zdb:         "12345"     -> "1234-5"
     * zdb:         "12345678"  -> "1234567-8"
     * 
     * @param type
     * @param str
     * @return
     */
    static String normIdentifier(String str, Object type) {
        if(!str)
            return str
        
        str = Normalizer.normString(str)
        str = str.replaceAll(/[\/-]+/,"")
        
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

        str
    }

    /**
     * 
     * @param list
     * @param dateType DataMapper.IS_START_DATE|DataMapper.IS_END_DATE
     * @return
     */
    static String normDate(ArrayList list, Object dateType) {
        def result = []
        list.each{ e ->
            result << Normalizer.normDate(e, dateType)
        }
        result ? result.join("|") : ""
    }
    
    /**
     * - "2008"
     * - "2005/06"
     * - "2002/2003"
     * - "2005-"
     * - "2005-06"
     * - "2002-2003"
     * - "20.2008 - 30.2010"
     * 
     * @param str
     * @param dateType DataMapper.IS_START_DATE|DataMapper.IS_END_DATE
     * @return "YYYY-01-01 00:00:00.000"|"YYYY-12-31 00:00:00.000"
     */
    static String normDate(String str, Object dateType) {
        
        str = Normalizer.normString(str)
        
        if(str){
            if(str.contains("-")){
                def tmp = str.split("-")
                
                if(dateType.equals(Normalizer.IS_START_DATE)){
                    if(tmp.size() > 1){
                        str = tmp[0]
                    }
                }
                else if(dateType.equals(Normalizer.IS_END_DATE)){
                    if(tmp.size() > 1){
                        str = tmp[1]
                    }
                }  
            }
            
            def strList = Normalizer.parseDate(str, dateType)
            
            if(4 == strList[0].size()) {
                str = strList[0]
                
                if(strList[1]){
                    LocalDate date = LocalDate.of(Integer.parseInt(strList[0]), Integer.parseInt(strList[1]), 1)
                    def tmp1 = String.format('%02d', Integer.parseInt(strList[1]))
                    def tmp2 = String.format('%02d', date.lengthOfMonth())
                    
                    if(dateType.equals(Normalizer.IS_START_DATE)){
                        str += ("-" + tmp1 + "-01 00:00:00.000")
                    }
                    else if(dateType.equals(Normalizer.IS_END_DATE)){
                        str += ("-" + tmp1 + "-" + tmp2 + " 23:59:59.000")
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
        }
        
        str
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
     * Concatenates list elements with "|" as delimiter
     *
     * @param list
     * @return
     */
    static String normURL(ArrayList list) {
        def result = []
        list.each{ e ->
            result << Normalizer.normURL(e)
        }
        result ? result.join("|") : ""
    }
    
    /**
     * Returns an url authority
     * 
     * @param str
     * @return
     */
    static String normURL(String str) {    
        //str = Normalizer.normString(str)
        
        try {
            if(str && str.indexOf('http://') == -1 && str.indexOf('https://') == -1){
                str = 'http://' + str
            }
            
            def url = new URL(str)
            if(url) {
                str = url.getAuthority()
            }
        } catch(Exception e) {
            log.error(e.getMessage())
            log.error(e.getStackTrace())
        }
        
        str
    }
    
    /**
     * Concatenates list elements with "|" as delimiter.
     * Eliminates null and empty values
     *
     * @param list
     * @param nominalPlatform
     * @return
     */
    static String normTippURL(ArrayList list, String nominalPlatform) {
        def result = []
        list.each{ e ->
            result << Normalizer.normTippURL(e, nominalPlatform)
        }
        result ? result.minus(null).minus("").join("|") : ""
    }
    
    /**
     * Returns given url if it matches to nominal platform url
     * 
     * @param str
     * @param nominalPlatform
     * @return
     */
    static String normTippURL(String str, String nominalPlatform) {
        
        def npTmp  = Normalizer.normURL(nominalPlatform)
        if(!npTmp)
            return str
            
        def strTmp = Normalizer.normURL(str)
        if(strTmp && strTmp.indexOf(npTmp) == 0)
            return str
        
        ""
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
            return ''
        
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
            return ''
            
        str = str.replace('Vol.', '').replace('Vol', '')
        str = str.replace('Nr.', '').replace('Nr', '')
        str = str.replace('Verlag;', '').replace('Verlag', '')
        str = str.replace('Agentur;', '').replace('Agentur', '')
        str = str.replace('Archivierung;', '').replace('Archivierung', '')
        str = str.replace('Digitalisierung;', '').replace('Digitalisierung', '')
        
        str
    }
}
