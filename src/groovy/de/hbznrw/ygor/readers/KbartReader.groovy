package de.hbznrw.ygor.readers

import de.hbznrw.ygor.normalizers.DateNormalizer
import de.hbznrw.ygor.processing.YgorFeedback
import de.hbznrw.ygor.tools.DateToolkit
import groovy.util.logging.Log4j
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.apache.commons.csv.QuoteMode
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate

import ygor.EnrichmentService

import java.util.regex.Matcher
import java.util.regex.Pattern

@Log4j
class KbartReader {

  EnrichmentService enrichmentService = new EnrichmentService()

  def messageSource = grails.util.Holders.applicationContext.getBean("messageSource")

  static final def IDENTIFIER = 'kbart'
  static final String KBART_HEADER_ZDB_ID = "zdb_id"
  static final String KBART_HEADER_ONLINE_IDENTIFIER = "online_identifier"
  static final String KBART_HEADER_PRINT_IDENTIFIER = "print_identifier"
  static final String KBART_HEADER_DOI_IDENTIFIER = "doi_identifier"
  static final Pattern DATE_PATTERN = Pattern.compile("[\\d]{4}-[\\d]{2}-[\\d]{2}")

  private BufferedReader csvReader
  private CSVFormat csvFormat
  private List<String> csvHeader
  Iterator<CSVRecord> csvRecords
  private CSVRecord lastItemReturned
  String fileName
  Date fileNameDate
  char delimiterChar
  private YgorFeedback ygorFeedback

  static ValidationTagLib VALIDATION_TAG_LIB = new ValidationTagLib()

  static MANDATORY_KBART_KEYS = [
      'title_url',
      'publication_type'
  ]


  static ALIASES = [
      'notes' : ['coverage_notes'],
      'zdb_id': ['zdb-id', 'ZDB_ID', 'ZDB-ID']
  ]


  KbartReader(){
    // not in use
  }


  KbartReader(def kbartFile, String originalFileName, YgorFeedback ygorFeedback) throws Exception{
    this.ygorFeedback = ygorFeedback
    init(kbartFile, originalFileName)
  }


  protected void init(File kbartFile, String originalFileName){
    if (kbartFile == null){
      return
    }
    // automatic delimiter adaptation by selection of the character with biggest count
    fileNameDate = extractDateFromFileName(originalFileName)
    BufferedReader bufferedReader = removeBOM(new BufferedReader(new FileReader(kbartFile)))
    String firstLine = bufferedReader.readLine()
    delimiterChar = calculateDelimiter(firstLine)
    firstLine = firstLine.replace("^${delimiterChar}", " ${delimiterChar}")
                         .replaceAll("(?<=${delimiterChar})${delimiterChar}", " ${delimiterChar}")
                         .replaceAll('([\t;,])\$', /$1 /)
    firstLine = checkForUpperCaseCharacters(firstLine)
    csvHeader = new ArrayList<String>()
    csvHeader.addAll(firstLine.split(String.valueOf(delimiterChar)))
    String lineSeparator = getLineSeparator(kbartFile)
    csvReader = new BufferedReader(new FileReader(kbartFile), 1048576 * 10)
    csvFormat = CSVFormat.RFC4180
        .withDelimiter(delimiterChar)
        .withEscape((char) "\\")
        .withRecordSeparator(lineSeparator)
    csvRecords = csvFormat.parse(bufferedReader).iterator()
  }


  private String checkForUpperCaseCharacters(String firstLine) {
    boolean firstLineContainsUpperCaseLetters = false
    firstLine.each {
      if (Character.isUpperCase(it.charAt(0))) {
        firstLineContainsUpperCaseLetters = true
      }
    }
    if (firstLineContainsUpperCaseLetters) {
      firstLine = firstLine.toLowerCase()
      ygorFeedback.reportingComponent = this.getClass()
      ygorFeedback.statusDescription = "KBart header contains upper-case letters. File is processed nonetheless."
      ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.WARNING
    }
    firstLine
  }


  private Date extractDateFromFileName(String fileName){
    if (StringUtils.isEmpty(fileName)){
      return null
    }
    Matcher dateMatcher = DATE_PATTERN.matcher(fileName)
    String dateString = null
    while (dateMatcher.find()) {
      // iterate through file name until last occurrence of a date
      dateString = dateMatcher.group()
    }
    if (!StringUtils.isEmpty(dateString)){
      try{
        return DateNormalizer.YYYY_MM_DD.parse(dateString)
      }
      catch(ParseException pe){
        return null
      }
    }
    // else
    return null
  }


  private BufferedReader removeBOM(BufferedReader bufferedReader){
    PushbackReader pushbackReader = new PushbackReader(bufferedReader)
    int c = pushbackReader.read()
    if(c != 0xFEFF) {
      pushbackReader.unread(c)
    }
    return new BufferedReader(pushbackReader)
  }


  private char calculateDelimiter(String line) {
    log.debug("Getting delimiter for line: ${line}")
    int maxCount = 0
    String delimiter
    for (String prop : ['comma', 'semicolon', 'tab']) {
      int num = StringUtils.countMatches(line, resolver.get(prop).toString())
      if (maxCount < num) {
        maxCount = num
        delimiter = prop
      }
    }
    char delimiterChar = resolver.get(delimiter)
    delimiterChar
  }


