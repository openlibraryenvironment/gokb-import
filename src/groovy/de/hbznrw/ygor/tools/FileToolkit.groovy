package de.hbznrw.ygor.tools

import ygor.Record

import java.security.MessageDigest
import java.nio.file.Paths
import org.codehaus.groovy.runtime.DateGroovyMethods
import org.springframework.core.io.Resource
import grails.util.Holders

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileToolkit {

  static String getDateTimePrefixedFileName(filename) {
    def p1 = Paths.get(filename).getParent()
    def p2 = DateGroovyMethods.format(new Date(), 'yyyyMMdd-HHmm-')
    def p3 = Paths.get(filename).getFileName()

    if (null != p1)
      return p1.toString() + File.separator + p2 + p3.toString()

    p2 + p3.toString()
  }


  static String getMD5Hash(String s) {
    MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8")).encodeHex().toString()
  }


  static Resource getResourceByClassPath(String path) {
    Holders.getGrailsApplication().parentContext.getResource('classpath:' + path)
  }


  static File zipFiles(File sessionFolder, String resultHash) throws IOException{
    if (!sessionFolder.isDirectory()){
      throw new IOException("Could not read from session directory.")
    }
    File enrichmentFolder = new File(sessionFolder.getAbsolutePath().concat(File.separator).concat(resultHash))
    File resultZip = new File(enrichmentFolder.getAbsolutePath().concat(".zip"))
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(resultZip))
    // zip header and configuration file
    File configFile = new File(enrichmentFolder.getAbsolutePath().concat(File.separator).concat(resultHash))
    zos.putNextEntry(new ZipEntry(configFile.getName()))
    streamByteArray(configFile, zos)
    // zip record files
    for (File record : enrichmentFolder.listFiles(new RecordFileFilter(resultHash))) {
      ZipEntry ze = new ZipEntry(record.getName())
      zos.putNextEntry(ze)
      streamByteArray(record, zos)
    }
    zos.close()
    return resultZip
  }


  private static void streamByteArray(File record, ZipOutputStream zos){
    FileInputStream fis = new FileInputStream(record)
    byte[] bytes = new byte[1024]
    int length
    while ((length = fis.read(bytes)) >= 0){
      zos.write(bytes, 0, length)
    }
    fis.close()
  }

}
