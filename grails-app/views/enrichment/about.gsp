<meta name="layout" content="enrichment">

<div class="row">
	<div class="col-xs-10 col-xs-offset-1">
		<g:render template="../logo" />
	</div>
	
	<div class="col-xs-10 col-xs-offset-1">
		<br />
		<p>Wer oder was ist <em>Ygor</em>?</p>
		<p>
			<em>Ygor</em> ist ein kleines Werkzeug zur Anreicherung existierender Daten.
			Im KBART-Format <em>(Knowledge Base and Related Tools)</em> vorliegende Dateien können durch automatisierte Nutzung externer Webservices
			um verschiedene Informationen erweitert werden.
			Dieses Verfahren benötigt geeignete Identifikatoren, z.B. ISSN-Angaben oder ID-Nummern der Zeitschriftendatenbank ZDB. 
		</p>
		
		<p>
			Die angereicherten Daten können anschließend über eine Schnittstelle an eine vorgegebene <em><strong>Knowledgebase</strong></em> übermittelt werden.
		</p>
		<p>
			Richten Sie sich nach den Anweisungen in den grauen Informationsfeldern.
		</p>
		<br />
		<p>
			<em>Version <g:meta name="app.version"/></em> <br/>
			<em>basierend auf Grails <g:meta name="app.grails.version"/></em> <br/>
			<em>Angeschlossene GOKb-Instanz ${grailsApplication.config.gokbApi.baseUri}</em>
		</p>

	</div>

</div>
