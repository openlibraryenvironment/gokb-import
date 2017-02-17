<!-- _listDocuments.gsp -->

<%@ page 
	import="ygor.Enrichment" 
	import="ygor.PlatformService"
	import="de.hbznrw.ygor.iet.export.structure.TitleStruct"
	import="de.hbznrw.ygor.iet.export.structure.PackageStruct"
	import="de.hbznrw.ygor.iet.bridge.*"
%>

	<g:each in="${documents}" var="doc">

		<g:form controller="enrichment" action="process">
			<g:hiddenField name="originHash" value="${doc.key}" />
			
			<div class="row">
				<div class="col-xs-10 col-xs-offset-1">
					
					<span class="glyphicon glyphicon-file" aria-hidden="true"></span>
					<span title="${doc.value.originHash}">${doc.value.originName}</span>
				
					<span><em>
						<g:if test="${doc.value.status == Enrichment.ProcessingState.PREPARE}">
							&rarr; Vorbereitung
						</g:if>
						<g:if test="${doc.value.status == Enrichment.ProcessingState.UNTOUCHED}">
							&rarr; Nicht bearbeitet
						</g:if>
						<g:if test="${doc.value.status == Enrichment.ProcessingState.WORKING}">
							&rarr; In Bearbeitung ..
						</g:if>
						<g:if test="${doc.value.status == Enrichment.ProcessingState.ERROR}">
							&rarr; Fehler / Die Datei konnte nicht verarbeitet werden
						</g:if>
						<g:if test="${doc.value.status == Enrichment.ProcessingState.FINISHED}">
							&rarr; Bearbeitung abgeschlossen
						</g:if>
					</span></em>
				</div>
			</div><!-- .row -->	
	
			<br />
			
			<div class="row">
				<div class="col-xs-10 col-xs-offset-1">
					<div id="progress-${doc.key}"class="progress">
						<g:if test="${doc.value.status == Enrichment.ProcessingState.FINISHED}">
							<div class="progress-bar" role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width:100%;">100%</div>
						</g:if>
						<g:else>
							<div class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width:0%;">0%</div>
						</g:else>
					</div>
				</div>
			</div><!-- .row -->	
			
			<g:if test="${doc.value.status == Enrichment.ProcessingState.PREPARE}">
				<div class="row">
				
					<div class="col-xs-5 col-xs-offset-1">
						Titel:
						<br /> 
						<g:textField name="pkgTitle" size="48" value="Munchhausen Verlag : hbz : 1999" />
						<br />
						<br /> 
					</div>
					
					<div class="col-xs-4 col-xs-offset-1">
						ZDB-Paketsigel:
						<br /> 
						<g:textField name="pkgVariantName" size="24" value="ZDB-0815" />
						<br />
						<br /> 
					</div>
				</div><!-- .row -->

				<div class="row">
					<div class="col-xs-5 col-xs-offset-1">
						Plattform:
						<br /> 
						<g:select name="pkgNominal" from="${platformService.getMap().entrySet()}" optionKey="key" optionValue="key" noSelection="['':'']"  class="form-control"/>
						<br /> 
					</div>
				</div><!-- .row -->
				
				<div class="row">
					<div class="col-xs-10 col-xs-offset-1">
						<g:checkBox name="ignorePkgData" value="true" checked="false" /> &nbsp; Diese Angaben ignorieren
						<br />
					</div>
				</div><!-- .row -->
				
				<br />
			</g:if>
			
			<g:if test="${doc.value.status == Enrichment.ProcessingState.UNTOUCHED}">			
				<div class="row">
					<div class="col-xs-4 col-xs-offset-1">
						<g:select name="processIndex" from="${1..15}" value="0" 
							noSelection="['':'Spaltenindex der .. ']"  class="form-control"/>
					</div>
					<div class="col-xs-6">
						<g:radio name="processIndexType" checked="true" value="${TitleStruct.PISSN}"/> pISSN
						&nbsp;
						<g:radio name="processIndexType" value="${TitleStruct.EISSN}"/> eISSN
					</div>
				</div><!-- .row -->
				
				<br />

				<div class="row">
					<div class="col-xs-10 col-xs-offset-1">
						Weitere Information sollen über die folgenden Schnittstellen hinzugefügt werden ..
						<br /><br />
						&nbsp;
						<g:checkBox name="processOption" required="true" checked="true" value="${GbvBridge.IDENTIFIER}"/> GBV
						&nbsp;
						<g:checkBox name="processOption" checked="true" value="${EzbBridge.IDENTIFIER}"/> EZB
						&nbsp;
						<g:checkBox name="processOption" checked="false" disabled="true" value="${ZdbBridge.IDENTIFIER}"/> ZDB
					</div>
				</div><!-- .row -->
				
				<br />
			</g:if>
			
			<div class="row">
			
				<div class="col-xs-10 col-xs-offset-1">
					<g:if test="${doc.value.status == Enrichment.ProcessingState.UNTOUCHED}">
		    			<g:actionSubmit action="deleteFile" value="Datei löschen" class="btn btn-danger"/>
						<g:actionSubmit action="processFile" value="Bearbeitung starten" class="btn btn-default"/>
					</g:if>
					<g:if test="${doc.value.status == Enrichment.ProcessingState.PREPARE}">
						<g:actionSubmit action="prepareFile" value="Weiter" class="btn btn-default"/>
		    		</g:if>
					<g:if test="${doc.value.status == Enrichment.ProcessingState.WORKING}">
						<g:actionSubmit action="stopProcessingFile" value="Bearbeitung abbrechen" class="btn btn-danger"/>
					</g:if>
					<g:if test="${doc.value.status == Enrichment.ProcessingState.ERROR}">
						<g:actionSubmit action="deleteFile" value="Datei löschen" class="btn btn-danger"/>
					</g:if>
					<g:if test="${doc.value.status == Enrichment.ProcessingState.FINISHED}">
						<g:actionSubmit action="deleteFile" value="Datei löschen" class="btn btn-danger"/>
						
						<g:link action="showStats" params="[originHash:doc.key]" target="_blank" class="btn btn-warning">Statistik anzeigen</g:link>
						
						<g:actionSubmit action="downloadPackageFile" value="Package/Tipps speichern" class="btn btn-success"/>
		    			<g:actionSubmit action="downloadTitlesFile" value="Titles speichern" class="btn btn-success"/>
		    			
		    			<g:if test="${grailsApplication.config.ygor.enableGokbUpload}">
							<g:actionSubmit action="exportFile" value="JSON zur GOKb senden" class="btn btn-success"
		    					data-toggle="tooltip" data-placement="top" title="${grailsApplication.config.gokbApi.xrTitleUri}" />
	    				</g:if>
	    				<g:else>
							<g:actionSubmit action="exportFile" value="JSON zur GOKb senden" class="btn btn-success disabled"
		    					data-toggle="tooltip" data-placement="top" title="Deaktiviert: ${grailsApplication.config.gokbApi.xrTitleUri}" disabled="disabled"/>
	    				</g:else>
		    			
		    			<br /><br />
		    			
		    			<g:actionSubmit action="downloadDebugFile" value="Debug-Datei speichern" class="btn"/>
		    			<g:actionSubmit action="downloadRawFile" value="Datenstruktur speichern" class="btn"/>

		    		</g:if>
		    		
				</div>
				
			</div><!-- .row -->	
		
			<br />
			
			<script>
				$(function(){
				  $('[data-toggle="tooltip"]').tooltip()
				})
			</script>

			<g:if test="${doc.value.status == Enrichment.ProcessingState.WORKING}">
				<script>
					$(function(){
						var ygorDocumentStatus${doc.key} = function(){
							jQuery.ajax({
								type:       'GET',
								url:         '/ygor/enrichment/ajaxGetStatus',
								data:        'originHash=${doc.key}',
								success:function(data, textStatus){
									
									data = jQuery.parseJSON(data)
									console.log(data)
									var status = data.status;
									var progress = data.progress;
									
									jQuery('#progress-${doc.key} > .progress-bar').attr('aria-valuenow', progress);
									jQuery('#progress-${doc.key} > .progress-bar').attr('style', 'width:' + progress + '%');
									jQuery('#progress-${doc.key} > .progress-bar').text(progress + '%');
	
									if(status == 'FINISHED') {
										window.location = '/ygor/enrichment/process';
									}
									if(status == 'ERROR') {
										window.location = '/ygor/enrichment/process';
									}
									
								},
								error:function(XMLHttpRequest, textStatus, errorThrown){
									clearInterval(ygorDocumentStatus${doc.key});
								}
							});
						}
	
						var ygorInterval${doc.key} = setInterval(ygorDocumentStatus${doc.key}, 1500);
					})
				</script>
			</g:if>
		</g:form>
	</g:each>
