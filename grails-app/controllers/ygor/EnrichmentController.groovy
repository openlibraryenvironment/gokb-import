package ygor

import de.hbznrw.ygor.processing.CompleteProcessingThread
import de.hbznrw.ygor.processing.UploadThreadGokb
import de.hbznrw.ygor.readers.KbartFromUrlReader
import de.hbznrw.ygor.readers.KbartReader
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.apache.commons.collections.MapUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.springframework.web.multipart.commons.CommonsMultipartFile

import javax.servlet.http.HttpServletRequest


@Log4j
class EnrichmentController implements ControllersHelper{

  static scope = "session"

  EnrichmentService enrichmentService
  GokbService gokbService
  KbartReader kbartReader

  def index = {
    redirect(action: 'process')
  }


  def process = {
    SessionService.setSessionDuration(request, 3600)
    def namespace_list = gokbService.getNamespaceList()
    def namespace_doi_list = []
    def gokb_cgs = gokbService.getCuratoryGroupsList()
    namespace_doi_list.addAll(namespace_list)
    namespace_doi_list  << [id: 'doi', text: 'doi']
    Enrichment en = getCurrentEnrichment()
    setErrorStatus(en)
    render(
        view: 'process',
        params: [
            resultHash: request.parameterMap.resultHash,
            originHash: request.parameterMap.originHash
        ],
        model: [
            enrichment        : en,
            gokbService       : gokbService,
            pkg_namespaces    : namespace_list,
            record_namespaces : namespace_doi_list,
            curatoryGroups    : gokb_cgs,
            currentView       : 'process'
        ]
    )
  }


  def json = {
    render(
        view: 'json',
        model: [
            enrichment : getCurrentEnrichment(),
            currentView: 'json'
        ]
    )
  }


  def howto = {
    render(
        view: 'howto',
        model: [currentView: 'howto']
    )
  }


  def about = {
    render(
        view: 'about',
        model: [currentView: 'about']
    )
  }


  def config = {
    render(
        view: 'config',
        model: [currentView: 'config']
    )
  }


  def contact = {
    render(
        view: 'contact',
        model: [currentView: 'contact']
    )
  }


  def uploadFile = {
    SessionService.setSessionDuration(request, 3600)
    def file = request.getFile('uploadFile')
    if (file.size < 1 && request.parameterMap.uploadFileLabel != null &&
        request.parameterMap.uploadFileLabel[0] == request.session.lastUpdate?.file?.originalFilename){
      // the file form is unpopulated but the previously selected file is unchanged
      file = request.session.lastUpdate.file
    }

    String encoding = enrichmentService.getEncoding(file.getInputStream(), null)
    if (encoding && encoding != "UTF-8"){
      flash.info = null
      flash.warning = null
      flash.error = message(code: 'error.kbart.invalidEncoding').toString().concat("<br>")
          .concat(message(code: 'error.kbart.messageFooter').toString())
      redirect(
          action: 'process'
      )
      return
    }

    def addOnly = false
    setInputFieldDataToLastUpdate(file, null, addOnly)

    if (file.empty){
      flash.info = null
      flash.warning = null
      flash.error = message(code: 'error.kbart.noValidFile').toString().concat("<br>")
          .concat(message(code: 'error.kbart.messageFooter').toString())
      Enrichment enrichment = getCurrentEnrichment()
      render(
          view: 'process',
          params: [
              resultHash: request.parameterMap.resultHash,
              originHash: enrichment.originHash
          ],
          model: [
              enrichment : enrichment,
              currentView: 'process'
          ]
      )
      return
    }
    try {
      Enrichment enrichment = Enrichment.fromCommonsMultipartFile(file)
      enrichment.addFileAndFormat()
      enrichment.status = Enrichment.ProcessingState.PREPARE_1
      kbartReader = new KbartReader(enrichment.transferredFile, enrichment.originName)
      kbartReader.checkHeader()
      redirect(
          action: 'process',
          params: [
              resultHash: enrichment.resultHash,
              originHash: enrichment.originHash
          ],
          model: [
              enrichment : enrichment,
              currentView: 'process'
          ]
      )
    }
    catch (Exception ype) {
      flash.info = null
      flash.warning = null
      flash.error = ype.getMessage()
      Enrichment enrichment = getCurrentEnrichment()

      render(
          view: 'process',
          params: [
              resultHash: request.parameterMap.resultHash,
              originHash: enrichment.originHash
          ],
          model: [
              enrichment : enrichment,
              currentView: 'process'
          ]
      )
      return
    }
  }


