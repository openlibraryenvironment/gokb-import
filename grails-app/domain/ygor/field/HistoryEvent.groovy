package ygor.field

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ArrayNode
import de.hbznrw.ygor.readers.KbartReader
import org.apache.commons.lang.StringUtils
import ygor.Record


class HistoryEvent {

  // fields from ZDB API:
  // historyEventDate     : 039E:H --> GOKb: "$TITLE.historyEvents.$ARRAY.$COUNT.date"
  // historyEventRelationType : 039E:b --> GOKb:
  String date
  List<GokbTitleReference> from
  List<GokbTitleReference> to

  static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance

  static hasMany = [ from : GokbTitleReference, to : GokbTitleReference ]
  static constraints = {}


  HistoryEvent(Record record, int index){
    date = record.multiFields.get("historyEventDate").getFieldValue("zdb", index)
    String fromId
    String toId
    String relationType = record.multiFields.get("historyEventRelationType").getFieldValue("zdb", index)
    if (relationType.equals("f")){
      fromId = record.multiFields.get("historyEventIdentifier").getFieldValue("zdb", index)
      toId = record.multiFields.get("zdbId").getFieldValue("zdb", index)
    }
    else if(relationType.equals("s")){
      toId = record.multiFields.get("historyEventIdentifier").getFieldValue("zdb", index)
      fromId = record.multiFields.get("zdbId").getFieldValue("zdb", index)
    }
    from = [] << new GokbTitleReference(fromId, KbartReader.KBART_HEADER_ZDB_ID)
    to   = [] << new GokbTitleReference(toId, KbartReader.KBART_HEADER_ZDB_ID)
  }


  boolean isValid(){
    if (StringUtils.isEmpty(date)){
      return false
    }
    if (from.size() != to.size() || from.size() < 1){
      return false
    }
    for (GokbTitleReference fromReference in from){
      if (!fromReference.isValid()){
        return false
      }
    }
    for (GokbTitleReference toReference in to){
      if (!toReference.isValid()){
        return false
      }
    }
    return true
  }


  ObjectNode toJson() {
    ObjectNode result = NODE_FACTORY.objectNode()
    if (this.isValid()){
      result.put("date", date)
      ArrayNode fromNode = NODE_FACTORY.arrayNode()
      for (GokbTitleReference fromReference in from){
        fromNode.add(fromReference.toJson())
      }
      result.set("from", fromNode)
      ArrayNode toNode = NODE_FACTORY.arrayNode()
      for (GokbTitleReference toReference in to){
        toNode.add(toReference.toJson())
      }
      result.set("to", toNode)
    }
    result
  }
}
