package ygor


import groovy.util.logging.Log4j

@Log4j
class StatisticController {
    
    def grailsApplication
    
    static scope = "session"
    static FileFilter DIRECTORY_FILTER = new FileFilter() {
        @Override
        boolean accept(File file) {
            return file.isDirectory()
        }
    }

    def index() {
        render(
            view:'index',
            model:[currentView:'statistic']
        )
    }
    
    def show() {
        String sthash = (String) request.parameterMap['sthash'][0]
        Set<Map<String, String>> invalidRecords = new HashSet<>()
        Set<Map<String, String>> validRecords = new HashSet<>()
        String ygorVersion
        String date
        String filename
         
        log.info('reading file: ' + sthash)
        
        try {
            Enrichment enrichment = getEnrichmentFromFile(sthash)
            String namespace = enrichment.dataContainer.info.namespace_title_id
            if (enrichment){
                for (Record record in enrichment.dataContainer.records.values()){
                    record.validate(namespace)
                    def multiFieldMap = record.asMultiFieldMap()
                    if (record.isValid()){
                        validRecords.add(multiFieldMap)
                    }
                    else {
                        invalidRecords.add(multiFieldMap)
                    }
                }
                ygorVersion = enrichment.ygorVersion
                date = enrichment.date
                filename = enrichment.originName
            }
            else{
                throw new EmptyStackException()
            }
        }
        catch(Exception e){
            log.error(e.getMessage())
            log.error(e.getStackTrace())
        }
        
        render(
            view:'show',
            model:[
                sthash:         sthash,
                currentView:    'statistic',
                ygorVersion:    ygorVersion,
                date:           date,
                filename:       filename,
                invalidRecords: invalidRecords,
                validRecords:   validRecords
            ]
        )
    }


    def edit(){
        Enrichment enrichment = getEnrichmentFromFile((String) request.parameterMap['sthash'][0])
        Record record = enrichment.dataContainer.getRecord(params.id)
        [record: record]
    }


    private Enrichment getEnrichmentFromFile(String sthash){
        File uploadLocation = new File(grailsApplication.config.ygor.uploadLocation)
        for (def dir in uploadLocation.listFiles(DIRECTORY_FILTER)){
            for (def file in dir.listFiles()){
                if (file.getName() == sthash) {
                    return Enrichment.fromFile(file)
                }
            }
        }
        return null
    }

    static final PROCESSED_KBART_ENTRIES = "processed kbart entries"
    static final IGNORED_KBART_ENTRIES = "ignored kbart entries"
    static final DUPLICATE_KEY_ENTRIES = "duplicate key entries"
}
