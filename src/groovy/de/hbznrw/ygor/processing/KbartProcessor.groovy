package de.hbznrw.ygor.processing

import de.hbznrw.ygor.bridges.KbartBridge
import de.hbznrw.ygor.connectors.KbartConnector
import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.export.DataMapper
import de.hbznrw.ygor.export.structure.PackageStruct
import de.hbznrw.ygor.export.structure.Pod
import de.hbznrw.ygor.export.structure.Title
import de.hbznrw.ygor.interfaces.AbstractEnvelope
import de.hbznrw.ygor.interfaces.AbstractProcessor
import de.hbznrw.ygor.interfaces.BridgeInterface
import groovy.util.logging.Log4j
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.QuoteMode

import java.nio.file.Paths
/**
 * Class for reading and processing kbart files
 */

@Log4j
class KbartProcessor extends AbstractProcessor {

    private CSVFormat csvFormat = CSVFormat.EXCEL.withHeader().withIgnoreEmptyLines()
    private String inputFile

    private stash = new Stash()

    private int total		    = 0
    private int count           = 0
    
    KbartProcessor(BridgeInterface bridge) {
        super(bridge)
    }
    KbartProcessor() {
    }

    void setBridge(BridgeInterface bridge) {
        super.bridge = bridge
    }
    
    void setConfiguration(String delimiter, String quote, String quoteMode, String recordSeparator) {
        
        // TODO refactoring
        def resolver = [
            'comma'         : ',',
            'semicolon'     : ';',
            'tab'           : '\t',
            'doublequote'   : '"',
            'singlequote'   : "'",
            'nullquote'     : 'null',
            'all'           : QuoteMode.ALL,
            'nonnumeric'    : QuoteMode.NON_NUMERIC,
            'none'          : QuoteMode.NONE
            ]
        
        delimiter = resolver.get(delimiter)
        quote     = resolver.get(quote)
        quoteMode = resolver.get(quoteMode)
        
        if(null != delimiter) {
            csvFormat = csvFormat.withDelimiter((char)delimiter)
        }
        if(null != quote) {
            if('null' == quote) {
                csvFormat = csvFormat.withQuote(null)
            }
            else {
                csvFormat = csvFormat.withQuote((char)quote)
            }
        }
        if(null != quoteMode) {
            csvFormat = csvFormat.withEscape((char)'^')
            csvFormat = csvFormat.withQuoteMode((QuoteMode)quoteMode)
        }
        if(null != recordSeparator) {
            csvFormat = csvFormat.withRecordSeparator(recordSeparator)
        }
        
        csvFormat = csvFormat.withAllowMissingColumnNames(true)
        csvFormat = csvFormat.withIgnoreHeaderCase(true)
    }

    void processFile(HashMap options) throws Exception {
        
        log.info("processFile() -> " + options)
        
        if(0 == stash.get(KbartBridge.IDENTIFIER).size() && 0 == stash.get(Stash.IGNORED_KBART_ENTRIES).size()){
            def nr  = initData(options)
            def pgt = Math.round(nr * bridge.master.getApiCallsSize())
            bridge.getMaster().setProgressTotal((int) pgt)
        }
        
        count = 0
        bridge.processStash()
        bridge.finish()
    }

