package de.hbznrw.ygor.processing

import de.hbznrw.ygor.readers.KbartFromUrlReader
import de.hbznrw.ygor.readers.KbartReader
import de.hbznrw.ygor.tools.UrlToolkit
import groovy.util.logging.Log4j
import ygor.AutoUpdateService
import ygor.Enrichment
import ygor.UploadJob
import ygor.field.MappingsContainer

@Log4j
class CompleteProcessingThread extends Thread {

  KbartReader kbartReader
  Map<String, Object> pkg
  Map<String, Object> src
  String token

  CompleteProcessingThread(KbartReader kbartReader, Map<String, Object> pkg, Map<String, Object> src, String token){
    this.kbartReader = kbartReader
    this.pkg = pkg
    this.src = src
    this.token = token
  }

  @Override
  void run() {
    Map<String, String> result = [:]
    try {
      String sessionFolder = grails.util.Holders.grailsApplication.config.ygor.uploadLocation.toString()
          .concat(File.separator).concat(UUID.randomUUID().toString())
      Locale locale = new Locale("en")                                    // TODO get from request or package
      List<URL> updateUrls
      if (Integer.valueOf(pkg._tippCount) == 0){
        // this is obviously a new package --> update with older timestamps
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
          String message = "Could not build enrichment for package ".concat(pkg.id).concat(" with uuid ").concat(pkg.uuid)
          log.error(message)
          result.status = UploadThreadGokb.Status.ERROR.toString()
          result.message = message
          continue
        }
        enrichment.originPathName = kbartReader.fileName
        UploadJob uploadJob = enrichmentService.processComplete(enrichment, null, null, false, false)
        if (uploadJob == null){
          String message = "Could not upload processed package ".concat(pkg.id).concat(" with uuid ").concat(pkg.uuid)
          log.error(message)
          result.status = UploadThreadGokb.Status.ERROR.toString()
          result.message = message
          continue
        }
        else{
          result.uploadStatus = uploadJob.getStatus().toString()
          result.jobId = uploadJob.uuid
          break
        }
      }
    }
    catch (Exception e) {
      result.status = UploadThreadGokb.Status.ERROR.toString()
      response.status = 500
      result.message = "Unable to process KBART file at the specified source url."
    }
  }


  private Enrichment prepareEnrichment(String updateToken, String sessionFolder, def pkg, def src)
      throws Exception{
    Enrichment enrichment = Enrichment.fromFilename(sessionFolder, pkg.name)
    String addOnly = "false"
    List<String> pmOptions = Arrays.asList(MappingsContainer.KBART)
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
    String pkgNominalPlatform = platformId.concat(";").concat(platformName)
    String pkgNominalProvider = pkg.provider?.name
    String uuid = pkg.uuid
    enrichment = setupEnrichment(enrichment, kbartReader, addOnly, pmOptions, platformName, platformUrl, params, pkgTitleId,
        pkgTitle, pkgCuratoryGroup, pkgId, pkgNominalPlatform, pkgNominalProvider, updateToken, uuid)
    return enrichment
  }

}
