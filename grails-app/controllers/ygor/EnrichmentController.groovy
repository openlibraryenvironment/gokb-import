package ygor

import de.hbznrw.ygor.iet.export.*

class EnrichmentController {

    static scope = "session"

    EnrichmentService enrichmentService
    PlatformService   platformService

    def index = { 
        redirect(action:'process')   
    }
    
    def process = {
        render(
            view:'process',
            model:[
                enrichments:     enrichmentService.getSessionEnrichments(), 
                platformService: platformService, 
                currentView:    'process'
                ]
            )
    }
    
    def json = {
        render(
            view:'json',
            model:[
                enrichments: enrichmentService.getSessionEnrichments(), 
                currentView: 'json'
                ]
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

    def config = {
        render(
            view:'config',
            model:[currentView:'config']
            )
    }
    
    def contact = {
        render(
            view:'contact',
            model:[currentView:'contact']
            )
    }
    
    def uploadFile = {

        def foDelimiter = request.parameterMap['formatDelimiter'][0]
        def foQuotes    = request.parameterMap['formatQuotes'][0]
        
        def file = request.getFile('uploadFile')
        if (file.empty) {
            flash.info    = null
            flash.warning = null
            flash.error   = 'Sie müssen eine gültige Datei auswählen.'
            render(view:'process', 
                model:[
                    enrichments: enrichmentService.getSessionEnrichments(),
                    currentView: 'process'
                ]
            )
            return
        }
        enrichmentService.addFileAndFormat(file, foDelimiter, foQuotes)

        redirect(action:'process')
    }

    def prepareFile = {
        
        enrichmentService.prepareFile(getCurrentEnrichment(), request.parameterMap)
        redirect(action:'process')
    }
    
    def processFile = {
        
        def pmIndexType = request.parameterMap['processIndexType'][0]
        def pmOptions   = request.parameterMap['processOption']

        if(!pmOptions) {
            flash.info    = null
            flash.warning = 'Wählen Sie mindestens eine Anreicherungsoption.'
            flash.error   = null
        }
        else {
            def en = getCurrentEnrichment()
            if(en.status != Enrichment.ProcessingState.WORKING) {
                flash.info    = 'Bearbeitung gestartet.'
                flash.warning = null
                flash.error   = null

                def format = getCurrentFormat()
                def options = [
                    'typeOfKey':    pmIndexType,
                    'options':      pmOptions,
                    'delimiter':    format.get('delimiter'),
                    'quotes':       format.get('quotes'),
                    'ygorVersion':  grailsApplication.config.ygor.version,
                    'ygorType':     grailsApplication.config.ygor.type
                    ]
                    
                en.process(options)
            }
        }
        render(
            view:'process',
            model:[
                enrichments: enrichmentService.getSessionEnrichments(), 
                currentView: 'process',
                pIndexType:  pmIndexType,
                pOptions:    pmOptions,
                ]
            )
    }
    
    def stopProcessingFile = {
        
        enrichmentService.stopProcessing(getCurrentEnrichment())
        deleteFile()
    }
    
    def deleteFile = {

        enrichmentService.deleteFileAndFormat(getCurrentEnrichment())    
        render(
            view:'process',
            model:[
                enrichments: enrichmentService.getSessionEnrichments(), 
                currentView: 'process'
                ]
            )
    }
    
    def downloadPackageFile = {

        def en = getCurrentEnrichment()
        if(en){
            def result = enrichmentService.getFile(en, Enrichment.FileType.JSON_PACKAGE_ONLY)
            render(file:result, fileName:"${en.resultName}.package.json")
        }
        else {
            noValidEnrichment()
        }
    }
    
    def downloadTitlesFile = {
        
        def en = getCurrentEnrichment()
        if(en){
            def result = enrichmentService.getFile(en, Enrichment.FileType.JSON_TITLES_ONLY)
            render(file:result, fileName:"${en.resultName}.titles.json")
        }
        else {
            noValidEnrichment()
        }
    }
    
    def downloadDebugFile = {
        
        def en = getCurrentEnrichment()
        if(en){
            def result = enrichmentService.getFile(en, Enrichment.FileType.JSON_DEBUG)
            render(file:result, fileName:"${en.resultName}.debug.json")
        }
        else {
            noValidEnrichment()
        }
    }
    
    def downloadRawFile = {
        
        def en = getCurrentEnrichment()
        if(en){
            def result = enrichmentService.getFile(en, Enrichment.FileType.JSON_OO_RAW)
            render(file:result, fileName:"${en.resultName}.raw.json")
        }
        else {
            noValidEnrichment()
        }
    }
    
    def sendPackageFile = {
        
        def status = enrichmentService.sendFile(currentEnrichment, Enrichment.FileType.JSON_PACKAGE_ONLY)
        
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
    
    def sendTitlesFile = {
        
        def status = enrichmentService.sendFile(currentEnrichment, Enrichment.FileType.JSON_TITLES_ONLY)
        
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

    def ajaxGetStatus = {
        
        def en = getCurrentEnrichment()
        render '{"status":"' + en.getStatus() + '", "progress":' + en.getProgress().round() + '}'
    }

    Enrichment getCurrentEnrichment() {
        
        def hash = (String) request.parameterMap['originHash'][0]
        enrichmentService.getSessionEnrichments().get("${hash}")
    }
    
    HashMap getCurrentFormat() {
        
        def hash = (String) request.parameterMap['originHash'][0]
        enrichmentService.getSessionFormats().get("${hash}")
    }
    
    void noValidEnrichment() {
        
        flash.info    = null
        flash.warning = 'Es existiert keine Datei. Vielleicht ist Ihre Session abgelaufen?'
        flash.error   = null
        
        redirect(action:'process')
    }
}
