<!-- _listDocuments.gsp -->

<%@ page import="ygor.Enrichment" %>

	<g:each in="${documents}" var="doc">

		<g:form controller="enrichment" action="process">
			<g:hiddenField name="originHash" value="${doc.key}" />
			
			<div class="row">
				<div class="col-xs-12">
					<div id="progress-${doc.key}"class="progress">
						<div class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width:0%;">0%</div>
					</div>
				</div>
			</div><!-- .row -->	
			
			<div class="row">
				<div class="col-xs-12">
					<span title="${doc.value.originHash}"><strong>${doc.value.originName}</strong></span>
				
					<span>
						<g:if test="${doc.value.status == Enrichment.StateOfProcess.UNTOUCHED}">
							&rarr; Nicht bearbeitet
						</g:if>
						<g:if test="${doc.value.status == Enrichment.StateOfProcess.WORKING}">
							&rarr; In Bearbeitung ..
						</g:if>
						<g:if test="${doc.value.status == Enrichment.StateOfProcess.ERROR}">
							&rarr; Fehler ]
						</g:if>
						<g:if test="${doc.value.status == Enrichment.StateOfProcess.FINISHED}">
							&rarr; Bearbeitung abgeschlossen
						</g:if>
					</span>
				</div>
			</div><!-- .row -->	
	
			<br />
					
			<div class="row">
				<div class="col-xs-4">
					<g:select name="processIndex" from="${1..20}" value="0" 
						noSelection="['':'Spaltenindex der .. ']"  class="form-control"/>
				</div>
				<div class="col-xs-8">
					<g:radio name="processIndexType" checked="true" value="pissn"/> pISSN
					<g:radio name="processIndexType" value="eissn"/> eISSN
				</div>
			</div><!-- .row -->
			
			<br />
			
			<div class="row">
				<div class="col-xs-12">
					Folgende Information soll hinzugefügt werden ..
					<br /><br />
					<g:checkBox name="processOption" checked="true" value="zdbid"/> ZDB-Id
					<g:checkBox name="processOption" checked="true" value="ezbid"/> EZB-Id
					
					<!--<g:checkBox name="processOption" checked="false" value="gokbid"/> GOKb-Id-->
			
				</div>
			</div><!-- .row -->
			
			<br />
			
			<div class="row">
			
				<div class="col-xs-6">
					<g:if test="${doc.value.status == Enrichment.StateOfProcess.UNTOUCHED}">
		    			<g:actionSubmit action="deleteFile" value="Datei löschen" class="btn btn-danger"/>
						<g:actionSubmit action="processFile" value="Bearbeitung starten" class="btn btn-default"/>
					</g:if>
					<g:if test="${doc.value.status == Enrichment.StateOfProcess.ERROR}">
						<g:actionSubmit action="deleteFile" value="Datei löschen" class="btn btn-danger"/>
					</g:if>
					<g:if test="${doc.value.status == Enrichment.StateOfProcess.FINISHED}">
						<g:actionSubmit action="deleteFile" value="Datei löschen" class="btn btn-danger"/>
		    			<g:actionSubmit action="downloadFile" value="Ergebnis speichern" class="btn btn-success"/>
		    		</g:if>
		    		
				</div>
				
			</div><!-- .row -->	
		
			<br />

			<g:if test="${doc.value.status == Enrichment.StateOfProcess.WORKING}">
				<script>
					var ygorDocumentProgress${doc.key} = function(){
						jQuery.ajax({
							type:'POST', 
							url:'/ygor/enrichment/ajaxGetProgress',
							data:'originHash=${doc.key}',
							success:function(data, textStatus){
								jQuery('#progress-${doc.key} > .progress-bar').attr('aria-valuenow', data);
								jQuery('#progress-${doc.key} > .progress-bar').attr('style', 'width:' + data + '%');
								jQuery('#progress-${doc.key} > .progress-bar').text(data + '%');

								if(parseFloat(data) == 100) {
									window.location = '/ygor/enrichment/process';
								}
							},
							error:function(XMLHttpRequest, textStatus, errorThrown){
								clearInterval(ygorDocumentProgress${doc.key});
							}
						});
					}
					var ygorInterval${doc.key} = setInterval(ygorDocumentProgress${doc.key}, 1000);
				</script>
			</g:if>
			
	  
		</g:form>
	</g:each>
