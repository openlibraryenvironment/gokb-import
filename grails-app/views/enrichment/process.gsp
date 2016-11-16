<!DOCTYPE html>
<html>
	<head>
		<title>Ygor - <% println session.id %></title>
		<meta name="layout" content="enrichment">
		<g:if env="development"><link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css"></g:if>
	</head>
	<body>
	
		<g:render template="processInfo" />
	
		<g:render template="uploadFile" />
		
		<g:render template="listDocuments" />

	</body>
</html>
