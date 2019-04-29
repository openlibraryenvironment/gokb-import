package de.hbznrw.ygor.export

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import de.hbznrw.ygor.format.GokbFormatter
import de.hbznrw.ygor.tools.JsonToolkit
import org.apache.commons.lang.StringUtils
import ygor.Enrichment

class GokbExporter {


    static GokbFormatter formatter = new GokbFormatter()
    static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance


    static void extractTitles(Enrichment enrichment) {
        def titles = []
        for (def record in enrichment.dataContainer.records){
            titles << JsonToolkit.getTitleJsonFromRecord("gokb", record, formatter)
        }
        enrichment.dataContainer.titles = titles
    }


    static void extractTipps(Enrichment enrichment) {
        def tipps = []
        for (def record in enrichment.dataContainer.records){
            tipps << JsonToolkit.getTippJsonFromRecord("gokb", record, formatter)
        }
        enrichment.dataContainer.tipps = tipps
    }


    static void removeInvalidFields(Enrichment enrichment){
        for (def title in enrichment.dataContainer.titles){
            removeEmptyIdentifiers(title.identifiers)
        }
        for (def tipp in enrichment.dataContainer.pkg.tipps){
            removeEmptyIdentifiers(tipp.title.identifiers)
        }
    }


    static def extractPackageHeader(Enrichment enrichment) {
        // this currently parses the old package header
        // TODO: refactor
        def packageHeader = enrichment.dataContainer.pkg.packageHeader.v
        def result = new ObjectNode(NODE_FACTORY)

        for (String field in ["breakable", "consistent", "fixed", "global", "listVerifiedDate", "listVerifier",
                              "listStatus", "nominalProvider", "paymentType", "scope", "userListVerifier"] ){
            result.put("${field}", packageHeader."${field}".v)
        }
        result.put("name", packageHeader.name.v.v)

        def nominalPlatform = new ObjectNode(NODE_FACTORY)
        nominalPlatform.put("name", packageHeader.nominalPlatform.name)
        nominalPlatform.put("primaryUrl", packageHeader.nominalPlatform.url)
        result.set("nominalPlatform", nominalPlatform)

        result.set("curatoryGroups", getArrayNode(packageHeader, "curatoryGroups"))
        result.set("variantNames", getArrayNode(packageHeader, "variantNames"))
        result.set("additionalProperties", getArrayNode(packageHeader, "additionalProperties"))

        def source = new ObjectNode(NODE_FACTORY)
        if (packageHeader.source?.v?.name && !StringUtils.isEmpty(packageHeader.source.v.name.v))
            source.put("name", packageHeader.source.v.name)
        if (packageHeader.source?.v?.normname && !StringUtils.isEmpty(packageHeader.source.v.normname.v))
            source.put("normname", packageHeader.source.v.normname)
        if (packageHeader.source?.v?.url && !StringUtils.isEmpty(packageHeader.source.v.url.v))
            source.put("url", packageHeader.source.v.url)
        result.set("source", source)

        enrichment.dataContainer.packageHeader = result
    }


    static private ArrayNode getArrayNode(def source, def sourceField){
        ArrayNode result = new ArrayNode(NODE_FACTORY)
        for (def item in source."${sourceField}"){
            result.add(item.v)
        }
        result
    }


    static private void removeEmptyIdentifiers(ArrayNode identifiers){
        def iter = identifiers.iterator()
        def count = 0
        def idsToBeRemoved = []
        while (iter.hasNext()){
            if (iter.next().value.toString() == "\"\""){
                idsToBeRemoved << count
            }
            count++
        }
        for (int i=idsToBeRemoved.size()-1; i>-1; i--){
            identifiers.remove(idsToBeRemoved[i])
        }
    }
}
