package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.export.structure.Tipp
import de.hbznrw.ygor.iet.export.structure.Title
import de.hbznrw.ygor.iet.export.structure.TitleStruct
import de.hbznrw.ygor.iet.bridge.*

class DataNormalizer {

    final static IS_START_DATE  = 1000
    final static IS_END_DATE    = 1001
    
    
    
    /**
     * Contatenates list elements with "|" as delimiter
     * 
     * @param list
     * @return  
     */
    static String normString(ArrayList list) {
        list ? DataNormalizer.normString(list.join("|")) : ""
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
        
        str = str.replaceAll("  "," ").replaceAll(" : ",": ")
        str.trim()
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
            println e
            result << DataNormalizer.normIdentifier(e.toString(), type)
        }
        result ? result.join("|") : ""
    }
    
    /**
     * eissn/pissn: "1234567"   -> "1234-5678"
     * eissn/pissn: "12345"     -> "123-456"
     *
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
        
        str = DataNormalizer.normString(str)
        str = str.replaceAll("-","").replaceAll("/","")
        
        if(type.equals(TitleStruct.EISSN) || type.equals(TitleStruct.PISSN)){
            str = new StringBuilder(str).insert(Math.abs(str.length()/2).toInteger(), "-").toString();
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
            result << DataNormalizer.normDate(e, dateType)
        }
        result ? result.join("|") : ""
    }
    
    /**
     * Following matches will be processed correctly
     * 
     * - "2008"
     * - "2005/06"
     * - "2002/2003"
     * 
     * @param str
     * @param dateType DataMapper.IS_START_DATE|DataMapper.IS_END_DATE
     * @return "YYYY-01-01 00:00:00.000"|"YYYY-12-31 00:00:00.000"
     */
    static String normDate(String str, Object dateType) {
        str = DataNormalizer.normString(str)
     
        if(str.contains("/")){
            if(dateType.equals(DataNormalizer.IS_START_DATE)){
                str = str.split("/")[0]
            }
            else if(dateType.equals(DataNormalizer.IS_END_DATE)){
                str = str.split("/")
                if(str[1].length()<str[0].length() && str[0].length() == 4 && str[1].length() == 2){
                    str = str[0].take(2) + str[1]
                }
                else {
                    str = str.split("/")[1]
                }
            }
        }
        
        if(dateType.equals(DataNormalizer.IS_START_DATE)){
            str += "-01-01 00:00:00.000"
        }
        else if(dateType.equals(DataNormalizer.IS_END_DATE)){
            str += "-12-31 23:59:59.000"
        }
        
        str
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
            result << DataNormalizer.normURL(e)
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
        str = DataNormalizer.normString(str)
        
        def url = new URL(str)
        if(url) {
            url.getAuthority()
        }
        else {
            str
        }
    }
}
