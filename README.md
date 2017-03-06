# Ygor

A spare hand when needed ..

### Setup

    $git clone https://github.com/hbz/laser-ygor.git
    $grails integrate-with --eclipse

Eclipse/GGTS &rarr; Import &rarr; Grails Project

### Config

Upload json result to knowledgebase

	ygor.enableGokbUpload = true|false
	
	ygor.gokbApi.xrTitleUri   = 'http://localhost:8080/gokb/integration/crossReferenceTitle'
	ygor.gokbApi.xrPackageUri = 'http://localhost:8080/gokb/integration/crossReferencePackage'
	ygor.gokbApi.user = ''
	ygor.gokbApi.pwd  = ''

Use knowledge base to obtain actual map with platform/provider. Fallback to hardcoded map 
	
	ygor.gokbDB.dbUri = 'jdbc:postgresql://localhost:5432/gokb'
	ygor.gokbDB.user  = ''
	ygor.gokbDB.pwd   = ''
	
### Running

In _development_ environment

	$grails run-app
	
In _production_ environment

	$grails prod run-app

### Testing

Current normalizer and validator support

	$grails test-app

Results are generated here

	<projectDir>/target/test-reports
	
