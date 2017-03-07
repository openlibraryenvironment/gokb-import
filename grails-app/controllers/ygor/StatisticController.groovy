package ygor

import de.hbznrw.ygor.tools.*
import groovy.util.logging.Log4j
import ygor.Enrichment.FileType
import groovy.json.*
import de.hbznrw.ygor.iet.export.Transformer

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
         
        log.info('reading file: ' + sthash)
        
        try {
            new File(grailsApplication.config.ygor.uploadLocation).eachDir() { dir ->
                dir.eachFile() { file ->
                    if(file.getName() == sthash){
                        def raw = JsonToolkit.parseFileToJson(file.getAbsolutePath())
                        def tmp = Transformer.getSimpleJSON(raw, FileType.JSON_DEBUG, Transformer.NO_PRETTY_PRINT)
                        json = tmp.toString()
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
                json:json,
                sthash: sthash,
                currentView:'statistic'
            ])
    }
}
