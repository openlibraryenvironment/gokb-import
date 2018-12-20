<!-- _stepInfo.gsp -->
<%@ page import="ygor.Enrichment" %>

<div class="row">

	<g:if test="${enrichments.size() == 0}">
	
		<div class="col-xs-8">
			<g:render template="messages" />		
			<g:render template="howtostep1" />
		</div>
		
		<div class="col-xs-4">
			<ul class="list-group">
				<li class="list-group-item active"><g:message code="processInfo.workflow.source" /></li>
			  	<li class="list-group-item"><g:message code="processInfo.workflow.package" /></li>
			  	<li class="list-group-item"><g:message code="processInfo.workflow.enrichment" /></li>
			  	<li class="list-group-item"><g:message code="processInfo.workflow.transmission" /></li>
			</ul>
		</div>	

	</g:if>

	<g:if test="${enrichments.size() == 1}">

		<g:each in="${enrichments}" var="e">
						
			<g:if test="${e.value.status == Enrichment.ProcessingState.PREPARE}">
			
				<div class="col-xs-8">
					<g:render template="messages" />
					<g:render template="howtostep2" />
				</div>

				<div class="col-xs-4">
					<ul class="list-group">
						<li class="list-group-item"><g:message code="processInfo.workflow.source" /></li>
						<li class="list-group-item active"><g:message code="processInfo.workflow.package" /></li>
						<li class="list-group-item"><g:message code="processInfo.workflow.enrichment" /></li>
						<li class="list-group-item"><g:message code="processInfo.workflow.transmission" /></li>
					</ul>
				</div>
				
			</g:if>
		
			<g:if test="${e.value.status == Enrichment.ProcessingState.FINISHED}">
			
				<div class="col-xs-8">
					<g:render template="messages" />
					<g:render template="howtostep4" />
				</div>
				
				<div class="col-xs-4">
					<ul class="list-group">
						<li class="list-group-item"><g:message code="processInfo.workflow.source" /></li>
						<li class="list-group-item"><g:message code="processInfo.workflow.package" /></li>
						<li class="list-group-item"><g:message code="processInfo.workflow.enrichment" /></li>
						<li class="list-group-item active"><g:message code="processInfo.workflow.transmission" /></li>
					</ul>
				</div>
				
			</g:if>
			
			<g:if test="${e.value.status != Enrichment.ProcessingState.FINISHED && e.value.status != Enrichment.ProcessingState.PREPARE}">
				
				<div class="col-xs-8">
					<g:render template="messages" />
					<g:render template="howtostep3" />
				</div>
				
				<div class="col-xs-4">
					<ul class="list-group">
						<li class="list-group-item"><g:message code="processInfo.workflow.source" /></li>
						<li class="list-group-item"><g:message code="processInfo.workflow.package" /></li>
						<li class="list-group-item active"><g:message code="processInfo.workflow.enrichment" /></li>
						<li class="list-group-item"><g:message code="processInfo.workflow.transmission" /></li>
					</ul>
				</div>

			</g:if>
			
			<g:if test="${e.value.status == Enrichment.ProcessingState.ERROR}">
			
				<div class="col-xs-8">	
					<blockquote>
						<p>${e.value.getMessage()}</p>
					</blockquote>
				</div>
				
			</g:if>
					
		</g:each>
		
	</g:if>	
		
</div><!-- .row -->	