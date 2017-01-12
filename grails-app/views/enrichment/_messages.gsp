
		<div id="ygor-messages">
			<g:if test="${flash.error}">
				<div class="alert alert-danger" role="alert">
					<g:message message="${flash.error}" args="${flash.args}" default="${flash.default}"/>
				</div>
			</g:if>
			<g:if test="${flash.warning}">
				<div class="alert alert-warning" role="alert">
					<g:message message="${flash.warning}" args="${flash.args}" default="${flash.default}"/>
				</div>
			</g:if>
			<g:if test="${flash.info}">
				<div class="alert alert-info" role="alert">
					<g:message message="${flash.info}" args="${flash.args}" default="${flash.default}"/>
				</div>
			</g:if>
		</div>
