package ygor

import de.hbznrw.ygor.export.structure.Pod
import de.hbznrw.ygor.processing.SendPackageThreadGokb
import de.hbznrw.ygor.processing.UploadThreadGokb
import de.hbznrw.ygor.processing.YgorFeedback
import de.hbznrw.ygor.readers.KbartFromUrlReader
import de.hbznrw.ygor.readers.KbartReader
import grails.util.Holders
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.LocaleUtils
import org.apache.commons.lang.StringUtils
import org.mozilla.universalchardet.UniversalDetector

import javax.annotation.Nonnull
import javax.servlet.http.HttpSession
import org.codehaus.groovy.grails.web.util.WebUtils
import de.hbznrw.ygor.tools.*

class EnrichmentService{

  GokbService gokbService
  KbartReader kbartReader
  static Map<String, UploadJob> UPLOAD_JOBS = new HashMap<>()


  File getFile(Enrichment enrichment, Enrichment.FileType type){
    enrichment.getAsFile(type, true)
  }


  void deleteFileAndFormat(Enrichment enrichment){
    if (enrichment){
      def origin = enrichment.getAsFile(Enrichment.FileType.ORIGIN, false)
      if (origin){
        origin.delete()
      }
      getSessionEnrichments()?.remove(enrichment.resultHash)
      getSessionFormats()?.remove(enrichment.originHash)
    }
  }


