<nav class="navbar navbar-inverse navbar-fixed-top">
	<div class="container">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
				<span class="sr-only">Toggle navigation</span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="/ygor">Ygor</a>
		</div>
		<div id="navbar" class="collapse navbar-collapse">
			<ul class="nav navbar-nav">
				<li <g:if test="${currentView == 'process'}">class="active"</g:if>>
					<a href="/ygor/enrichment/process">Informationsanreicherung</a>
				</li>
				<!--<li><a href="zevport">ZevPort-Export</a></li>-->
				<li <g:if test="${currentView == 'howto'}">class="active"</g:if>>
					<a href="/ygor/enrichment/howto">Anleitung</a>
				</li>
				<li <g:if test="${currentView == 'about'}">class="active"</g:if>>
					<a href="/ygor/enrichment/about">Über</a>
				</li>
				<li <g:if test="${currentView == 'contact'}">class="active"</g:if>>
					<a href="/ygor/enrichment/contact">Kontakt</a>
				</li>
			</ul>
		</div><!--/.nav-collapse -->
	</div>
</nav>