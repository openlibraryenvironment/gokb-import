<!-- _stepInfo.gsp -->
<%@ page import="ygor.Enrichment" %>

<div class="row">

    <g:if test="${enrichment == null || enrichment.status == null}">

        <div class="col-xs-8">
            <g:render template="messages"/>
            <g:render template="howtostep1"/>
        </div>

        <div class="col-xs-4">
            <ul class="list-group">
                <li class="list-group-item active"><g:message code="processInfo.workflow.source"/></li>
                <li class="list-group-item"><g:message code="processInfo.workflow.package"/></li>
                <li class="list-group-item"><g:message code="processInfo.workflow.enrichment"/></li>
                <li class="list-group-item"><g:message code="processInfo.workflow.transmission"/></li>
            </ul>
        </div>

    </g:if>

    <g:if test="${enrichment}">

        <g:if test="${enrichment.value.status == Enrichment.ProcessingState.PREPARE_2}">

            <div class="col-xs-8">
                <g:render template="messages"/>
                <g:render template="howtostep2"/>
            </div>

            <div class="col-xs-4">
                <ul class="list-group">
                    <li class="list-group-item"><g:message code="processInfo.workflow.source"/></li>
                    <li class="list-group-item active"><g:message code="processInfo.workflow.package"/></li>
                    <li class="list-group-item"><g:message code="processInfo.workflow.enrichment"/></li>
                    <li class="list-group-item"><g:message code="processInfo.workflow.transmission"/></li>
                </ul>
            </div>

        </g:if>

        <g:if test="${enrichment.value.status == Enrichment.ProcessingState.FINISHED}">

            <div class="col-xs-8">
                <g:render template="messages"/>
                <g:render template="howtostep4"/>
            </div>

            <div class="col-xs-4">
                <ul class="list-group">
                    <li class="list-group-item"><g:message code="processInfo.workflow.source"/></li>
                    <li class="list-group-item"><g:message code="processInfo.workflow.package"/></li>
                    <li class="list-group-item"><g:message code="processInfo.workflow.enrichment"/></li>
                    <li class="list-group-item active"><g:message code="processInfo.workflow.transmission"/></li>
                </ul>
            </div>

        </g:if>

        <g:if test="${enrichment.value.status != Enrichment.ProcessingState.FINISHED && enrichment.value.status != Enrichment.ProcessingState.PREPARE_2}">

            <div class="col-xs-8">
                <g:render template="messages"/>
                <g:render template="howtostep3"/>
            </div>

            <div class="col-xs-4">
                <ul class="list-group">
                    <li class="list-group-item"><g:message code="processInfo.workflow.source"/></li>
                    <li class="list-group-item"><g:message code="processInfo.workflow.package"/></li>
                    <li class="list-group-item active"><g:message code="processInfo.workflow.enrichment"/></li>
                    <li class="list-group-item"><g:message code="processInfo.workflow.transmission"/></li>
                </ul>
            </div>

        </g:if>

        <g:if test="${enrichment.value.status == Enrichment.ProcessingState.ERROR}">

            <div class="col-xs-8">
                <blockquote>
                    <p>${enrichment.value.getMessage()}</p>
                </blockquote>
            </div>

        </g:if>

    </g:if>

</div><!-- .row -->