  def uploadUrl = {
    SessionService.setSessionDuration(request, 3600)
    def urlString = request.parameterMap["uploadUrlText"][0]
    // validate
    if (!(new org.apache.commons.validator.routines.UrlValidator()).isValid(urlString)){
      flash.error = message(code: 'error.kbart.noValidUrl').toString()
      redirect(
          action: 'process'
      )
    }
    // set last update settings
    if (!request.session.lastUpdate){
      request.session.lastUpdate = [:]
    }
    request.session.lastUpdate.url = urlString
    request.session.lastUpdate.foQuoteUrl = null
    request.session.lastUpdate.foQuoteModeUrl = null
    request.session.lastUpdate.recordSeparatorUrl = "none"
    request.session.lastUpdate.addOnlyUrl = false
    // load file from URL
    String kbartFileName = KbartFromUrlReader.urlStringToFileString(urlString)
    Enrichment enrichment = Enrichment.fromFilename(kbartFileName)
    enrichment.addOnly = false
    enrichment.markDuplicates = true
    enrichment.processingOptions = null
    enrichment.locale = request.locale
    try {
      kbartReader = new KbartFromUrlReader(new URL(urlString), new File (enrichment.enrichmentFolder), request.locale)
      kbartReader.checkHeader()
    }
    catch (Exception e) {
      flash.info = null
      flash.warning = null
      flash.error = e.getMessage()

      render(
          view: 'process',
          params: [
              resultHash: request.parameterMap.resultHash,
              originHash: enrichment.originHash
          ],
          model: [
              enrichment : enrichment,
              currentView: 'process'
          ]
      )
      return
    }
    enrichment.originPathName = kbartFileName
    enrichment.originUrl = urlString
    if (request.parameterMap['urlAutoUpdate'] != null){
      enrichment.autoUpdate = request.parameterMap['urlAutoUpdate'][0].equals("on")
    }
    enrichment.addFileAndFormat()
    enrichment.status = Enrichment.ProcessingState.PREPARE_1

    redirect(
        action: 'process',
        params: [
            resultHash: enrichment.resultHash,
            originHash: enrichment.originHash
        ],
        model: [
            enrichment : enrichment,
            currentView: 'process'
        ]
    )
  }


  private void setInputFieldDataToLastUpdate(file, String recordSeparator, boolean addOnly){
    if (!request.session.lastUpdate){
      request.session.lastUpdate = [:]
    }
    request.session.lastUpdate.file = file
    request.session.lastUpdate.recordSeparator = recordSeparator
    request.session.lastUpdate.addOnly = addOnly
  }


  def uploadRawFile = {
    SessionService.setSessionDuration(request, 3600)
    def file = request.getFile('uploadRawFile')
    Enrichment enrichment = Enrichment.fromZipFile(file, enrichmentService.sessionFolder.parentFile.absolutePath)
    enrichmentService.addSessionEnrichment(enrichment)
    if (null == request.session.lastUpdate){
      request.session.lastUpdate = [:]
    }
    enrichment.setCurrentSession()
    enrichment.save()
    redirect(
        controller: 'Statistic',
        action: 'show',
        params: [
            resultHash: enrichment.resultHash,
            originHash: enrichment.originHash
        ],
        model: [
            enrichment : enrichment,
            currentView: 'process'
        ]
    )
  }


  def prepareFile = {
    SessionService.setSessionDuration(request, 3600)
    Enrichment enrichment = getCurrentEnrichment()
    enrichmentService.prepareFile(enrichment, request.parameterMap)
    enrichmentService.preparePackageHeader(enrichment, request.parameterMap)
    enrichment.setStatus(Enrichment.ProcessingState.PREPARE_2)
    if (request.session.lastUpdate != null){
      request.session.lastUpdate.parameterMap = request.parameterMap
    }
    setErrorStatus(enrichment)
    redirect(
        action: 'process',
        params: [
            resultHash: enrichment.resultHash,
            originHash: enrichment.originHash
        ],
        model: [
            enrichment : enrichment,
            currentView: 'process'
        ]
    )
  }


