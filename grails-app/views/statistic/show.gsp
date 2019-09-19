<meta name="layout" content="enrichment">

<div class="row">

	<div class="col-xs-12">
		<div class="panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title">${invalidRecords.size}<g:message code="statistic.show.records.invalid"/></h3>
			</div>
			<div class="statistics-data">
				<table class="statistics-details">
                    <g:if test="${dataType.toLowerCase() != 'ebooks'}">
                        <tr>
                            <th>Title</th>
                            <th>ZDB ID</th>
                            <th>eISSN</th>
                        </tr>
                        <g:set var="lineCounter" value="${0}" />
                        <g:each in="${invalidRecords}" var="record">
                            <tr class="${ (lineCounter % 2) == 0 ? 'even hover' : 'odd hover'}">
                                <td class="statistics-cell"><g:link action="edit" params="[sthash:sthash]" id="${record.uid}">${record.publicationTitle}</g:link><br/></td>
                                <td class="statistics-cell">${record.zdbId}<br/></td>
                                <td class="statistics-cell">${record.onlineIdentifier}<br/></td>
                            </tr>
                            <g:set var="lineCounter" value="${lineCounter + 1}"/>
                        </g:each>
                    </g:if>
                    <g:else>
                        <tr>
                            <th>Title</th>
                            <th>eISBN</th>
                        </tr>
                        <g:set var="lineCounter" value="${0}" />
                        <g:each in="${invalidRecords}" var="record">
                            <tr class="${ (lineCounter % 2) == 0 ? 'even hover' : 'odd hover'}">
                                <td class="statistics-cell"><g:link action="edit" params="[sthash:sthash]" id="${record.uid}">${record.publicationTitle}</g:link><br/></td>
                                <td class="statistics-cell">${record.printIdentifier}<br/></td>
                            </tr>
                            <g:set var="lineCounter" value="${lineCounter + 1}"/>
                        </g:each>
                    </g:else>
				</table>
			</div>
		</div>
	</div>

	<div class="col-xs-12">
		<div class="panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title">${validRecords.size}<g:message code="statistic.show.records.valid"/></h3>
			</div>
			<div class="statistics-data">
				<table class="statistics-details">
                    <g:if test="${dataType.toLowerCase() != 'ebooks'}">
                        <tr>
                            <th>Title</th>
                            <th>ZDB ID</th>
                            <th>eISSN</th>
                        </tr>
                        <g:set var="lineCounter" value="${0}" />
                        <g:each in="${validRecords}" var="record">
                            <tr class="${ (lineCounter % 2) == 0 ? 'even hover' : 'odd hover'}">
                                <td class="statistics-cell"><g:link action="edit" params="[sthash:sthash]" id="${record.uid}">${record.publicationTitle}</g:link><br/></td>
                                <td class="statistics-cell">${record.zdbId}<br/></td>
                                <td class="statistics-cell">${record.onlineIdentifier}<br/></td>
                            </tr>
                            <g:set var="lineCounter" value="${lineCounter + 1}"/>
                        </g:each>
                    </g:if>
                    <g:else>
                        <tr>
                            <th>Title</th>
                            <th>eISBN</th>
                        </tr>
                        <g:set var="lineCounter" value="${0}" />
                        <g:each in="${validRecords}" var="record">
                            <tr class="${ (lineCounter % 2) == 0 ? 'even hover' : 'odd hover'}">
                                <td class="statistics-cell"><g:link action="edit" params="[sthash:sthash]" id="${record.uid}">${record.publicationTitle}</g:link><br/></td>
                                <td class="statistics-cell">${record.printIdentifier}<br/></td>
                            </tr>
                            <g:set var="lineCounter" value="${lineCounter + 1}"/>
                        </g:each>
                    </g:else>
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
				<table class="statistics-details">
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
