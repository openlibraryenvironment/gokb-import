package ygor

import de.hbznrw.ygor.export.structure.Pod
import de.hbznrw.ygor.processing.SendPackageThreadGokb
import de.hbznrw.ygor.processing.YgorProcessingException
import de.hbznrw.ygor.readers.KbartFromUrlReader
import de.hbznrw.ygor.readers.KbartReader
import grails.util.Holders
import org.apache.commons.io.IOUtils
import org.mozilla.universalchardet.UniversalDetector
import ygor.field.FieldKeyMapping

import javax.annotation.Nonnull
import javax.servlet.http.HttpSession
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.codehaus.groovy.grails.web.util.WebUtils
import de.hbznrw.ygor.tools.*

import java.nio.charset.Charset

class EnrichmentService{

  def grailsApplication
  GokbService gokbService
  KbartReader kbartReader

  Enrichment fromCommonsMultipartFile(CommonsMultipartFile file){
    Enrichment en = fromFilename(file.originalFilename)
    file.transferTo(new File(en.originPathName))
    return en
  }

  Enrichment fromFilename(String filename){
    return new Enrichment(getSessionFolder(), filename)
  }

  void addFileAndFormat(Enrichment en, String delimiter, String quote, String quoteMode){
    en.setStatus(Enrichment.ProcessingState.PREPARE_1)
    def tmp = [:]
    tmp << ['delimiter': delimiter]
    tmp << ['quote': quote]
    tmp << ['quoteMode': quoteMode]
    def formats = getSessionFormats()
    formats << ["${en.originHash}": tmp]
    getSessionEnrichments() << ["${en.resultHash.toString()}": en]
  }


  File getFile(Enrichment enrichment, Enrichment.FileType type){
    enrichment.getAsFile(type, true)
  }


  void deleteFileAndFormat(Enrichment enrichment){
    if (enrichment){
      def origin = enrichment.getAsFile(Enrichment.FileType.ORIGIN, false)
      if (origin){
        origin.delete()
      }
      getSessionEnrichments()?.remove("${enrichment.resultHash}")
      getSessionEnrichments()?.remove(enrichment.resultHash)
      getSessionFormats()?.remove("${enrichment.originHash}")
      getSessionFormats()?.remove(enrichment.originHash)
    }
  }


  void prepareFile(Enrichment enrichment, Map pm){
    if (enrichment == null || pm == null){
      return
    }
    def ph = enrichment.dataContainer.pkg.packageHeader
    if (pm['pkgTitle']){
      ph.name = new Pod(pm['pkgTitle'][0])
      enrichment.packageName = pm['pkgTitle'][0]
    }
    if (pm['addOnly'] && pm['addOnly'][0] in ["on", "true"]){
      enrichment.addOnly = true
    }
    if (pm['pkgIsil'] && pm['pkgIsil'][0]){
      enrichment.dataContainer.isil = pm['pkgIsil'][0]
    }
    if (pm['pkgCuratoryGroup']){
      enrichment.dataContainer.curatoryGroup = (pm['pkgCuratoryGroup'][0])
    }
    if (pm['pkgId'] && "" != pm['pkgId'][0].trim()){
      enrichment.dataContainer.pkgId = (pm['pkgId'][0])
    }
    if (pm['pkgIdNamespace'] && "" != pm['pkgIdNamespace'][0].trim()){
      enrichment.dataContainer.pkgIdNamespace = (pm['pkgIdNamespace'][0])
    }
    def platform = getPlatform(pm)
    if (platform != null){
      applyPlatformToPackageHeader(platform, ph)
    }
    if (pm['pkgNominalProvider']){
      ph.nominalProvider = pm['pkgNominalProvider'][0]
    }
    if (pm['pkgTitleId']){
      enrichment.dataContainer.info.namespace_title_id = pm['pkgTitleId'][0]
    }
    enrichment.setStatus(Enrichment.ProcessingState.PREPARE_2)
  }


