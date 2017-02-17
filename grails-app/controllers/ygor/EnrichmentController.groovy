package ygor

// ignore strange errors ..
import grails.util.Environment
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.POST
import org.apache.commons.io.IOUtils
import de.hbznrw.ygor.iet.export.*
import de.hbznrw.ygor.iet.export.structure.Pod
import de.hbznrw.ygor.iet.export.structure.PackageStruct


class EnrichmentController {

    static scope = "session"

    EnrichmentService enrichmentService
    PlatformService   platformService
    
    def documents = [:]

    def index = { 
        redirect(action:'process')   
    }
    
    def process = {
        render(
            view:'process',
            model:[documents:documents, platformService:platformService, currentView:'process']
            )
    }
    
    def json = {
        render(
            view:'json',
            model:[documents:documents, currentView:'json']
            )
    }
    
    def howto = {
        render(
            view:'howto',
            model:[currentView:'howto']
            )
    }

    def about = {
        render(
            view:'about',
            model:[currentView:'about']
            )
    }

    def contact = {
        render(
            view:'contact',
            model:[currentView:'contact']
            )
    }
    
    def uploadFile = {
        
        def file = request.getFile('uploadFile')
        if (file.empty) {
            flash.info    = null
            flash.warning = null
            flash.error   = 'Sie müssen eine gültige Datei auswählen.'
            render(view:'process', model:[
                documents:documents,
                currentView: 'process'
                ]
            )
            return
        }
        enrichmentService.addFile(file, documents)

        redirect(action:'process')
    }

    def prepareFile = {
        
        enrichmentService.prepareFile(getEnrichment(), request.parameterMap)
        redirect(action:'process')
    }
    
    def processFile = {
        
        def pmIndex     = request.parameterMap['processIndex'][0]
        def pmIndexType = request.parameterMap['processIndexType'][0]
        def pmOptions   = request.parameterMap['processOption']
        
        if(!pmIndex) {
            flash.info    = null
            flash.warning = 'Geben Sie einen gültigen Index an.'
            flash.error   = null
        }
        else if(!pmOptions) {
            flash.info    = null
            flash.warning = 'Wählen Sie mindestens eine Anreicherungsoption.'
            flash.error   = null
        }
        else {
            def en = getEnrichment()
            if(en.status != Enrichment.ProcessingState.WORKING) {
                flash.info    = 'Bearbeitung gestartet.'
                flash.warning = null
                flash.error   = null

                def options = [
                    'indexOfKey':   pmIndex.toInteger() - 1,
                    'typeOfKey':    pmIndexType,
                    'options':      pmOptions,
                    'ygorVersion':  grailsApplication.config.ygor.version,
                    'ygorType':     grailsApplication.config.ygor.type
                    ]
                    
                en.process(options)
            }
        }
        render(
            view:'process',
            model:[
                documents:   documents, 
                currentView: 'process',
                pIndex:      pmIndex,
                pIndexType:  pmIndexType,
                pOptions:    pmOptions,
                ]
            )
    }
    
    def stopProcessingFile = {
        
        enrichmentService.stopProcessing(getEnrichment())
        deleteFile()
    }
    
    def deleteFile = {

        enrichmentService.deleteFile(getEnrichment(), documents)    
        render(
            view:'process',
            model:[documents:documents, currentView:'process']
            )
    }
    
    def downloadPackageFile = {

        def en     = getEnrichment()
        def result = enrichmentService.getFile(en, Enrichment.FileType.JSON_PACKAGE_ONLY)
        render(
            file:result,
            fileName:"${en.resultName}.package.json"
            )
    }
    
    def downloadTitlesFile = {
        
        def en     = getEnrichment()
        def result = enrichmentService.getFile(en, Enrichment.FileType.JSON_TITLES_ONLY)
        render(
            file:result,
            fileName:"${en.resultName}.titles.json"
            )
    }
    
    def downloadDebugFile = {
        
        def en     = getEnrichment()
        def result = enrichmentService.getFile(en, Enrichment.FileType.JSON_DEBUG)
        render(
            file:result,
            fileName:"${en.resultName}.debug.json"
            )
    }
    
    def downloadRawFile = {
        
        def en     = getEnrichment()
        def result = enrichmentService.getFile(en, Enrichment.FileType.JSON_OO_RAW)
        render(
            file:result,
            fileName:"${en.resultName}.raw.json"
            )
    }
    
    def exportFile = {
        
        def status = enrichmentService.exportFile(enrichment)
        
        status.each{ st ->
            if(st.get('info'))
                flash.info = st.get('info')
            if(st.get('warning'))
                flash.warning = st.get('warning')
            if(st.get('error'))
                flash.error = st.get('error')
        }

        process()
    }
    
    def showStats = {
        
        def en   = getEnrichment()
        def json = null
        
        if(en){
            json = Transformer.getSimpleJSON(en.dataContainer, Enrichment.FileType.JSON_DEBUG, Transformer.NO_PRETTY_PRINT)
        }

        render(view:'statistics', model:[
            documents:documents,
            json:json,
            currentView: 'process'
            ]
        )
    }
    
    def ajaxGetStatus = {
        
        def en = getEnrichment()
        render '{"status":"' + en.getStatus() + '", "progress":' + en.getProgress().round() + '}'
    }

    Enrichment getEnrichment() {
        
        def hash = (String) request.parameterMap['originHash'][0]
        documents.get("${hash}")
    }
}
