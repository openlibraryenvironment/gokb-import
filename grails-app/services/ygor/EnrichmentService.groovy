package ygor

import de.hbznrw.ygor.iet.export.*
import de.hbznrw.ygor.iet.export.structure.*
import groovyx.net.http.HTTPBuilder
import javax.servlet.http.HttpSession
import groovyx.net.http.*
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.codehaus.groovy.grails.web.util.WebUtils
import de.hbznrw.ygor.tools.*

class EnrichmentService {
    
    def grailsApplication
    PlatformService platformService
    
    void addFileAndFormat(CommonsMultipartFile file, String delimiter, String quotes) {
        
        def en = new Enrichment(getSessionFolder(), file.originalFilename)
        en.setStatus(Enrichment.ProcessingState.PREPARE)
        
        def tmp = [:]
        tmp << ['delimiter': delimiter]
        tmp << ['quotes':    quotes]
        
        def formats = getSessionFormats()
        formats << ["${en.originHash}":tmp]
        
        def enrichments = getSessionEnrichments()
        enrichments << ["${en.originHash}": en]
        
        file.transferTo(new File(en.originPathName))
    }
    
    File getFile(Enrichment enrichment, Enrichment.FileType type) {
        
        enrichment.getFile(type)
    }
    
    void deleteFileAndFormat(Enrichment enrichment) {
        
        if(enrichment) {
            def origin = enrichment.getFile(Enrichment.FileType.ORIGIN)
            if(origin)
                origin.delete()
            getSessionEnrichments()?.remove("${enrichment.originHash}")
            getSessionFormats()?.remove("${enrichment.originHash}")
        }
    }
    
    void prepareFile(Enrichment enrichment, Map pm){
        
        def ph = enrichment.dataContainer.pkg.packageHeader
        
        ph.v.name.v          = new Pod(pm['pkgTitle'][0])
        ph.v.variantNames   << new Pod(pm['pkgVariantName'][0])
        if("" != pm['pkgCuratoryGroup1'][0].trim()){
            ph.v.curatoryGroups << new Pod(pm['pkgCuratoryGroup1'][0])
        }
        if("" != pm['pkgCuratoryGroup2'][0].trim()){
            ph.v.curatoryGroups << new Pod(pm['pkgCuratoryGroup2'][0])
        }
        
        def map = platformService.getMap()
        def pkgNominal = map.find{it.key == pm['pkgNominal'][0]}
        if(pkgNominal){
            ph.v.nominalPlatform.v = pkgNominal.value
            ph.v.nominalPlatform.m = Validator.isValidURL(ph.v.nominalPlatform.v)
            ph.v.nominalProvider.v = pkgNominal.key
        }

        enrichment.setStatus(Enrichment.ProcessingState.UNTOUCHED)
    }
    
    void stopProcessing(Enrichment enrichment) {

        enrichment.thread.isRunning = false
    }
    
    List sendFile(Enrichment enrichment, Object fileType) {
        
        def result = []
        def json
        
        if(fileType == Enrichment.FileType.JSON_PACKAGE_ONLY){
            json = enrichment.getFile(Enrichment.FileType.JSON_PACKAGE_ONLY)
            
            result << exportFileToGOKb(enrichment, json, grailsApplication.config.gokbApi.xrPackageUri)
        }
        else if(fileType == Enrichment.FileType.JSON_TITLES_ONLY){
            json = enrichment.getFile(Enrichment.FileType.JSON_TITLES_ONLY)
            
            result << exportFileToGOKb(enrichment, json, grailsApplication.config.gokbApi.xrTitleUri)
        }

        result
    }
    
    private Map exportFileToGOKb(Enrichment enrichment, Object json, String url){
        
        log.info("exportFile: " + enrichment.resultHash + " -> " + url)

        def http = new HTTPBuilder(url)
        http.auth.basic grailsApplication.config.gokbApi.user, grailsApplication.config.gokbApi.pwd
        
        http.request(Method.POST, ContentType.JSON) { req ->
            headers.'User-Agent' = 'ygor'

            body = json.getText()
            response.success = { resp, html ->
                log.info("server response: ${resp.statusLine}")
                log.debug("server:          ${resp.headers.'Server'}")
                log.debug("content length:  ${resp.headers.'Content-Length'}")
                if(resp.status < 400){
                    return ['warning':html]
                }
                else {
                    return ['info':html]
                }
            }
            response.failure = { resp ->
                log.error("server response: ${resp.statusLine}")
                return ['error':resp.statusLine]
            }
        }
    }
    
    def getSessionEnrichments(){
        HttpSession session = SessionToolkit.getSession()
        if(!session.enrichments){
            session.enrichments = [:]
        }
        session.enrichments
    }
    
    def getSessionFormats(){
        HttpSession session = SessionToolkit.getSession()
        if(!session.formats){
            session.formats = [:]
        }
        session.formats
    }
    
    /**
     * Return session depending directory for file upload.
     * Creates if not existing.
     */

    File getSessionFolder() {
        
        def session = WebUtils.retrieveGrailsWebRequest().session
        def path = grailsApplication.config.ygor.uploadLocation + File.separator + session.id
        def sessionFolder = new File(path)
        if(!sessionFolder.exists()) {
            sessionFolder.mkdirs()
        }
        sessionFolder
    }
}
