package de.hbznrw.ygor.readers

import jdk.nashorn.api.scripting.URLReader

import java.nio.charset.Charset

class KbartFromUrlReader extends KbartReader{

  KbartFromUrlReader(URL url, String delimiter, Charset charset, File sessionFolder) throws Exception{
    URLReader urlReader = new URLReader(url, charset)
    String fileData = urlReader.getText()
    init(fileData, delimiter)
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
