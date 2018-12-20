<meta name="layout" content="enrichment">

<div class="row">
	<div class="col-xs-10 col-xs-offset-1">
		<g:render template="../logo" />
	</div>
	
	<div class="col-xs-10 col-xs-offset-1">
		<br />
	
		<p class="lead"><g:message code="processInfo.workflow.source" /></p>
		<g:render template="howtostep1" />
		<p class="lead"><g:message code="processInfo.workflow.package" /></p>
		<g:render template="howtostep2" />	
		<p class="lead"><g:message code="processInfo.workflow.enrichment" /></p>
		<g:render template="howtostep3" />
		<p class="lead"><g:message code="processInfo.workflow.transmission" /></p>
		<g:render template="howtostep4" />
		
		<g:render template="howtostep4hint" />		
	</div>
</div>