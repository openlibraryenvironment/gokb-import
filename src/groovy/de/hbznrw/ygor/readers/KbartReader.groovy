package de.hbznrw.ygor.readers

import de.hbznrw.ygor.processing.YgorProcessingException
import grails.converters.JSON
import groovy.json.JsonOutput
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.apache.commons.csv.QuoteMode
import ygor.field.FieldKeyMapping

import java.nio.file.Paths

class KbartReader extends AbstractReader{

    private CSVFormat csvFormat = CSVFormat.EXCEL.withHeader().withIgnoreEmptyLines()
    private CSVParser csv
    private Map<String, Integer> csvHeader
    private Iterator<CSVRecord> iterator
    private CSVRecord lastItemReturned

    static MANDATORY_KBART_KEYS = [
        'date_first_issue_online',
        'date_last_issue_online',
        'num_first_vol_online',
        'num_last_vol_online',
        'num_first_issue_online',
        'num_last_issue_online',
        'title_url',
        'embargo_info',
        'coverage_depth',
        'notes'
    ]

    KbartReader(String kbartFile) {
        Paths.get(kbartFile).withReader { reader ->
            csv = getCSVParserFromReader(reader)
        }
        checkHeader(csv)
        csvHeader = csv.getHeaderMap()
        iterator = csv.iterator()
    }


    @Override
    def readItemData(FieldKeyMapping fieldKeyMapping, String identifier) {
        // guess, the iterator is in the position to return the desired next record
        def next = getNext()
        if (next && (!identifier || !fieldKeyMapping || next.get(fieldKeyMapping.kbartKey == identifier))) {
            return returnItem(next)
        }
        // otherwise, re-iterate over all entries
        CSVRecord currentRecord = next
        CSVRecord item
        while ({
            item = getNext()
            if (item && item.get(fieldKeyMapping.kbartKey == identifier)){
                return returnItem(item)
            }
            // following: "do while" continue condition, see https://stackoverflow.com/a/46474198
            item != currentRecord
        }()) continue
        null
    }


    private JSON returnItem(CSVRecord item) {
        if (!item) {
            return null
        }
        if (item.size() != csvHeader.size()) {
            log.info('Crappy record ignored, "size != header size" for: ' + item)
            return null
        }
        lastItemReturned = item
        JsonOutput.toJson(item.toMap())
    }


    CSVRecord getNext() {
        if (iterator.hasNext()) {
            iterator.next()
        }
        null
    }


    void checkHeader(CSVParser csv){
        def missingKeys = []
        if (! csv || ! csv.headerMap){
            throw new YgorProcessingException("Fehlender Dateiinhalt im CSV-File.")
        }
        MANDATORY_KBART_KEYS.each{ kbk ->
            if (csv.headerMap.get(kbk) == null){
                missingKeys << kbk.toString()
            }
        }
        if (missingKeys.size() > 0){
            throw new YgorProcessingException("Fehlende Spalten im CSV-Header: " + missingKeys.toString())
        }
    }


    private CSVParser getCSVParserFromReader(Reader reader  ) {
        // Skip BOM
        reader.mark(1)
        if (reader.read() != 0xFEFF) reader.reset()
        new CSVParser(reader, csvFormat)
    }


    KbartReader setConfiguration(KbartReaderConfiguration configuration) {
        if(null != configuration.delimiter) {
            csvFormat = csvFormat.withDelimiter((char)configuration.delimiter)
        }
        if(null != configuration.quote) {
            if('null' == configuration.quote) {
                csvFormat = csvFormat.withQuote(null)
            }
            else {
                csvFormat = csvFormat.withQuote((char)configuration.quote)
            }
        }
        if(null != configuration.quoteMode) {
            csvFormat = csvFormat.withEscape((char)'^')
            csvFormat = csvFormat.withQuoteMode((QuoteMode)configuration.quoteMode)
        }
        if(null != configuration.recordSeparator) {
            csvFormat = csvFormat.withRecordSeparator(configuration.recordSeparator)
        }
        csvFormat = csvFormat.withAllowMissingColumnNames(true)
        csvFormat = csvFormat.withIgnoreHeaderCase(true)
        this
    }


    class KbartReaderConfiguration{
        String delimiter
        String quote
        String quoteMode
        String recordSeparator
        static def resolver = [
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

        KbartReaderConfiguration(String delimiter, String quote, String quoteMode, String recordSeparator){
            delimiter = resolver.get(delimiter)
            quote     = resolver.get(quote)
            quoteMode = resolver.get(quoteMode)
            this.delimiter       = delimiter
            this.quote           = quote
            this.quoteMode       = quoteMode
            this.recordSeparator = recordSeparator
        }
    }
}
