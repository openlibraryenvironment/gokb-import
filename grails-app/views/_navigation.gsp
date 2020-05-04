<nav class="navbar navbar-inverse navbar-fixed-top">
	<div class="container">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
				<span class="sr-only">Toggle navigation</span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="${grailsApplication.config.grails.app.context}">Ygor <sup><small>alpha</small></sup></a>
		</div>
		<div id="navbar" class="collapse navbar-collapse">
			<ul class="nav navbar-nav">
				<li <g:if test="${currentView == 'process'}">class="active"</g:if>>
					<g:link controller="enrichment" action="process" params="[flash.error = null, flash.warning = null]">${message(code: 'navigation.menue.enrichment')}</g:link>
				</li>
				<!--<li><a href="zevport">ZevPort-Export</a></li>-->
				<!-- <li <g:if test="${currentView == 'config'}">class="active"</g:if>>
					<a href="${grailsApplication.config.grails.app.context}/enrichment/config"><g:message code="navigation.menue.config" /></a>
				</li> -->
				<li <g:if test="${currentView == 'howto'}">class="active"</g:if>>
					<a href="${grailsApplication.config.grails.app.context}/enrichment/howto"><g:message code="navigation.menue.howto" /></a>
				</li>
				<li <g:if test="${currentView == 'about'}">class="active"</g:if>>
					<a href="${grailsApplication.config.grails.app.context}/enrichment/about"><g:message code="navigation.menue.about" /></a>
				</li>
				<li <g:if test="${currentView == 'contact'}">class="active"</g:if>>
					<a href="${grailsApplication.config.grails.app.context}/enrichment/contact"><g:message code="navigation.menue.contact" /></a>
				</li>
			</ul>
		</div><!--/.nav-collapse -->
	</div>
</nav>