package de.hbznrw.ygor.export.structure

import com.fasterxml.jackson.databind.JsonNode
import de.hbznrw.ygor.tools.JsonToolkit

class PackageHeaderNominalProvider {

  String name
  String oid

  static PackageHeaderNominalProvider fromJson(JsonNode node, String subField) throws IOException{
    PackageHeaderNominalProvider result = new PackageHeaderNominalProvider()
    result.name = JsonToolkit.fromJson(node, subField.concat(".name"))
    result.oid = JsonToolkit.fromJson(node, subField.concat(".oid"))
    result
  }

}
