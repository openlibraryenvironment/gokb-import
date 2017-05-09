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
			<em>Version <g:meta name="app.version"/></em> <br />
			<em>basierend auf Grails <g:meta name="app.grails.version"/></em>
		</p>
		
	</div>
	
	<div class="col-xs-10 col-xs-offset-1">
		<br />		
		<h3>Versionshistorie</h3>
		
		<dl>
			<br />
			<dt>0.21</dt>
			<br />
			
			<dd>- KBART-Mapping über korrekte Feld <em>coverage_notes</em></dd>
			<dd>- TIPP-Plattform wird <em>immer</em> vom PackageHeader übernommen</dd>
			<dd>- Subdomains in TIPP-Url werden beim Matching berücksichtigt</dd> 
			<dd>- Defekte Tests repariert</dd>
			<dd>- Versionshistorie</dd>
			
			<br />
			<dt>0.20</dt>
			<br />

		</dl>
		
	</div>
</div>
