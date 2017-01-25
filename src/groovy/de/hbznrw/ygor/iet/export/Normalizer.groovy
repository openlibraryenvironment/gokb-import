package de.hbznrw.ygor.iet.export

import org.springframework.util.StringUtils

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.export.structure.PackageHeader
import de.hbznrw.ygor.iet.export.structure.Tipp
import de.hbznrw.ygor.iet.export.structure.Title
import de.hbznrw.ygor.iet.export.structure.TitleStruct
import de.hbznrw.ygor.iet.bridge.*

class Normalizer {

    final static IS_START_DATE  = 1000
    final static IS_END_DATE    = 1001
    
    
    
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
     * Following matches will be processed correctly
     * 
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
            if(str.contains(";")){
                def tmp = str.split(";")
                if(tmp.size() == 1)
                    str = tmp[0]
                else if(tmp.size() == 2)
                    str = tmp[1]
                else
                    str = tmp.join(' ')
            }
            
            if(str.contains("/")){
                def tmp = str.split("/")
                
                if(dateType.equals(Normalizer.IS_START_DATE)){
                    str = tmp[0]
                }
                else if(dateType.equals(Normalizer.IS_END_DATE)){
                    if(tmp[1].length()<tmp[0].length() && tmp[0].length() == 4 && tmp[1].length() == 2){
                        str = tmp[0].take(2) + tmp[1]
                    }
                    else {
                        str = tmp[1]
                    }
                }
            }
            else if(str.contains("-")){
                def tmp = str.split("-")
                
                if(dateType.equals(Normalizer.IS_START_DATE)){
                    str = tmp[0]
                }
                else if(dateType.equals(Normalizer.IS_END_DATE)){
                    if(tmp.length == 2) {
                        if(tmp[1].length()<tmp[0].length() && tmp[0].length() == 4 && tmp[1].length() == 2){
                            str = tmp[0].take(2) + tmp[1]
                        }
                        else {
                            str = tmp[1]
                        }
                    }
                    else {
                        return ''
                    }
                }
                
                if(1 == StringUtils.countOccurrencesOf(str, ".")){
                    def tmp2 = str.split("\\.")
                    str = Normalizer.normString(tmp2[1])
                }
            }
        
            if(str != "") {
                if(['EZB', 'Verlag', 'Agentur'].contains(str)){
                    return str
                }
                
                str = str.trim()
                
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
     
        if(str && str.contains(";")){
            def tmp = str.split(";")
            str = tmp[1]
        }
        if(str && str.contains("-")){
            def tmp = str.split("-")
            
            if(dateType.equals(Normalizer.IS_START_DATE)){
                str = tmp[0]
            }
            else if(dateType.equals(Normalizer.IS_END_DATE)){
                if(tmp.size() > 1)
                    str = tmp[1]
                else
                    str = ""
            }
        }
        if(1 == StringUtils.countOccurrencesOf(str, ".")){
            def tmp2 = str.split("\\.")
            str = tmp2[0]
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
        } catch(Exception e) {}
        
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
}
