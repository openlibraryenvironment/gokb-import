<!-- _uploadFile.gsp -->
<%@ page import="ygor.Document" %>

<g:if test="${documents.size() == 0}">

	<div class="row">
	
		<div class="col-md-12">
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
		
		<div class="col-md-12">
			<g:uploadForm action="uploadFile">
				<p>
					<span class="glyphicon glyphicon-file"></span>
					<span id="uploadFileLabel">Keine Datei ausgewählt</span>
				</p>
				<label class="btn btn-default btn-file">
					<input type="file" name="uploadFile" style="display: none;"/>Datei auswählen
				</label>
				<input type="submit" value="Datei hochladen" class="btn btn-default" />
			</g:uploadForm>
			
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

</g:if>