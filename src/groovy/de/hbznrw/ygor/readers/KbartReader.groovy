package de.hbznrw.ygor.readers

import de.hbznrw.ygor.processing.KbartProcessor
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.processing.YgorProcessingException
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.apache.commons.csv.QuoteMode
import org.apache.commons.lang.StringUtils
import ygor.field.FieldKeyMapping

import java.nio.charset.Charset

class KbartReader extends AbstractReader{

    private MultipleProcessingThread owner

    private CSVFormat csvFormat
    private CSVParser csv
    private List<String> csvHeader
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

    static ALIASES = [
        'notes' : ['coverage_notes'],
        'zdb_id' : ['zdb-id', 'ZDB_ID', 'ZDB-ID']
    ]

    KbartReader(MultipleProcessingThread owner, String delimiter) {
        this.owner = owner
        char delimiterChar = KbartProcessor.resolver.get(delimiter)
        csvFormat = CSVFormat.EXCEL.withHeader().withIgnoreEmptyLines().withDelimiter(delimiterChar)
        csv = CSVParser.parse(new File(owner.kbartFile), Charset.defaultCharset(), csvFormat)
        csvHeader = csv.getHeaderMap().keySet() as ArrayList
        checkHeader(csvHeader)
        iterator = csv.iterator()
    }


    @Override
    Map<String, String> readItemData(FieldKeyMapping fieldKeyMapping, String identifier) {
        // guess, the iterator is in the position to return the desired next record
        CSVRecord next = getNext()
        if (next && (!identifier || !fieldKeyMapping || next.get(fieldKeyMapping.kbartKeys == identifier))) {
            return returnItem(next)
        }
        // otherwise, re-iterate over all entries
        CSVRecord currentRecord = next
        CSVRecord item
        while ({
            item = getNext()
            if (item && item.get(fieldKeyMapping.kbartKeys == identifier)){
                return returnItem(item)
            }
            // following: "do while" continue condition, see https://stackoverflow.com/a/46474198
            item != currentRecord
        }()) continue
        null
        // this last return statement should never be reached
    }


    private Map<String, String> returnItem(CSVRecord item) {
        if (!item) {
            return null
        }
        def splitItem = item.values()
        if (splitItem.length != csvHeader.size()) {
            log.info('Crappy record ignored, "size != header size" for: ' + item)
            return null
        }
        Map<String, String> resultMap = new HashMap<>()
        for (int i=0; i<csvHeader.size(); i++) {
            resultMap.put(csvHeader.get(i), splitItem[i])
        }
        lastItemReturned = item
        resultMap
    }


    CSVRecord getNext() {
        if (iterator.hasNext()) {
            owner.increaseProgress()
            return iterator.next()
        }
        null
    }


    void checkHeader(List<String> csvHeader){
        def missingKeys = []
        if (! csvHeader){
            throw new YgorProcessingException("Fehlender Dateiinhalt im CSV-File.")
        }
        // check mandatory fields
        MANDATORY_KBART_KEYS.each{ kbk ->
            if (!csvHeader.contains(kbk)){
                boolean isMissing = true
                for (def alias : ALIASES[kbk]){
                    if (csvHeader.contains(alias)){
                        isMissing = false
                    }
                }
                if (isMissing) {
                    missingKeys << kbk.toString()
                }
            }
        }
        if (missingKeys.size() > 0){
            throw new YgorProcessingException("Fehlende Spalten im CSV-Header: " + missingKeys.toString())
        }
        // replace aliases
        for (Map.Entry<String, List<String>> alias : ALIASES){
            if (!csvHeader.contains(alias.getKey())){
                for (String value : alias.getValue()){
                    if (csvHeader.contains(value)){
                        csvHeader.set(csvHeader.indexOf(value), alias.getKey())
                    }
                }
            }
        }
    }


    private CSVParser getCSVParserFromReader(Reader reader) {
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


    private ArrayList<String> splitLine(String line){
        def result = new ArrayList<String>()
        if (StringUtils.isEmpty(line)){
            return result
        }
        String delimiter = KbartReaderConfiguration.resolve(owner.delimiter)
        int i
        while (line.length() > 0){
            i = line.indexOf(delimiter)
            if (i == -1){
                result.add(line)
                break
            }
            result.add(line.substring(0, i))
            line = line.substring(i + delimiter.length())
        }
        return result
    }

}
