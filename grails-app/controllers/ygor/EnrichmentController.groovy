package ygor

import grails.converters.JSON

class EnrichmentController {

    static scope = "session"

    EnrichmentService enrichmentService
    GokbService gokbService

    def index = { 
        redirect(action:'process')   
    }
    
    def process = {

        def gokb_ns = gokbService.getNamespaces()

        render(
            view:'process',
            model:[
                enrichments:     enrichmentService.getSessionEnrichments(), 
                gokbService:     gokbService,
                namespaces:      gokb_ns,
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
        def file        = request.getFile('uploadFile')
        if (file.size < 1 && request.parameterMap.uploadFileLabel != null &&
            request.parameterMap.uploadFileLabel[0] == request.session.lastUpdate.file?.originalFilename){
            // the file form is unpopulated but the previously selected file in unchanged
            file = request.session.lastUpdate.file
        }
        def foDelimiter = request.parameterMap['formatDelimiter'][0]
        def foQuote     = null // = request.parameterMap['formatQuote'][0]
        def foQuoteMode = null // = request.parameterMap['formatQuoteMode'][0]
        def dataTyp     = request.parameterMap['dataTyp'][0]

        if (!request.session.lastUpdate){
            request.session.lastUpdate = [:]
        }
        request.session.lastUpdate.file = file
        request.session.lastUpdate.foDelimiter = foDelimiter
        request.session.lastUpdate.foQuote = foQuote
        request.session.lastUpdate.foQuoteMode = foQuoteMode
        request.session.lastUpdate.dataTyp = dataTyp

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
        enrichmentService.addFileAndFormat(file, foDelimiter, foQuote, foQuoteMode, dataTyp)
        redirect(action:'process')
    }

    def prepareFile = {
        enrichmentService.prepareFile(getCurrentEnrichment(), request.parameterMap)
        request.session.lastUpdate.parameterMap = request.parameterMap
        redirect(action:'process')
    }
    
    def processFile = {
        def pmOptions   = request.parameterMap['processOption']
        if(!pmOptions) {
            flash.info    = null
            flash.warning = 'Wählen Sie mindestens eine Anreicherungsoption.'
            flash.error   = null
        }
        else {
            if (!request.session.lastUpdate){
                request.session.lastUpdate = [:]
            }
            request.session.lastUpdate.pmOptions = pmOptions
            def en = getCurrentEnrichment()
            if(en.status != Enrichment.ProcessingState.WORKING) {
                flash.info    = 'Bearbeitung gestartet.'
                flash.warning = null
                flash.error   = null

                def format = getCurrentFormat()
                def options = [
                    'options':      pmOptions,
                    'delimiter':    format.get('delimiter'),
                    'quote':        format.get('quote'),
                    'quoteMode':    format.get('quoteMode'),
                    'dataTyp':      format.get('dataTyp'),
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
                pOptions:    pmOptions,
            ]
        )
    }
    
    def stopProcessingFile = {
        enrichmentService.stopProcessing(getCurrentEnrichment())
        deleteFile()
    }
    
    def deleteFile = {
        request.session.lastUpdate = [:]
        enrichmentService.deleteFileAndFormat(getCurrentEnrichment())    
        render(
            view:'process',
            model:[
                enrichments: enrichmentService.getSessionEnrichments(), 
                currentView: 'process'
            ]
        )
    }

    def correctFile = {
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
        def status = enrichmentService.sendFile(currentEnrichment, Enrichment.FileType.JSON_PACKAGE_ONLY,
                params.gokbUsername, params.gokbPassword)
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
        def status = enrichmentService.sendFile(currentEnrichment, Enrichment.FileType.JSON_TITLES_ONLY,
                params.gokbUsername, params.gokbPassword)
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
        if(en) {
            render '{"status":"' + en.getStatus() + '", "message":"' + en.getMessage() + '", "progress":' + en.getProgress().round() + '}'
        }
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

    // get Platform suggestions for typeahead
    def suggestPlatform = {
      log.debug("Getting platform suggestions..")
      def result = [:]
      def platforms = gokbService.getPlatformMap(params.q)
      result.items = platforms.records

      render result as JSON
    }

    // get Org suggestions for typeahead
    def suggestProvider = {
      log.debug("Getting provider suggestions..")
      def result = [:]
      def providers = gokbService.getProviderMap(params.q)
      result.items = providers.records

      render result as JSON
    }

    def gokbNameSpaces = {
      log.debug("Getting namespaces of connected GOKb instance..")
      def result = [:]

      result.items = gokbService.getNamespaces()

      render result as JSON
    }
}
