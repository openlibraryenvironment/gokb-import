package de.hbznrw.ygor.iet.prototyp

import groovy.sql.Sql
import de.hbznrw.ygor.tools.FileToolkit
import de.hbznrw.ygor.iet.bridge.ZdbIdBridge
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter

import com.mysql.jdbc.Driver

/*
 * TODO: prototype
 */


class ZevPortZdbIdEnrichment {

    static String query = '''
        SELECT eISSNTab.t_name as name, eISSNTab.ti_value AS eISSN, pISSNTab.ti_value AS pISSN
        FROM (SELECT * FROM Title
        INNER JOIN TitleIdentifier ON ti_title_fk = t_id
        WHERE ti_identifiernamespace_fk = 1) AS eISSNTab
        JOIN TitleIdentifier AS pISSNTab ON pISSNTab.ti_title_fk = t_id
        WHERE pISSNTab.ti_identifiernamespace_fk = 2
        LIMIT 10;'''
    
    static sqlConfig = [
        'url'    : 'jdbc:mysql://193.30.112.88:3306/zevport',
        'user'   : 'kloberd',
        'pass'   : 'hbz102016',
        'driver' : 'com.mysql.jdbc.Driver'
     ]

    static void main(args) {
        
        println('ZevPortZdbIdExport started .. ')
        
        def sql = Sql.newInstance(
            sqlConfig['url'], sqlConfig['user'], sqlConfig['pass'], sqlConfig['driver']
            )
        
        def tmpPath    = '/tmp/' + FileToolkit.getMD5Hash(
            ZevPortZdbIdEnrichment.class.getName() + Math.random()
            )
        
        def fileWriter = new FileWriter(tmpPath)
        def csvFormat  = CSVFormat.EXCEL
        def csvPrinter = new CSVPrinter(fileWriter, csvFormat)
        
        csvPrinter.printRecord(['name','eISSN','pISSN'])
        
        sql.eachRow(ZevPortZdbIdEnrichment.query){ row ->
            ArrayList record = [ row['name'], row['eISSN'], row['pISSN'] ]  
            csvPrinter.printRecord(record)
        }
        
        fileWriter.flush()
        fileWriter.close()
        csvPrinter.close()
        
        println('ZevPortZdbIdExport done .. ')
        
        println('ZdbIdBridge called  .. ')
        ZdbIdBridge bridge = new ZdbIdBridge(tmpPath, 1)
        bridge.go()
    }
}
