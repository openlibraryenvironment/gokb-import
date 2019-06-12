package ygor

import de.hbznrw.ygor.tools.*
import groovy.util.logging.Log4j
import ygor.Enrichment.FileType

@Log4j
class StatisticController {
    
    def grailsApplication
    
    static scope = "session"
        
    def index() {
        render(
            view:'index',
            model:[currentView:'statistic']
        )
    }
    
    def show() {
        def sthash = (String) request.parameterMap['sthash'][0]
        def json = {}
        String ygorVersion
        String date
        String filename
         
        log.info('reading file: ' + sthash)
        
        try {
            new File(grailsApplication.config.ygor.uploadLocation).eachDir() { dir ->
                dir.eachFile() { file ->
                    if(file.getName() == sthash){
                        json = JsonToolkit.parseFileToJson(file.getAbsolutePath())
                        ygorVersion = json.getAt("ygorVersion")
                        date = json.getAt("date")
                        filename = json.getAt("originalFileName")
                    }
                }
            }
        }
        catch(Exception e){
            log.error(e.getMessage())
            log.error(e.getStackTrace())
        }
        
        render(
            view:'show',
            model:[
                json:        json,
                sthash:      sthash,
                currentView: 'statistic',
                ygorVersion: ygorVersion,
                date:        date,
                filename:    filename
            ]
        )
    }

    static final PROCESSED_KBART_ENTRIES = "processed kbart entries"
    static final IGNORED_KBART_ENTRIES = "ignored kbart entries"
    static final DUPLICATE_KEY_ENTRIES = "duplicate key entries"
}
