package de.hbznrw.ygor.readers

import jdk.nashorn.api.scripting.URLReader
import ygor.EnrichmentService

import java.nio.charset.Charset

class KbartFromUrlReader extends KbartReader{

  EnrichmentService enrichmentService = new EnrichmentService()

  KbartFromUrlReader(URL url, File sessionFolder) throws Exception{
    String encoding = enrichmentService.getEncoding(url.openStream())
    if (!("UTF-8".equals(encoding))){
      throw new IllegalFormatException("Encoding of KBart file at ".concat(url.toExternalForm()).concat(" was ")
          .concat(encoding).concat(" Only UTF-8 is allowed."))
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