    private int initData(HashMap options) throws Exception {

        log.info("filling stash with initial data ..")

        def keyType
        def keys           = [:]
        def kbartFields    = [:]

        this.inputFile = options.get('inputFile')

        if(options.get('typeOfKey').toString() in [
                KbartConnector.KBART_HEADER_ZDB_ID,
                KbartConnector.KBART_HEADER_ONLINE_IDENTIFIER,
                KbartConnector.KBART_HEADER_PRINT_IDENTIFIER]
        ) {
            keyType = options.get('typeOfKey')
        }

        Paths.get(inputFile).withReader { reader ->
            // Skip BOM
            reader.mark(1)
            if (reader.read() != 0xFEFF) reader.reset()
            CSVParser csv = new CSVParser(reader, csvFormat)
            checkHeader(csv, bridge.connector.kbartKeys)

            for (record in csv.iterator()) {
                if (record.size() < csv.getHeaderMap().size()) {
                    log.info('crappy record ignored: size < kex[index]')
                } else {
                    def identifier = record.get(keyType)?.toString()?.trim()
                    countUp()

                    if (identifier) {
                        def uid = UUID.randomUUID().toString() // TODO NEW

                        // store enrichment keys (zdb or issn or eissn)
                        keys << ["${uid}": "${identifier}"]

                        // store kbart fields
                        def kbfs = [:]
                        bridge.connector.kbartKeys.each { kbk ->
                            kbfs << ["${kbk}": record.get(kbk).toString()]
                        }
                        bridge.connector.optionalKbartKeys.each { kbk ->
                            if (record.isMapped(kbk)) {
                                kbfs << ["${kbk}": record.get(kbk).toString()]
                            }
                        }
                        if (kbfs.size() > 0) {
                            kbartFields << ["${uid}": kbfs]
                        }
                    } else {
                        // store invalid csv records
                        log.info('no enrichment key (' + keyType + ') found; entry ignored')
                        stash.get(Stash.IGNORED_KBART_ENTRIES).add(record.get('publication_title').toString())
                    }
                }
            }
        }
        stash.put(keyType, keys)
        stash.put(KbartBridge.IDENTIFIER, kbartFields)
        return stash.get(KbartBridge.IDENTIFIER).size()
    }

    void checkHeader(CSVParser csv, def kbartKeys){
        def missingKeys = []
        if (! csv || ! csv.headerMap){
            throw new YgorProcessingException("Fehlender Dateiinhalt im CSV-File.")
        }
        kbartKeys.each{ kbk ->
            if (csv.headerMap.get(kbk) == null){
                missingKeys << kbk.toString()
            }
        }
        if (missingKeys.size() > 0){
            throw new YgorProcessingException("Fehlende Spalten im CSV-Header: " + missingKeys.toString())
        }
    }


    Title processEntry(DataContainer dc, String uid) {
        return processEntry(dc, uid, null, null)
    }
    
    Title processEntry(DataContainer dc, String uid, String queryKey) {
        return processEntry(dc, uid, queryKey, null)
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
                log.info("#" + count + " processed " + uid + " : " + q + " -> " + msg + " : " + state)
            }
            else if(env.type == Envelope.COMPLEX){
                
                // used for tipp.coverage           (kbart file)
                // used for title.publisher_history (api)
                // used for title.historyEvents     (api)
                env.messages.eachWithIndex{ item, i ->
                    if(AbstractEnvelope.RESULT_OK == env.states[i]) {
                        msg = item.value
                    }
                    else if(AbstractEnvelope.RESULT_MULTIPLE_MATCHES == env.states[i]) {
                        if(env.messages[i]){
                            msg = item.value.join("|")
                        }
                        else {
                            msg = null // todo ??
                        }
                    }

                    log.info("#" + count + " processed " + uid + " : " + q + " -> " + msg + " : " + env.states[i])
                }
            }
            
            DataMapper.mapEnvelopeToTitle(title, q, env)
            DataMapper.mapEnvelopeToTipp (tipp, q, env, dc)
        }
        
        if(saveTitle){
            log.debug("> stored as new Title: " + uid)
            dc.titles   << ["${uid}": new Pod(title)]
            title._meta.put('uid', uid)
            title._meta.put('api', [])
        }
        if(saveTipp){
            log.debug("> stored as new Tipp: " + uid)

            dc.pkg.tipps << ["${uid}": new Pod(tipp)]
            tipp._meta.put('uid', uid)
            tipp._meta.put('api', [])
        }
        
        def api = bridge.getConnector().getAPIQuery(queryKey)
        
        title._meta.api << api
        tipp._meta.api << api
        
        title
    }
    
    Stash getStash() {
        stash
    }
    
    void countUp() {
        count++
    }
    int getCount() {
        count
    }

}
