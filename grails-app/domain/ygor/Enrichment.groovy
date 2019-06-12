package ygor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import de.hbznrw.ygor.export.GokbExporter
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.tools.*
import com.fasterxml.jackson.databind.node.ObjectNode
import groovy.json.JsonOutput
import org.apache.commons.lang.StringUtils
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField

import java.time.LocalDateTime

class Enrichment {

    static enum FileType {
        ORIGIN, 
        RESULT, 
        JSON, 
        JSON_PACKAGE_ONLY, 
        JSON_TITLES_ONLY,
        JSON_OO_RAW
    }
    
    static enum ProcessingState {
        UNTOUCHED, PREPARE, WORKING, FINISHED, ERROR
    }

    static ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper()
    
    ProcessingState status
    
    // frontend api stuff
    String apiMessage
    float  apiProgress = 0.0
    
    String originName
    String originHash
    String originPathName
    
    String resultName
    String resultHash
    String resultPathName
    String dataType

    File sessionFolder
    String ygorVersion
    String date

    def thread
    MappingsContainer mappingsContainer
    def dataContainer
    def stats

    static constraints = {
    }

    Enrichment(File sessionFolder, String originalFilename) {
        this.sessionFolder     = sessionFolder
        originName             = originalFilename.replaceAll(/\s+/,'_')
        originHash             = FileToolkit.getMD5Hash(originName + Math.random())
        originPathName         = this.sessionFolder.getPath() + File.separator + originHash

        dataContainer          = new DataContainer()
        setStatus(ProcessingState.UNTOUCHED)
    }
    
    def process(HashMap options) {    
        resultName        = FileToolkit.getDateTimePrefixedFileName(originName)
        resultHash        = FileToolkit.getMD5Hash(originName + Math.random())
        resultPathName    = sessionFolder.getPath() + File.separator + resultHash
        dataType          = options.get('dataTyp')
        ygorVersion       = options.get('ygorVersion')

        dataContainer.info.file = originName
        dataContainer.info.type = options.get('ygorType')

        mappingsContainer = new MappingsContainer()
        thread            = new MultipleProcessingThread(this, options)
        date              = LocalDateTime.now().toString()
        thread.start()
    }
    
    def setProgress(float progress) {
        this.apiProgress = progress
    }
    
    float getProgress() {
        apiProgress
    }

    def setMessage(String message) {
        this.apiMessage = message
    }
    
    String getMessage() {
        apiMessage
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
            case FileType.JSON_PACKAGE_ONLY:
                ObjectNode result = GokbExporter.extractPackage(this)
                def file = new File(originPathName)
                file.write(JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result), "UTF-8")
                return file
                break
            case FileType.JSON_TITLES_ONLY:
                ArrayNode result = GokbExporter.extractTitles(this)
                def file = new File(originPathName)
                file.write(JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result), "UTF-8")
                return file
                break
            case FileType.JSON_OO_RAW:
                return new File(resultPathName)
                break
        }
    }


    void saveResult() {
        StringWriter result = new StringWriter()
        result.append("{\"sessionFolder\":\"").append(sessionFolder.absolutePath).append("\",")
        result.append("\"originalFileName\":\"").append(originName).append("\",")
        result.append("\"ygorVersion\":\"").append(ygorVersion).append("\",")
        result.append("\"date\":\"").append(date).append("\",")
        result.append("\"configuration\":{")
        result.append("\"dataType\":\"").append(dataType).append("\",")
        result.append("\"mappingsContainer\":")
        result.append(JsonToolkit.toJson(mappingsContainer))
        result.append("},\"data\":")
        result.append(JsonToolkit.toJson(dataContainer.records))
        result.append("}")
        File file = new File(resultPathName)
        file.getParentFile().mkdirs()
        file.write(JsonOutput.prettyPrint(result.toString()), "UTF-8")
    }


    static Enrichment fromRawJson(JsonNode rootNode){
        String sessionFolder = JsonToolkit.fromJson(rootNode, "sessionFolder")
        String originalFileName = JsonToolkit.fromJson(rootNode, "originalFileName")
        def en = new Enrichment(new File(sessionFolder), originalFileName)
        en.ygorVersion = JsonToolkit.fromJson(rootNode, "ygorVersion") // TODO compare with current version and abort?
        en.date = JsonToolkit.fromJson(rootNode, "date")
        en.mappingsContainer = JsonToolkit.fromJson(rootNode, "configuration.mappingsContainer")
        en.resultName = FileToolkit.getDateTimePrefixedFileName(originalFileName)
        en.resultPathName = sessionFolder.concat(File.separator).concat(FileToolkit.getMD5Hash(originalFileName + Math.random()))
        en.dataContainer = DataContainer.fromJson(rootNode.path("data"), en.mappingsContainer)
        en.dataType = JsonToolkit.fromJson(rootNode, "configuration.dataType")
        return en
    }


    static Enrichment fromFile(def file){
        String json = file.getInputStream()?.text
        JsonNode rootNode = JSON_OBJECT_MAPPER.readTree(json)
        Enrichment enrichment = Enrichment.fromRawJson(rootNode)
        enrichment.setTitleMediumMapping()
        enrichment.setTitleTypeMapping()
        enrichment.setTippPlatformNameMapping()
        enrichment.setTippPlatformUrlMapping()
        enrichment.setStatusByCallback(Enrichment.ProcessingState.FINISHED)
        enrichment
    }


    FieldKeyMapping setTitleMediumMapping() {
        FieldKeyMapping mediumMapping = mappingsContainer.getMapping("medium", MappingsContainer.YGOR)
        if (dataType == 'ebooks') {
            mediumMapping.val = "Book"
        }
        else if (dataType == 'database') {
            mediumMapping.val = "Database"
        }
        else {
            mediumMapping.val = "Journal"
        }
        return mediumMapping
    }


    FieldKeyMapping setTitleTypeMapping() {
        FieldKeyMapping typeMapping = mappingsContainer.getMapping("publicationType", MappingsContainer.YGOR)
        if (dataType == 'ebooks') {
            typeMapping.val = "Book"
        }
        else if (dataType == 'database') {
            typeMapping.val = "Database"
        }
        else {
            typeMapping.val = "Serial"
        }
        return typeMapping
    }


    FieldKeyMapping setTippPlatformNameMapping() {
        FieldKeyMapping platformNameMapping = mappingsContainer.getMapping("platformName", MappingsContainer.YGOR)
        if (StringUtils.isEmpty(platformNameMapping.val)){
            platformNameMapping.val = dataContainer.pkg.packageHeader.v.nominalPlatform.name
        }
        platformNameMapping
    }


    FieldKeyMapping setTippPlatformUrlMapping() {
        FieldKeyMapping platformUrlMapping = mappingsContainer.getMapping("platformUrl", MappingsContainer.YGOR)
        if (StringUtils.isEmpty(platformUrlMapping.val)){
            platformUrlMapping.val = dataContainer.pkg.packageHeader.v.nominalPlatform.url
        }
        platformUrlMapping
    }


    void enrollMappingToRecords(FieldKeyMapping mapping){
        MultiField titleMedium = new MultiField(mapping)
        for (Record record in dataContainer.records) {
            record.addMultiField(titleMedium)
        }
    }




}
