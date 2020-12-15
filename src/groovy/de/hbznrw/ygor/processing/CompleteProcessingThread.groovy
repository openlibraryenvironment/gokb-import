package de.hbznrw.ygor.processing

import de.hbznrw.ygor.readers.KbartFromUrlReader
import de.hbznrw.ygor.readers.KbartReader
import de.hbznrw.ygor.tools.UrlToolkit
import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils
import ygor.AutoUpdateService
import ygor.Enrichment
import ygor.EnrichmentService
import ygor.UploadJob
import ygor.UploadJobFrame
import ygor.field.MappingsContainer

@Log4j
class CompleteProcessingThread extends Thread {

  EnrichmentService enrichmentService
  KbartReader kbartReader
  Map<String, Object> pkg
  Map<String, Object> src
  String token
  UploadJobFrame uploadJobFrame

  /**
   * used by EnrichmentController.processGokbPackage()
   * @param kbartReader
   * @param pkg
   * @param src
   * @param token
   */
  CompleteProcessingThread(KbartReader kbartReader, Map<String, Object> pkg, Map<String, Object> src, String token,
      UploadJobFrame uploadJobFrame){
    enrichmentService = new EnrichmentService()
    this.kbartReader = kbartReader
    this.pkg = pkg
    this.src = src
    this.token = token
    this.uploadJobFrame = uploadJobFrame
  }

  @Override
  void run() throws Exception {
    enrichmentService.addUploadJob(uploadJobFrame)
    String sessionFolder = grails.util.Holders.grailsApplication.config.ygor.uploadLocation.toString()
        .concat(File.separator).concat(UUID.randomUUID().toString())
    Locale locale = new Locale("en")                                    // TODO get from request or package
    List<URL> updateUrls
    if (Integer.valueOf(pkg._tippCount) == 0){
      // this is obviously a new package --> update with older timestamp
      updateUrls = new ArrayList<>()
      updateUrls.add(new URL(src.url))
    }
    else{
      // this package had already been filled with data
      updateUrls = AutoUpdateService.getUpdateUrls(src.url, src.lastRun, pkg.dateCreated)
    }
    updateUrls = UrlToolkit.removeNonExistentURLs(updateUrls)
    Iterator urlsIterator = updateUrls.listIterator(updateUrls.size())
    while(urlsIterator.hasPrevious()){
      URL url = urlsIterator.previous()
      kbartReader = enrichmentService.kbartReader = new KbartFromUrlReader(url, new File(sessionFolder), locale)
      Enrichment enrichment
      try {
        enrichment = prepareEnrichment(token, sessionFolder, pkg, src)
      }
      catch (Exception e) {
        e.printStackTrace()
        log.error("Could not build enrichment for package ${pkg.id} with uuid ${pkg.uuid}")
        uploadJobFrame
        continue
      }
      enrichment.originPathName = kbartReader.fileName
      UploadJob uploadJob = enrichmentService.processComplete(uploadJobFrame, enrichment, null, null, false, true)
      enrichmentService.addUploadJob(uploadJob)                             // replacing uploadJobFrame with same uuid
      if (uploadJob == null){
        log.error("Could not upload processed package ${pkg.id} with uuid ${pkg.uuid}")
        continue
      }
      else{
        // successfully proceeded upload
        break
      }
    }
  }


  private Enrichment prepareEnrichment(String updateToken, String sessionFolder, def pkg, def src)
      throws Exception{
    Enrichment enrichment = Enrichment.fromFilename(sessionFolder, pkg.name)
    String addOnly = "false"
    def pmOptions = [MappingsContainer.KBART]
    if (src.zdbMatch){
      pmOptions.add(MappingsContainer.ZDB)
    }
    if (src.ezbMatch){
      pmOptions.add(MappingsContainer.EZB)
    }
    String platformName = pkg._embedded?.nominalPlatform?.name
    String platformId = pkg._embedded?.nominalPlatform?.id
    String platformUrl = pkg._embedded?.nominalPlatform?.primaryUrl
    Map<String, Object> params = new HashMap<>()
    String pkgTitleId                                  // TODO
    String pkgTitle = pkg.name
    String pkgCuratoryGroup = pkg.get("_embedded")?.get("curatoryGroups")?.getAt(0)?.get("name")
    String pkgId = pkg.id
    String pkgNominalPlatform = "${platformId};${platformName}".toString()
    String pkgNominalProvider = pkg.provider?.name
    String uuid = pkg.uuid
    String lastUpdated = null
    if (!StringUtils.isEmpty(pkg.lastUpdated)){
      lastUpdated = pkg.lastUpdated
    }
    enrichment = enrichmentService.setupEnrichment(enrichment, kbartReader, addOnly, pmOptions, platformName, platformUrl, params, pkgTitleId,
        pkgTitle, pkgCuratoryGroup, pkgId, pkgNominalPlatform, pkgNominalProvider, updateToken, uuid, lastUpdated)
    return enrichment
  }

}
