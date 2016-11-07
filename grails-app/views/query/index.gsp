<!DOCTYPE html>
<html>
	<head>
		<title><g:if env="development">Grails Runtime Exception</g:if><g:else>Error</g:else></title>
		<meta name="layout" content="main">
		<g:if env="development"><link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css"></g:if>
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
