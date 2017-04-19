<!-- _uploadFile.gsp -->
<%@ page import="ygor.Enrichment" %>

<g:if test="${enrichments.size() == 0}">

	<g:uploadForm action="uploadFile">
	<div class="row">
	
		<div class="col-xs-6">
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
		
		<div class="col-xs-6">
			Trennzeichen:
			<br /><br />

			<g:radio name="formatDelimiter" checked="true" value="comma"/> Komma <code>(,)</code>
			&nbsp;
			<g:radio name="formatDelimiter" value="semicolon"/> Semikolon <code>(;)</code>
			&nbsp;
			<g:radio name="formatDelimiter" value="tab"/> Tabulator <code>(\t)</code>
			
			<br /><br />
			Zeichenbegrenzer:
			<br /><br />

			<g:radio name="formatQuotes" checked="true" value="doublequote"/> Anführungszeichen <code>(")</code>
			&nbsp;
			<g:radio name="formatQuotes" value="singlequote"/> Hochkomma <code>(')</code>
			
		</div>
		
	</div><!-- .row -->
	</g:uploadForm>
	
</g:if>