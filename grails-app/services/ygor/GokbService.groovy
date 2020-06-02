package ygor

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.commons.lang.StringUtils

class GokbService {

  def grailsApplication
  def messageSource

  Map getPlatformMap(def qterm = null, def suggest = true) {
    log.info("getting platform map from gokb ..")
    def result = [:]
    try {
      String esQuery = qterm ? URLEncoder.encode(qterm) : ""
      def json = null
      if (suggest) {
        json = geElasticsearchSuggests(esQuery, "Platform", null) // 10000 is maximum value allowed by now
      } else {
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
    if (result.map?.size() == 0)
      result.map = getPackageHeaderNominalPlatformPreset()
    result
  }


  Map geElasticsearchSuggests(final String query, final String type, final String role) {
    String url = buildUri(grailsApplication.config.gokbApi.xrSuggestUriStub.toString(), query, type, role, null)
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
//         http.auth.basic user, pwd
    http.request(Method.GET) { req ->
      headers.'User-Agent' = 'ygor'
      response.success = { resp, html ->
        log.info("server response: ${resp.statusLine}")
        log.debug("server:          ${resp.headers.'Server'}")
        log.debug("content length:  ${resp.headers.'Content-Length'}")
        if (resp.status > 400) {
          return ['warning': html]
        } else {
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


  Map getProviderMap(def qterm = null) {
    log.info("getting provider map from gokb ..")
    def result = [:]
    try {
      String esQuery = qterm ? URLEncoder.encode(qterm) : ""
      def json = geElasticsearchSuggests(esQuery, "Org", null) // 10000 is maximum value allowed by now
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


  def getNamespaceList() {
    String nsBase = grailsApplication.config.gokbApi.baseUri.toString() + "api/namespaces"
    String nsCategory = grailsApplication.config.gokbApi.namespaceCategory ?: null
    String nsUrl = buildUri(nsBase, null, null, null, null, nsCategory)
    def placeholderNamespace = messageSource.getMessage('listDocuments.js.placeholder.namespace', null, Locale.default)
    def result = [[id: '', text: placeholderNamespace]]

    log.debug("Quering namespaces via: ${nsUrl}")
    try {
      def json = queryElasticsearch(nsUrl)
      if (json?.info?.result) {
        log.debug("Retrieved namespaces via: ${nsUrl}")
        json.info.result.each { r ->
          if (!(r.value in ["issn", "eissn", "doi"])) {
            result.add([id: r.value, text: r.value, cat: r.category])
          }
        }
      }
      if (json?.warning?.result) {
        json.warning.result.each { r ->
          if (!(r.value in ["issn", "eissn"])) {
            result.add([id: r.value, text: r.value, cat: r.category])
          }
        }
      }
    }
    catch (Exception e) {
      log.error(e.getMessage())
    }
    result
  }


  def getCuratoryGroupsList() {
    String cgsUrl = grailsApplication.config.gokbApi.baseUri.toString() + "api/groups"
    def placeholderCuratoryGroup = messageSource.getMessage('listDocuments.js.placeholder.curatorygroup', null, Locale.default)
    def result = [[id: '', text: placeholderCuratoryGroup]]

    log.debug("Quering curatory groups via: ${cgsUrl}")
    try {
      def json = queryElasticsearch(cgsUrl)
      if (json?.info?.result) {
        log.debug("Retrieved curatory groups via: ${cgsUrl}")
        json.info.result.each { r ->
          if (!StringUtils.isEmpty(r.name)){
            result.add([id: r.uuid, text: r.name])
          }
        }
      }
      if (json?.warning?.result) {
        if (!StringUtils.isEmpty(r.value)){
          result.add([id: r.value, text: r.value])
        }
      }
    }
    catch (Exception e) {
      log.error(e.getMessage())
    }
    result
  }

  // --- fallback

  Map getPackageHeaderNominalPlatformPreset() {
    log.warn("fallback: using static platform map")
    return [
        "ACM Digital Library"                                             : "http://dl.acm.org/",
        "ACS Publications"                                                : "http://pubs.acs.org/",
        "AIP Scitation"                                                   : "http://scitation.aip.org/",
        "American Chemical Society"                                       : "",
        "American Institute of Physics"                                   : "http://scitation.aip.org/admin/reporting/kbart/list.action",
        "American Mathematical Society"                                   : "",
        "American Medical Association"                                    : "",
        "American Physical Society"                                       : "http://www.the-aps.org/mm/Publications",
        "American Psychological Association"                              : "http://supp.apa.org/kbart/journals/",
        "American Society for Microbiology"                               : "",
        "American Society of Civil Engineers"                             : "",
        "American Society of Mechanical Engineers"                        : "",
        "Annual Reviews"                                                  : "http://www.annualreviews.org/",
        "BioOne"                                                          : "http://www.bioone.org/",
        "BMJ Publications"                                                : "",
        "Brepols Publishers"                                              : "",
        "Brill"                                                           : "http://www.brill.com/",
        "Budrich Journals"                                                : "http://budrich-journals.de",
        "BWV Digitale Bibliothek"                                         : "http://bwv.verlag-online.eu/digibib/bwv/",
        "Cambridge University Press"                                      : "http://www.cambridge.org/",
        "Content Select"                                                  : "https://content-select.com",
        "CUFTS Open Knowledgebase"                                        : "",
        "Cultura - Hombre - Sociedad"                                     : "",
        "DeGruyter Online"                                                : "http://www.degruyter.com/",
        "Directory of Open Access Journals"                               : "",
        "Duke University Press"                                           : "https://www.dukeupress.edu/",
        "Duncker & Humblot eJournals"                                     : "http://ejournals.duncker-humblot.de/",
        "East View Information Services"                                  : "",
        "Edinburgh University Press"                                      : "http://www.euppublishing.com/action/showPublications?display=bySubject&pubType=journal",
        "Elsevier ScienceDirect"                                          : "http://www.sciencedirect.com/",
        "EMBO Press"                                                      : "http://embopress.org/",
        "Emerald Insight"                                                 : "http://www.emeraldinsight.com/",
        "ESVCampus"                                                       : "www.esvcampus.de",
        "Gale"                                                            : "",
        "GeoScienceWorld"                                                 : "http://www.geoscienceworld.org/",
        "GSA Publications"                                                : "http://www.gsapubs.org/",
        "Hanser eLibrary"                                                 : "",
        "HeinOnline"                                                      : "",
        "HighWire"                                                        : "http://cufts2.lib.sfu.ca/knowledgebase/",
        "Hogrefe eContent"                                                : "http://econtent.hogrefe.com",
        "IEEE Xplore"                                                     : "http://ieeexplore.ieee.org",
        "ingentaconnect"                                                  : "www.ingentaconnect.com",
        "IOPscience"                                                      : "http://iopscience.iop.org",
        "Iowa Research Online"                                            : "",
        "Journals.ASM.org"                                                : "http://journals.asm.org/",
        "JSTOR"                                                           : "",
        "Karger"                                                          : "http://www.karger.com",
        "Lab Animal"                                                      : "http://www.labanimal.com",
        "Laboratory Investigation"                                        : "www.laboratoryinvestigation.org",
        "link.springer.com"                                               : "link.springer.com",
        "Loeb Classical Library"                                          : "http://www.loebclassics.com/",
        "Maney Publishing"                                                : "http://maneypublishing.com/",
        "Mary Ann Liebert, Inc. Publishers"                               : "http://www.liebertpub.com/",
        "Microscopy Today"                                                : "http://microscopy-today.com/",
        "nature.com"                                                      : "http://www.nature.com/",
        "Nomos eLibrary"                                                  : "http://www.nomos-elibrary.de/",
        "OECD"                                                            : "",
        "OhioLINK"                                                        : "",
        "Open Journal System"                                             : "",
        "Ovid"                                                            : "",
        "Oxford University Press Journals"                                : "http://oxfordjournals.org",
        "Project Euclid"                                                  : "http://projecteuclid.org/",
        "Project Muse"                                                    : "http://muse.jhu.edu/",
        "Revista Iberoamericana de Viticultura, Agroindustria y Ruralidad": "http://revistarivar.cl/",
        "Revista iZQuierdas"                                              : "http://www.izquierdas.cl",
        "Revistas y Publicaciones Ediciones Electrónicas"                 : "http://www.revistas.usach.cl",
        "Royal Society of Chemistry"                                      : "",
        "R&W Online"                                                      : "http://www.ruw.de/",
        "Sage"                                                            : "http://www.sagepub.com",
        "Schulz-Kirchner"                                                 : "http://www.schulz-kirchner.de/",
        "Science"                                                         : "http://www.sciencemag.org/",
        "SpringerLink"                                                    : "http://link.springer.com",
        "Taylor & Francis Online"                                         : "http://www.tandfonline.com",
        "Thieme Connect"                                                  : "https://www.thieme-connect.de/products/all/home.html",
        "UNC Greensboro"                                                  : "http://libjournal.uncg.edu/",
        "Vahlen eLibrary"                                                 : "http://elibrary.vahlen.de/",
        "V&R eLibrary"                                                    : "http://www.vr-elibrary.de/",
        "Wiley Online Library"                                            : "http://onlinelibrary.wiley.com/",
        "WorldSciNet"                                                     : "",
        "World Textile Information Network"                               : ""
    ]
  }

}
