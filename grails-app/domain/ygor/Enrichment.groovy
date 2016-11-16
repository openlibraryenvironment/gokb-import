package ygor

import de.hbznrw.ygor.iet.IetWrapper
import de.hbznrw.ygor.iet.MultipleProcessingThread
import de.hbznrw.ygor.tools.FileToolkit


class Enrichment {

	static enum StateOfProcess {UNTOUCHED, WORKING, FINISHED, ERROR}
	StateOfProcess status
    float progress = 0.0
	
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
	
	Enrichment(File sessionFolder, String originalFilename) {
		this.sessionFolder 	= sessionFolder
		originName 			= originalFilename
		originHash 			= FileToolkit.getMD5Hash(originName + Math.random())
		originPathName 		= this.sessionFolder.getPath() + File.separator + originHash
		
		setStatus(StateOfProcess.UNTOUCHED)
	}
	
	def process(HashMap options) {    
        //options.dump()
        
		resultName 			= FileToolkit.getDateTimePrefixedFileName(originName)
		resultHash 			= FileToolkit.getMD5Hash(originName + Math.random())
		resultPathName 		= sessionFolder.getPath() + File.separator + resultHash
		
        thread = new MultipleProcessingThread(this, options)
		thread.start()
	}
	
    def setProgress(float progress) {
        this.progress = progress
    }
    
    float getProgress() {
        progress
    }
    
    def setStatusByCallback(StateOfProcess status) {
        setStatus(status)
    }
    
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
}
