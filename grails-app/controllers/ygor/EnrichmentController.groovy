package ygor

import de.hbznrw.ygor.processing.SendPackageThreadGokb
import de.hbznrw.ygor.processing.SendTitlesThreadGokb
import de.hbznrw.ygor.processing.YgorProcessingException
import de.hbznrw.ygor.readers.KbartReader
import grails.converters.JSON
import org.apache.commons.io.IOUtils
import org.mozilla.universalchardet.UniversalDetector
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.springframework.web.servlet.support.RequestContextUtils
import ygor.field.FieldKeyMapping

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
    if (getEncoding(file) != "UTF-8"){
      flash.info = null
      flash.warning = null
      flash.error = message(code: 'error.kbart.noUtf8Encoding').toString().concat("<br>")
          .concat(message(code: 'error.kbart.messageFooter').toString())
      redirect(
          action: 'process'
      )
      return
    }
    def foDelimiter = request.parameterMap['formatDelimiter'][0]

    def foQuote = null                  // = request.parameterMap['formatQuote'][0]
    def foQuoteMode = null              // = request.parameterMap['formatQuoteMode'][0]
    def recordSeparator = "none"        // = request.parameterMap['recordSeparator'][0]
    def addOnly = false

    setInputFieldDataToLastUpdate(file, foDelimiter, foQuote, foQuoteMode, recordSeparator, addOnly)

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
      kbartReader = new KbartReader(new InputStreamReader(file.getInputStream()), foDelimiter)
      kbartReader.checkHeader()
    }
    catch (YgorProcessingException ype) {
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

    Enrichment enrichment = enrichmentService.addFileAndFormat(file, foDelimiter, foQuote, foQuoteMode)
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


  private void setInputFieldDataToLastUpdate(file, String foDelimiter, foQuote, foQuoteMode, String recordSeparator, boolean addOnly){
    if (!request.session.lastUpdate){
      request.session.lastUpdate = [:]
    }
    request.session.lastUpdate.file = file
    request.session.lastUpdate.foDelimiter = foDelimiter
    request.session.lastUpdate.foQuote = foQuote
    request.session.lastUpdate.foQuoteMode = foQuoteMode
    request.session.lastUpdate.recordSeparator = recordSeparator
    request.session.lastUpdate.addOnly = addOnly
  }


  private String getEncoding(file){
    String encoding
    try{
      encoding = UniversalDetector.detectCharset(file.getInputStream())
    }
    catch (IllegalStateException ise){
      ByteArrayOutputStream baos = new ByteArrayOutputStream()
      IOUtils.copy(file.getInputStream(), baos)
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())
      encoding = UniversalDetector.detectCharset(file.getInputStream())
    }
    log.debug("Detected encoding ${encoding}")
    encoding
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
   * POST /ygor/enrichment/processCompleteNoInteraction?
   * formatDelimiter=null&
   * formatQuote=null&
   * formatQuoteMode=null&
   * recordSeparator=null&
   * addOnly=false&
   * processOption=kbart,zdb,ezb&
   * gokbUsername=<aValidGokbUser>&
   * gokbPassword=<theUser'sPassword>&
   * pkgTitle=<yourPackageTitle>&
   * pkgIsil&
   * pkgCuratoryGroup=<yourCuratoryGroupName>&
   * pkgNominalProvider=Organisation for Economic Co-operation and Development&
   * pkgNominalPlatform=org.gokb.cred.Platform:408671;OECD UN iLibrary
   *
   * (examples given for pkgNominalProvider and pkgNominalPlatform)
   *
   * HTTP/1.1
   * Host: localhost:8092
   * cache-control: no-cache
   * Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW
   *
   * Content-Disposition: form-data; name="uploadFile"; filename="yourKBartTestFile.tsv"
   */
  def processCompleteNoInteraction = {
    CommonsMultipartFile file = request.getFile('uploadFile')
    if (file == null){
      log.error("Received request missing a file. Aborting.")
      return
    }
    if (file.empty){
      log.error("Received request with empty file. Aborting.")
      return
    }
    String fileName = file.originalFilename
    String encoding = getEncoding(file)
    if (encoding != "UTF-8"){
      log.error(String.format("Transferred file has encoding %s. Aborting.", encoding))
      return
    }
    def foDelimiter = params.get('formatDelimiter')      // inactive, set null
    def foQuote = params.get('formatQuote')              // inactive, set null
    def foQuoteMode = params.get('formatQuoteMode')      // inactive, set null
    def recordSeparator = params.get('recordSeparator')  //
    def addOnly = params.get('addOnly')                  // "on" or "off"
    def pmOptions = params.get('processOption')          // "kbart", "zdb", "ezb"
    try{
      kbartReader = new KbartReader(new InputStreamReader(file.getInputStream()), foDelimiter)
      kbartReader.checkHeader()
    }
    catch (YgorProcessingException ype){
      log.error("Aborting on KBart header check for file " + fileName)
      return
    }
    Enrichment enrichment = enrichmentService.addFileAndFormat(file, foDelimiter, foQuote, foQuoteMode)
    enrichmentService.prepareFile(enrichment, request.parameterMap)
    FieldKeyMapping tippNameMapping =
        enrichment.setTippPlatformNameMapping(enrichment.dataContainer.pkg.packageHeader.nominalPlatform.name)
    enrichment.enrollMappingToRecords(tippNameMapping)
    FieldKeyMapping tippUrlMapping =
        enrichment.setTippPlatformUrlMapping(enrichment.dataContainer.pkg.packageHeader.nominalPlatform.url)
    enrichment.enrollMappingToRecords(tippUrlMapping)
    def options = [
        'options'         : pmOptions,
        'delimiter'       : foDelimiter,
        'quote'           : foQuote,
        'quoteMode'       : foQuoteMode,
        'recordSeparator' : recordSeparator,
        'addOnly'         : addOnly,
        'ygorVersion'     : grailsApplication.config.ygor.version,
        'ygorType'        : grailsApplication.config.ygor.type
    ]
    enrichment.process(options, kbartReader)
    while (enrichment.status != Enrichment.ProcessingState.FINISHED){
      Thread.sleep(1000)
    }
    // Main processing finished here.
    // Upload following.
    def gokbUsername = params.gokbUsername
    def gokbPassword = params.gokbPassword
    // send package with integrated title data
    String uri = getDestinationUri(grailsApplication, Enrichment.FileType.PACKAGE, enrichment.addOnly)
    def locale = RequestContextUtils.getLocale(request).toString()
    SendPackageThreadGokb sendPackageThreadGokb = new SendPackageThreadGokb(grailsApplication, enrichment, uri,
        gokbUsername, gokbPassword, locale, true)
    UploadJob uploadJob = new UploadJob(Enrichment.FileType.PACKAGE, sendPackageThreadGokb)
    uploadJob.start()
    watchUpload(uploadJob, Enrichment.FileType.PACKAGE, fileName)
  }


  private void watchUpload(UploadJob uploadJob, Enrichment.FileType fileType, String fileName){
    while (true){
      uploadJob.updateCount()
      uploadJob.refreshStatus()
      if (uploadJob.status == UploadJob.Status.STARTED){
        // still running
        Thread.sleep(1000)
      }
      if (uploadJob.status == UploadJob.Status.ERROR){
        log.error("Aborting. Couldn't upload " + fileType.toString() + " for file " + fileName)
        return
      }
      if (uploadJob.status == UploadJob.Status.SUCCESS || uploadJob.status == UploadJob.Status.FINISHED_UNDEFINED){
        return
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
                'delimiter'  : format.get('delimiter'),
                'quote'      : format.get('quote'),
                'quoteMode'  : format.get('quoteMode'),
                'ygorVersion': grailsApplication.config.ygor.version,
                'ygorType'   : grailsApplication.config.ygor.type
            ]
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
}
