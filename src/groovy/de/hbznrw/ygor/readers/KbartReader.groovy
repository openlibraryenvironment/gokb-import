package de.hbznrw.ygor.readers

import de.hbznrw.ygor.processing.YgorProcessingException
import grails.converters.JSON
import groovy.json.JsonOutput
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import ygor.field.FieldKeyMapping

import java.nio.file.Paths

class KbartReader extends AbstractReader{

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

    CSVParser csv
    Map<String, Integer> csvHeader
    Iterator<CSVRecord> iterator
    CSVRecord lastItemReturned

    KbartReader(String kbartFile) {
        Paths.get(kbartFile).withReader { reader ->
            csv = getCSVParserFromReader(reader)
        }
        checkHeader(csv)
        csvHeader = csv.getHeaderMap()
        iterator = csv.iterator()
    }


    @Override
    def readItemData(FieldKeyMapping fieldKeyMapping, String id) {
        // guess, the iterator is in the position to return the desired next record
        def next = getNext()
        if (next && (!id || next.get(fieldKeyMapping.kbartKey == id))) {
            return returnItem(next)
        }
        // otherwise, re-iterate over all entries
        CSVRecord currentRecord = next
        CSVRecord item
        while ({
            item = getNext()
            if (item && item.get(fieldKeyMapping.kbartKey == id)){
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

    private CSVParser getCSVParserFromReader(Reader reader) {
        // Skip BOM
        reader.mark(1)
        if (reader.read() != 0xFEFF) reader.reset()
        new CSVParser(reader, csvFormat)
    }
}
