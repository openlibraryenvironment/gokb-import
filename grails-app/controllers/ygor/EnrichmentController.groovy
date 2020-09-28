package ygor

import de.hbznrw.ygor.readers.KbartFromUrlReader
import de.hbznrw.ygor.readers.KbartReader
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.springframework.web.servlet.support.RequestContextUtils


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
    def file = request.getFile('uploadFile')
    if (file.size < 1 && request.parameterMap.uploadFileLabel != null &&
        request.parameterMap.uploadFileLabel[0] == request.session.lastUpdate?.file?.originalFilename){
      // the file form is unpopulated but the previously selected file is unchanged
      file = request.session.lastUpdate.file
    }

    String encoding = enrichmentService.getEncoding(file.getInputStream())
    if (encoding && encoding != "UTF-8"){
      flash.info = null
      flash.warning = null
      flash.error = message(code: 'error.kbart.noUtf8Encoding').toString().concat("<br>")
          .concat(message(code: 'error.kbart.messageFooter').toString())
      redirect(
          action: 'process'
      )
      return
    }

    def recordSeparator = "none"        // = request.parameterMap['recordSeparator'][0]
    def addOnly = false

    setInputFieldDataToLastUpdate(file, recordSeparator, addOnly)

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
      kbartReader = new KbartReader(new InputStreamReader(file.getInputStream()))
      kbartReader.checkHeader()
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


    Enrichment enrichment = enrichmentService.fromCommonsMultipartFile(file)
    enrichmentService.addFileAndFormat(enrichment)
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


  def uploadUrl = {
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
    Enrichment enrichment = enrichmentService.fromFilename(kbartFileName)
    enrichment.addOnly = false
    enrichment.processingOptions = null
    try {
      kbartReader = new KbartFromUrlReader(new URL(urlString), new File (enrichment.enrichmentFolder))
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
    enrichmentService.addFileAndFormat(enrichment)
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
    // create a sessionFolder
    CommonsMultipartFile file = request.getFile('uploadFile')
    def locale = RequestContextUtils.getLocale(request).getLanguage()
    if (file == null){
      log.error("Received request missing a file. Aborting.")
      return
    }
    if (file.empty){
      log.error("Received request with empty file. Aborting.")
      return
    }
    def addOnly = params.get('addOnly')                  // "on" or "off"
    def pmOptions = params.get('processOption')          // "kbart", "zdb", "ezb"
    File sessionFolder = enrichmentService.getSessionFolder()
    String fileName = file.originalFilename
    Enrichment enrichment = new Enrichment(sessionFolder, fileName)
    enrichment.addOnly = (addOnly.equals("on") || addOnly.equals("true")) ? true : false
    enrichment.processingOptions = EnrichmentService.decodeApiCalls(pmOptions)

    Map<String, Object> pkg = enrichmentService.getPackage(params.get('pkgId'))
    // Map<String, Object> platform = enrichmentService.getPlatform(params.get('platformId'))
    Map<String, Object> parameterMap = new HashMap<>()
    parameterMap.putAll(request.parameterMap)

    addParameterToParameterMap("pkgTitle", pkg.get("name"), parameterMap)
    addParameterToParameterMap("pkgCuratoryGroup", pkg.get("_embedded")?.get("curatoryGroups")?.getAt(0)?.get("name"), parameterMap)
    addParameterToParameterMap("pkgId", String.valueOf(pkg.get("id")), parameterMap)
    addParameterToParameterMap("pkgNominalPlatform", String.valueOf(pkg.get("nominalPlatform")?.get("id"))?.concat(";")
        .concat(pkg.get("nominalPlatform")?.get("name")), parameterMap)
    addParameterToParameterMap("pkgNominalProvider", pkg.get("provider")?.get("name"), parameterMap)

    enrichmentService.prepareFile(enrichment, parameterMap)
    UploadJob uploadJob = enrichmentService.processComplete(enrichment, null, null, false)
    render(
        model: [
            message : watchUpload(uploadJob, Enrichment.FileType.PACKAGE, file.originalFilename)
        ]
    )
  }


  static String watchUpload(UploadJob uploadJob, Enrichment.FileType fileType, String fileName){
    while (true){
      uploadJob.updateCount()
      uploadJob.refreshStatus()
      if (uploadJob.status == UploadJob.Status.STARTED){
        // still running
        Thread.sleep(1000)
      }
      if (uploadJob.status == UploadJob.Status.ERROR){
        String message = "Aborting. Couldn't upload " + fileType.toString() + " for file " + fileName
        log.error(message)
        return message
      }
      if (uploadJob.status == UploadJob.Status.SUCCESS || uploadJob.status == UploadJob.Status.FINISHED_UNDEFINED){
        String message = "Success. Finished upload for file " + fileName
        log.info(message)
        return message
      }
    }
  }


  def processFile = {
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
    getCurrentEnrichment().stop()
    deleteFile()
  }


  def deleteFile = {
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
    enrichmentService.getSessionFormats().get("${hash}")
  }


  void noValidEnrichment(){
    flash.info = null
    flash.warning = message(code: 'warning.fileNotFound')
    flash.error = null
    redirect(action: 'process')
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


  private void addParameterToParameterMap(String parameterName, String parameterValue, Map<String, String[]> parameterMap){
    if (parameterMap == null){
      parameterMap = new HashMap<>()
    }
    String[] value = new String[1]
    value[0] = parameterValue
    parameterMap.put(parameterName, value)
  }
}
