<meta name="layout" content="enrichment">

<div class="row">
	<div class="col-xs-10 col-xs-offset-1">
	</div>
	
	<div class="col-xs-10 col-xs-offset-1">
		<br />
		<p><g:message code="about.title" /></p>
		<p>
			<g:message code="about.content.1" />
		</p>
		
		<p>
			<g:message code="about.content.2" />
		</p>
		<p>
			<g:message code="about.content.3" />
		</p>
		<br />
		<p>
			<em><g:message code="about.version" /> <g:meta name="app.version"/></em> <br/>
			<em><g:message code="about.grails" /> <g:meta name="app.grails.version"/></em> <br/>
			<em><g:message code="about.gokb" />: <a href="${grailsApplication.config.gokbApi.baseUri}">${grailsApplication.config.gokbApi.baseUri}</a></em>
			<br/><br/>

			<em> <g:if test="${grailsApplication.metadata['repository.revision.number']}">
				<a target="_blank" class="item" href="https://github.com/hbz/laser-ygor/tree/${grailsApplication.metadata['repository.revision.number']}">
					Build: ${grailsApplication.metadata['build.DateTimeStamp']}

				</a><br>
				Branch: ${grailsApplication.metadata['repository.branch']}
			</g:if>
				<g:else>
					Build: ${grailsApplication.metadata['build.DateTimeStamp']}<br>
					Branch: ${grailsApplication.metadata['repository.branch']}
				</g:else></em>
		</p>

	</div>

</div>
