package ygor

import grails.util.Environment

class EnrichmentController {
	
	static scope = "session"
	
	def documents = [:]
	
    def index = {
		redirect(action:'process')
	}
    
    def process = {
        render(controller:'Enrichment', view:'process', model:[documents:documents, currentView:'process'])
    }
    
    def howto = {
        render(controller:'Enrichment', view:'howto', model:[currentView:'howto'])
    }
    
    def about = {
        render(controller:'Enrichment', view:'about', model:[currentView:'about'])
    }
    
    def contact = {
        render(controller:'Enrichment', view:'contact', model:[currentView:'contact'])
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
		
		def pIndex  = request.parameterMap['processIndex'][0]
		def pOptions = request.parameterMap['processOption'][0]
		
		if(!pIndex) {
			flash.info    = null
			flash.warning = 'Geben Sie einen gültigen Index an.'
			flash.error   = null
		}
		else {
			def doc = getDocument()
			
			if(doc.status != Enrichment.StateOfProcess.WORKING) {
				flash.info    = 'Bearbeitung gestartet.'
				flash.warning = null
				flash.error   = null
				
				doc.process((pIndex.toInteger() - 1), pOptions)
			}
		}
		render(view:'process', model:[documents:documents, currentView:'process'])
	}
	
	def deleteFile = {
	
		def doc = getDocument()
		def origin = doc.getOriginFile()
		
		origin.delete()
		documents.remove("${doc.originHash}")
		
		render(view:'process', model:[documents:documents, currentView:'process'])
	}
	
	def downloadFile() {
		
		def doc = getDocument()
		def result = doc.getResultFile()
		
		render(file:result, fileName:"${doc.resultName}")
	}
	
	def getStatus() {
        def doc = getDocument()
		render doc.getStatus()
	}
	/*
	def updateProgressbar = {
		
		def doc = getDocument()
		render doc.getProgress()
	}
	*/
	
	Enrichment getDocument() {
		def hash = (String) request.parameterMap['originHash'][0]
		documents.get("${hash}")
	}
	
	/**
	 * Return session depending directory for file upload.
	 * Creates if not existing.
	 */
	
	File getSessionFolder() {
		def path = '/tmp/ygor/' + session.id
		def sessionFolder = new File(path)
		if(!sessionFolder.exists()) {
			sessionFolder.mkdirs()
		}
		return sessionFolder
	}

}
