package de.hbznrw.ygor.export

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import de.hbznrw.ygor.format.GokbFormatter
import de.hbznrw.ygor.tools.JsonToolkit
import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils
import ygor.Enrichment
import ygor.Enrichment.FileType

@Log4j
class GokbExporter {

    static ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper()
    static GokbFormatter FORMATTER = new GokbFormatter()
    static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance

    static File getFile(Enrichment enrichment, FileType type, String path){
        enrichment.validateContainer()
        switch(type){
            case FileType.ORIGIN:
                return new File(path)
                break
            case FileType.JSON_PACKAGE_ONLY:
                ObjectNode result = GokbExporter.extractPackage(enrichment)
                def file = new File(path)
                file.write(JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result), "UTF-8")
                return file
                break
            case FileType.JSON_TITLES_ONLY:
                ArrayNode result = GokbExporter.extractTitles(enrichment)
                def file = new File(path)
                file.write(JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result), "UTF-8")
                return file
                break
            case FileType.JSON_OO_RAW:
                return new File(path)
                break
        }
        return null
    }


    static ObjectNode extractPackage(Enrichment enrichment){
        ObjectNode pkg = new ObjectNode(NODE_FACTORY)
        log.debug("extracting package ...")
        pkg.set("packageHeader", extractPackageHeader(enrichment))
        pkg.set("tipps", extractTipps(enrichment))
        log.debug("extracting package finished")
        pkg
    }


    static ArrayNode extractTitles(Enrichment enrichment){
        log.debug("extracting titles ...")
        ArrayNode titles = new ArrayNode(NODE_FACTORY)
        for (def record in enrichment.dataContainer.records){
            titles.add(JsonToolkit.getTitleJsonFromRecord("gokb", record, FORMATTER))
        }
        titles = removeEmptyFields(titles)
        enrichment.dataContainer.titles = removeEmptyIdentifiers(titles, FileType.JSON_TITLES_ONLY)
        log.debug("extracting titles finished")
        titles
    }


    static ArrayNode extractTipps(Enrichment enrichment){
        log.debug("extracting tipps ...")
        ArrayNode tipps = new ArrayNode(NODE_FACTORY)
        for (def record in enrichment.dataContainer.records){
            tipps.add(JsonToolkit.getTippJsonFromRecord("gokb", record, FORMATTER))
        }
        tipps = removeEmptyFields(tipps)
        enrichment.dataContainer.tipps = removeEmptyIdentifiers(tipps, FileType.JSON_PACKAGE_ONLY)
        log.debug("extracting tipps finished")
        tipps
    }


    static ArrayNode removeEmptyIdentifiers(ArrayNode arrayNode, FileType type){
        log.debug("removing invalid fields ...")
        if (type.equals(FileType.JSON_TITLES_ONLY)){
            for (def title in arrayNode.elements()){
                removeEmptyIds(title.identifiers)
            }
        }
        else if (type.equals(FileType.JSON_PACKAGE_ONLY)){
            for (def tipp in arrayNode.elements()){
                removeEmptyIds(tipp.title.identifiers)
            }
        }
        log.debug("removing invalid fields finished")
        arrayNode
    }


    static ObjectNode extractPackageHeader(Enrichment enrichment){
        // this currently parses the old package header
        // TODO: refactor
        log.debug("parsing package header ...")
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
        log.debug("parsing package header finished")
        result
    }


    static private ArrayNode getArrayNode(def source, def sourceField){
        ArrayNode result = new ArrayNode(NODE_FACTORY)
        for (def item in source."${sourceField}"){
            result.add(item.v)
        }
        result
    }


    static private void removeEmptyIds(ArrayNode identifiers){
        def count = 0
        def idsToBeRemoved = []
        for (ObjectNode idNode in identifiers.elements()){
            if (idNode.elements().size() == 1 && idNode.get("type") != null){
                // identifier has "type" only ==> remove
                idsToBeRemoved << count
            }
            else if (idNode.elements().size() > 1 && idNode.get("value").asText().trim() == "\"\""){
                idsToBeRemoved << count
            }
            count++
        }
        for (int i=idsToBeRemoved.size()-1; i>-1; i--){
            identifiers.remove(idsToBeRemoved[i])
        }
    }


    // adapted from: https://technicaldifficulties.io/2018/04/26/using-jackson-to-remove-empty-json-fields/ -thx Stacie!
    static ObjectNode removeEmptyFields(final ObjectNode jsonNode){
        ObjectNode result = new ObjectMapper().createObjectNode()
        for (def entry in jsonNode.fields()){
            String key = entry.getKey()
            JsonNode value = entry.getValue()
            if (value instanceof ObjectNode){
                JsonNode subNode = removeEmptyFields((ObjectNode)value)
                if (subNode.size() > 0) {
                    Map<String, ObjectNode> map = new HashMap<String, ObjectNode>()
                    map.put(key, subNode)
                    result.setAll(map)
                }
            }
            else if (value instanceof ArrayNode){
                JsonNode subNode = removeEmptyFields((ArrayNode) value)
                if (subNode.size() > 0){
                    result.set(key, subNode)
                }
            }
            else if (value.asText() != null){
                if (value.asText().equals(" ")){
                    result.put(key, "")
                }
                else if (!value.asText().isEmpty()){
                    result.set(key, value)
                }
            }
        }
        return result
    }


    static ArrayNode removeEmptyFields(ArrayNode array){
        ArrayNode result = new ObjectMapper().createArrayNode()
        for (JsonNode value in array.elements()){
            if (value instanceof ArrayNode){
                JsonNode subNode = removeEmptyFields((ArrayNode)(value))
                if (subNode.size() > 0){
                    result.add(subNode)
                }
            }
            else if (value instanceof ObjectNode){
                JsonNode subNode = removeEmptyFields((ObjectNode) (value))
                if (subNode.size() > 0){
                    result.add(removeEmptyFields((ObjectNode) (value)))
                }
            }
            else if (value.asText() != null){
                if (value.asText().equals(" ")){
                    result.add("")
                }
                else if (!value.asText().isEmpty()){
                    result.add(value)
                }
            }
        }
        return result
    }
}
