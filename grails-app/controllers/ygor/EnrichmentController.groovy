package ygor

import de.hbznrw.ygor.export.Statistics
import grails.converters.JSON
import org.mozilla.universalchardet.UniversalDetector


class EnrichmentController {

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
      model: [
        enrichments: enrichmentService.getSessionEnrichments(),
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
        enrichments: enrichmentService.getSessionEnrichments(),
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
      request.parameterMap.uploadFileLabel[0] == request.session.lastUpdate.file?.originalFilename) {
      // the file form is unpopulated but the previously selected file in unchanged
      file = request.session.lastUpdate.file
    }
    String encoding = UniversalDetector.detectCharset(file.getInputStream())
    log.debug("Detected encoding ${encoding}")
    if (!encoding || encoding == "UTF-8") {
      def foDelimiter = request.parameterMap['formatDelimiter'][0]

      def foQuote = null              // = request.parameterMap['formatQuote'][0]
      def foQuoteMode = null              // = request.parameterMap['formatQuoteMode'][0]
      def recordSeparator = "none"        // = request.parameterMap['recordSeparator'][0]
      def dataTyp = request.parameterMap['dataTyp'][0]

      if (!request.session.lastUpdate) {
        request.session.lastUpdate = [:]
      }
      request.session.lastUpdate.file = file
      request.session.lastUpdate.foDelimiter = foDelimiter
      request.session.lastUpdate.foQuote = foQuote
      request.session.lastUpdate.foQuoteMode = foQuoteMode
      request.session.lastUpdate.recordSeparator = recordSeparator
      request.session.lastUpdate.dataTyp = dataTyp

      if (file.empty) {
        flash.info = null
        flash.warning = null
        flash.error = message(code: 'error.noValidFile')
        render(view: 'process',
          model: [
            enrichments: enrichmentService.getSessionEnrichments(),
            currentView: 'process'
          ]
        )
        return
      }
      enrichmentService.addFileAndFormat(file, foDelimiter, foQuote, foQuoteMode, dataTyp)
      redirect(action: 'process')
    } else {
      flash.error = message(code: 'error.noUtf8Encoding') //+ encoding!=null?" but: "+encoding:""
      redirect(action: 'process')
    }
  }


  def uploadRawFile = {
    def file = request.getFile('uploadRawFile')
    Enrichment enrichment = Enrichment.fromFile(file)
    enrichmentService.addSessionEnrichment(enrichment)
    if (null == request.session.lastUpdate) {
      request.session.lastUpdate = [:]
    }
    request.session.lastUpdate << [dataTyp: enrichment.dataType]
    Statistics.getRecordsStatisticsBeforeParsing(enrichment)
    enrichment.setCurrentSession()
    enrichment.saveResult()

    render(
      view: 'process',
      model: [
        enrichments: enrichment,
        currentView: 'process'
      ]
    )
    redirect(action: 'process')
  }


  def prepareFile = {
    enrichmentService.prepareFile(getCurrentEnrichment(), request.parameterMap)
    request.session.lastUpdate.parameterMap = request.parameterMap
    redirect(action: 'process')
  }


  def processFile = {
    def pmOptions = request.parameterMap['processOption']
    if (getCurrentEnrichment().status != Enrichment.ProcessingState.WORKING) {
      if (!pmOptions) {
        flash.info = null
        flash.warning = message(code: 'warning.noEnrichmentOption')
        flash.error = null
      } else {
        if (!request.session.lastUpdate) {
          request.session.lastUpdate = [:]
        }
        request.session.lastUpdate.pmOptions = pmOptions
        def en = getCurrentEnrichment()
        if (en.status != Enrichment.ProcessingState.WORKING) {
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
    render(
      view: 'process',
      model: [
        enrichments: enrichmentService.getSessionEnrichments(),
        currentView: 'process',
        pOptions   : pmOptions,
      ]
    )
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
        enrichments: enrichmentService.getSessionEnrichments(),
        currentView: 'process'
      ]
    )
  }


  def correctFile = {
    enrichmentService.deleteFileAndFormat(getCurrentEnrichment())
    render(
      view: 'process',
      model: [
        enrichments: enrichmentService.getSessionEnrichments(),
        currentView: 'process'
      ]
    )
  }


  def downloadPackageFile = {
    def en = getCurrentEnrichment()
    if (en) {
      def result = enrichmentService.getFile(en, Enrichment.FileType.JSON_PACKAGE_ONLY)
      render(file: result, fileName: "${en.resultName}.package.json")
    } else {
      noValidEnrichment()
    }
  }


  def downloadTitlesFile = {
    def en = getCurrentEnrichment()
    if (en) {
      def result = enrichmentService.getFile(en, Enrichment.FileType.JSON_TITLES_ONLY)
      render(file: result, fileName: "${en.resultName}.titles.json")
    } else {
      noValidEnrichment()
    }
  }


  def downloadRawFile = {
    def en = getCurrentEnrichment()
    if (en) {
      def result = enrichmentService.getFile(en, Enrichment.FileType.JSON_OO_RAW)
      render(file: result, fileName: "${en.resultName}.raw.json")
    } else {
      noValidEnrichment()
    }
  }


  def sendPackageFile = {
    def en = getCurrentEnrichment()
    if (en) {
      def response = enrichmentService.sendFile(en, Enrichment.FileType.JSON_PACKAGE_ONLY,
        params.gokbUsername, params.gokbPassword)
      flash.info = []
      flash.warning = []
      flash.error = []

      if (response.JSON) {
        if (response.info[0] != null) {
          if (response.info[0].result == 'ERROR') {
            flash.warning = [response.info[0].message]
          } else {
            flash.info = response.info[0].message
          }
          flash.error = []

          response.info[0].errors?.each { e ->
            flash.error.add(e.message)
          }
        }
        if (response.warning[0] != null) {
          log.debug("${response.warning[0]}")
          flash.warning = [response.warning[0].message]

          response.warning[0].errors.each { e ->
            flash.warning.add(e.message)
          }
        }
        if (response.error[0] != null) {
          log.warn("ERROR: ${response.error}")
          flash.error = [response.error[0].message]

          response.error.errors?.each { e ->
            flash.error.add(e.message)
          }
        }
      }
    } else {
      flash.error = "There was an error authenticating with GOKb!"
    }
    process()
  }


  def sendTitlesFile = {
    def en = getCurrentEnrichment()
    if (en) {
      def response = enrichmentService.sendFile(en, Enrichment.FileType.JSON_TITLES_ONLY,
        params.gokbUsername, params.gokbPassword)
      flash.info = []
      flash.warning = []
      List errorList = []
      def total = 0
      def errors = 0
      log.debug("sendTitlesFile response: ${response}")
      if (response.info) {
        log.debug("json class: ${response.info.class}")
        def info_objects = response.info.results
        info_objects[0].each { robj ->
          log.debug("robj: ${robj}")
          if (robj.result == 'ERROR') {
            errorList.add(robj.message)
            errors++
          }
          total++
        }
        flash.info = "Total: ${total}, Errors: ${errors}"
        flash.error = errorList
        // parsing errors for user feedback
//        for (result in response.info.results){
//
//        }
      }
      process()
    }
  }


  def ajaxGetStatus = {
    def en = getCurrentEnrichment()
    if (en) {
      render '{"status":"' + en.getStatus() + '", "message":"' + en.getMessage() + '", "progress":' + en.getProgress().round() + '}'
    }
  }


  Enrichment getCurrentEnrichment() {
    def hash = (String) request.parameterMap['resultHash'][0]
    def enrichments = enrichmentService.getSessionEnrichments()
    Enrichment result = enrichments[hash]
    if (null == result) {
      result = enrichments.get("${hash}")
    }
    result
  }


  HashMap getCurrentFormat() {
    def hash = (String) request.parameterMap['originHash'][0]
    enrichmentService.getSessionFormats().get("${hash}")
  }


  void noValidEnrichment() {
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
