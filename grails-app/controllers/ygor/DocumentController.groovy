package ygor

import grails.util.Environment

class DocumentController {
	
	static scope = "session"
	
	def documents = [:]
	
    def index = {
			
		render(view:'index', model:[documents:documents])
	}
		
	def uploadFile = {
		
		def file = request.getFile('uploadFile')
		if (file.empty) {
			flash.info    = null
			flash.warning = null
			flash.error   = 'Sie müssen eine gültige Datei auswählen.'
			render(view:'index', model:[documents:documents])
			return
		}
		
		def document = new Document(getSessionFolder(), file.originalFilename)
		documents << ["${document.originHash}":document]
		file.transferTo(new File(document.originPathName))
		//response.sendError(200, 'Done')
		
		redirect(action:'index')
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
			
			if(doc.status != Document.StateOfProcess.WORKING) {
				flash.info    = 'Bearbeitung gestartet.'
				flash.warning = null
				flash.error   = null
				
				doc.process((pIndex.toInteger() - 1), pOptions)
			}
		}
		render(view:'index', model:[documents:documents])
	}
	
	def deleteFile = {
	
		def doc = getDocument()
		def origin = doc.getOriginFile()
		
		origin.delete()
		documents.remove("${doc.originHash}")
		
		render(view:'index', model:[documents:documents])
	}
	
	def downloadFile() {
		
		def doc = getDocument()
		def result = doc.getResultFile()
		
		render(file:result, fileName:"${doc.resultName}")
	}
	
	def getStatus() {
		render document.getStatus()
	}
	/*
	def updateProgressbar = {
		
		def doc = getDocument()
		render doc.getProgress()
	}
	*/
	
	Document getDocument() {
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
