<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
	<head>
		<title><g:layoutTitle default="Grails"/></title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
  		<asset:stylesheet src="application.css"/>
		<asset:javascript src="application.js"/>
		<g:layoutHead/>
	</head>
	<body>
	
		<g:render template="/navigation" />
		
		<div class="container">			
    		<div class="content">
		
      			<g:layoutBody/>
      			
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
			
				<g:render template="/about" />
				<g:render template="/contact" />
				
			</div>
    	</div><!-- /.container -->
    	
    	<script>
			$('#ygor-messages .alert').fadeOut(5000)
    	</script>
	</body>
</html>
