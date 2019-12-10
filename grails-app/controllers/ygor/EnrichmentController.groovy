package ygor

import de.hbznrw.ygor.export.Statistics
import grails.converters.JSON
import org.mozilla.universalchardet.UniversalDetector


class EnrichmentController{

  static scope = "session"

  EnrichmentService enrichmentService
  GokbService gokbService

  def index = {
    redirect(action: 'process')
  }


  def process = {
    def gokb_ns = gokbService.getNamespaceList()
    render(
        view: 'process',
        params: [
            resultHash: request.parameterMap.resultHash,
            originHash: request.parameterMap.originHash
        ],
        model: [
            enrichment : getCurrentEnrichment(),
            gokbService: gokbService,
            namespaces : gokb_ns,
            currentView: 'process'
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
    String encoding
    try {
      encoding = UniversalDetector.detectCharset(file.getInputStream())
    }
    catch (java.lang.IllegalStateException ise){
      ByteArrayOutputStream baos = new ByteArrayOutputStream()
      org.apache.commons.io.IOUtils.copy(file.getInputStream(), baos)
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())
      encoding = UniversalDetector.detectCharset(file.getInputStream())
    }
    log.debug("Detected encoding ${encoding}")
    if (!encoding || encoding == "UTF-8"){
      def foDelimiter = request.parameterMap['formatDelimiter'][0]

      def foQuote = null                  // = request.parameterMap['formatQuote'][0]
      def foQuoteMode = null              // = request.parameterMap['formatQuoteMode'][0]
      def recordSeparator = "none"        // = request.parameterMap['recordSeparator'][0]
      def dataTyp = request.parameterMap['dataTyp'][0]

      if (!request.session.lastUpdate){
        request.session.lastUpdate = [:]
      }
      request.session.lastUpdate.file = file
      request.session.lastUpdate.foDelimiter = foDelimiter
      request.session.lastUpdate.foQuote = foQuote
      request.session.lastUpdate.foQuoteMode = foQuoteMode
      request.session.lastUpdate.recordSeparator = recordSeparator
      request.session.lastUpdate.dataTyp = dataTyp

      if (file.empty){
        flash.info = null
        flash.warning = null
        flash.error = message(code: 'error.noValidFile')
        render(view: 'process',
            model: [
                enrichment : getCurrentEnrichment(),
                currentView: 'process'
            ]
        )
        return
      }
      Enrichment enrichment = enrichmentService.addFileAndFormat(file, foDelimiter, foQuote, foQuoteMode, dataTyp)
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
    else{
      flash.error = message(code: 'error.noUtf8Encoding') //+ encoding!=null?" but: "+encoding:""
      redirect(action: 'process')
    }
  }


  def uploadRawFile = {
    def file = request.getFile('uploadRawFile')
    Enrichment enrichment = Enrichment.fromFile(file)
    enrichmentService.addSessionEnrichment(enrichment)
    if (null == request.session.lastUpdate){
      request.session.lastUpdate = [:]
    }
    request.session.lastUpdate << [dataTyp: enrichment.dataType]
    Statistics.getRecordsStatisticsBeforeParsing(enrichment)
    enrichment.setCurrentSession()
    enrichment.saveResult()

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
    request.session.lastUpdate.parameterMap = request.parameterMap
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
              'dataTyp'    : format.get('dataTyp'),
              'ygorVersion': grailsApplication.config.ygor.version,
              'ygorType'   : grailsApplication.config.ygor.type
          ]
          en.process(options)
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


  def stopProcessingFile = {
    enrichmentService.stopProcessing(getCurrentEnrichment())
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


  def ajaxGetStatus = {
    def en = getCurrentEnrichment()
    if (en){
      render '{"status":"' + en.getStatus() + '", "message":"' + en.getMessage() + '", "progress":' + en.getProgress().round() + '}'
    }
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
