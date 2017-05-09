package de.hbznrw.ygor.tools

import groovy.util.logging.Log4j
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import com.google.common.net.InternetDomainName

@Log4j
class UrlToolkit {

    static final HARD_CHECK                 = 'HARD_CHECK'
    static final ONLY_HIGHEST_LEVEL_DOMAIN  = 'ONLY_HIGHEST_LEVEL_DOMAIN'
    
    static def getURL(String str){
        UrlToolkit.buildUrl(str)
    }
    
    static def getURLWithProtocol(String str){
        
        def url = UrlToolkit.buildUrl(str)
        if(url) {
            return url
        }
        else {
            return UrlToolkit.buildUrl('http://' + str)
        }
    }
    
    static def getURLAuthority(String str){

        def url = UrlToolkit.getURLWithProtocol(str)
        if(url) {
            return url.getAuthority()
        }            
        null
    }
    
    static def getURLAuthorityWithProtocol(String str){
        
        def url = UrlToolkit.getURLWithProtocol(str)
        if(url) {
            return url.getProtocol() + '://' + url.getAuthority()
            
        }
        null
    }

    
    static def buildUrl(String str){
        def url
        try {
            url = new URL(str)
        } catch(Exception e) {
            log.error(e.getMessage())
            log.error(e.getStackTrace())
        }
        url
    }
    
    static sortOutUrl(String url, String nominal, Object checkConditions){
    
        def u = UrlToolkit.getURLWithProtocol(url)
        def n = UrlToolkit.getURLWithProtocol(nominal)
        
        if(checkConditions == UrlToolkit.ONLY_HIGHEST_LEVEL_DOMAIN){
            u = InternetDomainName.from(u.getAuthority()).topPrivateDomain().name
            n = InternetDomainName.from(n.getAuthority()).topPrivateDomain().name
        }
 
        if(u && n && u.toString().indexOf(n.toString()) == 0){
            return url
        }
        null
    }
    
    static sortOutUrl(ArrayList urls, String nominal, Object checkConditions){
        
        def result = []
        def n = UrlToolkit.getURLWithProtocol(nominal)

        urls.each{ e ->
            def u = UrlToolkit.getURLWithProtocol(e)
            
            result << UrlToolkit.sortOutUrl(e, nominal, checkConditions)
        }
        result.minus(null).minus("").join("|")
    }
}
