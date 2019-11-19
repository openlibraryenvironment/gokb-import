package de.hbznrw.ygor.export

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import ygor.Enrichment
import ygor.StatisticController

class Statistics {

  static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance


  static ObjectNode getRecordsStatisticsBeforeParsing(Enrichment enrichment) {

    enrichment.stats = new ObjectNode(NODE_FACTORY)
    ObjectNode tipps = new ObjectNode(NODE_FACTORY)
    ObjectNode titles = new ObjectNode(NODE_FACTORY)
    ObjectNode general = new ObjectNode(NODE_FACTORY)

    tipps.set("tipps before cleanUp", new TextNode(enrichment.dataContainer.tipps.size))
    titles.set("titles before cleanUp", new TextNode(enrichment.dataContainer.titles.size))

    enrichment.stats.set("tipps", tipps)
    enrichment.stats.set("titles", titles)
    enrichment.stats.set("general", general)
    enrichment.stats.set("identifiers", new ObjectNode(NODE_FACTORY))

    general.put(StatisticController.PROCESSED_KBART_ENTRIES, enrichment.dataContainer.records.size())
    general.put(StatisticController.IGNORED_KBART_ENTRIES, 0) // TODO
    general.put(StatisticController.DUPLICATE_KEY_ENTRIES, 0) // TODO

    enrichment.stats
  }


  static format(String key, List data, int indexCount, int indexResult, ObjectNode target) {
    if (data[indexCount] > 0 && data[indexResult].minus("").size() > 0) {
      ArrayNode dataNode = toArrayNode(data[indexResult])
      target.set(key, new ObjectNode(NODE_FACTORY).set(data[indexCount].toString(), dataNode))
    } else {
      target.put(key, data[indexCount])
    }
  }


  static ArrayNode toArrayNode(List<Object> list) {
    ArrayNode result = new ArrayNode(NODE_FACTORY)
    for (Object o in list) {
      result.add(o)
    }
    result
  }
}