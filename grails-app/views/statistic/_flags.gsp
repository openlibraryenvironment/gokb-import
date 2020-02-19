<g:set var="colour" value="${flag.colour.toString().toLowerCase()}"/>
<div class="panel-heading-${colour}">
    <h3 class="panel-title">${String.format(flag.text, message(code: flag.messageCode))}
    <g:if test="${!colour.equals("red")}">
        <!-- input name="flag.${flag.uid}" value="${flag.uid}" type="hidden" / -->
        <g:actionSubmit action="setFlag" value="${message(code: 'flag.button.reject')}" class="btn btn-danger"
                        id="reject-flag" onclick="createHiddenFlagField('${flag.uid}', 'RED')"/>
    </g:if>
    <g:if test="${!colour.equals("yellow")}">
        <g:actionSubmit action="setFlag" value="${message(code: 'flag.button.warn')}" class="btn btn-warning"
                        id="warn-flag" onclick="createHiddenFlagField('${flag.uid}', 'YELLOW')"/>
    </g:if>
    <g:if test="${!colour.equals("green")}">
        <g:actionSubmit action="setFlag" value="${message(code: 'flag.button.approve')}" class="btn btn-success"
                        id="approve-flag" onclick="createHiddenFlagField('${flag.uid}', 'GREEN')"/>
    </g:if>
    </h3>
</div>