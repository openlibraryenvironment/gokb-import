package ygor

import grails.util.Environment

class EnrichmentController {

    static scope = "session"

    def documents = [:]

    def index = { redirect(action:'process')   }

    def process = {
        render(
                view:'process',
                model:[documents:documents, currentView:'process']
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
            render(view:'process', model:[documents:documents])
            return
        }

        def document = new Enrichment(getSessionFolder(), file.originalFilename)
        documents << ["${document.originHash}":document]
        file.transferTo(new File(document.originPathName))
        //response.sendError(200, 'Done')

        redirect(action:'process')
    }

    def processFile = {
        
        def pmIndex     = request.parameterMap['processIndex'][0]
        def pmIndexType = request.parameterMap['processIndexType'][0]
        def pmOptions   = request.parameterMap['processOption']

        println request.parameterMap['processOption']
        
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
            def doc = getDocument()
            def processOptions = [
                'indexOfKey':   pmIndex.toInteger() - 1,
                'type':         pmIndexType,
                'options':      pmOptions
                ]
            
            if(doc.status != Enrichment.StateOfProcess.WORKING) {
                flash.info    = 'Bearbeitung gestartet.'
                flash.warning = null
                flash.error   = null

                doc.process(processOptions)
            }
        }
        render(
                view:'process',
                model:[
                    documents:documents, 
                    currentView:'process',
                    pIndex:pmIndex,
                    pIndexType:pmIndexType,
                    pOptions:pmOptions,
                    ]
                )
    }
    def stopProcessingFile = {
        def doc = getDocument()
        doc.thread.isRunning = false
        
        deleteFile()
    }
    
    def deleteFile = {

        def doc = getDocument()
        def origin = doc.getOriginFile()

        origin.delete()
        documents.remove("${doc.originHash}")

        render(
                view:'process',
                model:[documents:documents, currentView:'process']
                )
    }

    def downloadFile() {

        def doc = getDocument()
        def result = doc.getResultFile()

        render(
                file:result,
                fileName:"${doc.resultName}"
                )
    }

    def ajaxGetStatus() {
        def doc = getDocument()
        
        render '{"status":"' + doc.getStatus() + '", "progress":' + doc.getProgress() + '}'
    }

    Enrichment getDocument() {
        def hash = (String) request.parameterMap['originHash'][0]
        documents.get("${hash}")
    }

    /**
     * Return session depending directory for file upload.
     * Creates if not existing.
     */

    File getSessionFolder() {
        def path = grailsApplication.config.ygor.uploadLocation + File.separator + session.id
        def sessionFolder = new File(path)
        if(!sessionFolder.exists()) {
            sessionFolder.mkdirs()
        }
        sessionFolder
    }
}
