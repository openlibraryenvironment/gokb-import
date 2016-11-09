<!-- _listDocuments.gsp -->

<%@ page import="ygor.Enrichment" %>

<g:if test="${documents.size() == 1}">

	<g:each in="${documents}" var="doc">

		<g:form controller="enrichment" action="process">
			<g:hiddenField name="originHash" value="${doc.key}" />
			
			<div class="row">
				<div class="col-md-8">
					<span class="glyphicon glyphicon-file"></span> 
					<span title="${doc.value.originHash}">${doc.value.originName}</span>
				</div>
				<div class="col-md-4">
					<g:if test="${doc.value.status == Enrichment.StateOfProcess.UNTOUCHED}">
						<span class="label label-default pull-right">Nicht bearbeitet</span>
					</g:if>
					<g:if test="${doc.value.status == Enrichment.StateOfProcess.WORKING}">
						<span class="label label-info pull-right">In Bearbeitung</span>
					</g:if>
					<g:if test="${doc.value.status == Enrichment.StateOfProcess.ERROR}">
						<span class="label label-danger pull-right">Fehler</span>
					</g:if>
					<g:if test="${doc.value.status == Enrichment.StateOfProcess.FINISHED}">
						<span class="label label-success pull-right">Bearbeitung abgeschlossen</span>
					</g:if>
	
				</div>
			</div><!-- .row -->	
	
			<br />
			
			<div class="row">
				<div class="col-md-4">
					<g:select name="processIndex" from="${1..20}" value="0" 
						noSelection="['':'Spaltenindex der ISSN']"  class="form-control"/>
				</div>
				<div class="col-md-8">
					<g:radio name="processIndexType" checked="true" value="pissn"/> pISSN
					<g:radio name="processIndexType" value="eissn"/> eISSN
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
					<!-- 	
					<label class="radio-inline">
						<g:radio name="processOption" value="hbzid" class="radio"/> HBZ-Id
					</label>
					<label class="radio-inline">
						<g:radio name="processOption" value="gokbid" class="radio"/> GOKb-Id
					</label>
					-->
				</div>
			</div><!-- .row -->
			
			<br />
			
			<div class="row">
			
				<div class="col-md-6">
					<g:if test="${doc.value.status == Enrichment.StateOfProcess.FINISHED}">
		    			<g:actionSubmit action="downloadFile" value="Ergebnis speichern" class="btn btn-success"/>
		    		</g:if>
		    		<g:actionSubmit action="deleteFile" value="Datei löschen" class="btn btn-danger"/>
					<g:actionSubmit action="processFile" value="Bearbeitung starten" class="btn btn-default"/>
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
			
			
			<g:if test="${doc.value.status == Enrichment.StateOfProcess.WORKING}">
				<script>
					var ygorDocumentStatus${doc.key} = function(){
						jQuery.ajax({
							type:'POST', 
							url:'/ygor/enrichment/getStatus',
							data:'originHash=${doc.key}',
							success:function(data, textStatus){
								console.log(data)
								if(data != 'WORKING') {
									window.location = '/ygor/enrichment/process';
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
