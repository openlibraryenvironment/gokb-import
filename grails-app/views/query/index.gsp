<!DOCTYPE html>
<html>
	<head>
		<title><g:meta name="app.name"/></title>
		<meta name="layout" content="main">
	</head>
	<body>
		query/index.gsp
		<g:if test="${flash.message}"><div class="message" role="status">${flash.message}</div></g:if>
		<g:uploadForm name="inputFileForm">
    		<input type="file" name="inputFile" />
    		<g:submitButton name="upload" class="save" value="Upload" />
		</g:uploadForm>
		
		<pre>
		<% println request.properties.collect{it}.join('\n') %>
		</pre>
		
	</body>
</html>
