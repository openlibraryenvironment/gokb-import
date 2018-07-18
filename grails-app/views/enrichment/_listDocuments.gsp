<!-- _listDocuments.gsp -->

<%@ page 
	import="ygor.Enrichment" 
	import="ygor.GokbService"
	import="de.hbznrw.ygor.export.structure.TitleStruct"
	import="de.hbznrw.ygor.export.structure.PackageStruct"
	import="de.hbznrw.ygor.bridges.*"
	import="de.hbznrw.ygor.connectors.KbartConnector"
%>

	<g:each in="${enrichments}" var="e">
		<g:form controller="enrichment" action="process">
			<g:hiddenField name="originHash" value="${e.key}" />
			
			<div class="row">
				<div class="col-xs-12">
					
					<br /><br />
					
					<ul class="list-group content-list">
						<li class="list-group-item">
				
							<div class="input-group">
								<span class="input-group-addon">Datei</span>
								<span class="form-control" title="${e.value.originHash}">
									${e.value.originName}
				
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
									</em></span>
									
								</span>
							</div>
							
							<br />
							
							<div id="progress-${e.key}" class="progress">
								<g:if test="${e.value.status == Enrichment.ProcessingState.FINISHED}">
									<div class="progress-bar" role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width:100%;">100%</div>
								</g:if>
								<g:else>
									<div class="progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width:0%;">0%</div>
								</g:else>
							</div>



							<g:if test="${e.value.status == Enrichment.ProcessingState.PREPARE}">
														
								<div class="input-group">
									<span class="input-group-addon">Titel</span>
									<g:textField name="pkgTitle" size="48" value="Munchhausen Verlag : hbz : 1999" class="form-control" />
								</div>
								
								<br />
							
								<div class="input-group">
									<span class="input-group-addon">ZDB-Paketsigel</span>
									<g:textField name="pkgVariantName" size="24" class="form-control" />
								</div>
								
								<br />

								<div class="input-group">
									<span class="input-group-addon"><em>GOKb</em> Plattform</span>
									<select name="pkgNominalPlatform" id="pkgNominalPlatform"></select>
								</div>

								<br />

								<div class="input-group">
									<span class="input-group-addon"><em>GOKb</em> Provider</span>
									<select name="pkgNominalProvider" id="pkgNominalProvider"></select>
								</div>

								<br />

								<script>
                                                                  $(document).ready(function() {
                                                                    $('#pkgNominalPlatform').select2({
                                                                      allowClear: true,
                                                                      placeholder: 'Bitte Plattform wählen..',
                                                                      debug: true,
                                                                      ajax: {
                                                                        url: '/ygor/enrichment/suggestPlatform',
                                                                        data: function (params) {
                                                                          var query = {
                                                                            q: params.term
                                                                          }
                                                                          return query;
                                                                        },
                                                                        processResults: function (data) {
                                                                          return {
                                                                            results: data.items
                                                                          }
                                                                        }
                                                                      }
                                                                    });

                                                                    $('#pkgNominalProvider').select2({
                                                                      allowClear: true,
                                                                      placeholder: 'Bitte Anbieter wählen..',
                                                                      debug: true,
                                                                      ajax: {
                                                                        url: '/ygor/enrichment/suggestProvider',
                                                                        data: function (params) {
                                                                          var query = {
                                                                            q: params.term
                                                                          }
                                                                          return query;
                                                                        },
                                                                        processResults: function (data) {
                                                                          return {
                                                                            results: data.items
                                                                          }
                                                                        }
                                                                      }
                                                                    });
                                                                  });
								</script>

								<div class="input-group">
									<span class="input-group-addon"><em>GOKb</em> Curatory Group 1</span>
									<g:textField name="pkgCuratoryGroup1" size="24" value="LAS:eR" class="form-control" />
								</div>
								
								<div class="input-group">
									<span class="input-group-addon"><em>GOKb</em> Curatory Group 2</span>
									<g:textField name="pkgCuratoryGroup2" size="24" value="hbz" class="form-control" />
								</div>
								
							<!-- 
							<div class="row">
								<div class="col-xs-6 col-xs-offset-6">
									GOKb Source Name:
									<br /><br /> 
									<g:textField name="pkgSourceName" size="24" value="LAS:eR" />
									&nbsp;
									<g:textField name="pkgSourceUrl" size="24" value="hbz" />
									<br />
									<br /> 
								</div>				
							</div> .row -->
							
							</g:if>

			
							<g:if test="${e.value.status == Enrichment.ProcessingState.UNTOUCHED}">
		
								Über die folgenden Schnittstellen sollen die Information hinzugefügt werden
								<br /><br />	
								
								<div class="input-group">
									<span class="input-group-addon">Quellen</span>
									<span class="form-control">
										<div class="checkbox">
											<label>
												<g:checkBox name="processOption" required="true" checked="true" value="${KbartBridge.IDENTIFIER}"/>
												KBART <code>Datei</code>
											</label>
											&nbsp;
											<label>
												<g:checkBox name="processOption" required="true" checked="true" value="${ZdbBridge.IDENTIFIER}"/>
												ZDB <em>@GBV</em> <code>API</code>
											</label>
											&nbsp;
											<label>
												<g:checkBox name="processOption" checked="true" value="${EzbBridge.IDENTIFIER}"/>
												EZB <code>API</code>
											</label>
											<!--
											&nbsp;
											<label>
												<g:checkBox name="processOption" checked="false" disabled="true" value="${ZdbBridge.IDENTIFIER}"/>
												ZDB <code>API</code>
											</label>
											-->
										</div>
									</span>
								</div>
							
								<br />
								Einstiegspunkt für die Anreicherung
								<br /><br />
		
								<div class="input-group">
									<span class="input-group-addon">Schlüssel</span>
									<span class="form-control">
										<div class="radio">
											Als Einstiegspunkt wird pro Datensatz die folgende Reihenfolge angewandt: ZDB-ID, eISSN, pISSN
										</div>
									</span>
								</div>
								
							</g:if>
							
						</li>
					</ul>
			
			<ul class="list-group content-list">
				<li class="list-group-item">
						
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
							<button type="button" class="btn btn-success" data-toggle="modal" gokbdata="package"
									data-target="#credentialsModal">Package zur GOKb senden</button>
							<button type="button" class="btn btn-success" data-toggle="modal" gokbdata="titles"
									data-target="#credentialsModal">Titles zur GOKb senden</button>
							<div class="modal fade" id="credentialsModal" role="dialog">
								<div class="modal-dialog">
									<div class="modal-content">
										<div class="modal-header">
											<button type="button" class="close" data-dismiss="modal">&times;</button>
											<h4 class="modal-title">Bitte geben Sie Ihre GOKb Credentials ein</h4>
										</div>
										<div class="modal-body">
											<g:form>
												<div class="input-group">
													<span class="input-group-addon">Benutzername</span>
													<g:textField name="gokbUsername" size="24" class="form-control" />
												</div>
												<div class="input-group">
													<span class="input-group-addon">Passwort</span>
													<g:passwordField name="gokbPassword" size="24" class="form-control"/>
												</div>
												<div align="right">
													<button type="button" class="btn btn-default" data-dismiss="modal">Abbrechen</button>
													<g:actionSubmit action="" value="Senden" class="btn btn-success"
																	name="cred-modal-btn-send" data-toggle="tooltip"
																	data-placement="top" title="JavaScript erforderlich" />
												</div>
											</g:form>
										</div>
									</div>
								</div>
							</div>
							<script>
                                $('#credentialsModal').on('show.bs.modal', function (event) {
                                    var uri = $(event.relatedTarget)[0].getAttribute("gokbdata");
                                    if (uri.localeCompare('package') == 0){
                                        $(this).find('.modal-body .btn.btn-success').attr('name', '_action_sendPackageFile');
									}
									else if (uri.localeCompare('titles') == 0){
                                        $(this).find('.modal-body .btn.btn-success').attr('name', '_action_sendTitlesFile');
                                    }
                                })
							</script>
	    				</g:if>
	    				<g:else>
							<g:actionSubmit action="sendPackageFile" value="Package zur GOKb senden" class="btn btn-success disabled"
		    					data-toggle="tooltip" data-placement="top" title="Deaktiviert: ${grailsApplication.config.gokbApi.xrPackageUri}" disabled="disabled"/>
		    				<g:actionSubmit action="sendTitlesFile" value="Titles zur GOKb senden" class="btn btn-success disabled"
		    					data-toggle="tooltip" data-placement="top" title="Deaktiviert: ${grailsApplication.config.gokbApi.xrTitleUri}" disabled="disabled"/>
	    				</g:else>
	    				
		    			<g:if test="${grailsApplication.config.ygor.enableDebugDownload}">
		    				</li>
			    			<li class="list-group-item">
			    				<g:actionSubmit action="downloadDebugFile" value="Debug-Datei speichern" class="btn"/>
			    				<g:actionSubmit action="downloadRawFile" value="Datenstruktur speichern" class="btn"/>
		    			</g:if>

		    		</g:if>
		    		
				</li>
			</ul>
		
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
