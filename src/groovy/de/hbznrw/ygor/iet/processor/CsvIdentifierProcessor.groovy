package de.hbznrw.ygor.iet.processor

import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVFormat
import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.Stash
import de.hbznrw.ygor.iet.enums.Status
import de.hbznrw.ygor.iet.export.*
import de.hbznrw.ygor.iet.export.structure.*
import de.hbznrw.ygor.interfaces.*
import groovy.util.logging.Log4j
import java.nio.file.Paths


/**
 * Class for reading and processing csv files
 * 
 * @author David Klober
 *
 */
@Deprecated
@Log4j
class CsvIdentifierProcessor extends AbstractProcessor {

    private stash               = new Stash()
    private String inputFile
    
    private CSVFormat csvFormat = CSVFormat.EXCEL
    private int total		    = 0
    private int count           = 0

    //

    CsvIdentifierProcessor(BridgeInterface bridge) {
        super(bridge)
    }
    CsvIdentifierProcessor() {
    }

    void setBridge(BridgeInterface bridge) {
        super.bridge = bridge
    }
    
    void setConfiguration(String delimiter, String quote, String recordSeparator) {
        
        if(null != delimiter) {
            csvFormat = csvFormat.withDelimiter((char)delimiter)
        }
        if(null != quote) {
            csvFormat = csvFormat.withQuote((char)quote)
        }
        if(null != recordSeparator) {
            csvFormat = csvFormat.withRecordSeparator(recordSeparator)
        }
    }

    void processFile(HashMap options) throws Exception {
        
        log.info("processFile() -> " + options)
        
        if(0 == stash.get(de.hbznrw.ygor.iet.export.structure.TitleStruct.ISSN).size()){ 
            def nr  = initData(options)
            def pgt = Math.round(nr * 2.15)
            bridge.getMaster().setProgressTotal((int) pgt)  // TODO dynamic calculation
        }
        
        count = 0
        bridge.processStash()
        bridge.finish()
    }
    
    private int initData(HashMap options) throws Exception {
        
        log.info("filling stash with initial data ..")
        
        def issn = [:]
        this.inputFile = options.get('inputFile')

        Paths.get(inputFile).withReader { reader ->
            CSVParser csv = new CSVParser(reader, csvFormat)
            for (record in csv.iterator()) {
                def k = (record.size() <= options.get('indexOfKey')) ? "" : record.get(options.get('indexOfKey')).toString()
                issn << ["${k}":null]
            }
        }
        stash.put(de.hbznrw.ygor.iet.export.structure.TitleStruct.ISSN, issn)
        
        return stash.get(de.hbznrw.ygor.iet.export.structure.TitleStruct.ISSN).size()
    }
    
    Title processEntry(DataContainer dc, String uid) {
        return processEntry(dc, uid, null, null)
    }
    
    Title processEntry(DataContainer dc, String uid, String queryKey, Object record) {
        
        def saveTitle = false
        def title     = DataMapper.getExistingTitleByPrimaryIdentifier(dc, uid)
        if(title) {
            log.debug("> modifying existing Title: " + uid)
        }
        else {
            title     = new Title()
            saveTitle = true
        }
        
        def saveTipp = false
        def tipp     = DataMapper.getExistingTippByPrimaryIdentifier(dc, uid)
        if(tipp) {
            log.debug("> modifying existing Tipp: " + uid)
        }
        else {
            tipp     = PackageStruct.getNewTipp()
            saveTipp = true
        }

        bridge.tasks.each{ q ->
            def msg = ""
            def state = AbstractEnvelope.STATUS_UNKNOWN_REQUEST
            
            Envelope env
            if(record){
                env = bridge.getConnector().query(record, q)
            }
            else {
                env = bridge.getConnector().query(q)
            }
            
            if(env.type == Envelope.SIMPLE){
                
                if(AbstractEnvelope.RESULT_OK == env.state){
                    msg = env.message[0]
                }
                else if(AbstractEnvelope.RESULT_MULTIPLE_MATCHES == env.state){
                    msg = env.message.join(", ")
                }

                state = env.state
                log.info("#" + count + " processed " + uid + " -> " + msg + " : " + state)
            }
            else if(env.type == Envelope.COMPLEX){
                
                // used for Publisher
                env.states.eachWithIndex { ste, i ->
                    if(AbstractEnvelope.RESULT_OK == ste) {
                        msg = env.messages[i]
                    }
                    else if(AbstractEnvelope.RESULT_MULTIPLE_MATCHES == ste) {
                        if(env.messages[i]){
                            msg = env.messages[i].join("|")
                        }
                        else {
                            msg = null // todo ??
                        }
                    }

                    log.info("#" + count + " processed " + uid + " -> " + msg + " : " + ste)
                }
            }
            
            DataMapper.mapEnvelopeToTitle(title, q, env)
            DataMapper.mapEnvelopeToTipp (tipp, q, env, dc)
        }
        
        if(saveTitle){
            log.debug("> stored as new Title: " + uid)
            
            dc.titles   << ["${uid}": new Pod(title)]
            title._meta << ['uid':    uid]
            title._meta << ['api':    bridge.getConnector().getAPIQuery(queryKey)]
        }
        
        if(saveTipp){
            log.debug("> stored as new Tipp: " + uid)
            
            dc.pkg.tipps << ["${uid}": new Pod(tipp)]
            tipp._meta   << ['uid':    uid]
            tipp._meta   << ['api':    bridge.getConnector().getAPIQuery(queryKey)]
        }      
        title
    }
    
    Stash getStash() {
        stash
    }
    
    void countUp() {
        count++
    }
}
