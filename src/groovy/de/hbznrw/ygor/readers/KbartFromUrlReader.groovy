package de.hbznrw.ygor.readers

import de.hbznrw.ygor.processing.YgorProcessingException
import jdk.nashorn.api.scripting.URLReader

import java.nio.charset.Charset

class KbartFromUrlReader extends KbartReader{

  KbartFromUrlReader(URL url , String delimiter, Charset charset) throws YgorProcessingException{
    URLReader urlReader = new URLReader(url, charset)
    init(urlReader, delimiter)
  }

  KbartFromUrlReader(URL url , String delimiter) throws YgorProcessingException{
    this(url, delimiter, Charset.forName("UTF-8"))
  }

}
