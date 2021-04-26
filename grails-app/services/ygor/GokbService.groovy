package ygor

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang.StringUtils

class GokbService {

  def grailsApplication
  def messageSource

  Map getTitleMap(def qterm = null, def suggest = true, String curatoryGroup = null) {
    log.info("getting title map from gokb ..")
    def result = [:]
    try {
      String esQuery = qterm ? URLEncoder.encode(qterm) : ""
      def json
      if (suggest) {
        json = geElasticsearchSuggests(esQuery, "Package", null, null, curatoryGroup) // 10000 is maximum value allowed by now
      }
      else {
        json = geElasticsearchFindings(esQuery, "Package", null, 10)
      }
      result.records = []
      result.map = [:]

      if (json?.info?.records) {
        json.info.records.each { r ->
          result.records.add([id: r.name, text: r.name.concat(" - ").concat(r.primaryUrl ?: "none").concat(r.status ? " (${r.status})" : ""), url: r.primaryUrl, status: r.status, oid: r.id, uuid: r.uuid, name: r.name, findFilter: r.id.concat(";").concat(r.name)])
          result.map.put(r.name.concat(" - ").concat(r.primaryUrl ?: "none"), r.primaryUrl ?: 'no URL!')
        }
      }
      if (json?.warning?.records) {
        json.warning.records.each { r ->
          result.records.add([id: r.name, text: r.name.concat(" - ").concat(r.primaryUrl ?: "none").concat(r.status ? " (${r.status})" : ""), url: r.primaryUrl, status: r.status, oid: r.id, uuid: r.uuid, name: r.name, findFilter: r.id.concat(";").concat(r.name)])
          result.map.put(r.name.concat(" - ").concat(r.primaryUrl ?: "none"), r.primaryUrl ?: 'no URL!')
        }
      }
    }
    catch (Exception e) {
      log.error(e.getMessage())
    }
    result
  }


  Map getPlatformMap(String qterm = null, def suggest = true, String curatoryGroup) {
    log.info("getting platform map from gokb ..")
    def result = [:]
    try {
      String esQuery = qterm ? URLEncoder.encode(qterm) : ""
      def json
      if (suggest) {
        json = geElasticsearchSuggests(esQuery, "Platform", null, null, curatoryGroup) // 10000 is maximum value allowed by now
      }
      else {
        json = geElasticsearchFindings(esQuery, "Platform", null, 10)
      }
      result.records = []
      result.map = [:]

      if (json?.info?.records) {
        json.info.records.each { r ->
          result.records.add([id: r.name, text: r.name.concat(" - ").concat(r.primaryUrl ?: "none").concat(r.status ? " (${r.status})" : ""), url: r.primaryUrl, status: r.status, oid: r.id, name: r.name, findFilter: r.id.concat(";").concat(r.name)])
          result.map.put(r.name.concat(" - ").concat(r.primaryUrl ?: "none"), r.primaryUrl ?: 'no URL!')
        }
      }
      if (json?.warning?.records) {
        json.warning.records.each { r ->
          result.records.add([id: r.name, text: r.name.concat(" - ").concat(r.primaryUrl ?: "none").concat(r.status ? " (${r.status})" : ""), url: r.primaryUrl, status: r.status, oid: r.id, name: r.name, findFilter: r.id.concat(";").concat(r.name)])
          result.map.put(r.name.concat(" - ").concat(r.primaryUrl ?: "none"), r.primaryUrl ?: 'no URL!')
        }
      }
    }
    catch (Exception e) {
      log.error(e.getMessage())
    }
    result
  }


  Map geElasticsearchSuggests(final String query, final String type, final String role,
                              final List<String> embeddedFields, final String curatoryGroup) {
    String url = buildUri(grailsApplication.config.gokbApi.xrSuggestUriStub.toString(), query, type, role, null)
    url = appendEmbeddedFields(url, embeddedFields)
    url = appendCuratoryGroup(url, curatoryGroup)
    queryElasticsearch(url)
  }


  Map geElasticsearchFindings(final String query, final String type,
                              final String role, final Integer max) {
    String url = buildUri(grailsApplication.config.gokbApi.xrFindUriStub.toString(), query, type, role, max)
    queryElasticsearch(url)
  }


  Map queryElasticsearch(String url) {
    log.info("querying: " + url)
    def http = new HTTPBuilder(url)
    // http.auth.basic user, pwd
    http.request(Method.GET) { req ->
      headers.'User-Agent' = 'ygor'
      response.success = { resp, html ->
        log.info("server response: ${resp.statusLine}")
        log.debug("server:          ${resp.headers.'Server'}")
        log.debug("content length:  ${resp.headers.'Content-Length'}")
        if (resp.status > 400) {
          return ['warning': html]
        }
        else {
          return ['info': html]
        }
      }
      response.failure = { resp ->
        log.error("server response: ${resp.statusLine}")
        return ['error': resp.statusLine]
      }
    }
  }


