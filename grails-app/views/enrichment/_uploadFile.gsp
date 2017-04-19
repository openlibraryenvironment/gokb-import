<!-- _uploadFile.gsp -->
<%@ page import="ygor.Enrichment" %>

<g:if test="${enrichments.size() == 0}">

	<g:uploadForm action="uploadFile">
	
	<div class="row">	
		<div class="col-xs-12">
			Datei:
			<br /><br />

			<p>
				<span class="glyphicon glyphicon-file"></span>
				<span id="uploadFileLabel">Keine Datei ausgewählt</span>
			</p>
			<label class="btn btn-default btn-file">
				<input type="file" name="uploadFile" style="display: none;"/>Datei auswählen
			</label>
			<input type="submit" value="Datei hochladen" class="btn btn-default" />
			
			<script>
				jQuery(document).on('change', ':file', function() {
			        var input = $(this),
			            numFiles = input.get(0).files ? input.get(0).files.length : 1,
			            label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
			        	jQuery('#uploadFileLabel').text(label);
			    });
			</script>
		</div>
	</div><!-- .row -->
		
	<div class="row">
		<div class="col-xs-12">
			<br />
			Trennzeichen:
			<br /><br />

			<g:radio name="formatDelimiter" checked="true" value="comma"/> Komma <code>(,)</code>
			&nbsp;
			<g:radio name="formatDelimiter" value="semicolon"/> Semikolon <code>(;)</code>
			&nbsp;
			<g:radio name="formatDelimiter" value="tab"/> Tabulator <code>(\t)</code>
		
			<!-- 
			<br /><br />
			Zeichenbegrenzer:
			<br /><br />

			<g:radio name="formatQuote" checked="true" value="doublequote"/> Anführungszeichen <code>(")</code>
			&nbsp;
			<g:radio name="formatQuote" value="singlequote"/> Hochkomma <code>(')</code>
			&nbsp;
			<g:radio name="formatQuote" value="nullquote"/> ohne Begrenzer
			
			<br /><br />
			Zeichenbegrenzungsmodus:
			<br /><br />

			<g:radio name="formatQuoteMode" value="all"/> Alle Felder begrenzen
			&nbsp;
			<g:radio name="formatQuoteMode" checked="true" value="nonnumeric"/> Alle nichtnumerischen Felder begrenzen
			&nbsp;
			<g:radio name="formatQuoteMode" value="none"/> Keine Felder begrenzen
			-->
		</div>
	</div><!-- .row -->
	
	</g:uploadForm>
	
</g:if>