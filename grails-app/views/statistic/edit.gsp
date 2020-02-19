<%@ page import="org.apache.commons.lang.StringUtils; de.hbznrw.ygor.tools.JsonToolkit; grails.converters.JSON" %>
<meta name="layout" content="enrichment">

<g:set var="displayZDB" value="${true}"/>

<div class="row">
    <div class="col-xs-12">
        <g:form>
            <input type="hidden" name="resultHash" value="${resultHash}"/>
            <input type="hidden" name="record.uid" value="${record.uid}"/>
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">
                        <g:message
                                code="statistic.edit.record"/> - ${record.multiFields.get("publicationTitle")?.getFirstPrioValue()}
                        <g:if test="${displayZDB && record.zdbIntegrationUrl}">
                            <a href="${record.zdbIntegrationUrl}" class="link-icon-header"></a>
                        </g:if>
                    </h3>
                </div>
                <g:if test="${record.multiFields.get("publicationType").getFirstPrioValue().equals("Serial") &&
                        !record.multiFields.get("zdbId").status.toString().equals(de.hbznrw.ygor.enums.Status.VALID.toString())}">
                    <div class="panel-heading-red">
                        <h3 class="panel-title"><g:message code="statistic.edit.record.zdbmatch"/> : <g:message
                                code="${record.multiFields.get("zdbId").status}"/></h3>
                    </div>
                </g:if>
                <g:if test="${!record.hasValidPublicationType()}">
                    <div class="panel-heading-red">
                        <h3 class="panel-title"><g:message code="statistic.edit.record.invalidPublicationType"/></h3>
                    </div>
                </g:if>
                <g:if test="${!record.duplicates.isEmpty()}">
                    <div class="panel-heading-yellow">
                        <h3 class="panel-title"><g:message code="statistic.edit.record.duplicateidentifiers"/>
                        <g:each in="${record.duplicates}" var="rec"> : ${rec.key}</g:each>
                    </div>
                </g:if>
                <g:render template="flags" collection="${record.flags}" var="flag"/>
                <g:if test="${(record.publicationType.equals("serial") && record.zdbIntegrationUrl == null)}">
                    <div class="panel-heading-yellow">
                        <h3 class="panel-title"><g:message code="statistic.edit.record.missingZdbAlignment"/>
                    </div>
                </g:if>
                <div class="statistics-data">
                    <table class="statistics-details" id="edit-table">
                        <thead>
                        <tr>
                            <th>internal field name (hidden by css)</th>
                            <th><g:message code="statistic.edit.field"/></th>
                            <th><g:message code="statistic.edit.value"/></th>
                            <th><g:message code="statistic.edit.source"/></th>
                            <th><g:message code="statistic.edit.status"/></th>
                        </tr>
                        </thead>
                        <g:set var="lineCounter" value="${0}"/>
                        <g:each in="${record.multiFieldsInGokbOrder()}" var="multiField">
                            <g:if test="${multiField.isCriticallyIncorrect(record.publicationType)}">
                                <g:set var="status" value="error"/>
                            </g:if>
                            <g:elseif test="${multiField.isNonCriticallyIncorrect(record.publicationType)}">
                                <g:set var="status" value="warning"/>
                            </g:elseif>
                            <g:else>
                                <g:set var="status" value="ok"/>
                            </g:else>
                            <g:if test="${(lineCounter % 2) == 0}">
                                <g:set var="status" value="${status}-even-hover"/>
                            </g:if>
                            <g:else>
                                <g:set var="status" value="${status}-odd-hover"/>
                            </g:else>
                            <tr class="${status}">
                                <td class="statistics-cell-key">${multiField.ygorFieldKey}</td>
                                <td class="statistics-cell-display">${multiField.displayName}</td>
                                <td class="statistics-cell-value"
                                    contenteditable="true">${multiField.getFirstPrioValue()}</td>
                                <td class="statistics-cell-source">${multiField.getPrioSource()}</td>
                                <td class="statistics-cell-status"><g:message code="${multiField.status}"/></td>
                            </tr>
                            <g:set var="lineCounter" value="${lineCounter + 1}"/>
                        </g:each>
                    </table>
                </div>
            </div>
            <g:link controller="statistic" action="cancel" params="[resultHash: resultHash]" id="${record.uid}">
                <g:actionSubmit action="cancel" value="${message(code: 'statistic.edit.cancel')}"
                                class="btn btn-default"/>
            </g:link>
            <g:link controller="statistic" action="save" params='[resultHash: resultHash, record: record.uid]'
                    id="commitchanges">
                <g:actionSubmit action="save" value="${message(code: 'statistic.edit.save')}" class="btn btn-success"
                                onclick="changesToHiddenInputFields()" id="saveChanges"/>
            </g:link>
        </g:form>
    </div>
