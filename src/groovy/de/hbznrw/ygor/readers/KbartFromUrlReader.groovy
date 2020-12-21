package de.hbznrw.ygor.readers

import de.hbznrw.ygor.tools.UrlToolkit
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import ygor.EnrichmentService

import java.nio.file.Files

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
    fileName = sessionFolder.absolutePath.concat(File.separator).concat(urlStringToFileString(url.toExternalForm()))
    File file = new File(fileName)
    // connection.setConnectTimeout(2000)
    // connection.setReadTimeout(30000)
    connection = UrlToolkit.resolveRedirects(connection, 5)
    byte[] content = getByteContent(connection.getInputStream())
    InputStream inputStream = new ByteArrayInputStream(content)
    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
      String encoding = enrichmentService.getEncoding(inputStream, url)
      if (encoding in ["text/plain", "text/tab-separated-values" /* add further "encoding-neutral" content types here*/]){
        // There was no encoding found in the url connection.
        encoding = enrichmentService.getEncoding(inputStream, null)
      }
      if (!(encoding in [null, "UTF-8"])){
        throw new IllegalArgumentException(messageSource.getMessage("error.kbart.invalidEncoding", ["foo"] as Object[], locale))
      }
    }
    FileUtils.copyInputStreamToFile(new ByteArrayInputStream(content), file)
    init(file)
    // copy content to local file
    Files.write(file.toPath(), content)
  }


  private byte[] getByteContent(InputStream inputStream){
    ByteArrayOutputStream baos = new ByteArrayOutputStream()
    byte[] buf = new byte[4096]
    int n = 0
    while ((n = inputStream.read(buf)) >= 0){
      baos.write(buf, 0, n)
    }
    baos.toByteArray()
  }


  static String urlStringToFileString(String url){
    url.replace("://", "_").replace(".", "_").replace("/", "_")
  }
}
