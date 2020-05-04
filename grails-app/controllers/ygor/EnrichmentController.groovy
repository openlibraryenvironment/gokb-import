package ygor

import de.hbznrw.ygor.export.Statistics
import de.hbznrw.ygor.processing.YgorProcessingException
import de.hbznrw.ygor.readers.KbartReader
import grails.converters.JSON
import org.apache.commons.io.IOUtils
import org.mozilla.universalchardet.UniversalDetector


class EnrichmentController{

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
    render(
        view: 'process',
        params: [
            resultHash: request.parameterMap.resultHash,
            originHash: request.parameterMap.originHash
        ],
        model: [
            enrichment        : getCurrentEnrichment(),
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
        request.parameterMap.uploadFileLabel[0] == request.session.lastUpdate.file?.originalFilename){
      // the file form is unpopulated but the previously selected file is unchanged
      file = request.session.lastUpdate.file
    }
    String encoding = getEncoding(file)
    if (encoding && encoding != "UTF-8"){
      flash.info = null
      flash.warning = null
      flash.error = message(code: 'error.kbart.noUtf8Encoding').toString().concat("<br>")
          .concat(message(code: 'error.kbart.messageFooter').toString())
      redirect(action: 'process')
      return
    }
    def foDelimiter = request.parameterMap['formatDelimiter'][0]

    def foQuote = null                  // = request.parameterMap['formatQuote'][0]
    def foQuoteMode = null              // = request.parameterMap['formatQuoteMode'][0]
    def recordSeparator = "none"        // = request.parameterMap['recordSeparator'][0]

    setInputFieldDataToLastUpdate(file, foDelimiter, foQuote, foQuoteMode, recordSeparator)

    if (file.empty){
      flash.info = null
      flash.warning = null
      flash.error = message(code: 'error.kbart.noValidFile').toString().concat("<br>")
          .concat(message(code: 'error.kbart.messageFooter').toString())
      render(view: 'process',
          model: [
              enrichment : getCurrentEnrichment(),
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
      render(view: 'process',
          model: [
              enrichment : getCurrentEnrichment(),
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
            enrichment : getCurrentEnrichment(),
            currentView: 'process'
        ]
    )
  }


  private void setInputFieldDataToLastUpdate(file, String foDelimiter, foQuote, foQuoteMode, String recordSeparator){
    if (!request.session.lastUpdate){
      request.session.lastUpdate = [:]
    }
    request.session.lastUpdate.file = file
    request.session.lastUpdate.foDelimiter = foDelimiter
    request.session.lastUpdate.foQuote = foQuote
    request.session.lastUpdate.foQuoteMode = foQuoteMode
    request.session.lastUpdate.recordSeparator = recordSeparator
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


  def processFile = {
    try{
      def pmOptions = request.parameterMap['processOption']
      def en = getCurrentEnrichment()
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
    catch(YgorProcessingException ype){
      flash.error = ype.getMessage()
      redirect(action: 'process')
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
    Enrichment result = enrichments[hash]
    if (null == result){
      result = enrichments.get("${hash}")
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
