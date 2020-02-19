<g:set var="colour" value="${flag.colour.toString().toLowerCase()}"/>
<div class="panel-heading-${colour}">
    <h3 class="panel-title">${String.format(flag.text, message(code: flag.messageCode))}
    <g:if test="${!colour.equals("red")}">
        <g:actionSubmit value="${message(code: 'flag.button.reject')}"
                        class="btn btn-danger"
                        action="rejectFlag"
                        params="[flag:flag.uid]"
                        id="approve-flag"/>
    </g:if>
    <g:if test="${!colour.equals("yellow")}">
        <g:actionSubmit value="${message(code: 'flag.button.warn')}"
                        class="btn btn-warning"
                        action="warnFlag"
                        params="[flag:flag.uid]"
                        id="warn-flag"/>
    </g:if>
    <g:if test="${!colour.equals("green")}">
        <g:actionSubmit value="${message(code: 'flag.button.approve')}"
                        class="btn btn-success"
                        action="approveFlag"
                        params="[flag:flag.uid]"
                        id="reject-flag"/>
    </g:if>
    </h3>
</div>