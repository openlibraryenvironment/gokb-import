<!DOCTYPE html>
<html>
	<head>
		<title>Ygor - Alpha</title>
		<asset:javascript src="jquery.dataTables.js"/>
		<asset:stylesheet src="jquery.dataTables.css"/>
		<meta name="layout" content="enrichment">
		<g:if env="development"><link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css"></g:if>
	</head>
	<body>
		<g:form controller="statistic" action="show">
			<div class="row">
				<div class="col-xs-10 col-xs-offset-1">
				</div>
	
				<div class="col-xs-10 col-xs-offset-1">
				<br />  
				<g:message code="statistic.index.hash" />
				<br /><br />  
				<g:textField name="resultHash" size="64" value="" />
				<br />
				<br />
				<g:actionSubmit action="show" value="${message(code:'statistic.index.button.show')}" class="btn btn-default"/>
			</div>
		</g:form>
	</body>
</html>
