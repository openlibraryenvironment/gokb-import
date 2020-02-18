<g:set var="colour" value="${flag.colour.toString().toLowerCase()}"/>
<div class="panel-heading-${colour}">
    <h3 class="panel-title">${String.format(flag.text, message(code: flag.messageCode))}
        <g:if test="${!colour.equals("red")}">
            <g:actionSubmitImage class="reject-icon-header" value="flag" action="rejectFlag" src="${resource(dir: 'images', file: 'reject_icon.png')}" />
        </g:if>
        <g:if test="${!colour.equals("yellow")}">
            <g:actionSubmitImage class="warn-icon-header" value="flag" action="warnFlag" src="${resource(dir: 'images', file: 'warn_icon.png')}" />
        </g:if>
        <g:if test="${!colour.equals("green")}">
            <g:actionSubmitImage class="approve-icon-header" value="flag" action="approveFlag" src="${resource(dir: 'images', file: 'approve_icon.svg')}" />
        </g:if>
    </h3>
</div>