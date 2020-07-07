package de.hbznrw.ygor.readers

import jdk.nashorn.api.scripting.URLReader

import java.nio.charset.Charset

class KbartFromUrlReader extends KbartReader{

  KbartFromUrlReader(URL url, String delimiter, Charset charset) throws Exception{
    URLReader urlReader = new URLReader(url, charset)
    String fileData = urlReader.getText()
    init(fileData, delimiter)
    // copy content to local file
    FileWriter fileWriter = new FileWriter(urlStringToFileString(url.toExternalForm()))
    fileWriter.write(fileData)
    fileWriter.close()
  }

  KbartFromUrlReader(URL url , String delimiter) throws Exception{
    this(url, delimiter, Charset.forName("UTF-8"))
  }

  static String urlStringToFileString(String url){
    url.replace("://", "_").replace(".", "_").replace("/", "_")
  }
}