  def processGokbPackage(){
    SessionService.setSessionDuration(request, 72000)
    String sessionFolder = grails.util.Holders.grailsApplication.config.ygor.uploadLocation.toString()
        .concat(File.separator).concat(UUID.randomUUID().toString())
    Map<String, String> result = [:]
    List<String> missingParams = []
    String attachedFilePath = null
    CommonsMultipartFile mpFile = params.localFile ? request.getFile('uploadFile') : null
    String addOnly = params.addOnly ?: 'false'
    String pkgId = params.get('pkgId')
    File transferredFile = null
    if (StringUtils.isEmpty(pkgId)){
      missingParams.add("pkgId")
    }
    String token = params.get('updateToken')
    if (StringUtils.isEmpty(token)){
      missingParams.add("updateToken")
    }
    if (!missingParams.isEmpty()){
      result.status = "error"
      result.missingParams = missingParams
      return result as JSON
    }
    boolean ignoreLastChanged = params.boolean('ignoreLastChanged') ?: false

    Map<String, Object> pkg = enrichmentService.getPackage(pkgId, ["source", "curatoryGroups", "nominalPlatform"], null)
    Map<String, Object> src = pkg?.get("_embedded")?.get("source")

    if (mpFile) {
      transferredFile = new File(sessionFolder, pkg.name)
      FileUtils.writeByteArrayToFile(transferredFile, mpFile.getBytes())
    }

    if (MapUtils.isEmpty(pkg)){
      result.status = UploadThreadGokb.Status.ERROR.toString()
      response.status = 404
      result.message = "No package found for id $pkgId"
    }
    else if (MapUtils.isEmpty(src)){
      result.status = UploadThreadGokb.Status.ERROR.toString()
      response.status = 404
      result.message = "No source found for package with id $pkgId"
    }
    else{
      UploadJobFrame uploadJobFrame = new UploadJobFrame(Enrichment.FileType.PACKAGE_WITH_TITLEDATA)
      CompleteProcessingThread completeProcessingThread = new CompleteProcessingThread(kbartReader, pkg, src, token,
          uploadJobFrame, transferredFile, addOnly, ignoreLastChanged)
      try {
        completeProcessingThread.start()
        result.status = UploadThreadGokb.Status.STARTED.toString()
        response.status = 200
        result.message = "Started upload job for package $pkgId"
        result.jobId = uploadJobFrame.uuid
      }
      catch(Exception e){
        e.printStackTrace()
        result.status = UploadThreadGokb.Status.ERROR.toString()
        response.status = 500
        result.message = "Unable to process KBART file at the specified source url. Exception was: ".concat(e.message)
      }
    }
    render result as JSON
  }


  /**
   * Current Test configuration via Postman:
   *
   * POST /ygor/enrichment/processCompleteWithToken?
   * addOnly=false&
   * processOption=kbart,zdb,ezb&
   * pkgId=<yourPackageId>&
   * pkgNominalPlatformId=<theIdOfThePlatformBelongingToThisPackage>&
   * updateToken=<packageUpdateToken>&
   * titleIdNamespace=<theNamespaceForTheTitleId>
   *
   * Content-Disposition: form-data; name="uploadFile"; filename="yourKBartTestFile.tsv"
   */
  def processCompleteWithToken(){
    SessionService.setSessionDuration(request, 72000)
    def result = [:]
    Enrichment enrichment = buildEnrichmentFromRequest()
    UploadJob uploadJob = enrichmentService.processComplete(enrichment, null, null, false, true)
    enrichmentService.addUploadJob(uploadJob)
    result.message = watchUpload(uploadJob, Enrichment.FileType.PACKAGE, enrichment.originName)

    render result as JSON
  }


  def ajaxGetPackageRelatedValues = {
    def en = getCurrentEnrichment()
    if (en){
      def pkg = enrichmentService.getPackage(params.uuid, null, null)
      def titleId = ""
      if (pkg.provider?.name){
        def provider = enrichmentService.getProvider(pkg.provider.name, ["titleId"])
        // titleId =
      }
      render '{"platform":"' + pkg.nominalPlatform?.name + '", "provider":"' + pkg.provider?.name + '", "curatoryGroup":"' + pkg._embedded?.curatoryGroups[0]?.name + '"}'
    }
  }