  private String buildUri(final String stub, final String query, final String type, final String role, final Integer max, String category = null) {
    String url = stub + "?"
    if (query) {
      url += "q=" + query + "&"
    }
    if (type) {
      url += "componentType=" + type + "&"
    }
    if (category) {
      url += "category=" + category + "&"
    }
    if (role) {
      url += "role=" + role + "&"
    }
    if (max) {
      url += "max=" + max + "&"
    }
    url.substring(0, url.length() - 1)
  }


  String appendEmbeddedFields(String uri, List<String> embeddedFields){
    if (!CollectionUtils.isEmpty(embeddedFields)){
      uri = uri.concat("?_embed=")
      for (String field : embeddedFields){
        if (uri.endsWith("=")){
          uri = uri.concat(field)
        }
        else{
          uri = uri.concat(",").concat(field)
        }
      }
    }
    uri
  }


  String appendCuratoryGroup(String uri, String curatoryGroup){
    if (!StringUtils.isEmpty(curatoryGroup)){
      uri = uri.contains("?") ? uri + "&" : uri + "?"
      uri = uri + "curatoryGroup=" + curatoryGroup
    }
    uri
  }


  Map getProviderMap(def qterm = null, List<String> embeddedFields, String curatoryGroup = null) {
    log.info("getting provider map from gokb ..")
    def result = [:]
    try {
      String esQuery = qterm ? URLEncoder.encode(qterm) : ""
      def json = geElasticsearchSuggests(esQuery, "Org", null, embeddedFields, null) // 10000 is maximum value allowed by now
      result.records = []
      result.map = [:]
      if (json?.info?.records) {
        json.info.records.each { r ->
          result.records.add([id: r.name, text: r.name.concat(r.status ? " (${r.status})" : ""), status: r.status, oid: r.id, name: r.name])
          result.map.put(r.name, r.name)
        }
      }
      if (json?.warning?.records) {
        json.warning.records.each { r ->
          result.records.add([id: r.name, text: r.name.concat(r.status ? " (${r.status})" : ""), status: r.status, oid: r.id, name: r.name])
          result.map.put(r.name, r.name)
        }
      }
    }
    catch (Exception e) {
      log.error(e.getMessage())
    }
    result
  }


  def getNamespaceList(String category) {
    String nsBase = grailsApplication.config.gokbApi.baseUri.toString() + "api/namespaces"
    String nsUrl = buildUri(nsBase, null, null, null, null, category)
    def placeholderNamespace = messageSource.getMessage('listDocuments.js.placeholder.namespace', null, Locale.default)
    def result = [[id: '', text: placeholderNamespace]]

    log.debug("Quering namespaces via: ${nsUrl}")
    try {
      def json = queryElasticsearch(nsUrl)
      if (json?.info?.result) {
        log.debug("Retrieved namespaces via: ${nsUrl}")
        json.info.result.each { r ->
          addValidNamespaceToResult(r, result)
        }
      }
      if (json?.warning?.result) {
        json.warning.result.each { r ->
          addValidNamespaceToResult(r, result)
        }
      }
    }
    catch (Exception e) {
      log.error(e.getMessage())
    }
    result
  }


  private void addValidNamespaceToResult(r, List<LinkedHashMap<String, String>> result){
    if (!(r.value in ["issn", "eissn", "doi"])){
      if (r.value && r.category){
        result.add([id: r.value, text: r.value, cat: r.category])
      }
    }
  }


  def getCurrentCuratoryGroupsList() {
    String cgsUrl = grailsApplication.config.gokbApi.baseUri.toString() + "api/groups"
    def result = []
    log.debug("Quering curatory groups via: ${cgsUrl}")
    try {
      def json = queryElasticsearch(cgsUrl)
      if (json?.info?.result) {
        log.debug("Retrieved curatory groups via: ${cgsUrl}")
        json.info.result.each { r ->
          if (!StringUtils.isEmpty(r.name) && r.status.equals("Current")){
            result.add([id: r.uuid, text: r.name])
          }
        }
      }
      if (json?.warning?.result) {
        if (!StringUtils.isEmpty(r.value) && r.status.equals("Current")){
          result.add([id: r.value, text: r.value])
        }
      }
    }
    catch (Exception e) {
      log.error(e.getMessage())
    }
    result
  }
}
