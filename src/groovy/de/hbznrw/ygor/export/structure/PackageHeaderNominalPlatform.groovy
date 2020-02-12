package de.hbznrw.ygor.export.structure

import com.fasterxml.jackson.databind.JsonNode
import de.hbznrw.ygor.tools.JsonToolkit

class PackageHeaderNominalPlatform {

  String name
  String url

  static PackageHeaderNominalPlatform fromJson(JsonNode node, String subField) throws IOException{
    PackageHeaderNominalPlatform result = new PackageHeaderNominalPlatform()
    result.name = JsonToolkit.fromJson(node, subField.concat(".name"))
    result.url = JsonToolkit.fromJson(node, subField.concat(".url"))
    result
  }

}
