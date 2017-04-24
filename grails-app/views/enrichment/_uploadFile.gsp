<!-- _uploadFile.gsp -->
<%@ page import="ygor.Enrichment" %>

<g:if test="${enrichments.size() == 0}">

	<div class="row">	
		<div class="col-xs-12">
			
			<br /><br />
			
			<g:uploadForm action="uploadFile">
			
			<ul class="list-group content-list">
				<li class="list-group-item">
				
					<div class="input-group">
						<span class="input-group-addon">Datei:</span>
						<span class="form-control" id="uploadFileLabel">Keine Datei ausgewählt</span>
					</div>
					
		  			<br />
		  			
		  			<div class="input-group">
						<span class="input-group-addon">Trennzeichen:</span>
						<span class="form-control">
							<div class="radio">
								<label>
									<g:radio name="formatDelimiter" checked="true" value="comma" />
									Komma <code>(,)</code>
								</label>
							
								&nbsp;
							
								<label>
									<g:radio name="formatDelimiter" value="semicolon" />
									Semikolon <code>(;)</code>
								</label>
							
								&nbsp;
							
								<label>
									<g:radio name="formatDelimiter" value="tab" />
									Tabulator <code>(\t)</code>
								</label>
							</div>
						</span>
					</div>

				</li>
			</ul>
			
			<ul class="list-group content-list">
				<li class="list-group-item">
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
				</li>
			</ul>
			
			</g:uploadForm>
			
		</div>
	</div><!-- .row -->
		
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
	
	
</g:if>