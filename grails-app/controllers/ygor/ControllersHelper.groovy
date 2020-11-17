package ygor

trait ControllersHelper{

  String getDestinationUri(def grailsApplication, Enrichment.FileType fileType, boolean addOnly){
    def uri = fileType in [Enrichment.FileType.PACKAGE, Enrichment.FileType.PACKAGE_WITH_TITLEDATA] ?
      grailsApplication.config.gokbApi.xrPackageUri :
      (fileType.equals(Enrichment.FileType.TITLES) ?
        grailsApplication.config.gokbApi.xrTitleUri :
        null
      )
    if (fileType in [Enrichment.FileType.PACKAGE, Enrichment.FileType.PACKAGE_WITH_TITLEDATA]){
      uri = uri.concat("?async=true")
      // Titles are being sent 1 per request, there's no need for asynchronicity
      if (addOnly){
        uri = uri.concat("&addOnly=true")
      }
      // (addOnly has no effect for titles, it only needs to be set for packages)
    }
    return uri
  }
}
