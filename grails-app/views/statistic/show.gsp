<meta name="layout" content="enrichment">

<div class="row">

	<div class="col-xs-12">
		<div class="panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title">Meta</h3>
			</div>
			<div id="statistics-meta" class="panel-body">
				<p class="text-left">
					<strong><g:message code="statistic.show.main.filename"/></strong>
					${filename}<br/>
					<strong><g:message code="statistic.show.main.creation"/></strong>
					${date}<br/>
					<strong><g:message code="statistic.show.main.ygorversion"/></strong>
					${ygorVersion}<br/>
					<strong><g:message code="statistic.show.main.hash"/></strong>
					${sthash}
				</p>
			</div>
		</div>
	</div>

	<div class="col-xs-12">
		<div class="panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title">Data</h3>
			</div>
			<div id="statistics-data" class="panel">
				<table>
					<tr>
						<th>Title</th>
						<th>ZDB ID</th>
						<th>eISSN</th>
					</tr>
					<g:set var="lineCounter" value="${0}" />
					<g:each in="${invalidRecords}" var="record">
						<tr class="${ (lineCounter % 2) == 0 ? 'active' : 'info active'}">
							<td>${record.publicationTitle}</td>
							<td>${record.zdbId}</td>
							<td>${record.eissn}</td>
						</tr>
						<g:set var="lineCounter" value="${lineCounter + 1}"/>
					</g:each>
				</table>
			</div>
		</div>
	</div>

</div>
