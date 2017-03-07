<!-- _stepInfo.gsp -->
<%@ page import="ygor.Enrichment" %>

<div class="row">

	<g:if test="${enrichments.size() == 0}">
	
		<div class="col-xs-12">
			<h3>Quelldaten bestimmen</h3>
			<g:render template="messages" />
		</div>
		<div class="col-xs-12">
			<g:render template="howtostep1" />
		</div>
		
	</g:if>

	<g:if test="${enrichments.size() == 1}">

		<g:each in="${enrichments}" var="doc">

			<g:if test="${doc.value.status == Enrichment.ProcessingState.PREPARE}">
				<div class="col-xs-12">
					<h3>Paketdaten festlegen</h3>
					<g:render template="messages" />
				</div>
				<div class="col-xs-12">
					<g:render template="howtostep2" />
				</div>
			</g:if>
		
			<g:if test="${doc.value.status != Enrichment.ProcessingState.FINISHED && doc.value.status != Enrichment.ProcessingState.PREPARE}">
				<div class="col-xs-12">
					<h3>Anreicherung</h3>
					<g:render template="messages" />
				</div>
				<div class="col-xs-12">
					<g:render template="howtostep3" />
				</div>
			</g:if>
			
			<g:if test="${doc.value.status == Enrichment.ProcessingState.FINISHED}">
				<div class="col-xs-12">
					<h3>Übermittlung an die Knowledgebase</h3>
					<g:render template="messages" />
				</div>
				<div class="col-xs-12">
					<g:render template="howtostep4" />
				</div>
			</g:if>
			
			<g:if test="${doc.value.status == Enrichment.ProcessingState.ERROR}">
				<div class="col-xs-12">
					<h3>Programmfehler</h3>
					<g:render template="messages" />
				</div>
			</g:if>
					
		</g:each>
		
	</g:if>	
		
</div><!-- .row -->	