  private Enrichment buildEnrichmentFromRequest(){
    // create a sessionFolder
    CommonsMultipartFile file = request.getFile('uploadFile')
    if (file == null){
      log.error("Received request missing a file. Aborting.")
      return
    }
    if (file.empty){
      log.error("Received request with empty file. Aborting.")
      return
    }
    enrichmentService.kbartReader = new KbartReader(file)
    Enrichment enrichment = Enrichment.fromCommonsMultipartFile(file)
    String addOnly = params.get('addOnly').toString()                           // "true" or "false"
    def pmOptions = params.get('processOption')                                 // "kbart", "zdb", "ezb"
    boolean ignoreLastChanged = params.boolean('ignoreLastChanged')       // "true" or "false"

    Map<String, Object> platform = enrichmentService.getPlatform(String.valueOf(params.get('pkgNominalPlatformId')))
    Map<String, Object> pkg = enrichmentService.getPackage(params.get('pkgId'), ["source", "curatoryGroups", "nominalPlatform"], null)
    String pkgTitleId = request.parameterMap.get("titleIdNamespace")
    String pkgTitle = pkg.get("name")
    String pkgCuratoryGroup = pkg.get("_embedded")?.get("curatoryGroups")?.getAt(0)?.get("name") // TODO query embed CG
    String pkgId = String.valueOf(pkg.get("id"))
    String pkgNominalPlatform = String.valueOf(pkg.get("nominalPlatform")?.get("id"))?.concat(";")
        .concat(pkg.get("nominalPlatform")?.get("name"))
    String pkgNominalProvider = pkg.get("provider")?.get("name")
    String updateToken = params.get('updateToken')
    String uuid = pkg.get("uuid")
    String lastUpdated = EnrichmentService.getLastRun(pkg)
    if (lastUpdated != null){
      addOnly = "true"
    }
    return enrichmentService.setupEnrichment(enrichment, enrichmentService.kbartReader, addOnly, pmOptions, platform.name,
        platform.primaryUrl, request.parameterMap, pkgTitleId, pkgTitle, pkgCuratoryGroup, pkgId, pkgNominalPlatform,
        pkgNominalProvider, updateToken, uuid, lastUpdated, ignoreLastChanged)
  }


  def getStatus(){
    String jobId = params.get('jobId')
    def result = [:]
    UploadJobFrame uploadJob = enrichmentService.getUploadJob(jobId)
    if (uploadJob == null){
      log.info("Received status request for uploadJob $jobId but there is no according job.")
      result.status = UploadThreadGokb.Status.ERROR.toString()
      result.message = "No job found for this id."
      render result as JSON
    }
    else if (uploadJob instanceof UploadJob) {
      uploadJob.updateCount()
      uploadJob.refreshStatus()
      result.status = uploadJob.getStatus().toString()
      result.gokbJobId = uploadJob.uploadThread?.gokbJobId
      render result as JSON
    }
    else if (uploadJob.status == UploadThreadGokb.Status.ERROR) {
      result.status = UploadThreadGokb.Status.ERROR.toString()
      response.status = 400
      result.message = "There was an error processing this job."
      render result as JSON
    }
    else if (uploadJob.status == UploadThreadGokb.Status.FINISHED_UNDEFINED) {
      result.status = UploadThreadGokb.Status.FINISHED_UNDEFINED.toString()
      result.message = "No URLs processed."
      render result as JSON
    }
    else{
      // uploadJob is instance of UploadJobFrame
      result.status = UploadThreadGokb.Status.PREPARATION.toString()
      render result as JSON
    }
  }


  static String watchUpload(UploadJob uploadJob, Enrichment.FileType fileType, String fileName){
    while (true){
      uploadJob.updateCount()
      uploadJob.refreshStatus()
      if (uploadJob.getStatus() == UploadThreadGokb.Status.STARTED){
        // still running
        Thread.sleep(1000)
      }
      if (uploadJob.getStatus() == UploadThreadGokb.Status.ERROR){
        String message = "Aborting. Couldn't upload " + fileType.toString() + " for file " + fileName
        log.error(message)
        return message
      }
      if (uploadJob.getStatus() == UploadThreadGokb.Status.SUCCESS || uploadJob.getStatus() == UploadThreadGokb.Status.FINISHED_UNDEFINED){
        String message = "Success. Finished upload for file " + fileName
        log.info(message)
        return message
      }
    }
  }


