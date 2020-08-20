package ygor

import de.hbznrw.ygor.export.structure.Pod
import groovyx.net.http.HTTPBuilder

import javax.servlet.http.HttpSession
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.codehaus.groovy.grails.web.util.WebUtils
import de.hbznrw.ygor.tools.*

class EnrichmentService{

  def grailsApplication
  GokbService gokbService

  Enrichment fromCommonsMultipartFile(CommonsMultipartFile file){
    Enrichment en = fromFilename(file.originalFilename)
    file.transferTo(new File(en.originPathName))
    return en
  }

  Enrichment fromFilename(String filename){
    return new Enrichment(getSessionFolder(), filename)
  }

  void addFileAndFormat(Enrichment en, String delimiter, String quote, String quoteMode){
    en.setStatus(Enrichment.ProcessingState.PREPARE_1)
    def tmp = [:]
    tmp << ['delimiter': delimiter]
    tmp << ['quote': quote]
    tmp << ['quoteMode': quoteMode]
    def formats = getSessionFormats()
    formats << ["${en.originHash}": tmp]
    getSessionEnrichments() << ["${en.resultHash.toString()}": en]
  }


  File getFile(Enrichment enrichment, Enrichment.FileType type){
    enrichment.getAsFile(type, true)
  }


  void deleteFileAndFormat(Enrichment enrichment){
    if (enrichment){
      def origin = enrichment.getAsFile(Enrichment.FileType.ORIGIN, false)
      if (origin){
        origin.delete()
      }
      getSessionEnrichments()?.remove("${enrichment.resultHash}")
      getSessionEnrichments()?.remove(enrichment.resultHash)
      getSessionFormats()?.remove("${enrichment.originHash}")
      getSessionFormats()?.remove(enrichment.originHash)
    }
  }


  void prepareFile(Enrichment enrichment, Map pm){
    if (enrichment == null || pm == null){
      return
    }
    def ph = enrichment.dataContainer.pkg.packageHeader
    if (pm['pkgTitle']){
      ph.name = new Pod(pm['pkgTitle'][0])
      enrichment.packageName = pm['pkgTitle'][0]
    }
    if (pm['addOnly'] && pm['addOnly'][0] in ["on", "true"]){
      enrichment.addOnly = true
    }
    if (pm['pkgIsil'] && pm['pkgIsil'][0]){
      enrichment.dataContainer.isil = pm['pkgIsil'][0]
    }
    if (pm['pkgCuratoryGroup']){
      enrichment.dataContainer.curatoryGroup = (pm['pkgCuratoryGroup'][0])
    }
    if (pm['pkgId'] && "" != pm['pkgId'][0].trim()){
      enrichment.dataContainer.pkgId = (pm['pkgId'][0])
    }
    if (pm['pkgIdNamespace'] && "" != pm['pkgIdNamespace'][0].trim()){
      enrichment.dataContainer.pkgIdNamespace = (pm['pkgIdNamespace'][0])
    }
    def platform = getPlatform(pm)
    if (platform != null){
      applyPlatformToPackageHeader(platform, ph)
    }
    if (pm['pkgNominalProvider']){
      ph.nominalProvider = pm['pkgNominalProvider'][0]
    }
    if (pm['pkgTitleId']){
      enrichment.dataContainer.info.namespace_title_id = pm['pkgTitleId'][0]
    }
    enrichment.setStatus(Enrichment.ProcessingState.PREPARE_2)
  }


  private def getPlatform(Map pm){
    log.debug("Getting platforms for: ${pm['pkgNominalPlatform'][0]}")
    def platformSplit = splitPlatformString(pm['pkgNominalPlatform'][0])
    if (platformSplit == null || platformSplit.size() != 2){
      log.error("Could not split platform string.")
      return
    }
    def platform = pickPlatform(platformSplit[0], platformSplit[1])
    if (platform == null){
      log.error("No platform found.")
    }
    return platform
  }


  private def pickPlatform(String platFormId, String queryTerm){
    def platforms = gokbService.getPlatformMap(queryTerm, false).records
    def pkgNomPlatform = null
    log.debug("Got platforms: ${platforms}")
    platforms.each{
      if (it.name == queryTerm && it.status == "Current" && it.oid == platFormId){
        if (pkgNomPlatform){
          log.warn("Multiple platforms found named: ".concat(it.name))
        }
        else{
          log.debug("Set ${it.name} as nominalPlatform.")
          pkgNomPlatform = it
        }
      }
      else{
        if (it.name != queryTerm){
          log.debug("No name match: ${it.name} - ${queryTerm}")
        }
        if (it.status != "Current"){
          log.debug("Wrong status: ${it.status}")
        }
        if (it.oid != platFormId){
          log.debug("No OID match: ${it.oid} - ${platFormId}")
        }
      }
    }
    return pkgNomPlatform
  }


  private void applyPlatformToPackageHeader(def platform, def packageHeader){
    try{
      new URL(platform.url)
      packageHeader.nominalPlatform.url = platform.url
    }
    catch (MalformedURLException e){
      packageHeader.nominalPlatform.url = ""
    }
    packageHeader.nominalPlatform.name = platform.name
  }


  /**
   * Splits into an array of platformId and platformName (query term)
   */
  private def splitPlatformString(String platformString){
    def tmp = platformString.split(';')
    if (tmp.size() != 2){
      return null
    }
    return [tmp[0], tmp[1]]
  }


  def addSessionEnrichment(Enrichment enrichment){
    HttpSession session = SessionToolkit.getSession()
    if (!session.enrichments){
      session.enrichments = [:]
    }
    session.enrichments.put(enrichment.resultHash.toString(), enrichment)
  }


  def getSessionEnrichments(){
    HttpSession session = SessionToolkit.getSession()
    if (!session.enrichments){
      session.enrichments = [:]
    }
    session.enrichments
  }


  def getSessionFormats(){
    HttpSession session = SessionToolkit.getSession()
    if (!session.formats){
      session.formats = [:]
    }
    session.formats
  }


  /**
   * Return session depending directory for file upload.
   * Creates if not existing.
   */
  File getSessionFolder(){
    def session = WebUtils.retrieveGrailsWebRequest().session
    def path = grailsApplication.config.ygor.uploadLocation + File.separator + session.id
    def sessionFolder = new File(path)
    if (!sessionFolder.exists()){
      sessionFolder.mkdirs()
    }
    sessionFolder
  }
}
