package ygor

import de.hbznrw.ygor.tools.*
import groovy.json.JsonOutput
import groovy.util.logging.Log4j

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
         
        log.info('reading statistic file: ' + sthash)
        
        try {
            new File(grailsApplication.config.ygor.uploadLocation).eachDir() { dir ->
                dir.eachFile() { file ->
                    if(file.getName() == sthash){
                        json = JsonToolkit.parseFileToJson(file.getAbsolutePath())
                        json = JsonOutput.toJson(json)   
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
