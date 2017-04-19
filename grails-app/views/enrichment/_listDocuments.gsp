<!-- _listDocuments.gsp -->

<%@ page 
	import="ygor.Enrichment" 
	import="ygor.PlatformService"
	import="de.hbznrw.ygor.iet.export.structure.TitleStruct"
	import="de.hbznrw.ygor.iet.export.structure.PackageStruct"
	import="de.hbznrw.ygor.iet.bridge.*"
%>

	<g:each in="${enrichments}" var="e">
		<g:form controller="enrichment" action="process">
			<g:hiddenField name="originHash" value="${e.key}" />
			
			<div class="row">
				<div class="col-xs-12">
					
					<span class="glyphicon glyphicon-file" aria-hidden="true"></span>
					<span title="${e.value.originHash}">${e.value.originName}</span>
				
					<span><em>
						<g:if test="${e.value.status == Enrichment.ProcessingState.PREPARE}">
							&rarr; Vorbereitung
						</g:if>
						<g:if test="${e.value.status == Enrichment.ProcessingState.UNTOUCHED}">
							&rarr; Nicht bearbeitet
						</g:if>
						<g:if test="${e.value.status == Enrichment.ProcessingState.WORKING}">
							&rarr; In Bearbeitung ..
						</g:if>
						<g:if test="${e.value.status == Enrichment.ProcessingState.ERROR}">
							&rarr; Fehler / Die Datei konnte nicht verarbeitet werden
						</g:if>
						<g:if test="${e.value.status == Enrichment.ProcessingState.FINISHED}">
							&rarr; Bearbeitung abgeschlossen
						</g:if>
					</span></em>
				</div>
			</div><!-- .row -->	
	
			<br />
			
			<div class="row">
				<div class="col-xs-12">
					<div id="progress-${e.key}"class="progress">
						<g:if test="${e.value.status == Enrichment.ProcessingState.FINISHED}">
							<div class="progress-bar" role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width:100%;">100%</div>
						</g:if>
						<g:else>
							<div class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width:0%;">0%</div>
						</g:else>
					</div>
				</div>
			</div><!-- .row -->	
		
			<g:if test="${e.value.status == Enrichment.ProcessingState.PREPARE}">
				<div class="row">
				
					<div class="col-xs-6">
						Titel:
						<br /><br />  
						<g:textField name="pkgTitle" size="48" value="Munchhausen Verlag : hbz : 1999" />
						<br />
						<br /> 
					</div>
					
					<div class="col-xs-6">
						ZDB-Paketsigel:
						<br /><br /> 
						<g:textField name="pkgVariantName" size="24" value="ZDB-0815" />
						<br />
						<br /> 
					</div>
				</div><!-- .row -->

				<div class="row">
				 
					<div class="col-xs-6">
						Plattform:
						<br /><br /> 
						<g:select name="pkgNominal" from="${platformService.getMap().entrySet()}" optionKey="key" optionValue="key" noSelection="['':'']"  class="form-control"/>
						<br /> 
					</div>
					
					<div class="col-xs-6">
						GOKb Curatory Group:
						<br /><br /> 
						<g:textField name="pkgCuratoryGroup1" size="24" value="LAS:eR" />
						&nbsp;
						<g:textField name="pkgCuratoryGroup2" size="24" value="hbz" />
						<br />
						<br /> 
					</div>
					
				</div><!-- .row -->
				
				<br />
			</g:if>
			
			<g:if test="${e.value.status == Enrichment.ProcessingState.UNTOUCHED}">

				<div class="row">
					<div class="col-xs-12">
						Über die folgenden Schnittstellen sollen die Information hinzugefügt werden:
						<br /><br />	
						
						<g:checkBox name="processOption" required="true" checked="true" value="${KbartBridge.IDENTIFIER}"/> KBART <code>Datei</code>
						&nbsp;
						<g:checkBox name="processOption" required="true" checked="true" value="${GbvBridge.IDENTIFIER}"/> GBV <code>API</code>
						&nbsp;
						<g:checkBox name="processOption" checked="true" value="${EzbBridge.IDENTIFIER}"/> EZB <code>API</code>
						&nbsp;
						<g:checkBox name="processOption" checked="false" disabled="true" value="${ZdbBridge.IDENTIFIER}"/> ZDB <code>API</code>
					</div>
				</div><!-- .row -->
				
				<br />
				
				<div class="row">
					<div class="col-xs-12">
						Einstiegungspunkt für die Anreicherung:
						<br /><br />

						<g:radio name="processIndexType" checked="true" value="${ZdbBridge.IDENTIFIER}"/> ZDB-ID <code>(ZDB-ID)</code>
						&nbsp;
						<g:radio name="processIndexType" value="${TitleStruct.PISSN}"/> pISSN <code>(print_identifier)</code> 
						&nbsp;
						<g:radio name="processIndexType" value="${TitleStruct.EISSN}"/> eISSN <code>(online_identifier)</code>
					</div>
				</div><!-- .row -->
							
				<br />
			</g:if>
			
			<div class="row">
			
				<div class="col-xs-12">
					<g:if test="${e.value.status == Enrichment.ProcessingState.UNTOUCHED}">
		    			<g:actionSubmit action="deleteFile" value="Datei löschen" class="btn btn-danger"/>
						<g:actionSubmit action="processFile" value="Bearbeitung starten" class="btn btn-default"/>
					</g:if>
					<g:if test="${e.value.status == Enrichment.ProcessingState.PREPARE}">
						<g:actionSubmit action="deleteFile" value="Datei löschen" class="btn btn-danger"/>
						<g:actionSubmit action="prepareFile" value="Weiter" class="btn btn-default"/>
		    		</g:if>
					<g:if test="${e.value.status == Enrichment.ProcessingState.WORKING}">
						<g:actionSubmit action="stopProcessingFile" value="Bearbeitung abbrechen" class="btn btn-danger"/>
					</g:if>
					<g:if test="${e.value.status == Enrichment.ProcessingState.ERROR}">
						<g:actionSubmit action="deleteFile" value="Datei löschen" class="btn btn-danger"/>
					</g:if>
					<g:if test="${e.value.status == Enrichment.ProcessingState.FINISHED}">
						<g:actionSubmit action="deleteFile" value="Datei löschen" class="btn btn-danger"/>
						
						<g:link controller="statistic" action="show" params="[sthash:e?.value?.resultHash]" target="_blank" class="btn btn-warning">Statistik anzeigen</g:link>
						
						<g:actionSubmit action="downloadPackageFile" value="Package speichern" class="btn btn-success"/>
		    			<g:actionSubmit action="downloadTitlesFile" value="Titles speichern" class="btn btn-success"/>

		    			<g:if test="${grailsApplication.config.ygor.enableGokbUpload}">
							<g:actionSubmit action="sendPackageFile" value="Package zur GOKb senden" class="btn btn-success"
		    					data-toggle="tooltip" data-placement="top" title="${grailsApplication.config.gokbApi.xrPackageUri}" />
		    				<g:actionSubmit action="sendTitlesFile" value="Titles zur GOKb senden" class="btn btn-success"
		    					data-toggle="tooltip" data-placement="top" title="${grailsApplication.config.gokbApi.xrTitleUri}" />
	    				</g:if>
	    				<g:else>
							<g:actionSubmit action="sendPackageFile" value="Package zur GOKb senden" class="btn btn-success disabled"
		    					data-toggle="tooltip" data-placement="top" title="Deaktiviert: ${grailsApplication.config.gokbApi.xrPackageUri}" disabled="disabled"/>
		    				<g:actionSubmit action="sendTitlesFile" value="Titles zur GOKb senden" class="btn btn-success disabled"
		    					data-toggle="tooltip" data-placement="top" title="Deaktiviert: ${grailsApplication.config.gokbApi.xrTitleUri}" disabled="disabled"/>
	    				</g:else>
		    			
		    			<br /><br />
		    			
		    			<g:if test="${grailsApplication.config.ygor.enableDebugDownload}">
		    				<g:actionSubmit action="downloadDebugFile" value="Debug-Datei speichern" class="btn"/>
		    				<g:actionSubmit action="downloadRawFile" value="Datenstruktur speichern" class="btn"/>
		    			</g:if>

		    		</g:if>
		    		
				</div>
				
			</div><!-- .row -->	
		
			<br />
			
			<script>
				$(function(){
				  $('[data-toggle="tooltip"]').tooltip()
				})
			</script>

			<g:if test="${e.value.status == Enrichment.ProcessingState.WORKING}">
				<script>
					$(function(){
						var ygorDocumentStatus${e.key} = function(){
							jQuery.ajax({
								type:       'GET',
								url:         '${grailsApplication.config.grails.app.context}/enrichment/ajaxGetStatus',
								data:        'originHash=${e.key}',
								success:function(data, textStatus){
									
									data = jQuery.parseJSON(data)
									console.log(data)
									var status = data.status;
									var progress = data.progress;
									
									jQuery('#progress-${e.key} > .progress-bar').attr('aria-valuenow', progress);
									jQuery('#progress-${e.key} > .progress-bar').attr('style', 'width:' + progress + '%');
									jQuery('#progress-${e.key} > .progress-bar').text(progress + '%');
	
									if(status == 'FINISHED') {
										window.location = '${grailsApplication.config.grails.app.context}/enrichment/process';
									}
									if(status == 'ERROR') {
										window.location = '${grailsApplication.config.grails.app.context}/enrichment/process';
									}
									
								},
								error:function(XMLHttpRequest, textStatus, errorThrown){
									clearInterval(ygorDocumentStatus${e.key});
								}
							});
						}
	
						var ygorInterval${e.key} = setInterval(ygorDocumentStatus${e.key}, 1500);
					})
				</script>
			</g:if>
		</g:form>
	</g:each>
