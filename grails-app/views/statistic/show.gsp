<meta name="layout" content="enrichment">

<div class="row">

    <g:set var="displayZDB" value="${dataType == null || dataType.toLowerCase() != 'ebooks'}"/>

    <div class="col-xs-12">
		<div class="panel panel-default">
			<div class="panel-heading-invalid">
				<h3 class="panel-title">${invalidRecords.size} <g:message code="statistic.show.records.invalid"/></h3>
			</div>
			<div class="statistics-data">
				<table class="statistics-details">
					<thead>
						<tr>
							<th>Title</th>
							<g:if test="${displayZDB}">
								<th>ZDB</th>
								<th>ZDB ID</th>
							</g:if>
							<th>eISSN</th>
						</tr>
					</thead>
					<tbody>
						<g:set var="lineCounter" value="${0}" />
						<g:each in="${invalidRecords}" var="record">
							<tr class="${ (lineCounter % 2) == 0 ? 'even hover' : 'odd hover'}">
								<td class="statistics-cell"><g:link action="edit" params="[sthash:sthash]" id="${record.key}">${record.value.publicationTitle}</g:link>
								</td>
								<g:if test="${displayZDB}">
									<td><g:if test="${record.value.zdbIntegrationUrl}">
										<a href="${record.value.zdbIntegrationUrl}" class="link-icon"></a>
									</g:if></td>
									<td class="statistics-cell">${record.value.zdbId}<br/></td>
								</g:if>
							<td class="statistics-cell">${record.value.onlineIdentifier}<br/></td>
							</tr>
							<g:set var="lineCounter" value="${lineCounter + 1}"/>
						</g:each>
					</tbody>
				</table>
			</div>
		</div>
	</div>


	<div class="col-xs-12">
		<div class="panel panel-default">
			<div class="panel-heading-valid">
				<h3 class="panel-title">${validRecords.size} <g:message code="statistic.show.records.valid"/></h3>
			</div>
			<div class="statistics-data">
				<table class="statistics-details">
                    <thead>
						<tr>
							<th>Title</th>
							<g:if test="${displayZDB}">
								<th>ZDB</th>
								<th>ZDB ID</th>
							</g:if>
							<th>eISSN</th>
						</tr>
					</thead>
                    <g:set var="lineCounter" value="${0}" />
					<tbody>
						<g:each in="${validRecords}" var="record">
							<tr class="${ (lineCounter % 2) == 0 ? 'even hover' : 'odd hover'}">
								<td class="statistics-cell"><g:link action="edit" params="[sthash:sthash]" id="${record.key}">${record.value.publicationTitle}</g:link></td>
								<g:if test="${displayZDB}">
									<td><g:if test="${record.value.zdbIntegrationUrl}">
										<a href="${record.value.zdbIntegrationUrl}" class="link-icon"></a>
									</g:if></td>
									<td class="statistics-cell">${record.value.zdbId}<br/></td>
								</g:if>
								<td class="statistics-cell">${record.value.onlineIdentifier}<br/></td>
							</tr>
							<g:set var="lineCounter" value="${lineCounter + 1}"/>
						</g:each>
					</tbody>
				</table>
			</div>
		</div>
	</div>

	<div class="col-xs-12">
		<div class="panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title"><g:message code="statistic.show.meta"/></h3>
			</div>
			<div id="statistics-meta" class="panel-body">
				<table>
					<tr class="${ (lineCounter % 2) == 0 ? 'active' : 'info active'}">
						<td class="statistics-cell"><strong><g:message code="statistic.show.main.filename"/></strong></td>
						<td class="statistics-cell">${filename}</td>
					</tr>
					<tr class="${ (lineCounter % 2) == 0 ? 'active' : 'info active'}">
						<td class="statistics-cell"><strong><g:message code="statistic.show.main.creation"/></strong></td>
						<td class="statistics-cell">${date}</td>
					</tr>
					<tr class="${ (lineCounter % 2) == 0 ? 'active' : 'info active'}">
						<td class="statistics-cell"><strong><g:message code="statistic.show.main.ygorversion"/></strong></td>
						<td class="statistics-cell">${ygorVersion}<br/></td>
					</tr>
					<tr class="${ (lineCounter % 2) == 0 ? 'active' : 'info active'}">
						<td class="statistics-cell"><strong><g:message code="statistic.show.main.hash"/></strong></td>
						<td class="statistics-cell">${sthash}</td>
					</tr>
				</table>
			</div>
		</div>
	</div>
</div>
<asset:javascript src="jquery.dataTables.js"/>
<asset:stylesheet src="jquery.dataTables.css"/>

<script>
	$(document).ready(function() {
		$(".statistics-details").dataTable();
	});
</script>