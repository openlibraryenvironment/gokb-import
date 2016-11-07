package ygor

import java.security.MessageDigest
import de.hbznrw.ygor.iet.IetWrapper
import de.hbznrw.ygor.iet.ProcessingThread
import de.hbznrw.ygor.filetools.FileToolkit


class Document {

	static enum StateOfProcess {UNTOUCHED, WORKING, FINISHED, ERROR}
	StateOfProcess status
	
	String originName
	String originHash
	String originPathName
	
	String resultName
	String resultHash
	String resultPathName

	File sessionFolder
	
	def thread
	
    static constraints = {
    }
	
	Document(File sessionFolder, String originalFilename) {
		this.sessionFolder 	= sessionFolder
		originName 			= originalFilename
		originHash 			= getMD5Hash(originName + Math.random())
		originPathName 		= sessionFolder.getPath() + '/' + originHash
		
		setStatus(StateOfProcess.UNTOUCHED)
	}
	
	def process(int indexOfIssn, String options) {
		resultName 			= FileToolkit.getDateTimePrefixedFileName(originName)
		resultHash 			= getMD5Hash(originName + Math.random())
		resultPathName 		= sessionFolder.getPath() + '/' + resultHash
		
		thread = new ProcessingThread(this, indexOfIssn, options)
		thread.start()
	}
	
	def processCallback(StateOfProcess status) {
		setStatus(status)
	}
	
	/*
	int getProgress() {
		thread.getProgress()
	}
	*/
	
	def setStatus(StateOfProcess status) {
		this.status = status
	}
	
	StateOfProcess getStatus() {
		status
	}
	
	File getOriginFile() {
		new File(originPathName)
	}
	
	File getResultFile() {
		new File(resultPathName)
	}
	
	String getMD5Hash(String s) {
		MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8")).encodeHex().toString()
	 }
}
