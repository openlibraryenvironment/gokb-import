package de.hbznrw.ygor.export

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import de.hbznrw.ygor.bridges.ZdbBridge
import de.hbznrw.ygor.enums.Status
import de.hbznrw.ygor.export.structure.TitleStruct
import de.hbznrw.ygor.normalizers.UrlNormalizer
import ygor.Enrichment
import ygor.Record
import ygor.StatisticController
import ygor.field.MultiField

class Statistics {

  static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance

  final static COUNT_1 = 0
  final static LIST_1 = 1
  final static COUNT_2 = 2
  final static LIST_2 = 3
  final static COUNT_3 = 4
  final static LIST_3 = 5
  final static COUNT_4 = 6
  final static LIST_4 = 7
  final static COUNT_5 = 8
  final static LIST_5 = 9


  static ObjectNode getRecordsStatisticsBeforeParsing(Enrichment enrichment) {

    enrichment.stats = new ObjectNode(NODE_FACTORY)
    ObjectNode tipps = new ObjectNode(NODE_FACTORY)
    ObjectNode titles = new ObjectNode(NODE_FACTORY)
    ObjectNode general = new ObjectNode(NODE_FACTORY)

    tipps.set("tipps before cleanup", new TextNode(enrichment.dataContainer.tipps.size))
    titles.set("titles before cleanUp", new TextNode(enrichment.dataContainer.titles.size))

    enrichment.stats.set("tipps", tipps)
    enrichment.stats.set("titles", titles)
    enrichment.stats.set("general", general)
    enrichment.stats.set("identifiers", new ObjectNode(NODE_FACTORY))

    Statistics.evaluateTippNames(enrichment.dataContainer.records.values(), enrichment.stats)
    Statistics.evaluateUrls(enrichment.dataContainer.records.values(), enrichment.stats)
    Statistics.evaluateIdentifiers(enrichment.dataContainer.records.values(), enrichment.stats)
    Statistics.evaluateCoverage(enrichment.dataContainer.records.values(), enrichment.stats)
    Statistics.evaluatePublisherHistory(enrichment.dataContainer.records.values(), enrichment.stats)
    Statistics.evaluateHistoryEvents(enrichment.dataContainer.records.values(), enrichment.stats)

    general.put(StatisticController.PROCESSED_KBART_ENTRIES, enrichment.dataContainer.records.size())
    general.put(StatisticController.IGNORED_KBART_ENTRIES, 0) // TODO
    general.put(StatisticController.DUPLICATE_KEY_ENTRIES, 0) // TODO

    enrichment.stats
  }

  static Object getStatsBeforeParsing(Object json) {
    // TODO move rest to getRecordsStatisticsBeforeParsing(Enrichment enrichment)

    json.meta.stats.general << ["tipps before cleanUp": json.package.tipps.size()]
    json.meta.stats.general << ["titles before cleanUp": json.titles.size()]

    json.meta.stats.general << [(StatisticController.PROCESSED_KBART_ENTRIES): json.meta.stash.processedKbartEntries]
    json.meta.stats.general << [(StatisticController.IGNORED_KBART_ENTRIES): json.meta.stash.ignoredKbartEntries]
    json.meta.stats.general << [(StatisticController.DUPLICATE_KEY_ENTRIES): json.meta.stash.duplicateKeyEntries]

    json
  }

  static Object evaluateTippNames(Collection<Record> records, ObjectNode stats) { // TODO rename evaluateNames ?
    List<Integer> tippTitleName = Statistics.getStorage()
    records.each { record ->
      MultiField nameField = record.getMultiField("publicationTitle")
      String nameStatus = nameField.status

      if (nameStatus?.equals(Status.VALIDATOR_STRING_IS_VALID.toString())) {
        tippTitleName[Statistics.COUNT_1]++
      } else if (nameStatus?.equals(Status.VALIDATOR_STRING_IS_NOT_ATOMIC.toString())) {
        tippTitleName[Statistics.COUNT_2]++
        tippTitleName[Statistics.LIST_2] << "${nameField?.getPrioValue()}"
        // Statistics.addMetaData(record, 'tipp.title.name', nameStatus) TODO ?
      } else if (nameStatus?.equals(Status.VALIDATOR_STRING_IS_INVALID.toString())) {
        tippTitleName[Statistics.COUNT_3]++
        tippTitleName[Statistics.LIST_3] << "${nameField?.getPrioValue()}"
        // Statistics.addMetaData(record, 'tipp.title.name', nameStatus) TODO ?
      } else if (nameStatus?.equals(Status.VALIDATOR_STRING_IS_MISSING.toString())) {
        tippTitleName[Statistics.COUNT_4]++
        // Statistics.addMetaData(record, 'tipp.title.name', nameStatus) TODO ?
      }
    }
    Statistics.format("NAME IS VALID", tippTitleName, Statistics.COUNT_1, Statistics.LIST_1, stats)
    Statistics.format("name is not atomic", tippTitleName, Statistics.COUNT_2, Statistics.LIST_2, stats)
    Statistics.format("name is not valid", tippTitleName, Statistics.COUNT_3, Statistics.LIST_3, stats)
    Statistics.format("name is missing", tippTitleName, Statistics.COUNT_4, Statistics.LIST_4, stats)
  }