</div>

<script>
    $(document).ready(function () {
        $("#edit-table").dataTable({
            "paging": false,
            "ordering": false
            // "order": [[ 3, "asc" ]]  // in case we want to order by status
        });
    });

    const validationMessages = getValidationMessageMap();
    var tableRowIndex;
    var keys = document.querySelectorAll('.statistics-cell-key'),
        values = document.querySelectorAll('.statistics-cell-value'),
        sources = document.querySelectorAll('.statistics-cell-source');

    values.forEach((valueField) => {
        valueField.addEventListener('keydown', function (event) {
            let target = event.target,
                input = target.nodeName != 'INPUT' && target.nodeName != 'TEXTAREA',
                data = {};
            if (input) {
                if (event.which == 27 /* that is "escape" */) {
                    // restore unedited state
                    document.execCommand('undo');
                    target.blur();
                }
                else if (event.which == 13 || event.which == 9 /* that is "enter" or "tab" */) {
                    // save && send update
                    var valuesArray = Array.prototype.slice.call(values);
                    tableRowIndex = valuesArray.indexOf(valueField);
                    const rowKey = keys.item(tableRowIndex).innerHTML;
                    const value = target.innerHTML;
                    jQuery.ajax({
                        method: "POST",
                        url: '${grailsApplication.config.grails.app.context}/statistic/update',
                        dataType: "json",
                        timeout: 60000,
                        async: true,
                        data: {
                            key: rowKey,
                            value: value,
                            uid: '${record.uid}',
                            resultHash: '${resultHash}'
                        },
                        success: function (data) {
                            const recordJson = JSON.parse(data.record)
                            const sourceField = valueField.nextElementSibling;
                            sourceField.innerHTML = recordJson[rowKey]["source"];
                            const statusField = sourceField.nextElementSibling;
                            statusField.innerHTML = getValidationMessage(validationMessages, recordJson[rowKey]["status"]);
                            window.location.reload(true);
                        },
                        error: function (deXMLHttpRequest, textStatus, errorThrown) {
                            console.error("ERROR - Could not update statistics table, failing Ajax request.");
                            console.error(textStatus + " : " + errorThrown);
                            console.error(data);
                        }
                    });
                    target.blur();
                    event.preventDefault();
                }
            }
        }, true);
    });


    function changesToHiddenInputFields() {
        for (let i = 0; i < keys.length; i++) {
            if (sources[i].innerHTML == "${g.message(code:"record.source.revised")}") {
                keys[i].parentElement.appendChild(createHiddenField(keys[i].innerHTML, values[i].innerHTML));
            }
        }
    }


    function getValidationMessageMap() {
        const vms = new Object();
        vms["valid"] = "${g.message(code:"valid")}";
        vms["invalid"] = "${g.message(code:"invalid")}";
        vms["missing"] = "${g.message(code:"missing")}";
        vms["undefined"] = "${g.message(code:"undefined")}";
        return vms;
    }


    function getValidationMessage(vms, key) {
        return (vms[key] != null) ? vms[key] : key;
    }


    function createHiddenField(name, value) {
        let input = document.createElement("input");
        input.setAttribute("type", "hidden");
        input.setAttribute("name", "fieldschanged." + name);
        input.setAttribute("value", value);
        return input;
    }


</script>
