<!-- _uploadFile.gsp -->
<%@ page import="ygor.Enrichment" %>

<g:if test="${enrichments.size() == 0}">

	<div class="row">	
		<div class="col-xs-12">
			
			<br /><br />

			<g:set var="filename" value="Keine Datei ausgewaehlt" scope="page" />
			<g:if test="${session.lastUpdate?.file?.originalFilename != null}">
				<g:set var="filename" value="${session.lastUpdate?.file?.originalFilename}" scope="page" />
			</g:if>

			<g:uploadForm action="uploadFile">
			
			<ul class="list-group content-list">
				<li class="list-group-item">
				
					<div class="input-group">
						<span class="input-group-addon">Datei</span>
						<input class="form-control" type="text" id="uploadFileLabel" name="uploadFileLabel" readonly value="${filename}" size="46"/>
					</div>
					
		  			<br />
		  			
		  			<div class="input-group">
						<span class="input-group-addon">Trennzeichen</span>
						<span class="form-control">
							<div class="radio">
								<label>
									<g:if test="${session.lastUpdate?.foDelimiter == null || session.lastUpdate?.foDelimiter == "comma"}">
										<g:radio name="formatDelimiter" checked="true" value="comma" />
									</g:if>
									<g:else>
										<g:radio name="formatDelimiter" value="comma" />
									</g:else>
									Komma <code>(,)</code>
								</label>
								&nbsp;
								<label>
									<g:if test="${session.lastUpdate?.foDelimiter == "semicolon"}">
										<g:radio name="formatDelimiter" checked="true" value="semicolon" />
									</g:if>
									<g:else>
										<g:radio name="formatDelimiter" value="semicolon" />
									</g:else>
									Semikolon <code>(;)</code>
								</label>
								&nbsp;
								<label>
									<g:if test="${session.lastUpdate?.foDelimiter == "tab"}">
										<g:radio name="formatDelimiter" checked="true" value="tab" />
									</g:if>
									<g:else>
										<g:radio name="formatDelimiter" value="tab" />
									</g:else>
									Tabulator <code>(\t)</code>
								</label>
							</div>
						</span>
					</div>
					
					<!-- <br />
					<div class="input-group">
						<span class="input-group-addon">Zeichenbegrenzer:</span>
						<span class="form-control">
							<div class="radio">
								<label>
									<g:radio name="formatQuoteMode" checked="true" value="none" />
									Ohne
								</label>
								&nbsp;
								<label>
									<g:radio name="formatQuoteMode" value="nonnumeric" />
									Nur alphanumerische Felder begrenzen
								</label>
								&nbsp;
								<label>
									<g:radio name="formatQuoteMode" value="all" />
									Alle Felder begrenzen
								</label>
							</div>
						</span>
					</div> -->
				</li>
			</ul>
			
			<ul class="list-group content-list">
				<li class="list-group-item">
					<label class="btn btn-link btn-file">
						<input type="file" name="uploadFile" style="display: none;"/>Datei auswählen
					</label>
					<input type="submit" value="Datei hochladen" class="btn btn-success" />
					<script>
						jQuery(document).on('change', ':file', function() {
					        var input = $(this),
					            numFiles = input.get(0).files ? input.get(0).files.length : 1,
					            label = input.get(0).value.replace(/\\/g, '/').replace(/.*\//, '');
					        	jQuery('#uploadFileLabel')[0].setAttribute("value", label)
					    });
					</script>
				</li>
			</ul>
			
			</g:uploadForm>
		</div>
	</div><!-- .row -->
</g:if>