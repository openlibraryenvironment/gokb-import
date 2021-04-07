package de.hbznrw.ygor.export.structure

import com.fasterxml.jackson.databind.JsonNode
import de.hbznrw.ygor.tools.JsonToolkit

class PackageHeaderNominalPlatform {

  String name
  String url
  String oid

  static PackageHeaderNominalPlatform fromJson(JsonNode node, String subField) throws IOException{
    PackageHeaderNominalPlatform result = new PackageHeaderNominalPlatform()
    result.name = JsonToolkit.fromJson(node, subField.concat(".name"))
    result.url = JsonToolkit.fromJson(node, subField.concat(".url"))
    result.oid = JsonToolkit.fromJson(node, subField.concat(".oid"))
    result
  }

}
