<meta name="layout" content="enrichment">

<div class="row">

    <div class="col-xs-12">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title"><g:message code="statistic.edit.record"/></h3>
            </div>
            <div class="statistics-data">
                <table class="statistics-details">
                    <tr>
                        <th><g:message code="statistic.edit.field"/></th>
                        <th><g:message code="statistic.edit.value"/></th>
                        <th><g:message code="statistic.edit.source"/></th>
                        <th><g:message code="statistic.edit.status"/></th>
                    </tr>
                    <g:set var="lineCounter" value="${0}" />
                    <g:each in="${record.multiFields}" var="multiField">
                        <g:if test="${multiField?.value?.getPrioValue()}">
                            <tr class="${ (lineCounter % 2) == 0 ? 'even hover' : 'odd hover'}">
                                <td class="statistics-cell"><a href="record.gsp">${multiField.key}</a></td>
                                <td class="statistics-cell"><a href="record.gsp">${multiField.value.getPrioValue()}</a></td>
                                <td class="statistics-cell"><a href="record.gsp">${multiField.value.getPrioSource()}</a></td>
                                <td class="statistics-cell"><a href="record.gsp">${multiField.value.status}</a></td>
                            </tr>
                            <g:set var="lineCounter" value="${lineCounter + 1}"/>
                        </g:if>
                    </g:each>
                </table>
            </div>
        </div>
        <ul class="list-group content-list">
            <input type="submit" value="${message(code:'statistic.edit.cancel')}" class="btn btn-warning"/>
            <input type="submit" value="${message(code:'statistic.edit.save')}" class="btn btn-success"/>
        </ul>
    </div>

</div>
