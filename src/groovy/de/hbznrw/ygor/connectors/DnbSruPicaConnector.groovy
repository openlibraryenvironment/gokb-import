package de.hbznrw.ygor.connectors

import groovy.util.logging.Log4j
import groovy.util.slurpersupport.GPathResult
import de.hbznrw.ygor.processing.Envelope
import de.hbznrw.ygor.enums.*
import de.hbznrw.ygor.interfaces.*
import groovy.util.slurpersupport.Node

/**
 * Controlling API calls using services.dnb.de/sru/zdb
 */
@Log4j
class DnbSruPicaConnector extends AbstractConnector {

  static Map queryIDs = [:]

  private String requestUrl = "http://services.dnb.de/sru/zdb?version=1.1&operation=searchRetrieve&maximumRecords=10"
  private String queryOnlyJournals = "%20and%20dnb.frm=O"

  private String formatIdentifier = 'PicaPlus-xml'
  private GPathResult response

  private picaRecords = []
  private currentRecord = null

  private static XmlSlurper SLURPER = new XmlSlurper()

  DnbSruPicaConnector(BridgeInterface bridge) {
    super(bridge)
    queryIDs.put(KbartConnector.KBART_HEADER_ZDB_ID, "query=dnb.zdbid%3D")
    queryIDs.put(KbartConnector.KBART_HEADER_ONLINE_IDENTIFIER, "query=dnb.iss%3D")
    queryIDs.put(KbartConnector.KBART_HEADER_PRINT_IDENTIFIER, "query=dnb.iss%3D")
  }


  @Override
  String getAPIQuery(String identifier, String queryIdentifier) {
    return requestUrl + "&recordSchema=" + formatIdentifier + "&" + queryIDs.get(queryIdentifier) + identifier + queryOnlyJournals
  }

  @Override
  def poll(String identifier, String queryIdentifier, def publicationTitle) {
    try {
      String q = getAPIQuery(identifier, queryIdentifier)
      log.info("polling(): " + q)
      picaRecords = setPicaRecords(q)

      // in case we have more than 1 hit, we repeat the query, trying to reduce the hits to 1
      // by specifying the query with the title name
      if (picaRecords?.size() > 1) {
        q = q + "%20and%20dnb.tst%3D${java.net.URLEncoder.encode(publicationTitle)}"
        log.info("Found more than 1 hit. Trying to reduce to 1...\npolling(): " + q)
        picaRecords = setPicaRecords(q)
      }

    } catch (Exception e) {
      log.error(e)
    }

    if (picaRecords.size() == 0) {
      return AbstractEnvelope.STATUS_NO_RESPONSE
    } else if (picaRecords.size() == 1) {
      return AbstractEnvelope.STATUS_OK
    }
  }

  private List setPicaRecords(String q) {
    picaRecords = []
    response = SLURPER.parseText(new URL(q).getText())
    def records = response.children().find { it.name() == "records" }.childNodes()
    while (records.hasNext()) picaRecords << records.next()
    picaRecords
  }

  @Override
  Envelope query(Object record, Query query) {
    try {
      getEnvelope(record, query)
    } catch (Exception e) {
      log.debug(getClass().getName() + ".query(): Could not get Envelope for " + query + " from " + record + ".")
      return getEnvelopeWithStatus(AbstractEnvelope.STATUS_ERROR)
    }
  }

  // FormatAdapterInterface

  // <zs:searchRetrieveResponse>
  //   <zs:records>
  //     <zs:record>          <------------------ x times
  //       <zs:recordData>
  //         <record>
  //           <datafield tag="039D">
  //             <subfield code="c">Online-Ausg.</subfield>
  //             <subfield code="a">International journal of accounting and information management</subfield>
  //             <subfield code="C">ZDB</subfield>
  //             <subfield code="6">24063605</subfield>
  //           </datafield>

  @Override
  Envelope getEnvelope(Object record, Query query) {

    if (response == null)
      return getEnvelopeWithStatus(AbstractEnvelope.STATUS_NO_RESPONSE)

    currentRecord = record
    if (currentRecord == null)
      return getEnvelopeWithStatus(AbstractEnvelope.STATUS_ERROR)

    switch (query) {
      case Query.ZDBID:
        return getFirstResultOnly('006Z', '0')
        break;
      case Query.ZDB_GVKPPN:
        return getFirstResultOnly('003@', '0')
        break;
      case Query.ZDB_EISSN:
        return getFirstResultOnly('005A', '0')
        break;
      case Query.ZDB_PISSN:
        return getFirstResultOnly('005P', '0')
        break;
      case Query.ZDB_TITLE:
        return getTitle()
        break;
      case Query.ZDB_PUBLISHER:
        return getPublisherHistoryAsFatEnvelope()
        break;
      case Query.ZDB_PUBLISHED_FROM:
        return getFirstResultOnly('011@', 'a')
        break;
      case Query.ZDB_PUBLISHED_TO:
        return getFirstResultOnly('011@', 'b')
        break;
      case Query.ZDB_HISTORY_EVENTS:
        return getHistoryEventAsFatEnvelope()
        break;
    }

    getEnvelopeWithStatus(AbstractEnvelope.STATUS_UNKNOWN_REQUEST)
  }

  Object getPicaRecords() {
    picaRecords
  }

