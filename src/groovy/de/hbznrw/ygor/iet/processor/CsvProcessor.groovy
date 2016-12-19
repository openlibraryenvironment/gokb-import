package de.hbznrw.ygor.iet.processor

//@Grab(group='org.apache.commons', module='commons-csv', version='1.4')

import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.Status
import de.hbznrw.ygor.iet.export.DataMapper
import de.hbznrw.ygor.iet.export.Title
import de.hbznrw.ygor.iet.interfaces.*
import de.hbznrw.ygor.tools.FileToolkit
import java.nio.file.Paths
import java.util.ArrayList

/**
 * Class for reading and processing csv files
 * 
 * @author David Klober
 *
 */
class CsvProcessor extends ProcessorAbstract {

    private CSVFormat csvFormat = CSVFormat.EXCEL
    private int indexOfKey 	    = 0
    private int count 		    = 0
    private int total		    = 0

    //

    CsvProcessor(BridgeInterface bridge) {
        super(bridge)
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

    void processFile(String inputFile, int indexOfKey, String outputFile) throws Exception {
        def fileWriter
        def csvPrinter

        fileWriter = new FileWriter(outputFile);
        csvPrinter = new CSVPrinter(fileWriter, csvFormat)
        count = 0

        Paths.get(inputFile).withReader { reader ->
            CSVParser csv = new CSVParser(reader, csvFormat)

            for (record in csv.iterator()) {
                if(!bridge.master.isRunning) {
                    println('Aborted by user action.')
                    return
                }
                
                bridge.increaseProgress()
                
                ArrayList modifiedRecord = processRecord(record, indexOfKey, ++count)
                csvPrinter.printRecord(modifiedRecord)
            }
        }

        fileWriter.flush()
        fileWriter.close()
        csvPrinter.close()
    }

    @Override
    ArrayList processRecord(CSVRecord record, int indexOfKey, int count) {

        ArrayList modifiedRecord = record.toList()

        def data    = bridge.master.document.data
        def key     = (record.size() <= indexOfKey) ? "" : record.get(indexOfKey)
        if("" != key) {

            bridge.connector.poll(key)
          
            // TODO: refacoring ..
            def saveTitle = false
            def title = DataMapper.getExistingTitleByISSN(data, key)
            
            if(!title) {
                title = new Title(key)
                saveTitle = true
            }
            
            bridge.query.each{ q -> // TODO
                def msg = ""
                def state = Status.UNKNOWN_REQUEST
                
                Envelope env = bridge.connector.query(q)
    
                if(Status.RESULT_OK == env.state) {
                    msg = env.message[0]
                }
                else if(Status.RESULT_MULTIPLE_MATCHES == env.state) {
                    msg = env.message.join(", ")
                }
                state = env.state

                modifiedRecord << (msg)
                modifiedRecord << (q.toString() + '_' + state)

                // TODO: refacoring ..
                DataMapper.mapping(title, q, env)
                                
                println("#" + count + " processed " + key + " -> " + env.message + " : " + state)
            }
            if(saveTitle) {
                data.content << title
            }
            
        } else {
            println("#" + count + " skipped empty ISSN")
        }

        return modifiedRecord
    }
}
