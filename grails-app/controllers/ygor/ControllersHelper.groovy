package ygor

trait ControllersHelper{

  String getDestinationUri(def grailsApplication, Enrichment.FileType fileType, boolean addOnly){
    def uri = fileType.equals(Enrichment.FileType.PACKAGE) ?
      grailsApplication.config.gokbApi.xrPackageUri :
      (fileType.equals(Enrichment.FileType.TITLES) ?
        grailsApplication.config.gokbApi.xrTitleUri :
        null
      )
    if (fileType.equals(Enrichment.FileType.PACKAGE)){
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
