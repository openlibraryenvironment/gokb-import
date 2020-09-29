package ygor

import de.hbznrw.ygor.export.structure.Pod
import de.hbznrw.ygor.processing.SendPackageThreadGokb
import de.hbznrw.ygor.processing.YgorProcessingException
import de.hbznrw.ygor.readers.KbartFromUrlReader
import de.hbznrw.ygor.readers.KbartReader
import grails.util.Holders
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.mozilla.universalchardet.UniversalDetector
import ygor.field.FieldKeyMapping

import javax.servlet.http.HttpSession
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.codehaus.groovy.grails.web.util.WebUtils
import de.hbznrw.ygor.tools.*

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

  void addFileAndFormat(Enrichment en){
    en.setStatus(Enrichment.ProcessingState.PREPARE_1)
    def tmp = [:]
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
    if (pm['pkgTitle']){
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
    if (pm['pkgTitleId']){
      enrichment.dataContainer.info.namespace_title_id = pm['pkgTitleId'][0]
    }
  }


  void preparePackageHeader(Enrichment enrichment, Map pm){
    if (enrichment == null || pm == null){
      return
    }
    def ph = enrichment.dataContainer.pkgHeader
    if (pm['pkgTitle']){
      ph.name = new Pod(pm['pkgTitle'][0])
    }
    def platform = getPlatform(pm)
    if (platform != null){
      applyPlatformToPackageHeader(platform, ph)
    }
    if (pm['pkgNominalProvider']){
      ph.nominalProvider = pm['pkgNominalProvider'][0]
    }
  }


  Map<String, Object> getPackage(String packageId){
    def uri = Holders.config.gokbApi.packageInfo.toString().concat(packageId)
    return gokbRestApiRequest(uri, null, null, Arrays.asList("id", "name", "nominalPlatform", "provider",
                                                              "uuid", "_embedded"))
  }


  Map<String, Object> getPlatform(String platformId){
    def uri = Holders.config.gokbApi.platformInfo.toString().concat(platformId)
    return gokbRestApiRequest(uri, null, null, Arrays.asList("id", "name", "primaryUrl", "provider", "uuid", "_embedded"))
  }


  Map<String, Object> gokbRestApiRequest(String uri, String user, String password, List<String> resultFields){
    if (StringUtils.isEmpty(uri)){
      return null
    }
    def http = new HTTPBuilder(uri)
    if (user != null && password != null){
      http.auth.basic user, password
    }
    Map<String, Object> result = new HashMap<>()
    http.request(Method.GET, ContentType.JSON){ req ->
      response.success = { response, resultMap ->
        if (response.headers.'Content-Type' == 'application/json;charset=UTF-8'){
          if (response.status < 400){
            if (resultMap.result.equals("ERROR")){
              result.put('responseStatus', 'error')
              result.putAll(resultMap)
            }
            else{
              result.put('responseStatus', 'ok')
              for (String resultField in resultFields){
                result.put(resultField, resultMap.get(resultField))
              }
            }
          }
          else{
            result.put('responseStatus', 'warning')
            result.putAll(resultMap)
          }
        }
        else{
          result.put('responseStatus', 'authenticationError')
        }
      }
      response.failure = { response, resultMap ->
        if (response.headers.'Content-Type' == 'application/json;charset=UTF-8'){
          result.put('responseStatus', 'error')
          result.putAll(resultMap)
        }
        else{
          result.put('responseStatus', 'authenticationError')
        }
      }
      response.'401'= {resp ->
        result.put('responseStatus', 'authenticationError')
      }
    }
    result
  }


  def getPlatform(Map pm){
    if (pm['pkgNominalPlatform'] == null){
      log.error("ParameterMap missing nominalPlatform.")
      return null
    }
    log.debug("Getting platforms for: ${pm['pkgNominalPlatform'][0]}")
    def platformSplit = splitPlatformString(pm['pkgNominalPlatform'][0])
    if (platformSplit == null || platformSplit.size() != 2){
      log.error("Could not split platform string.")
      return null
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
      if ((queryTerm == null || it.name == queryTerm) && it.status == "Current" && it.oid == platFormId){
        if (pkgNomPlatform){
          log.warn("Multiple platforms found named: ".concat(pkgNomPlatform.name).concat(" and ").concat(it.name))
        }
        else{
          log.debug("Set ${it.name} as nominalPlatform.")
          pkgNomPlatform = it
        }
      }
      else{
        if (queryTerm != null && it.name != queryTerm){
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


  Enrichment enrichmentFromFile(CommonsMultipartFile commonsMultipartFile){
    String fileName = commonsMultipartFile.originalFilename
    String encoding = getEncoding(commonsMultipartFile.getInputStream())
    if (encoding != "UTF-8"){
      log.error(String.format("Transferred file has encoding %s. Aborting.", encoding))
      return
    }
    try{
      kbartReader = new KbartReader(new InputStreamReader(commonsMultipartFile.getInputStream()))
      kbartReader.checkHeader()
    }
    catch (YgorProcessingException ype){
      log.error("Aborting on KBart header check for file " + fileName)
      return
    }
    Enrichment enrichment = addFileAndFormat(commonsMultipartFile)
    return enrichment
  }


  /**
   * used by AutoUpdateService
   */
  UploadJob processCompleteUpdate(Enrichment enrichment){
    try{
      URL originUrl = new URL(enrichment.originUrl)
      kbartReader = new KbartFromUrlReader(originUrl, enrichment.sessionFolder)
      enrichment.dataContainer.records = []
      new File(enrichment.enrichmentFolder).mkdirs()
      processComplete(enrichment, null, null, true)
    }
    catch (Exception e){
      log.error(e.getMessage())
      log.error("Could not process update ".concat(enrichment?.resultName))
    }
  }


  /**
   * used by AutoUpdateService --> processCompleteUpdate
   * used by                       processCompleteWithToken
   */
  UploadJob processComplete(Enrichment enrichment, String gokbUsername, String gokbPassword, boolean isUpdate){
    def options = [
        'options'        : enrichment.processingOptions,
        'addOnly'        : enrichment.addOnly,
        'ygorVersion'    : Holders.config.ygor.version,
        'ygorType'       : Holders.config.ygor.type
    ]
    enrichment.process(options, kbartReader)
    while (enrichment.status != Enrichment.ProcessingState.FINISHED){
      Thread.sleep(1000)
    }
    FieldKeyMapping tippNameMapping =
        enrichment.setTippPlatformNameMapping(enrichment.dataContainer?.pkgHeader?.nominalPlatform.name)
    enrichment.enrollMappingToRecords(tippNameMapping)
    FieldKeyMapping tippUrlMapping =
        enrichment.setTippPlatformUrlMapping(enrichment.dataContainer?.pkgHeader?.nominalPlatform.url)
    enrichment.enrollMappingToRecords(tippUrlMapping)
    // Main processing finished here.
    // Upload is following - send package with integrated title data
    String uri = Holders.config.gokbApi.xrPackageUri
    SendPackageThreadGokb sendPackageThreadGokb
    if (!StringUtils.isEmpty(enrichment.dataContainer.pkgHeader.token)){
      // send with token-based authentification
      sendPackageThreadGokb = new SendPackageThreadGokb(enrichment, uri, enrichment.locale)
    }
    else{
      // send with basic auth
      sendPackageThreadGokb = new SendPackageThreadGokb(enrichment, uri,
          gokbUsername, gokbPassword, enrichment.locale, true)
    }
    UploadJob uploadJob = new UploadJob(Enrichment.FileType.PACKAGE_WITH_TITLEDATA, sendPackageThreadGokb)
    uploadJob.start()
    while (uploadJob.status in [UploadJob.Status.PREPARATION, UploadJob.Status.STARTED]){
      Thread.sleep(1000)
    }
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