  private Envelope getFirstResultOnly(String tag, String code) {
    def result = []

    result << getFirstPicaValue(currentRecord.children()[2].children()[0], tag, code)
    getEnvelopeWithMessage(result.minus(null).unique())
  }

  private String getFirstPicaValue(Object record, String tag, String code) {
    def df = getFirstChildById(record.children()[0].children(), tag)
    def sf
    if (df) {
      sf = getFirstChildById(df.children(), code)
      log.debug("getPicaValue(" + tag + "" + code + ") = " + sf)
    }
    return sf ? sf.text() : null
  }

  private def getFirstChildById(List nodes, String subId) {
    Iterator<String> it = nodes.listIterator()
    while (it.hasNext()) {
      def child = it.next()
      if (child.attributes.'id' == subId) {
        return child
      }
    }
    return null
  }

  private String[] getAllPicaValues(Object record, String tag, String code) {
    String[] result = []
    def df = getAllChildrenById(record.children()[0].children(), tag)
    def sf
    if (df.size() > 0) {
      sf = getAllChildrenById(df.children(), code)
      if (sf.size() > 0) {
        log.debug("getPicaValue(" + tag + "" + code + ") = " + sf)
        result.addAll(sf)
      }
    }
    return result
  }

  private def getAllChildrenById(List nodes, String subId) {
    def result = []
    Iterator<String> it = nodes.listIterator()
    while (it.hasNext()) {
      def child = it.next()
      if (child instanceof Node) {
        if (child.attributes?.'id' == subId) {
          result << child
        }
        if (child.children() != null) {
          result.addAll(getAllChildrenById(child.children(), subId))
        }
      }
    }
    return result
  }

  private Envelope getTitle() {
    def result = []

    // correction
    result << getFirstPicaValue(currentRecord.children()[2].children()[0], '025@', 'a')

    // or .. main title
    if (result.minus(null).isEmpty()) {
      result << getFirstPicaValue(currentRecord.children()[2].children()[0], '021A', 'a')
    }

    def noAt = []

    result.each { r ->
      def correctedField = null
      if (r) {
        correctedField = r.minus('@')
      }
      noAt << correctedField
    }

    result = noAt
    log.debug("Got title ${result}")

    getEnvelopeWithMessage(result.minus(null).unique())
  }

  private Envelope getPublisherHistoryAsFatEnvelope() {
    def result = []
    def resultStartDate = []
    def resultEndDate = []
    def resultName = []
    def resultStatus = []

    def record = currentRecord.children()[2].children()[0]
    getAllChildrenById(record.children(), '033A').each { df ->
      def n = getFirstChildById(df.children(), "n") // TODO or use p here ?
      def h = getFirstChildById(df.children(), "h")

      resultName << (n ? n.text() : null)
      resultStartDate << (h ? h.text() : '')
      resultEndDate << (h ? h.text() : '')
      resultStatus << null
    }
    log.debug("getPicaValues(033An) = " + resultName)
    log.debug("getPicaValues(033Ah) = " + resultStartDate)

    // TODO refactor this

    result << getEnvelopeWithComplexMessage([
        'name'     : resultName,
        'startDate': resultStartDate,
        'endDate'  : resultEndDate,
        'status'   : resultStatus
    ])

    getEnvelopeWithMessage(result)
  }

  private Envelope getHistoryEventAsFatEnvelope() {

    def result = []
    def resultType = []
    def resultTitle = []
    def resultIdentifierValue = []
    def resultIdentifierType = []
    def resultDate = []

    def record = currentRecord.children()[2].children()[0]
    getAllChildrenById(record.children(), '039E').each { df ->

      def b = getFirstChildById(df.children(), "b") // s=später, f=früher
      def g = getFirstChildById(df.children(), "g") // Materialcode, O = Online, A = Druckwerk
      def Y = getFirstChildById(df.children(), "Y") // default (Y/D)
      def D = getFirstChildById(df.children(), "D") // falls in der ZDB ein übergeordneter Titel existiert (Y/D)
      def H = getFirstChildById(df.children(), "H")
      def C = getFirstChildById(df.children(), "C") // ID-Typ
      def f0 = getFirstChildById(df.children(), "0")

      if ( g?.text().startsWith('O') ) {
        resultType << (b ? b.text() : null)
        resultTitle << (D ? D.text().minus('@') : (Y ? Y.text().minus('@') : null))
        resultIdentifierType << (C ? C.text() : 'zdb') // default
        resultIdentifierValue << (f0 ? f0.text() : null)
        resultDate << (H ? H.text() : null)
      }
    }

    // zdbdb
    log.debug("getPicaValues(039Eb) = " + resultType)
    log.debug("getPicaValues(039E(D|Y)) = " + resultTitle)
    log.debug("getPicaValues(039EC) = " + resultIdentifierType)
    log.debug("getPicaValues(039E0) = " + resultIdentifierValue)
    log.debug("getPicaValues(039EH) = " + resultDate)

    result << getEnvelopeWithComplexMessage([
        'type'           : resultType,
        'name'           : resultTitle,
        'title'          : resultTitle,
        'identifierType' : resultIdentifierType,
        'identifierValue': resultIdentifierValue,
        'date'           : resultDate
    ])

    getEnvelopeWithMessage(result)
  }
}