  private def getPlatform(Map pm){
    log.debug("Getting platforms for: ${pm['pkgNominalPlatform'][0]}")
    def platformSplit = splitPlatformString(pm['pkgNominalPlatform'][0])
    if (platformSplit == null || platformSplit.size() != 2){
      log.error("Could not split platform string.")
      return
    }
    def platform = pickPlatform(platformSplit[0], platformSplit[1])
    if (platform == null){
      log.error("No platform found.")
    }
    return platform
  }


  private def pickPlatform(String platFormId, String queryTerm){
    def platforms = gokbService.getPlatformMap(queryTerm, false).records
    def pkgNomPlatform = null
    log.debug("Got platforms: ${platforms}")
    platforms.each{
      if (it.name == queryTerm && it.status == "Current" && it.oid == platFormId){
        if (pkgNomPlatform){
          log.warn("Multiple platforms found named: ".concat(it.name))
        }
        else{
          log.debug("Set ${it.name} as nominalPlatform.")
          pkgNomPlatform = it
        }
      }
      else{
        if (it.name != queryTerm){
          log.debug("No name match: ${it.name} - ${queryTerm}")
        }
        if (it.status != "Current"){
          log.debug("Wrong status: ${it.status}")
        }
        if (it.oid != platFormId){
          log.debug("No OID match: ${it.oid} - ${platFormId}")
        }
      }
    }
    return pkgNomPlatform
  }


  private void applyPlatformToPackageHeader(def platform, def packageHeader){
    try{
      new URL(platform.url)
      packageHeader.nominalPlatform.url = platform.url
    }
    catch (MalformedURLException e){
      packageHeader.nominalPlatform.url = ""
    }
    packageHeader.nominalPlatform.name = platform.name
  }


  /**
   * Splits into an array of platformId and platformName (query term)
   */
  private def splitPlatformString(String platformString){
    def tmp = platformString.split(';')
    if (tmp.size() != 2){
      return null
    }
    return [tmp[0], tmp[1]]
  }


  def addSessionEnrichment(Enrichment enrichment){
    HttpSession session = SessionToolkit.getSession()
    if (!session.enrichments){
      session.enrichments = [:]
    }
    session.enrichments.put(enrichment.resultHash.toString(), enrichment)
  }


  def getSessionEnrichments(){
    HttpSession session = SessionToolkit.getSession()
    if (!session.enrichments){
      session.enrichments = [:]
    }
    session.enrichments
  }


  def getSessionFormats(){
    HttpSession session = SessionToolkit.getSession()
    if (!session.formats){
      session.formats = [:]
    }
    session.formats
  }


  /**
   * Return session depending directory for file upload.
   * Creates if not existing.
   */
  File getSessionFolder(){
    def session = WebUtils.retrieveGrailsWebRequest().session
    def path = grailsApplication.config.ygor.uploadLocation + File.separator + session.id
    def sessionFolder = new File(path)
    if (!sessionFolder.exists()){
      sessionFolder.mkdirs()
    }
    sessionFolder
  }


  Enrichment enrichmentFromFile(CommonsMultipartFile commonsMultipartFile,
                                def foDelimiter, def foQuote, def foQuoteMode){
    String fileName = commonsMultipartFile.originalFilename
    String encoding = getEncoding(commonsMultipartFile)
    if (encoding != "UTF-8"){
      log.error(String.format("Transferred file has encoding %s. Aborting.", encoding))
      return
    }
    try{
      kbartReader = new KbartReader(new InputStreamReader(commonsMultipartFile.getInputStream()), foDelimiter)
      kbartReader.checkHeader()
    }
    catch (YgorProcessingException ype){
      log.error("Aborting on KBart header check for file " + fileName)
      return
    }
    Enrichment enrichment = addFileAndFormat(commonsMultipartFile, foDelimiter, foQuote, foQuoteMode)
    return enrichment
  }


