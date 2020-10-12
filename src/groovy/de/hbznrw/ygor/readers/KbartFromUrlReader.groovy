package de.hbznrw.ygor.readers

import jdk.nashorn.api.scripting.URLReader
import ygor.EnrichmentService

import java.nio.charset.Charset

class KbartFromUrlReader extends KbartReader{

  EnrichmentService enrichmentService = new EnrichmentService()

  KbartFromUrlReader(URL url, File sessionFolder) throws Exception{
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
      if (encoding in ["text/plain" /* add further encoding "neutral" content types here*/ ]){
        throw new IllegalArgumentException("No encoding found for KBart file at ".concat(url.toExternalForm())
            .concat(". File needs to be UTF-8 encoded."))
      }
      if (!("UTF-8".equals(encoding))){
        throw new IllegalArgumentException("Encoding of KBart file at ".concat(url.toExternalForm()).concat(" was ")
            .concat(encoding).concat(". Only UTF-8 is allowed."))
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
