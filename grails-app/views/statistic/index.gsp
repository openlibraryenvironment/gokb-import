<!DOCTYPE html>
<html>
	<head>
		<title>Ygor - Alpha</title>
		<meta name="layout" content="enrichment">
		<g:if env="development"><link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css"></g:if>
	</head>
	<body>
		<g:form controller="statistic" action="show">
			<div class="row">
				<div class="col-xs-12">
				Statistikabfrage per Hash:
				<br /><br />  
				<g:textField name="sthash" size="64" value="" />
				<br />
				<br />
				<g:actionSubmit action="show" value="Anzeigen" class="btn"/>
			</div>
		</g:form>
	</body>
</html>