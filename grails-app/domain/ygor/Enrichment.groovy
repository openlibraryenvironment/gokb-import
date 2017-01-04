package ygor

import de.hbznrw.ygor.iet.MultipleProcessingThread
import de.hbznrw.ygor.iet.export.DataContainer
import de.hbznrw.ygor.tools.*
import de.hbznrw.ygor.iet.export.DataMapper

class Enrichment {

    static enum FileType {ORIGIN, RESULT, JSON}
    
	static enum ProcessingState {UNTOUCHED, PREPARE, WORKING, FINISHED, ERROR}
	ProcessingState status
    float progress = 0.0
    
	String originName
	String originHash
	String originPathName
	
	String resultName
	String resultHash
	String resultPathName

	File sessionFolder
    
	def thread
    def data
	
    static constraints = {
    }
	
	Enrichment(File sessionFolder, String originalFilename) {
		this.sessionFolder 	= sessionFolder
		originName 			= originalFilename
		originHash 			= FileToolkit.getMD5Hash(originName + Math.random())
		originPathName 		= this.sessionFolder.getPath() + File.separator + originHash
		
        data                = new DataContainer()
        
		setStatus(ProcessingState.UNTOUCHED)
	}
	
	def process(HashMap options) {    
        //options.dump()
        
		resultName 			= FileToolkit.getDateTimePrefixedFileName(originName)
		resultHash 			= FileToolkit.getMD5Hash(originName + Math.random())
		resultPathName 		= sessionFolder.getPath() + File.separator + resultHash
		
        data.meta.ygor      = options.get('ygorVersion')
        data.meta.type      = options.get('ygorType')
        
        thread              = new MultipleProcessingThread(this, options)
		thread.start()
	}
	
    def setProgress(float progress) {
        this.progress = progress
    }
    
    float getProgress() {
        progress
    }
    
    def setStatusByCallback(ProcessingState status) {
        setStatus(status)
    }
    
	def setStatus(ProcessingState status) {
		this.status = status
	}
	
	ProcessingState getStatus() {
		status
	}
	
    File getFile(FileType type) {
        switch(type) {
            case FileType.ORIGIN:
                return new File(originPathName)
                break
            /*
            case FileType.RESULT:
                return new File(resultPathName)
                break
            */
            case FileType.JSON:
                def file = new File(resultPathName)
                def result = JsonToolkit.parseDataToJson(data)
                file.write(result)
                return file
                break
        }
    }
}
