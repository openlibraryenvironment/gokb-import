package ygor.field

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ArrayNode
import org.apache.commons.lang.StringUtils
import ygor.Enrichment
import ygor.Record


class HistoryEvent {

  // fields from ZDB API:
  // historyEventDate     : 039E:H --> GOKb: "$TITLE.historyEvents.$ARRAY.$COUNT.date"
  // historyEventRelationType : 039E:b --> GOKb:
  String date
  List<GokbTitleReference> from = []
  List<GokbTitleReference> to   = []

  static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance

  static hasMany = [ from : GokbTitleReference, to : GokbTitleReference ]
  static constraints = {}


  HistoryEvent(Record record, int index, Enrichment enrichment){
    String fromId
    String toId
    String relationType = record.multiFields.get("historyEventRelationType").getFieldValue("zdb", index)
    if (relationType.equals("f")){
      fromId = record.multiFields.get("historyEventIdentifier").getFieldValue("zdb", index)
      toId = record.multiFields.get("zdbId").getFieldValue("zdb", 0)
      date = extractDate("from", record.multiFields.get("historyEventDate").getFieldValue("zdb", index))
    }
    else if(relationType.equals("s")){
      toId = record.multiFields.get("historyEventIdentifier").getFieldValue("zdb", index)
      fromId = record.multiFields.get("zdbId").getFieldValue("zdb", 0)
    }
    GokbTitleReference fromReference
    GokbTitleReference toReference
    try{
      // exceptions can occur if we have a second order link
      fromReference = new GokbTitleReference(fromId, enrichment, record.publicationType)
      toReference = new GokbTitleReference(toId, enrichment, record.publicationType)
    }
    catch (Exception e){
      return
    }
    from << fromReference
    to   << toReference
  }


  String extractDate(String relation, String zdbDateSpan){
    if (zdbDateSpan.matches("[\\d]{4}-[\\d]{4}")){
      String year
      if (relation.equals("from")){
        year = String.valueOf(Integer.valueOf(zdbDateSpan.substring(5)) + 1)
      }
      else if (relation.equals("to")){
        year = zdbDateSpan.substring(0,4)
      }
      return appendDateFormat(year)
    }
    if (zdbDateSpan.matches("[\\d]{4}")){
      return appendDateFormat(zdbDateSpan)
    }
    return null
  }


  private static String appendDateFormat(String year){
    return year.concat("-01-01")
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
    result
  }
}
