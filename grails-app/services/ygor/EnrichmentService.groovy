package ygor

import java.io.File
import de.hbznrw.ygor.iet.export.*
import de.hbznrw.ygor.iet.export.structure.*
import groovyx.net.http.HTTPBuilder
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.codehaus.groovy.grails.web.util.WebUtils

class EnrichmentService {
    
    def grailsApplication

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
            
            def preset = PackageStruct.getPackageHeaderNominalPlatformPreset()
            def pkgNominal = preset.find{it.key == pm['pkgNominal'][0]}
            if(pkgNominal){
                ph.v.nominalPlatform.v = pkgNominal.value
                ph.v.nominalPlatform.m = Validator.isValidURL(ph.v.nominalPlatform.v)
                ph.v.nominalProvider.v = pkgNominal.key
            }
            
            def vn =  PackageStruct.getNewPackageHeaderVariantName()
            vn.variantName.v = pm['pkgVariantName'][0]
            ph.v.variantNames << vn
        }
        
        enrichment.setStatus(Enrichment.ProcessingState.UNTOUCHED)
    }
    
    void stopProcessing(Enrichment enrichment) {

        enrichment.thread.isRunning = false
    }
    
    void exportFile(Enrichment enrichment) {
        
        // TODO split file
        //return only json.package
        //return only json.titles

        def rawFile = enrichment.getFile(Enrichment.FileType.JSON)
        def result  = Mapper.clearUp(rawFile)
        def http    = new HTTPBuilder(grailsApplication.config.gokbApi.xrTitleUri)
        
        http.auth.basic grailsApplication.config.gokbApi.user, grailsApplication.config.gokbApi.pwd

        println "EC.exportFile(" + en.resultHash + ") -> " + grailsApplication.config.gokbApi.xrTitleUri
        
        http.request(POST) { req ->
            headers.'User-Agent' = 'ygor'
            req.getParams().setParameter("http.socket.timeout", new Integer(5000))
            
            MultipartEntity entity = new MultipartEntity()
            entity.addPart("file", new FileBody(result))
            entity.addPart("info", new StringBody("greetings from ygor"))
            req.setEntity(entity)
            
            response.success = { resp, html ->
                println "server response: ${resp.statusLine}"
                println "server:          ${resp.headers.'Server'}"
                println "content length:  ${resp.headers.'Content-Length'}"
                if(resp.status < 400){
                    flash.warning = html
                }
                else {
                    flash.info = html
                }
            }
            response.failure = { resp ->
                println "server response: ${resp.statusLine}"
                flash.error = resp.statusLine
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
