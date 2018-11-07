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
import org.apache.commons.io.FileUtils

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

    private Map keysPerKeyType = [:]
    
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
        for (key in MultipleProcessingThread.KEY_ORDER) {
            keysPerKeyType.put(key, [:])
        }
        def kbartFields    = [:]
        this.inputFile = options.get('inputFile')
        trimHeader(inputFile)

        Paths.get(inputFile).withReader { reader ->
            CSVParser csv = getCSVParserFromReader(reader)
            checkHeader(csv, bridge.connector.kbartKeys)

            for (record in csv.iterator()) {
                if (record.size() != csv.getHeaderMap().size()) {
                    log.info('crappy record ignored: size != kex[index]')
                }
                else {
                    countUp()
                    def identifier

                    for (key in MultipleProcessingThread.KEY_ORDER) {
                        if (key != KbartConnector.KBART_HEADER_ZDB_ID || record.isMapped(key)) {
                            identifier = record.get(key)?.toString()?.trim()
                            if (identifier) {
                                def uid = UUID.randomUUID().toString()
                                // store enrichment keys (zdb or issn or eissn)
                                addKey(key, ["${uid}": "${identifier}"])

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
                                stash.putKeyType(uid, key)
                                break
                            }
                        }
                    }
                    if (!identifier) {
                        // store invalid csv record
                        log.info('Entry "' + "${record.get('publication_title')}" + '" ignored due to missing identifier.')
                        stash.get(Stash.IGNORED_KBART_ENTRIES).add(record.get('publication_title').toString())
                    }
                }
            }
            stash.put(KbartBridge.IDENTIFIER, kbartFields)
            for (key in MultipleProcessingThread.KEY_ORDER) {
                stash.put(key, keysPerKeyType.get(key))
            }
        }
        return stash.get(KbartBridge.IDENTIFIER).size()
    }

    private void trimHeader(String inputFile){
        File file = new File(inputFile)
        List<String> lines = FileUtils.readLines(file)
        if (lines != null && lines.size() > 0){
            lines.set(0,
                lines.get(0).toLowerCase()
                    .replace("zdb-id", "zdb_id")
                    .replace("coverage_notes", "notes"))
        }
        FileUtils.writeLines(file, lines)
    }

    private CSVParser getCSVParserFromReader(Reader reader) {
        // Skip BOM
        reader.mark(1)
        if (reader.read() != 0xFEFF) reader.reset()
        new CSVParser(reader, csvFormat)
    }


    private void addKey(String keyType, def value){
        def existing = keysPerKeyType.get(keyType)
        existing << value
        keysPerKeyType.put(keyType, existing)
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


    Title processEntry(DataContainer dc, String uid, String queryKey) {
        return processEntry(dc, uid, queryKey, null)
    }


    Title processEntry(DataContainer dc, String uid, String queryKey, Object record) {

        def options = super.bridge.options

        def saveTitle = false
        def title     = DataMapper.getExistingTitleByUid(dc, uid)
        if(title) {
            log.debug("> modifying existing Title: " + uid)
        }
        else {
            if(options.dataTyp == 'ebooks')
            {
                title     = new Title(type: new Pod("Book"), medium: new Pod('Book'))
            }
            else if (options.dataTyp == 'database')
            {
                title     = new Title(type: new Pod("Database"), medium: new Pod('Database'))
            }
            else
            {
                title     = new Title(type: new Pod("Serial"), medium: new Pod('Journal'))
            }
            saveTitle = true
        }
        
        def saveTipp = false
        def tipp     = DataMapper.getExistingTippByUid(dc, uid)
        if(tipp) {
            log.debug("> modifying existing Tipp: " + uid)
        }
        else {
            tipp     = PackageStruct.getNewTipp()

            if(options.dataTyp == 'ebooks')
            {
                tipp.title.v.type.v     = "Book"
            }
            else if (options.dataTyp == 'database')
            {
                tipp.title.v.type.v     = "Database"
            }
            else
            {
                tipp.title.v.type.v     = "Serial"
            }

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
            
            DataMapper.mapEnvelopeToTitle(title, q, env, dc)
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
        
        def api = bridge.getConnector().getAPIQuery(queryKey, stash.getKeyType(uid))
        
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
