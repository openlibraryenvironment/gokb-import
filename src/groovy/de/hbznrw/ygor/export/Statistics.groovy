package de.hbznrw.ygor.export

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode

class Statistics {

  static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance


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