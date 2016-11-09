<!DOCTYPE html>
<html>
	<head>
		<title>Ygor - <% println session.id %></title>
		<meta name="layout" content="enrichment">
		<g:if env="development"><link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css"></g:if>
	</head>
	<body>
	
	<div class="row">
		<div class="col-md-12">
			<h3 id="iet" title="Session: ${session.id}">Informationsanreicherung</h3>
		</div>
	</div>
	
		<g:render template="processInfo" />
	
		<g:render template="uploadFile" />
		
		<g:render template="listDocuments" />

	</body>
</html>
