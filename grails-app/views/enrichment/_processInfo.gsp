<!-- _stepInfo.gsp -->
<%@ page import="ygor.Enrichment" %>

<div class="row">

	<g:if test="${documents.size() == 0}">
	
		<div class="col-xs-12">
			<g:render template="howtostep1" />
		</div>
		
	</g:if>

	<g:if test="${documents.size() == 1}">

		<g:each in="${documents}" var="doc">

			<g:if test="${doc.value.status == Enrichment.ProcessingState.PREPARE}">
				<div class="col-xs-12">
					<g:render template="howtostep2" />
				</div>
			</g:if>
		
			<g:if test="${doc.value.status != Enrichment.ProcessingState.FINISHED && doc.value.status != Enrichment.ProcessingState.PREPARE}">
				<div class="col-xs-12">
					<g:render template="howtostep3" />
				</div>
			</g:if>
			
			<g:if test="${doc.value.status == Enrichment.ProcessingState.FINISHED}">
				<div class="col-xs-12">
					<g:render template="howtostep4" />
				</div>
			</g:if>
			
		</g:each>
		
	</g:if>	
		
</div><!-- .row -->	