  static Object evaluateUrls(Collection<Record> records, ObjectNode stats) {
    List<Integer> tippUrls = Statistics.getStorage()
    records.each { record ->
      def urlField = record.getMultiField("titleUrl")
      if (urlField) {
        String urlStatus = urlField?.status
        if (urlStatus) {
          if (urlStatus.equals(Status.VALIDATOR_URL_IS_VALID.toString())) {
            tippUrls[Statistics.COUNT_1]++
          } else if (urlStatus.equals(Status.VALIDATOR_URL_IS_INVALID.toString())) {
            tippUrls[Statistics.COUNT_2]++
            tippUrls[Statistics.LIST_2] << "${urlField.getPrioValue()}"
            // Statistics.addMetaData(record, 'tipp.url', url) TODO ?
          } else if (urlStatus.equals(Status.VALIDATOR_URL_IS_NOT_ATOMIC.toString())) {
            tippUrls[Statistics.COUNT_3]++
            tippUrls[Statistics.LIST_3] << "${url.org}"
            // Statistics.addMetaData(record, 'tipp.url', url) TODO ?
          } else if (urlStatus.equals(Status.VALIDATOR_URL_IS_MISSING.toString())) {
            tippUrls[Statistics.COUNT_4]++
            // Statistics.addMetaData(record, 'tipp.url', url) TODO ?
          } else if (urlStatus.equals(Status.VALIDATOR_TIPPURL_NOT_MATCHING.toString())) {
            tippUrls[Statistics.COUNT_5]++
            tippUrls[Statistics.LIST_5] << "${UrlNormalizer.normURL(urlField.getPrioValue(), false)}"
            // Statistics.addMetaData(record, 'tipp.url', url) TODO ?
          }
        }
      }
    }
    Statistics.format("URL IS VALID", tippUrls, Statistics.COUNT_1, Statistics.LIST_1, stats)
    Statistics.format("url is not valid", tippUrls, Statistics.COUNT_2, Statistics.LIST_2, stats)
    Statistics.format("url is not atomic", tippUrls, Statistics.COUNT_3, Statistics.LIST_3, stats)
    Statistics.format("url is missing", tippUrls, Statistics.COUNT_4, Statistics.LIST_4, stats)
    Statistics.format("url is not matching against packageHeader.nominalPlatform",
        tippUrls, Statistics.COUNT_5, Statistics.LIST_5, stats)
  }