  void prepareFile(Enrichment enrichment, Map pm){
    if (enrichment == null || pm == null){
      return
    }
    if (pm['pkgTitle'] && pm['pkgTitle'][0]){
      if (pm['pkgTitle'][0].startsWith("org.gokb.cred.Package:")){
        String[] packageInfo = pm['pkgTitle'][0].split(";")
        if (packageInfo.length == 2){
          enrichment.packageId = Long.valueOf(packageInfo[0].substring(22))
          enrichment.packageName = packageInfo[1]
          def pkg = getPackage(String.valueOf(enrichment.packageId), null, null, null)
          if (pkg != null){
            enrichment.packageUuid = pkg.uuid
          }
        }
      }
      else{
        enrichment.packageName = pm['pkgTitle'][0]
      }
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
    if (pm['pkgNominalProvider'] == null){
      log.error("ParameterMap missing nominalProvider.")
    }
    else{
      def provider = getProvider(pm['pkgNominalProvider'][0], null, pm['pkgCuratoryGroup'][0])
      if (provider != null){
        applyProviderToPackageHeader(provider, ph)
      }
    }
  }


  Map<String, Object> getPackage(String packageId, List<String> embeddedFields, String[] fields, String curatoryGroup){
    if (packageId == null){
      return null
    }
    def uri = Holders.config.gokbApi.packageInfo.toString().concat(packageId)
    uri = gokbService.appendEmbeddedFields(uri, embeddedFields)
    uri = gokbService.appendCuratoryGroup(uri, curatoryGroup)
    List<String, Object> fieldList = new ArrayList()
    if (fields != null){
      fieldList.addAll(fields)
    }
    return gokbRestApiRequest(uri, null, null, fieldList)
  }


  Map<String, Object> getOrganisation(String orgId){
    if (orgId == null){
      return null
    }
    def uri = Holders.config.gokbApi.orgInfo.toString().concat(orgId)
    return gokbRestApiRequest(uri, null, null, null)
  }


  Map<String, Object> getPlatform(String platformId){
    def uri = Holders.config.gokbApi.platformInfo.toString().concat(platformId)
    return gokbRestApiRequest(uri, null, null, Arrays.asList("id", "name", "primaryUrl", "provider", "uuid", "_embedded"))
  }


  Map<String, Object> getTippsOfPackage(String packageUuid, int maxTippCount = 1){
    def uri = Holders.config.gokbApi.xrFindUriStub.toString().concat("?componentType=TitleInstancePackagePlatform&pkg=")
        .concat(packageUuid).concat("&max=").concat(String.valueOf(maxTippCount))
    return gokbRestApiRequest(uri, null, null, null)
  }


  /**
   * @param resultFields Optional. All fields are returned if left empty.
   */
  Map<String, Object> gokbRestApiRequest(@Nonnull String uri, String user, String password, List<String> resultFields){
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
              if (CollectionUtils.isEmpty(resultFields)){
                result.putAll(resultMap)
              }
              else{
                for (String resultField in resultFields){
                  result.put(resultField, resultMap.get(resultField))
                }
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
    if (pm['pkgNominalPlatform'] == null || StringUtils.isEmpty(pm['pkgNominalPlatform'][0])){
      log.error("ParameterMap missing nominalPlatform.")
      return null
    }
    log.debug("Getting platforms for: ${pm['pkgNominalPlatform'][0]}")
    def platformSplit = pm['pkgNominalPlatform'][0].split(';')
    if (platformSplit == null || platformSplit.size() > 2){
      log.error("Could not split platform string.")
      return null
    }
    def platform
    if (platformSplit.size() == 1){
      def platforms = gokbService.getPlatformMap(platformSplit[0], false, null)
      for (def pf in platforms.records){
        if (pf.id?.equals(platformSplit[0])){
          platform = pf
          break
        }
      }
    }
    else if (platformSplit.size() == 2){
      platform = pickPlatform(platformSplit[0], platformSplit[1])
      if (platform == null){
        log.error("No platform found.")
      }
    }
    return platform
  }


  private def pickPlatform(String platFormId, String queryTerm){
    def platforms = gokbService.getPlatformMap(queryTerm, false, null).records
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


  def getProvider(String providerName, List<String> embeddedFields, String curatoryGroup){
    log.debug("Getting providers for: ${providerName}")
    def providers = gokbService.getProviderMap(providerName, embeddedFields, curatoryGroup).records
    def pkgNomProvider = null
    log.debug("Got providers: ${providers}")
    providers.each{
      if ((providerName == null || it.name == providerName) && it.status == "Current"){
        if (pkgNomProvider){
          log.warn("Multiple providers found named: ".concat(pkgNomProvider.name).concat(" and ").concat(it.name))
        }
        else{
          log.debug("Set ${it.name} as nominalProvider.")
          pkgNomProvider = it
        }
      }
      else{
        if (providerName != null && it.name != providerName){
          log.debug("No name match: ${it.name} - ${providerName}")
        }
        if (it.status != "Current"){
          log.debug("Wrong status: ${it.status}")
        }
      }
    }
    if (pkgNomProvider == null){
      log.error("No provider found.")
    }
    return pkgNomProvider
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
    packageHeader.nominalPlatform.oid = platform.oid
  }


  private void applyProviderToPackageHeader(def provider, def packageHeader){
    packageHeader.nominalProvider.name = provider.name
    packageHeader.nominalProvider.oid = provider.oid
  }


  def addSessionEnrichment(Enrichment enrichment){
    HttpSession session = SessionToolkit.getSession()
    if (!session.enrichments){
      session.enrichments = [:]
    }
    session.enrichments.put(enrichment.resultHash, enrichment)
  }


  static def getSessionEnrichments(){
    HttpSession session = SessionToolkit.getSession()
    if (!session.enrichments){
      session.enrichments = [:]
    }
    session.enrichments
  }


  static def getSessionFormats(){
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
  static File getSessionFolder(){
    def session = WebUtils.retrieveGrailsWebRequest().session
    def path = grails.util.Holders.grailsApplication.config.ygor.uploadLocation.toString().concat(File.separator).concat(session.id)
    def sessionFolder = new File(path)
    if (!sessionFolder.exists()){
      sessionFolder.mkdirs()
    }
    sessionFolder
  }


  /**
   * used by AutoUpdateService
   */
  UploadJob buildCompleteUpdateProcess(Enrichment enrichment, YgorFeedback ygorFeedback){
    try{
      String urlString = StringUtils.isEmpty(enrichment.updateUrl) ? enrichment.originUrl : enrichment.updateUrl
      URL originUrl = new URL(urlString)
      kbartReader = new KbartFromUrlReader(originUrl, enrichment.sessionFolder, LocaleUtils.toLocale(enrichment.locale),
          ygorFeedback)
      enrichment.dataContainer.records = []
      new File(enrichment.enrichmentFolder).mkdirs()
      return processComplete(enrichment, null, null, true, true)
    }
    catch (Exception e){
      log.error(e.getMessage())
      log.error("Could not process update ".concat(enrichment?.resultName))
    }
  }


  /**
   * used by AutoUpdateService    --> processCompleteUpdate
   * used by EnrichmentController --> processCompleteWithToken
   */
  UploadJob processComplete(Enrichment enrichment, String gokbUsername, String gokbPassword, boolean isUpdate,
                            boolean needsPreciseClassification, boolean waitForFinish, YgorFeedback ygorFeedback) {
    UploadJobFrame uploadJob = new UploadJobFrame(Enrichment.FileType.PACKAGE_WITH_TITLEDATA)
    return processComplete(uploadJob, enrichment, gokbUsername, gokbPassword, isUpdate, needsPreciseClassification, waitForFinish, ygorFeedback)
  }


  /**
   * used by EnrichmentService    --> processComplete
   */
  UploadJob processComplete(@Nonnull UploadJobFrame uploadJobFrame, @Nonnull Enrichment enrichment, String gokbUsername,
                            String gokbPassword, boolean waitForFinish, YgorFeedback ygorFeedback) {
    def options = [
        'options'        : enrichment.processingOptions,
        'addOnly'        : enrichment.addOnly,
        'ygorVersion'    : Holders.config.ygor.version,
        'ygorType'       : Holders.config.ygor.type
    ]
    enrichment.process(options, kbartReader, ygorFeedback)
    while (enrichment.status in [Enrichment.ProcessingState.PREPARE_1, Enrichment.ProcessingState.PREPARE_2,
                                 Enrichment.ProcessingState.WORKING, null]){
      Thread.sleep(1000)
    }
    // Main processing finished here.
    if (enrichment.status == Enrichment.ProcessingState.ERROR){
      ygorFeedback.statusDescription += " Error occured during main processing phase."
      ygorFeedback.reportingComponent = EnrichmentService.class
      ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.ERROR
      return null
    }
    // Upload is following - send package with integrated title data
    String uri = Holders.config.gokbApi.xrPackageUri.concat("?async=true")
    SendPackageThreadGokb sendPackageThreadGokb
    if (!StringUtils.isEmpty(enrichment.dataContainer.pkgHeader.token)){
      // send with token-based authentication
      sendPackageThreadGokb = new SendPackageThreadGokb(enrichment, uri, true, ygorFeedback)
    }
    else{
      // send with basic auth
      sendPackageThreadGokb = new SendPackageThreadGokb(enrichment, uri, gokbUsername, gokbPassword, true, ygorFeedback)
    }
    UploadJob uploadJob = uploadJobFrame.toUploadJob(sendPackageThreadGokb)
    addUploadJob(uploadJob)
    uploadJob.start()
    if (waitForFinish){
      while (uploadJob.getStatus() in [UploadThreadGokb.Status.PREPARATION, UploadThreadGokb.Status.STARTED]){
        Thread.sleep(1000)
        uploadJob.updateCount()
        uploadJob.refreshStatus()
      }
    }
    return uploadJob
  }


  String getEncoding(def inputStream, URL uri){
    String encoding
    try{
      encoding = UniversalDetector.detectCharset(inputStream)
      if (encoding == null){
        encoding = URLConnection.guessContentTypeFromStream(inputStream)
        if (encoding == null){
          if (uri != null){
            encoding = URLConnection.guessContentTypeFromName(uri.toString())
          }
          if (encoding == null){
            throw new IllegalStateException("Could not determine encoding of the KBart file.")
          }
        }
        if (StringUtils.containsIgnoreCase(encoding, "UTF-8")){
          return "UTF-8"
        }
      }
    }
    catch (Exception e){
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


  void addUploadJob(UploadJobFrame uploadJob){
    if (uploadJob != null){
      log.debug("Adding ".concat(uploadJob.getClass().getName()).concat(" with uuid ").concat(uploadJob.uuid))
      UPLOAD_JOBS.put(uploadJob.uuid, uploadJob)
    }
  }


  UploadJobFrame getUploadJob(String uuid){
    if (uuid == null){
      return null
    }
    UploadJobFrame uploadJob = UPLOAD_JOBS.get(uuid)
    log.debug("Getting upload job for uuid ".concat(uuid).concat(" : ").concat(uploadJob?.getClass()?.getName()))
    return uploadJob
  }


  Enrichment setupEnrichment(Enrichment enrichment, KbartReader kbartReader, String addOnly, def pmOptions,
                             String platformName, String platformUrl, def params, def titleIdNamespace,
                             String pkgTitle, String pkgCuratoryGroup, String pkgId, String pkgNominalPlatform,
                             String pkgNominalProvider, String updateToken, String uuid, String lastUpdated,
                             boolean ignoreLastChanged){
    kbartReader.checkHeader()
    Map<String, Object> parameterMap = new HashMap<>()
    parameterMap.putAll(params)
    parameterMap.put("pkgTitleId", [titleIdNamespace])
    addParameterToParameterMap("pkgTitle", pkgTitle, parameterMap)
    addParameterToParameterMap("pkgCuratoryGroup", pkgCuratoryGroup, parameterMap)
    addParameterToParameterMap("pkgId", pkgId, parameterMap)
    addParameterToParameterMap("pkgNominalPlatform", pkgNominalPlatform, parameterMap)
    addParameterToParameterMap("pkgNominalProvider", pkgNominalProvider, parameterMap)
    prepareFile(enrichment, parameterMap)
    enrichment.addOnly = (addOnly.equals("on") || addOnly.equals("true")) ? true : false
    enrichment.ignoreLastChanged = ignoreLastChanged
    enrichment.processingOptions = EnrichmentService.decodeApiCalls(pmOptions)
    enrichment.dataContainer.pkgHeader.token = updateToken
    enrichment.dataContainer.pkgHeader.uuid = uuid
    enrichment.dataContainer.pkgHeader.nominalPlatform.name = platformName
    enrichment.dataContainer.pkgHeader.nominalPlatform.url = platformUrl
    if (lastUpdated != null){
      enrichment.lastProcessingDate = lastUpdated
      enrichment.isUpdate = true
      enrichment.needsPreciseClassification = false
    }
    enrichment
  }


  static private void addParameterToParameterMap(String parameterName, String parameterValue, Map<String, String[]> parameterMap){
    if (parameterMap == null){
      parameterMap = new HashMap<>()
    }
    String[] value = new String[1]
    value[0] = parameterValue
    parameterMap.put(parameterName, value)
  }


  static String getLastRun(Map<String, Object> pkg){
    if (!StringUtils.isEmpty(pkg._embedded.source.lastRun)){
      return pkg._embedded.source.lastRun
    }
    return null
  }

}