  def processFile = {
    SessionService.setSessionDuration(request, 72000)
    def en = getCurrentEnrichment()
    try{
      def pmOptions = request.parameterMap['processOption']
      if (en.status != Enrichment.ProcessingState.WORKING){
        if (!pmOptions){
          flash.info = null
          flash.warning = message(code: 'warning.noEnrichmentOption')
          flash.error = null
        }
        else{
          if (!request.session.lastUpdate){
            request.session.lastUpdate = [:]
          }
          request.session.lastUpdate.pmOptions = pmOptions

          if (en.status != Enrichment.ProcessingState.WORKING){
            flash.info = message(code: 'info.started')
            flash.warning = null
            flash.error = null

            def format = getCurrentFormat()
            def options = [
                'options'    : pmOptions,
                'quote'      : format.get('quote'),
                'quoteMode'  : format.get('quoteMode'),
                'ygorVersion': grailsApplication.config.ygor.version,
                'ygorType'   : grailsApplication.config.ygor.type
            ]
            en.processingOptions = Arrays.asList(pmOptions)
            en.process(options, kbartReader)
          }
        }
      }
      if (en.status != Enrichment.ProcessingState.FINISHED){
        setErrorStatus(en)
        redirect(
            action: 'process',
            params: [
                resultHash: en.resultHash,
                originHash: en.originHash
            ],
            model: [
                enrichment : en,
                currentView: 'process'
            ]
        )
      }
      else{
        redirect(
            controller: 'Statistic',
            action: 'show',
            params: [
                resultHash: en.resultHash,
                originHash: en.originHash
            ],
            model: [
                enrichment : en,
                currentView: 'process'
            ]
        )
      }
    }
    catch(Exception e){
      setErrorStatus(en)
      redirect(action: 'process')
    }
  }


  private void setErrorStatus(Enrichment en){
    if (en.apiMessage != null){
      en.status == Enrichment.ProcessingState.ERROR
      flash.error = en.apiMessage
    }
  }


  def stopProcessingFile = {
    SessionService.setSessionDuration(request, 3600)
    getCurrentEnrichment().stop()
    deleteFile()
  }


  def deleteFile = {
    SessionService.setSessionDuration(request, 3600)
    request.session.lastUpdate = [:]
    enrichmentService.deleteFileAndFormat(getCurrentEnrichment())
    render(
        view: 'process',
        model: [
            enrichment : getCurrentEnrichment(),
            currentView: 'process'
        ]
    )
  }


  def correctFile = {
    SessionService.setSessionDuration(request, 3600)
    enrichmentService.deleteFileAndFormat(getCurrentEnrichment())
    render(
        view: 'process',
        model: [
            enrichment : getCurrentEnrichment(),
            currentView: 'process'
        ]
    )
  }


  Enrichment getCurrentEnrichment(){
    return getCurrentEnrichmentStatic(enrichmentService, request)
  }


  static Enrichment getCurrentEnrichmentStatic(EnrichmentService enrichmentService, HttpServletRequest request){
    if (!request.parameterMap['resultHash'] || !(String) request.parameterMap['resultHash'][0]){
      return new Enrichment()
    }
    def hash = (String) request.parameterMap['resultHash'][0]
    def enrichments = enrichmentService.getSessionEnrichments()
    Enrichment result = enrichments[hash.toString()]
    if (null == result){
      result = enrichments.get("${hash.toString()}")
    }
    result
  }


  HashMap getCurrentFormat(){
    def hash = (String) request.parameterMap['originHash'][0]
    enrichmentService.getSessionFormats().get(hash)
  }


  void noValidEnrichment(){
    flash.info = null
    flash.warning = message(code: 'warning.fileNotFound')
    flash.error = null
    redirect(action: 'process')
  }


  // get package title suggestions for typeahead
  def suggestPackageTitle = {
    log.debug("Getting title suggestions..")
    def result = [:]
    def titles = gokbService.getTitleMap(params.q)
    result.items = titles.records
    render result as JSON
  }


  // get Platform suggestions for typeahead
  def suggestPlatform = {
    log.debug("Getting platform suggestions..")
    def result = [:]
    def platforms = gokbService.getPlatformMap(params.q)
    result.items = platforms.records
    render result as JSON
  }


  // get Org suggestions for typeahead
  def suggestProvider = {
    log.debug("Getting provider suggestions..")
    def result = [:]
    def providers = gokbService.getProviderMap(params.q)
    result.items = providers.records
    render result as JSON
  }


  def gokbNameSpaces = {
    log.debug("Getting namespaces of connected GOKb instance..")
    def result = [:]
    result.items = gokbService.getNamespaceList()
    render result as JSON
  }

}
