# GOKb-Import

### Setup

    $ git clone https://github.com/openlibraryenvironment/gokb-import.git

For automatic updates, set up the following directory:

    $ mkdir -p /var/lib/ygor/automaticUpdates
    
### Config

Upload json result to Knowledge Base

	ygor.enableGokbUpload = true|false
	
	ygor.gokbApi.xrTitleUri   = 'http://localhost:8080/gokb/integration/crossReferenceTitle'
	ygor.gokbApi.xrPackageUri = 'http://localhost:8080/gokb/integration/crossReferencePackage'
	
### Running

In _development_ environment

	$ grails run-app
	
In _production_ environment

	$ grails prod run-app

### Testing

Current normalizer and validator support - warning: tests are out of date.

	$ grails test-app

Results are generated here

	<projectDir>/target/test-reports
	