  UploadJob processCompleteNoInteraction(Enrichment enrichment, List<String> pmOptions, def foDelimiter, foQuote, foQuoteMode,
                                         recordSeparator, addOnly, gokbUsername, gokbPassword, String locale){
    enrichment.kbartRecordSeparator = recordSeparator
    enrichment.processingOptions = pmOptions
    enrichment.kbartDelimiter = foDelimiter
    enrichment.kbartQuote = foQuote
    enrichment.kbartQuoteMode = foQuoteMode
    enrichment.locale = locale
    processComplete(enrichment, recordSeparator, addOnly, gokbUsername, gokbPassword, false, null)
  }


  UploadJob processCompleteUpdate(Enrichment enrichment){
    try{
      URL originUrl = new URL(enrichment.originUrl)
      kbartReader = new KbartFromUrlReader(originUrl, enrichment.kbartDelimiter, Charset.forName("UTF-8"),
          enrichment.sessionFolder)
      enrichment.dataContainer.records = []
      processComplete(enrichment, enrichment.addOnly, null, null, true,
          enrichment.dataContainer.pkg.packageHeader.token)
    }
    catch (Exception e){
      log.error("Could not process update ".concat(enrichment?.resultName))
    }
  }


  private UploadJob processComplete(Enrichment enrichment, boolean addOnly, String gokbUsername, String gokbPassword,
                                    boolean isUpdate, String token){
    FieldKeyMapping tippNameMapping =
        enrichment.setTippPlatformNameMapping(enrichment.dataContainer.pkg.packageHeader.nominalPlatform.name)
    enrichment.enrollMappingToRecords(tippNameMapping)
    FieldKeyMapping tippUrlMapping =
        enrichment.setTippPlatformUrlMapping(enrichment.dataContainer.pkg.packageHeader.nominalPlatform.url)
    enrichment.enrollMappingToRecords(tippUrlMapping)
    def options = [
        'options'        : enrichment.processingOptions,
        'delimiter'      : enrichment.kbartDelimiter,
        'quote'          : enrichment.kbartQuote,
        'quoteMode'      : enrichment.kbartQuoteMode,
        'recordSeparator': enrichment.kbartRecordSeparator,
        'addOnly'        : addOnly,
        'ygorVersion'    : Holders.config.ygor.version,
        'ygorType'       : Holders.config.ygor.type
    ]
    enrichment.process(options, kbartReader)
    while (enrichment.status != Enrichment.ProcessingState.FINISHED){
      Thread.sleep(1000)
    }
    // Main processing finished here.
    // Upload is following - send package with integrated title data
    String uri = Holders.config.gokbApi.xrPackageUri
    SendPackageThreadGokb sendPackageThreadGokb
    if (isUpdate){
      sendPackageThreadGokb = new SendPackageThreadGokb(enrichment, uri, enrichment.locale)
    }
    else{
      sendPackageThreadGokb = new SendPackageThreadGokb(enrichment, uri,
          gokbUsername, gokbPassword, enrichment.locale, true)
    }
    UploadJob uploadJob = new UploadJob(Enrichment.FileType.PACKAGE, sendPackageThreadGokb)
    uploadJob.start()
    return uploadJob
  }


  String getEncoding(def inputStream){
    String encoding
    try{
      encoding = UniversalDetector.detectCharset(inputStream)
    }
    catch (IllegalStateException ise){
      ByteArrayOutputStream baos = new ByteArrayOutputStream()
      IOUtils.copy(inputStream, baos)
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())
      encoding = UniversalDetector.detectCharset(inputStream)
    }
    log.debug("Detected encoding ${encoding}")
    encoding
  }


  static List<String> decodeApiCalls(def apiCalls){
    if (apiCalls == null){
      return new ArrayList()
    }
    if (apiCalls instanceof Collection){
      return new ArrayList(apiCalls)
    }
    if (apiCalls.getClass().isArray()){
      return Arrays.asList(apiCalls)
    }
    if (apiCalls instanceof String){
      // remove all kinds of braces
      apiCalls = apiCalls.replaceAll("[{}[\\\\]()]", "")
      def split = apiCalls.split(",")
      // check if it is a comma-separated list
      if (split.size() > 1){
        return Arrays.asList(split)
      }
      // eventually split by semicolon
      return Arrays.asList(split[0].split(";"))
    }
  }
}
