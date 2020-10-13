package de.hbznrw.ygor.readers

import jdk.nashorn.api.scripting.URLReader
import ygor.EnrichmentService

import java.nio.charset.Charset

class KbartFromUrlReader extends KbartReader{

  EnrichmentService enrichmentService = new EnrichmentService()

  def messageSource = grails.util.Holders.applicationContext.getBean("messageSource")

  KbartFromUrlReader(URL url, File sessionFolder, Locale locale) throws Exception{
    HttpURLConnection connection
    try {
      connection = (HttpURLConnection) url.openConnection()
      connection.addRequestProperty("User-Agent", "Mozilla/5.0")
    }
    catch (IOException e) {
      throw new RuntimeException("URL Connection was not established.")
    }
    connection.connect()
    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
      String encoding = enrichmentService.getEncoding(connection.getInputStream(), url)
      if (encoding in ["text/plain" /* add further "encoding-neutral" content types here*/ ]){
        throw new IllegalArgumentException(messageSource.getMessage("error.kbart.noEncoding", ["foo"] as Object[], locale))
      }
      if (!("UTF-8".equals(encoding))){
        throw new IllegalArgumentException(messageSource.getMessage("error.kbart.invalidEncoding", ["foo"] as Object[], locale))
      }
    }
    URLReader urlReader = new URLReader(url, Charset.forName("UTF-8"))
    String fileData = urlReader.getText()
    init(fileData)
    // copy content to local file
    file = sessionFolder.absolutePath.concat(File.separator).concat(urlStringToFileString(url.toExternalForm()))
    FileWriter fileWriter = new FileWriter(file)
    fileWriter.write(fileData)
    fileWriter.close()
  }

  static String urlStringToFileString(String url){
    url.replace("://", "_").replace(".", "_").replace("/", "_")
  }
}
