<!-- _listDocuments.gsp -->

<%@ page import="ygor.Document" %>

<g:if test="${documents.size() == 1}">

	<g:each in="${documents}" var="doc">
	
		<div class="row">

			<g:if test="${doc.value.status != Document.StateOfProcess.FINISHED}">
				<div class="col-md-12">
					<div class="well">
						<strong>Schritt 2</strong> 
						<br /><br />
						<span class="glyphicon glyphicon-hand-right" aria-hidden="true"></span>
						&nbsp; Geben Sie den Index der Spalte an, welche die eISSN enthält.
						<br />
						<span class="glyphicon glyphicon-hand-right" aria-hidden="true"></span>
						&nbsp; Wählen Sie die hinzuzufügenden Informationen aus.
						<br />
						<span class="glyphicon glyphicon-hand-right" aria-hidden="true"></span>
						&nbsp; Starten Sie die Bearbeitung.
					</div>
				</div>
			</g:if>
			
			<g:if test="${doc.value.status == Document.StateOfProcess.FINISHED}">
				<div class="col-md-12">
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
			
		</div><!-- .row -->	
	
	
	
		<g:form controller="document">
			<g:hiddenField name="originHash" value="${doc.key}" />
			
			<div class="row">
				<div class="col-md-8">
					<span class="glyphicon glyphicon-file"></span> 
					<span title="${doc.value.originHash}">${doc.value.originName}</span>
				</div>
				<div class="col-md-4">
					<g:if test="${doc.value.status == Document.StateOfProcess.UNTOUCHED}">
						<span class="label label-default pull-right">Nicht bearbeitet</span>
					</g:if>
					<g:if test="${doc.value.status == Document.StateOfProcess.WORKING}">
						<span class="label label-info pull-right">In Bearbeitung</span>
					</g:if>
					<g:if test="${doc.value.status == Document.StateOfProcess.ERROR}">
						<span class="label label-danger pull-right">Fehler</span>
					</g:if>
					<g:if test="${doc.value.status == Document.StateOfProcess.FINISHED}">
						<span class="label label-success pull-right">Bearbeitung abgeschlossen</span>
					</g:if>
	
				</div>
			</div><!-- .row -->	
	
			<br />
			
			<div class="row">
				<div class="col-md-4">
					<g:select name="processIndex" from="${1..20}" value="0" 
						noSelection="['':'Die ISSN findet man in der Spalte ..']"  class="form-control"/>
				</div>
			</div><!-- .row -->
			
			<br />
			
			<div class="row">
				<div class="col-md-12">
					Folgende Information soll hinzugefügt werden ..
					<br /><br />
					<label class="radio-inline">
						<g:radio name="processOption" checked="true" value="zdbid" class="radio"/> ZDB-Id
					</label>
					<label class="radio-inline">
						<g:radio name="processOption" value="ezbid" class="radio"/> EZB-Id
					</label>			
					<label class="radio-inline">
						<g:radio name="processOption" value="hbzid" class="radio"/> HBZ-Id
					</label>
					<label class="radio-inline">
						<g:radio name="processOption" value="gokbid" class="radio"/> GOKb-Id
					</label>
				</div>
			</div><!-- .row -->
			
			<br />
			
			<div class="row">
			
				<div class="col-md-6">
					<g:if test="${doc.value.status == Document.StateOfProcess.FINISHED}">
		    			<g:actionSubmit action="downloadFile" value="Ergebnis speichern" class="btn btn-success"/>
		    		</g:if>
		    		<g:actionSubmit action="deleteFile" value="Datei löschen" class="btn btn-danger"/>
					<g:actionSubmit action="processFile" value="Bearbeitung starten" class="btn btn-default"/>
				</div>
				
				<div class="col-md-6">
					<div class="progress-bar progress-bar-${doc.key}" role="progressbar" aria-valuenow="50" aria-valuemin="0" aria-valuemax="100" style="width: 50%;">
						50%
					</div>
				</div>
				
			</div><!-- .row -->	
			

				
				
				
				
				
				
			
				
				
				<!--
				<g:checkBox name="processOption" checked="true" value="zdbid"/> ZDBId
				<g:checkBox name="processOption" checked="false" value="ezbid"/> EZBId
				<g:checkBox name="processOption" checked="false" value="gokbid"/> GOkbId
				-->
				<!-- 
				<div class="progress">
					<div class="progress-bar" role="progressbar" aria-valuenow="5" aria-valuemin="0" aria-valuemax="100" style="width: 5%;">
						0%
					</div>
				</div>
				-->

			
			<br />
			
			<!-- 
			<div class="row">
				<div class="col-md-12">
					<div class="progress-bar progress-bar-${doc.key}" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;">
						0%
					</div>
					
					<g:if test="${doc.value.status == Document.StateOfProcess.WORKING}">
						<script>
							var ygorProgressBar${doc.key} = function(){
								jQuery.ajax({
									type:'POST', 
									url:'/ygor/document/updateProgressbar',
									data:'originHash=${doc.key}',
									success:function(data, textStatus){
										jQuery('.progress-bar-${doc.key}').attr('aria-valuenow', data);
										jQuery('.progress-bar-${doc.key}').text(data + '%');
										jQuery('.progress-bar-${doc.key}').attr('style', 'width: ' + data + '%;');
										if(100 == data){
											clearInterval(ygorInterval${doc.key})}
										},
									error:function(XMLHttpRequest, textStatus, errorThrown){
										}
								});
							}
							var ygorInterval${doc.key} = setInterval(ygorProgressBar${doc.key}, 1000);
						</script>
					</g:if>
				</div>
			</div>
			
			<br />
			-->
			
			<g:if test="${doc.value.status == Document.StateOfProcess.WORKING}">
				<script>
					var ygorDocumentStatus${doc.key} = function(){
						jQuery.ajax({
							type:'POST', 
							url:'/ygor/document/getStatus',
							data:'originHash=${doc.key}',
							success:function(data, textStatus){
								console.log(data)
								if(data != 'WORKING') {
									window.location = '/ygor/document/index';
								}
							},
							error:function(XMLHttpRequest, textStatus, errorThrown){
							}
						});
					}
					var ygorInterval${doc.key} = setInterval(ygorDocumentStatus${doc.key}, 2000);
				</script>
			</g:if>
			
	  
		</g:form>
	</g:each>

</g:if>
