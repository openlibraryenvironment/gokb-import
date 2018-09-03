<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Ygor - Alpha</title>
	</head>
	<body>
		<div class="row">
			<div class="col-xs-10 col-xs-offset-1">
				<g:render template="logo" />
			</div>

			<div class="col-xs-10 col-xs-offset-1">
				<br />

				<h3>
					<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
					Bei dieser Installation handelt es sich um Software in der Entwicklungsphase.
					Verwendung auf eigene Verantwortung.
				</h3>
			</div>

			<div class="col-xs-10 col-xs-offset-1">
				<br />
				<p class="lead">Versionshistorie</p>

				<dl>
					<br /><dt>0.47 (Release)</dt><br />

					<dd>- Füge Button "Eingabe korrigieren" hinzu und behalte für diesen Schritt die vorherigen Einstellungen</dd>

					<br /><dt>0.46</dt><br />

					<dd>- Erlaube verschiedene Schreibweisen in der KBART-Spalte 'zdb_id'</dd>
					<dd>- Erlaube 'coverage_notes' statt 'notes' im KBART-File (Rückwärts-Kompatibilität zu KBART Phase I)</dd>
					<dd>- Zeige Basisadresse der angeschlossenen GOKb-Instanz im Bereich 'Über'</dd>

					<br /><dt>0.45</dt><br />

					<dd>- Optimiere nominal platform Behandlung</dd>
					<dd>- Passe DNB-Request an Restrukturierung der DNB-API an</dd>

					<br /><dt>0.44</dt><br />

					<dd>- Ersetze Setzen eines exklusiven primären Identifiers durch fixe Priorisierung: 1. ZDB-ID, 2. eISSN, 3. pISSN</dd>

					<br /><dt>0.43</dt><br />

					<dd>- Korrektur: Entferne "@" aus Titelfeldern bei allen verwendeten Konnektoren</dd>

					<br /><dt>0.42</dt><br />

					<dd>- Korrektur: Verarbeitung von Coverage num_-Feldern</dd>

					<br /><dt>0.41</dt><br />

					<dd>- Hole ZDB-Daten von services.dnb.de</dd>

					<br /><dt>0.40</dt><br />

					<dd>- Hole Plattform und Provider aus Elasticsearch Index</dd>

					<br /><dt>0.39</dt><br />

					<dd>- Korrektur: Verarbeitung von access_date-Feldern</dd>

					<br /><dt>0.38</dt><br />

					<dd>- Entferne "@" aus Pica-Titelfeldern</dd>
					<dd>- Korrigiere History Events mit leerem Titel</dd>

					<br /><dt>0.37</dt><br />

					<dd>- Erhöhe Session-Dauer (auf 16 Stunden)</dd>

					<br /><dt>0.36</dt><br />

					<dd>- Korrektur: formatiere Coverage-Felder als String</dd>

					<br /><dt>0.35</dt><br />

					<dd>- Mache access_start_date und access_end_date optional</dd>
					<dd>- Korrektur: verstecke GOKb-Passwort bei der Eingabe</dd>
					<dd>- Korrektur: repariere Senden von Paketen an die GOKb</dd>

					<br /><dt>0.34</dt><br />

					<dd>- Verarbeite access_start_date und access_end_date </dd>

					<br /><dt>0.33</dt><br />

					<dd>- Verwende GOKb-Elasticsearch-Index zur Befüllung </dd>

					<br /><dt>0.32</dt><br />

					<dd>- Korrektur: repariere Credentials "Senden" Button</dd>

					<br /><dt>0.31</dt><br />

					<dd>- Ignoriere BOM in Windows-generierten Eingabedateien</dd>

					<br /><dt>0.30</dt><br />

					<dd>- Eingabe von Credentials beim Senden von prozessierten Daten zur GOKb.</dd>
					<dd>- Zeige Plattform-URL in Plattform-Auswahlliste.</dd>
					<dd>- Setze Plattformnamen (statt -URL) im Exportfeld "nominalPlatform".</dd>

					<br /><dt>0.29</dt><br />

					<dd>- Nutzerfreundlichere Fehlermeldung bei irregulären CSV-Headern.</dd>

					<br /><dt>0.28</dt><br />

					<dd>- Korrektur: TIPP-Url muss nicht Plattform-URL matchen</dd>

					<br /><dt>0.27</dt><br />

					<dd>- Korrektur: Verarbeitung von SRU-Anfragen ohne konkreten Treffer</dd>
					<dd>- Korrektur: JSON-Struktur für History Events</dd>
					<dd>- Verbesserte Statistik</dd>

					<br /><dt>0.26</dt><br />

					<dd>- Auswahl für <em>nominalProvider</em> hinzugefügt</dd>

					<br /><dt>0.25</dt><br />

					<dd>- Korrektur: SRU-Anfragen liefern multiple Treffer</dd>
					<dd>- KBART-Mapping: Normierung der Platform-URL</dd>
					<dd>- KBART-Mapping: <em>@</em>-Zeichen in Titeln entfernen</dd>

					<br /><dt>0.24</dt><br />

                    <dd>- KBART-Mapping: Fehlerbehebung</dd>
                    <dd>- KBART-Mapping: Feld <em>cover_depth</em> hinzugefügt</dd>
                    <dd>- KBART-Mapping: Feld <em>notes</em> umbenannt</dd>
					<dd>- Quellcodeüberarbeitung</dd>

					<br /><dt>0.23</dt><br />

					<dd>- Kleinere Fehlerkorrekturen und Verbesserungen</dd>

					<br /><dt>0.22</dt><br />

					<dd>- Stacktrace bei Fehlern anzeigen</dd>

					<br /><dt>0.21</dt><br />

					<dd>- KBART-Mapping: Feld <em>coverage_notes</em> umbenannt</dd>
					<dd>- TIPP-Plattform wird <em>immer</em> vom PackageHeader übernommen</dd>
					<dd>- Subdomains in TIPP-Url werden beim Matching berücksichtigt</dd>
					<dd>- Defekte Tests repariert</dd>
					<dd>- Versionshistorie angelegt</dd>

					<br /><dt>0.20</dt><br />

				</dl>

			</div>
		</div>
	</body>
</html>