  static Object evaluateIdentifiers(Collection<Record> records, ObjectNode stats) {
    HashMap<String, List<Integer>> identifiers = [:]
    identifiers[TitleStruct.EISSN] = Statistics.getStorage()
    identifiers[ZdbBridge.IDENTIFIER] = Statistics.getStorage()
    identifiers[TitleStruct.DOI] = Statistics.getStorage()

    records.each { record ->
      record.getIdentifierFields().each { ident ->
        def tmp = identifiers["${ident.getPrioValue()}"]
        if (tmp) {
          if (ident.status.equals(Status.VALIDATOR_IDENTIFIER_IS_VALID.toString())) {
            tmp[Statistics.COUNT_1]++
          } else if (ident.status.equals(Status.VALIDATOR_IDENTIFIER_IS_INVALID.toString())) {
            tmp[Statistics.COUNT_2]++
            tmp[Statistics.LIST_2] << "${ident.value.org}"
            // Statistics.addMetaData(record, "tipp.title.identifier(${ident.type.v})", ident.value) TODO?
          } else if (ident.status.equals(Status.VALIDATOR_IDENTIFIER_IS_NOT_ATOMIC.toString())) {
            tmp[Statistics.COUNT_3]++
            tmp[Statistics.LIST_3] << "${ident.value.org}"
            // Statistics.addMetaData(record, "tipp.title.identifier(${ident.type.v})", ident.value) TODO?
          } else if (ident.status.equals(Status.VALIDATOR_IDENTIFIER_IS_MISSING.toString())) {
            tmp[Statistics.COUNT_4]++
            // Statistics.addMetaData(record, "tipp.title.identifier(${ident.type.v})", ident.value) TODO?
          } else if (ident.status.equals(Status.VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE.toString())) {
            tmp[Statistics.COUNT_5]++
            // Statistics.addMetaData(record, "tipp.title.identifier(${ident.type.v})", ident.value) TODO?
          }
        }
      }
    }
    identifiers.each { i ->
      stats.get("identifiers").put("${i.key.toUpperCase()} IS VALID", i.value[Statistics.COUNT_1])
      stats.get("identifiers").put("${i.key.toUpperCase()} IS unkown state", i.value[Statistics.COUNT_5])
      Statistics.format("${i.key.toUpperCase()} is invalid", i.value, Statistics.COUNT_2, Statistics.LIST_2, stats)
      Statistics.format("${i.key.toUpperCase()} is not atomic", i.value, Statistics.COUNT_3, Statistics.LIST_3, stats)
      Statistics.format("${i.key.toUpperCase()} is missing", i.value, Statistics.COUNT_4, Statistics.LIST_4, stats)
    }
  }


