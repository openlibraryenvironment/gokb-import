package de.hbznrw.ygor.iet.export

import groovy.util.logging.Log4j

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
            
            str = Normalizer.parseDate(str, dateType)
            str = str.trim()
            
            if(str.size() == 4) {
                if(dateType.equals(Normalizer.IS_START_DATE)){
                    str += "-01-01 00:00:00.000"
                }
                else if(dateType.equals(Normalizer.IS_END_DATE)){
                    str += "-12-31 23:59:59.000"
                }
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
    
    static String parseDate(String str, Object dateType) {
        
        if(!str)
            return ''

        str = Normalizer.removeText(str)
        str = str.replaceAll(/\s+/,'').trim()
        
        // 22.2010/11 or 22.2011-12 -> [22.2010/11, 2, 0, /, 1]
        def matches8 = (str =~ /^(\d)+[.](\d){4}(\/|-)(\d){2}$/)
        if(matches8){
            str = str.split("[.]")[1]
            if(dateType.equals(Normalizer.IS_START_DATE)){
                str = str.split(matches8[0][3])[0]
            }
            else if(dateType.equals(Normalizer.IS_END_DATE)){
                str = str.take(2) + str.split(matches8[0][3])[1]
            }
         
        }
        // 22.2010/11- -> [22.2010/11-, 2, 0, 1]
        def matches10 = (str =~ /^(\d)+[.](\d){4}\/(\d){2}-$/)
        if(matches10){
            str = str.split("[.]")[1]
            if(dateType.equals(Normalizer.IS_START_DATE)){
                str = str.split("/")[0]
            }
            else if(dateType.equals(Normalizer.IS_END_DATE)){
                str = ''
            }
        }
        
        // 4.2010,2 -> [4.2010,2, 4, 0, 2]
        def matches1 = (str =~ /^(\d)+[.](\d){4},(\d)+$/)
        if(matches1){
            str = str.split("[.]")[1]
            str = str.split(',')[0]
            return str
        }
        
        // 4.2010 or 5.2011- -> [4.2010, 4, 0]
        def matches2 = (str =~ /^(\d)+[.](\d){4}-?$/)
        if(matches2){
            if(str.contains("-")){
                if(dateType.equals(Normalizer.IS_START_DATE)){
                    str = str.split("-")[0]
                    str = str.split("[.]")[1]
                }
                else {
                    str = ''
                }
            }
            else {
                str = str.split("[.]")[1]
            }
            return str
        }
         
        // 2010,2 -> [2010,2, 0, 2]
        def matches3 = (str =~ /^(\d){4},(\d)+$/)
        if(matches3){
            str = str.split(',')[0]
            return str
        }
         
        // 2010-2011 or 2011/2012 -> [2010/2011, 0, -, 1]
        def matches5 = (str =~ /^(\d){4}(\/|-)(\d){4}$/)
        if(matches5){
            if(dateType.equals(Normalizer.IS_START_DATE)){
                str = str.split(matches5[0][2])[0]
            }
            else if(dateType.equals(Normalizer.IS_END_DATE)){
                str = str.split(matches5[0][2])[1]
            }
            return str
        }
        
        // 2010/11 or 2011-12 -> [2010-11, 0, /, 1]
        def matches4 = (str =~ /^(\d){4}(\/|-)(\d){2}$/)
        if(matches4){
            if(dateType.equals(Normalizer.IS_START_DATE)){
                str = str.split(matches4[0][2])[0]
            }
            else if(dateType.equals(Normalizer.IS_END_DATE)){
                str = str.take(2) + str.split(matches4[0][2])[1]
            }
            return str
        }
        
        // 10-11 or 11/12 -> [10-11, 0, -, 1]
        def matches6 = (str =~ /^(\d){2}(\/|-)(\d){2}$/)
        if(matches6){
            if(dateType.equals(Normalizer.IS_START_DATE)){
                str = '20' + str.split(matches6[0][2])[0]
            }
            else if(dateType.equals(Normalizer.IS_END_DATE)){
                str = '20' + str.split(matches6[0][2])[1]
            }
            return str
        }
        
        // 2010- -> [2010-, 0]
        def matches9 = (str =~ /^(\d){4}-$/)
        if(matches9){
            if(dateType.equals(Normalizer.IS_START_DATE)){
                str = str.split("-")[0]
            }
            else {
                str = ""
            }
            return str
        }
        
        // 00 or 17 -> [00, 0]
        def matches7 = (str =~ /^(\d){2}$/)
        if(matches7){
            str = '20' + str
            return str
        }
        
        if(dateType.equals(Normalizer.IS_START_DATE) && str.startsWith("[")){
            str = str.replaceFirst("\\[", '')
        }
        else if(dateType.equals(Normalizer.IS_END_DATE) && str.endsWith("]")){
            str = str.take(str.length() - 1)
        }
        
        str
    }
    
    static String parseCoverageVolume(String str) {
        
        if(!str)
            return ''
        
        str = Normalizer.removeText(str) 
        str = str.replaceAll(/\s+/,'').trim()
        
        // 4.2010,2 -> [4.2010,2, 4, 0, 2]
        def matches1 = (str =~ /(\d)+[.](\d){4},(\d)+/)
        if(matches1){
            // TODO parseCoverageVolume
            return str
        }
        
        // 4.2010 -> [4.2010, 4, 0]
        def matches2 = (str =~ /(\d)+[.](\d){4}/)
        if(matches2){
            str = str.split("[.]")[0]
            return str
        }
        
        // 2010,2 -> [2010,2, 0, 2]
        def matches3 = (str =~ /(\d){4},(\d)+/)
        if(matches3){
            str = str.split(',')[1]
            return str
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
