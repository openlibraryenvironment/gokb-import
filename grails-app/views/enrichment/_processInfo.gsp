<!-- _stepInfo.gsp -->
<%@ page import="ygor.Enrichment" %>

<div class="row">

	<g:if test="${documents.size() == 0}">

		<div class="col-xs-12">
			<div class="well">
				<strong>Schritt 1</strong> 
				<br /><br />
				<span class="glyphicon glyphicon-hand-right" aria-hidden="true"></span>
				&nbsp; Wählen Sie eine CSV-Datei zur Bearbeitung.
				<br />
				<span class="glyphicon glyphicon-hand-right" aria-hidden="true"></span>
				&nbsp; Laden Sie die Datei auf den Server.
			</div>
		</div>

	</g:if>

	<g:if test="${documents.size() == 1}">

		<g:each in="${documents}" var="doc">

			<g:if test="${doc.value.status != Enrichment.StateOfProcess.FINISHED}">
				<div class="col-xs-12">
					<div class="well">
						<strong>Schritt 2</strong> 
						<br /><br />
						<span class="glyphicon glyphicon-hand-right" aria-hidden="true"></span>
						&nbsp; Geben Sie den Index der Spalte an, welche die ISSN enthält.
						<br />
						<span class="glyphicon glyphicon-hand-right" aria-hidden="true"></span>
						&nbsp; Bestimmen Sie den Typ der angegebenen ISSN.
						<br />
						<span class="glyphicon glyphicon-hand-right" aria-hidden="true"></span>
						&nbsp; Wählen Sie die hinzuzufügenden Informationen aus.
						<br />
						<span class="glyphicon glyphicon-hand-right" aria-hidden="true"></span>
						&nbsp; Starten Sie die Bearbeitung.
					</div>
				</div>
			</g:if>
			
			<g:if test="${doc.value.status == Enrichment.StateOfProcess.FINISHED}">
				<div class="col-xs-12">
					<div class="well">
						<strong>Schritt 3</strong> 
						<br /><br />
						<span class="glyphicon glyphicon-hand-right" aria-hidden="true"></span>
						&nbsp; Speichern Sie das Ergebnis.
						<br />
						<span class="glyphicon glyphicon-hand-right" aria-hidden="true"></span>
						&nbsp; Die Datei wird automatisch nach einiger Zeit auf dem Server gelöscht.
					</div>
				</div>
			</g:if>
			
		</g:each>
		
	</g:if>	
		
</div><!-- .row -->	