  static Object evaluateCoverage(Collection<Record> records, ObjectNode stats) {
    // TODO invalid coverages
    List<Integer> coverages = Statistics.getStorage()
    List<Integer> covDates = Statistics.getStorage()

    records.each { record ->
      String coverageStatus = record.getValidation("coverage")
      if (coverages) {
        if (coverageStatus.equals(Status.STRUCTVALIDATOR_COVERAGE_IS_VALID.toString())) {
          coverages[Statistics.COUNT_1]++
        } else if (coverageStatus.equals(Status.STRUCTVALIDATOR_COVERAGE_IS_INVALID.toString())) {
          coverages[Statistics.COUNT_2]++
        } else if (coverageStatus.equals(Status.STRUCTVALIDATOR_COVERAGE_IS_UNDEF.toString())) {
          coverages[Statistics.COUNT_3]++
        }
      }

      MultiField coverageStartDate = record.multiFields.get("dateFirstIssueOnline")
      MultiField coverageEndDate = record.multiFields.get("dateLastIssueOnline")
      // TODO iterate over multiple coverage entries
      if (coverageStartDate?.status.equals(Status.VALIDATOR_DATE_IS_VALID.toString()) ||
          coverageEndDate?.status.equals(Status.VALIDATOR_DATE_IS_VALID.toString())) {
        covDates[Statistics.COUNT_1]++
      }
      if (coverageStartDate?.status.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())) {
        covDates[Statistics.COUNT_2]++
        covDates[Statistics.LIST_2] << "${coverageStartDate.getPrioValue()}"
        // Statistics.addMetaData(multiField, 'tipp.coverage.startDate', sd) TODO?
      } else if (coverageStartDate?.status.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())) {
        covDates[Statistics.COUNT_3]++
        // Statistics.addMetaData(multiField, 'tipp.coverage.startDate', sd) TODO?
      }
      if (coverageEndDate?.status.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())) {
        covDates[Statistics.COUNT_2]++
        covDates[Statistics.LIST_2] << "${coverageEndDate.getPrioValue()}"
        // Statistics.addMetaData(multiField, 'tipp.coverage.endDate', sd) TODO?
      } else if (coverageEndDate?.status.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())) {
        covDates[Statistics.COUNT_3]++
        // Statistics.addMetaData(multiField, 'tipp.coverage.endDate', ed) TODO?
      }
    }
    stats.get("tipps").set("coverage", new ObjectNode(NODE_FACTORY))
    stats.get("tipps").get("coverage").put("~ ARE VALID", coverages[Statistics.COUNT_1])
    stats.get("tipps").get("coverage").put("~ are invalid", coverages[Statistics.COUNT_2])
    stats.get("tipps").get("coverage").put("~ are in undefined state", coverages[Statistics.COUNT_3])
    Statistics.format("VALID DATES FOUND", covDates, Statistics.COUNT_1, Statistics.LIST_1, stats)
    Statistics.format("invalid dates found", covDates, Statistics.COUNT_2, Statistics.LIST_2, stats)
    Statistics.format("missing dates found", covDates, Statistics.COUNT_3, Statistics.LIST_3, stats)
    stats
  }


  static Object evaluatePublisherHistory(Collection<Record> records, ObjectNode stats) {
    List<Integer> pubStruct = Statistics.getStorage()
    List<Integer> pubHistName = Statistics.getStorage()
    // TODO: iterate over publisher history entries
    records.each { record ->
      MultiField ph = record.multiFields.get("publisherHistory")
      if (ph) {
        if (ph.status.equals(Status.STRUCTVALIDATOR_PUBLISHERHISTORY_IS_VALID.toString())) {
          pubStruct[Statistics.COUNT_1]++
        } else if (ph.status.equals(Status.STRUCTVALIDATOR_PUBLISHERHISTORY_IS_INVALID.toString())) {
          pubStruct[Statistics.COUNT_2]++
        } else if (ph.status.equals(Status.STRUCTVALIDATOR_PUBLISHERHISTORY_IS_UNDEF.toString())) {
          pubStruct[Statistics.COUNT_3]++
        }
      }
      MultiField phName = record.multiFields.get("publisherName")
      if (phName) {
        if (phName?.status.equals(Status.VALIDATOR_STRING_IS_VALID.toString())) {
          pubHistName[Statistics.COUNT_1]++
        }
        if (phName?.status.equals(Status.VALIDATOR_STRING_IS_INVALID.toString())) {
          pubHistName[Statistics.COUNT_2]++
          pubHistName[Statistics.LIST_2] << "${phName.getPrioValue()}"
          // Statistics.addMetaData(record, 'title.publisher_history.name', phName) TODO?
        } else if (phName?.status.equals(Status.VALIDATOR_STRING_IS_NOT_ATOMIC.toString())) {
          pubHistName[Statistics.COUNT_3]++
          pubHistName[Statistics.LIST_3] << "${phName.getPrioValue()}"
          // Statistics.addMetaData(record, 'title.publisher_history.name', phName) TODO?
        } else if (phName?.status.equals(Status.VALIDATOR_STRING_IS_MISSING.toString())) {
          pubHistName[Statistics.COUNT_4]++
          // Statistics.addMetaData(record, 'title.publisher_history.name', phName) TODO?
        } else if (phName?.status.equals(Status.VALIDATOR_PUBLISHER_NOT_MATCHING.toString())) {
          pubHistName[Statistics.COUNT_5]++
          pubHistName[Statistics.LIST_5] << "${phName.getPrioValue()}"
          // Statistics.addMetaData(record, 'title.publisher_history.name', phName) TODO?
        }
      }
    }
    stats.get("titles").set("publisher_history", new ObjectNode(NODE_FACTORY))
    stats.get("titles").get("publisher_history").put("~ ARE VALID", pubStruct[Statistics.COUNT_1])
    stats.get("titles").get("publisher_history").put("~ are invalid", pubStruct[Statistics.COUNT_2])
    stats.get("titles").get("publisher_history").put("~ are in undefined state", pubStruct[Statistics.COUNT_3])

    Statistics.format("NAME IS VALID", pubHistName, Statistics.COUNT_1, Statistics.LIST_1, stats)
    Statistics.format("name is not valid", pubHistName, Statistics.COUNT_2, Statistics.LIST_2, stats)
    Statistics.format("name is not atomic", pubHistName, Statistics.COUNT_3, Statistics.LIST_3, stats)
    Statistics.format("name is missing", pubHistName, Statistics.COUNT_4, Statistics.LIST_4, stats)

    Statistics.format("name is not matching against ONLD.jsonld",
        pubHistName, Statistics.COUNT_5, Statistics.LIST_5, stats.titles.publisher_history)


    // TODO: iterate over publisher history date entries
    /* records.each{ record ->
        record.multiFields.get
    json.titles.each{ title ->
        title.value.v.publisher_history.each { ph ->

            def sd = ph.v.startDate
            def ed = ph.v.endDate

            if(sd?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString())){
                pubHistDates[Statistics.COUNT_1]++
            }

            if(sd?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                pubHistDates[Statistics.COUNT_2]++
                pubHistDates[Statistics.LIST_2] << "${sd.org}"

                Statistics.addMetaData(title, 'title.publisher_history.startDate', sd)
            }
            if(ed?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                pubHistDates[Statistics.COUNT_2]++
                pubHistDates[Statistics.LIST_2] << "${ed.org}"

                Statistics.addMetaData(title, 'title.publisher_history.endDate', ed)
            }
            if(sd?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                pubHistDates[Statistics.COUNT_3]++

                Statistics.addMetaData(title, 'title.publisher_history.startDate', sd)
            }
            if(ed?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                pubHistDates[Statistics.COUNT_3]++

                Statistics.addMetaData(title, 'title.publisher_history.endDate', ed)
            }
        }
    }

    Statistics.format("VALID DATES FOUND",   pubHistDates, Statistics.COUNT_1, Statistics.LIST_1, stats.titles.publisher_history)
    Statistics.format("invalid dates found", pubHistDates, Statistics.COUNT_2, Statistics.LIST_2, stats.titles.publisher_history)
    Statistics.format("missing dates found", pubHistDates, Statistics.COUNT_3, Statistics.LIST_3, stats.titles.publisher_history)

    json*/
  }


  static Object evaluateHistoryEvents(Collection<Record> records, ObjectNode stats) {
    List<Integer> theHistoryEvents = Statistics.getStorage()
    records.each { record ->
      // TODO iterate over history events
      MultiField historyEvent = record.multiFields.get("historyEvents")
      if (historyEvent) {
        if (historyEvent.status.equals(Status.STRUCTVALIDATOR_HISTORYEVENT_IS_VALID.toString())) {
          theHistoryEvents[Statistics.COUNT_1]++
        } else if (historyEvent.status.equals(Status.STRUCTVALIDATOR_HISTORYEVENT_IS_INVALID.toString())) {
          theHistoryEvents[Statistics.COUNT_2]++
        } else if (historyEvent.status.equals(Status.STRUCTVALIDATOR_HISTORYEVENT_IS_UNDEF.toString())) {
          theHistoryEvents[Statistics.COUNT_3]++
        }
      }
    }
    stats.get("titles").set("historyEvents", new ObjectNode(NODE_FACTORY))
    stats.get("titles").get("historyEvents").put("~ ARE VALID", theHistoryEvents[Statistics.COUNT_1])
    stats.get("titles").get("historyEvents").put("~ are invalid", theHistoryEvents[Statistics.COUNT_2])
    stats.get("titles").get("historyEvents").put("~ are in undefined state", theHistoryEvents[Statistics.COUNT_3])
  }

  /* TODO ?
  static Object getStatsAfterCleanUp(Object json){
      def nonEmptyTipps = 0
      def nonEmptyTitles = 0

      json.package.tipps.each{ tipp ->
          if(! (tipp.title.identifiers.size() == 0 || tipp.title.name == "")) {
              nonEmptyTipps++
          }
      }
      json.titles.each{ title ->
          if(! (title.identifiers.size() == 0 || title.name == "")) {
              nonEmptyTitles++
          }
      }

      stats.general << ["tipps after cleanUp":  nonEmptyTipps]
      stats.general << ["titles after cleanUp": nonEmptyTitles]

      json
  }*/


  static format(String key, List data, int indexCount, int indexResult, ObjectNode target) {
    if (data[indexCount] > 0 && data[indexResult].minus("").size() > 0) {
      ArrayNode dataNode = toArrayNode(data[indexResult])
      target.set(key, new ObjectNode(NODE_FACTORY).set(data[indexCount].toString(), dataNode))
    } else {
      target.put(key, data[indexCount])
    }
  }

  /**
   * Adding statistic data into json
   * @param target
   * @param dom
   * @param obj
   * @return
   */
  // TODO delete ?
  static addMetaData(Object target, String dom, Object obj) {
    obj.dom = dom
    if (!target.value.v._meta.data) {
      target.value.v._meta << ['data': []]
    }
    target.value.v._meta.data.add(obj)
  }

  /**
   * List for storing data
   * @return
   */
  static getStorage() {
    List<Integer> storage = [0, [], 0, [], 0, [], 0, [], 0, [], 0, [], 0, [], 0, [], 0, [], 0, [], 0, [], 0, [], 0, [], 0, []]
    storage
  }

  static ArrayNode toArrayNode(List<Object> list) {
    ArrayNode result = new ArrayNode(NODE_FACTORY)
    for (Object o in list) {
      result.add(o)
    }
    result
  }
}