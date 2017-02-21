package ygor

import de.hbznrw.ygor.iet.export.*
import de.hbznrw.ygor.iet.export.structure.*
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.POST
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.codehaus.groovy.grails.web.util.WebUtils

import de.hbznrw.ygor.tools.*

class EnrichmentService {
    
    def grailsApplication
    PlatformService platformService
    
    void addFile(CommonsMultipartFile file, HashMap documents) {
        
        def en = new Enrichment(getSessionFolder(), file.originalFilename)
        en.setStatus(Enrichment.ProcessingState.PREPARE)
        documents << ["${en.originHash}":en]
        
        file.transferTo(new File(en.originPathName))
    }

    File getFile(Enrichment enrichment, Enrichment.FileType type) {
        
        enrichment.getFile(type)
    }
    
    void deleteFile(Enrichment enrichment, HashMap documents) {

        if(enrichment) {
            def origin = enrichment.getFile(Enrichment.FileType.ORIGIN)
            if(origin)
                origin.delete()
            documents.remove("${enrichment.originHash}")
        }
    }

    void prepareFile(Enrichment enrichment, HashMap pm){
        
        def ph = enrichment.dataContainer.pkg.packageHeader
        
        if(!pm['ignorePkgData']) {
            
            ph.v.name.v = new Pod(pm['pkgTitle'][0])
            
            def map = platformService.getMap()
            def pkgNominal = map.find{it.key == pm['pkgNominal'][0]}
            if(pkgNominal){
                ph.v.nominalPlatform.v = pkgNominal.value
                ph.v.nominalPlatform.m = Validator.isValidURL(ph.v.nominalPlatform.v)
                ph.v.nominalProvider.v = pkgNominal.key
            }
            
            ph.v.variantNames << new Pod(pm['pkgVariantName'][0])
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
        
        http.request(POST) { req ->
            headers.'User-Agent' = 'ygor'
            req.getParams().setParameter("http.socket.timeout", new Integer(5000))
            
            MultipartEntity entity = new MultipartEntity()
            entity.addPart("file", new FileBody(json))
            entity.addPart("info", new StringBody("greetings from ygor"))
            req.setEntity(entity)
            
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
