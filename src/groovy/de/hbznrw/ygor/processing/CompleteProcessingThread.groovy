package de.hbznrw.ygor.processing

import de.hbznrw.ygor.readers.KbartFromUrlReader
import de.hbznrw.ygor.readers.KbartReader
import de.hbznrw.ygor.tools.UrlToolkit
import groovy.util.logging.Log4j
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
  String addOnly
  UploadJobFrame uploadJobFrame
  File localFile

  /**
   * used by EnrichmentController.processGokbPackage()
   * @param kbartReader
   * @param pkg
   * @param src
   * @param token
   */
  CompleteProcessingThread(KbartReader kbartReader, Map<String, Object> pkg, Map<String, Object> src, String token,
      UploadJobFrame uploadJobFrame, File file, def addOnly){
    enrichmentService = new EnrichmentService()
    this.kbartReader = kbartReader
    this.pkg = pkg
    this.src = src
    this.token = token
    this.uploadJobFrame = uploadJobFrame
    this.localFile = file
    this.addOnly
  }

  @Override
  void run() throws Exception {
    enrichmentService.addUploadJob(uploadJobFrame)
    String sessionFolder = grails.util.Holders.grailsApplication.config.ygor.uploadLocation.toString()
        .concat(File.separator).concat(UUID.randomUUID().toString())

    if (!localFile) {
      log.info("Checking for usable URLs..")
      Locale locale = new Locale("en")                                    // TODO get from request or package
      boolean proceeding = false
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
      log.info("Got ${updateUrls}")

      updateUrls = UrlToolkit.removeNonExistentURLs(updateUrls)
      Iterator urlsIterator = updateUrls.listIterator(updateUrls.size())

      if (updateUrls.size() > 0) {
        while(urlsIterator.hasPrevious()){
          URL url = urlsIterator.previous()
          try {
            kbartReader = enrichmentService.kbartReader = new KbartFromUrlReader(url, new File(sessionFolder), locale)
          }
          catch (Exception e) {
            e.printStackTrace()
            log.error("ERROR while trying to read file!")
            continue
          }

          Enrichment enrichment
          try {
            enrichment = prepareEnrichment(token, sessionFolder, pkg, src, "false")
            log.info("Prepared enrichment ${enrichment.originName}.")
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
            proceeding = true
            break
          }
        }

        if (!proceeding) {
          log.info("All valid URLs produced errors.")
          uploadJobFrame.status = UploadThreadGokb.Status.ERROR
        }
      }
      else {
        log.info("No usable URLs, skipping job!")
        uploadJobFrame.status = UploadThreadGokb.Status.FINISHED_UNDEFINED
      }
    }
    else {
      kbartReader = enrichmentService.kbartReader = new KbartReader(localFile)
      kbartReader.fileName = localFile.toString()
      Enrichment enrichment

      try {
        enrichment = prepareEnrichment(token, sessionFolder, pkg, src, addOnly)
        log.info("Prepared enrichment ${enrichment.originName}.")

        enrichment.originPathName = kbartReader.fileName
        UploadJob uploadJob = enrichmentService.processComplete(uploadJobFrame, enrichment, null, null, false, true)
        enrichmentService.addUploadJob(uploadJob)
      }
      catch (Exception e) {
        e.printStackTrace()
        log.error("Could not build enrichment for package ${pkg.id} with uuid ${pkg.uuid}")
        uploadJobFrame
      }
    }
  }

  private Enrichment prepareEnrichment(String updateToken, String sessionFolder, def pkg, def src, def addOnly)
      throws Exception{
    Enrichment enrichment = Enrichment.fromFilename(sessionFolder, pkg.name)
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
    String lastUpdated = EnrichmentService.getLastRun(pkg)
    if (!addOnly || lastUpdated != null){
      addOnly = "true"
    }
    enrichment = enrichmentService.setupEnrichment(enrichment, kbartReader, addOnly, pmOptions, platformName, platformUrl, params, pkgTitleId,
        pkgTitle, pkgCuratoryGroup, pkgId, pkgNominalPlatform, pkgNominalProvider, updateToken, uuid, lastUpdated)
    return enrichment
  }

}
