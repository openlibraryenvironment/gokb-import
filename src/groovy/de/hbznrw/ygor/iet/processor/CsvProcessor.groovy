package de.hbznrw.ygor.iet.processor

import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.connector.EzbConnector
import de.hbznrw.ygor.iet.connector.SruPicaConnector
import de.hbznrw.ygor.iet.enums.Status
import de.hbznrw.ygor.iet.export.*
import de.hbznrw.ygor.iet.export.structure.*
import de.hbznrw.ygor.iet.interfaces.*
import de.hbznrw.ygor.tools.FileToolkit
import de.hbznrw.ygor.iet.bridge.ZdbBridge
import groovy.util.logging.Log4j

import java.io.ObjectInputStream.ValidationList
import java.nio.file.Paths
import java.util.ArrayList

/**
 * Class for reading and processing csv files
 * 
 * @author David Klober
 *
 */

@Log4j
class CsvProcessor extends ProcessorAbstract {

    private stash               = [:]
    private String inputFile
    
    private CSVFormat csvFormat = CSVFormat.EXCEL
    private int total		    = 0
    private int count           = 0

    //

    CsvProcessor(BridgeInterface bridge) {
        super(bridge)
    }
    CsvProcessor() {
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
        
        count = 0
        
        if(stash.size() == 0){
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
            stash.put(ZdbBridge.IDENTIFIER, [:])
            
            def pgt = Math.round(stash.get(de.hbznrw.ygor.iet.export.structure.TitleStruct.ISSN).size() * 2.15)
            bridge.getMaster().setProgressTotal((int) pgt)  // TODO dynamic calculation
        }

        bridge.processStash()
        bridge.finish()
    }
    
    Title processEntry(DataContainer dc, String hash) {
        return processEntry(dc, hash, null)
    }
    
    Title processEntry(DataContainer dc, String hash, Object record) {
        
        def saveTitle = false
        def title     = Mapper.getExistingTitleByPrimaryIdentifier(dc, hash)
        if(title) {
            log.info("> modifying existing Title: " + hash)
        }
        else {
            title     = new Title()
            saveTitle = true
        }
        
        def saveTipp = false
        def tipp     = Mapper.getExistingTippByPrimaryIdentifier(dc, hash)
        if(tipp) {
            log.info("> modifying existing Tipp: " + hash)
        }
        else {
            tipp     = PackageStruct.getNewTipp()
            saveTipp = true
        }

        bridge.tasks.each{ q ->
            def msg = ""
            def state = Status.UNKNOWN_REQUEST
            
            Envelope env
            if(record)
                env = bridge.getConnector().query(record, q)
            else
                env = bridge.getConnector().query(q)

            if(env.type == Envelope.SIMPLE){
                
                if(Status.RESULT_OK == env.state)
                    msg = env.message[0]
                else if(Status.RESULT_MULTIPLE_MATCHES == env.state)
                    msg = env.message.join(", ")

                state = env.state
                log.info("#" + count + " processed " + hash + " -> " + msg + " : " + state)
            }
            else if(env.type == Envelope.COMPLEX){
                
                // used for Publisher
                env.states.eachWithIndex { ste, i ->
                    if(Status.RESULT_OK == ste) {
                        msg = env.messages[i]
                    }
                    else if(Status.RESULT_MULTIPLE_MATCHES == ste) {
                        if(env.messages[i])
                            msg = env.messages[i].join("|")
                        else
                            msg = null // todo ??
                    }

                    log.info("#" + count + " processed " + hash + " -> " + msg + " : " + state)
                }
            }
            
            Mapper.mapToTitle (dc, title, q, env)
            Mapper.mapToTipp  (dc, tipp, q, env)
        }
        
        if(saveTitle){
            log.info("> stored as new Title: " + hash)
            title._hash = new Pod(hash, Status.IGNORE)
            dc.titles << ["${hash}": new Pod(title)]
        }
        if(saveTipp){
            log.info("> stored as new Tipp: " + hash)
            tipp._hash = new Pod(hash, Status.IGNORE)
            dc.pkg.tipps << ["${hash}": new Pod(tipp)]
        }
        
        title
    }
    
    HashMap getStash() {
        stash
    }
    
    void countUp() {
        count++
    }
}
