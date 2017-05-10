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
				<li class="list-group-item active">Quelldaten bestimmen</li>
			  	<li class="list-group-item">Paketdaten festlegen</li>
			  	<li class="list-group-item">Anreicherung</li>
			  	<li class="list-group-item">Übermittlung an die Knowledgebase</li>
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
						<li class="list-group-item">Quelldaten bestimmen</li>
					  	<li class="list-group-item active">Paketdaten festlegen</li>
					  	<li class="list-group-item">Anreicherung</li>
					  	<li class="list-group-item">Übermittlung an die Knowledgebase</li>
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
						<li class="list-group-item">Quelldaten bestimmen</li>
					  	<li class="list-group-item">Paketdaten festlegen</li>
					  	<li class="list-group-item">Anreicherung</li>
					  	<li class="list-group-item active">Übermittlung an die Knowledgebase</li>
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
						<li class="list-group-item">Quelldaten bestimmen</li>
					  	<li class="list-group-item">Paketdaten festlegen</li>
					  	<li class="list-group-item active">Anreicherung</li>
					  	<li class="list-group-item">Übermittlung an die Knowledgebase</li>
					</ul>
				</div>

			</g:if>
			
			<g:if test="${e.value.status == Enrichment.ProcessingState.ERROR}">
			
				<div class="col-xs-8">	
					<blockquote>
						<p>${e.value.getMessage()}</p>
						<footer>stacktrace</footer>
					</blockquote>
				</div>
				
			</g:if>
					
		</g:each>
		
	</g:if>	
		
</div><!-- .row -->	