  String getLineSeparator(File file) throws IOException {
    char current
    String lineSeparator = ""
    FileInputStream fis = new FileInputStream(file)
    try {
      while (fis.available() > 0) {
        current = (char) fis.read()
        if ((current == '\n') || (current == '\r')) {
          lineSeparator += current
          if (fis.available() > 0) {
            char next = (char) fis.read()
            if ((next != current)
                && ((next == '\r') || (next == '\n'))) {
              lineSeparator += next
            }
          }
          return lineSeparator
        }
      }
    } finally {
      if (fis!=null) {
        fis.close()
      }
    }
    return null
  }


  // NOTE: should have been an override of AbstractReader.readItemData(), but the parameters are too different
  Map<String, String> readItemData(LocalDate lastPackageUpdate, boolean ignoreLastChanged) {
    int i = csvHeader.indexOf("last_changed")
    while (csvRecords.hasNext()){
      CSVRecord next = csvRecords.next()
      LocalDate itemLastUpdate = i > -1 ? DateToolkit.getAsLocalDate(next.get(i)) : null
      if (itemLastUpdate == null || lastPackageUpdate == null ||
          ignoreLastChanged || !itemLastUpdate.isBefore(lastPackageUpdate)) {
        Map<String, String> nextAsMap = returnItem(next)
        if (nextAsMap != null) return nextAsMap
      }
    }
    return null
  }


  private Map<String, String> returnItem(CSVRecord item) {
    if (!item) {
      return null
    }
    def splitItem = item.values()
    if (splitItem.length != csvHeader.size()) {
      log.info('Crappy record ignored, "size != header size" for: ' + item)
      return null
    }
    Map<String, String> resultMap = new HashMap<>()
    boolean hasContentYet = false
    for (int i = 0; i < csvHeader.size(); i++) {
      if (!StringUtils.isEmpty(csvHeader.get(i))) {
        resultMap.put(csvHeader.get(i), splitItem[i])
        if (!hasContentYet && StringUtils.isNotBlank(splitItem[i])){
          hasContentYet = true
        }
      }
    }
    if (!hasContentYet){
      return null
    }
    lastItemReturned = item
    // Fix coverage_depth = Volltext
    if (resultMap.get("coverage_depth")?.equalsIgnoreCase("volltext")) {
      resultMap.put("coverage_depth", "fulltext")
    }
    resultMap
  }


  void checkHeader() throws Exception {
    def missingKeys = []
    if (!csvHeader) {
      throw new Exception(VALIDATION_TAG_LIB.message(code: 'error.kbart.missingHeader').toString()
          .concat("<br>").concat(VALIDATION_TAG_LIB.message(code: 'error.kbart.messageFooter').toString()))
    }
    // check mandatory fields
    List<String> headerFields = new ArrayList<>()
    headerFields.addAll(csvHeader)
    MANDATORY_KBART_KEYS.each { kbk ->
      if (!headerFields.contains(kbk)) {
        boolean isMissing = true
        for (def alias : ALIASES[kbk]) {
          if (headerFields.contains(alias)) {
            isMissing = false
          }
        }
        if (isMissing) {
          missingKeys << kbk.toString()
        }
      }
    }
    if (missingKeys.size() > 0) {
      throw new Exception(VALIDATION_TAG_LIB.message(code: 'error.kbart.missingColumns').toString()
          .replace("{}", missingKeys.toString())
          .concat("<br>").concat(VALIDATION_TAG_LIB.message(code: 'error.kbart.messageFooter').toString()))
    }
    // replace aliases
    for (Map.Entry<String, List<String>> alias : ALIASES) {
      if (!csvHeader.contains(alias.getKey())) {
        for (String value : alias.getValue()) {
          if (csvHeader.contains(value)) {
            csvHeader.set(csvHeader.indexOf(value), alias.getKey())
          }
        }
      }
    }
  }


  KbartReader setConfiguration(KbartReaderConfiguration configuration) {
    if (null != configuration.quote) {
      if ('null' == configuration.quote) {
        csvFormat = csvFormat.withQuote(null)
      }
      else {
        csvFormat = csvFormat.withQuote((char) configuration.quote)
      }
    }
    if (null != configuration.quoteMode) {
      csvFormat = csvFormat.withEscape((char) '^')
      csvFormat = csvFormat.withQuoteMode((QuoteMode) configuration.quoteMode)
    }
    if (null != configuration.recordSeparator) {
      csvFormat = csvFormat.withRecordSeparator(configuration.recordSeparator)
    }
    csvFormat = csvFormat.withAllowMissingColumnNames(true)
    csvFormat = csvFormat.withIgnoreHeaderCase(true)
    this
  }


  static def resolver = [
      'comma'      : ',',
      'semicolon'  : ';',
      'tab'        : '\t',
      'doublequote': '"',
      'singlequote': "'",
      'nullquote'  : 'null',
      'all'        : QuoteMode.ALL,
      'nonnumeric' : QuoteMode.NON_NUMERIC,
      'none'       : QuoteMode.NONE
  ]

}
