<%@ page import="de.hbznrw.ygor.tools.JsonToolkit; grails.converters.JSON" %>
<meta name="layout" content="enrichment">

<g:set var="displayZDB" value="${dataType == null || dataType.toLowerCase() != 'ebooks'}"/>

<div class="row">
    <div class="col-xs-12">
        <g:form>
            <input type="hidden" name="sthash" value="${sthash}"/>
            <input type="hidden" name="record.uid" value="${record.uid}"/>
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">
                        <g:message code="statistic.edit.record"/> - ${record.multiFields.get("publicationTitle")?.getPrioValue()}
                        <g:if test="${displayZDB}">
                            <a href="${record.zdbIntegrationUrl}" class="link-icon-header"></a>
                        </g:if>
                    </h3>
                </div>
                <div class="statistics-data">
                    <table class="statistics-details" id="edit-table">
                        <thead>
                            <tr>
                                <th><g:message code="statistic.edit.field"/></th>
                                <th><g:message code="statistic.edit.value"/></th>
                                <th><g:message code="statistic.edit.source"/></th>
                                <th><g:message code="statistic.edit.status"/></th>
                            </tr>
                        </thead>
                        <g:set var="lineCounter" value="${0}" />
                        <g:each in="${record.multiFields}" var="multiField">
                            <g:if test="${multiField.value.isCriticallyInvalid()}">
                                <g:set var="status" value="critical"/>
                            </g:if>
                            <g:elseif test="${multiField.value.isNonCriticallyInvalid()}">
                                <g:set var="status" value="noncritical"/>
                            </g:elseif>
                            <g:else>
                                <g:set var="status" value="valid"/>
                            </g:else>
                            <g:if test="${(lineCounter % 2) == 0}">
                                <g:set var="status" value="${status}-even-hover"/>
                            </g:if>
                            <g:else>
                                <g:set var="status" value="${status}-odd-hover"/>
                            </g:else>
                            <tr class="${status}">
                                <td class="statistics-cell-key">${multiField.key}</td>
                                <td class="statistics-cell-value" contenteditable="true">${multiField.value.getPrioValue()}</td>
                                <td class="statistics-cell-source">${multiField.value.getPrioSource()}</td>
                                <td class="statistics-cell-status"><g:message code="${multiField.value.status}"/></td>
                            </tr>
                            <g:set var="lineCounter" value="${lineCounter + 1}"/>
                        </g:each>
                    </table>
                </div>
            </div>
            <g:link controller="statistic" action="cancel" params="[sthash:sthash]" id="${record.uid}">
                <g:actionSubmit action="cancel" value="${message(code:'statistic.edit.cancel')}" class="btn btn-default"/>
            </g:link>
            <g:link controller="statistic" action="save" params='[sthash:sthash, record:record.uid]' id="commitchanges">
                <g:actionSubmit action="save" value="${message(code:'statistic.edit.save')}" class="btn btn-success"
                                onclick="changesToHiddenInputFields()" id="saveChanges"/>
            </g:link>
        </g:form>
    </div>
</div>
<asset:javascript src="jquery.dataTables.js"/>
<asset:stylesheet src="jquery.dataTables.css"/>

<script>
    $(document).ready(function() {
        $("#edit-table").dataTable({
            "pageLength": 100
            // "order": [[ 3, "asc" ]]  // in case we want to order by status
        });
    });

    const validationMessages = getValidationMessageMap();
    var tableRowIndex;
    var keys    = document.querySelectorAll('.statistics-cell-key'),
        values  = document.querySelectorAll('.statistics-cell-value'),
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
                        method : "POST",
                        url: '${grailsApplication.config.grails.app.context}/statistic/update',
                        dataType : "json",
                        timeout : 60000,
                        async: true,
                        data: {
                            key: rowKey,
                            value: value,
                            uid: '${record.uid}',
                            sthash: '${sthash}'
                        },
                        success: function(data) {
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


    function changesToHiddenInputFields(){
        for (let i=0; i<keys.length; i++){
            if (sources[i].innerHTML == "${g.message(code:"record.source.revised")}"){
                keys[i].parentElement.appendChild(createHiddenField(keys[i].innerHTML, values[i].innerHTML));
            }
        }
    }


    // TODO is possible to iterate over all messages starting with "VALIDATOR_" - do this
    function getValidationMessageMap(){
        const vms = new Object();
        vms["VALIDATOR_DATE_IS_INVALID"] = "${g.message(code:"VALIDATOR_DATE_IS_INVALID")}";
        vms["VALIDATOR_DATE_IS_MISSING"] = "${g.message(code:"VALIDATOR_DATE_IS_MISSING")}";
        vms["VALIDATOR_DATE_IS_VALID"] = "${g.message(code:"VALIDATOR_DATE_IS_VALID")}";
        vms["VALIDATOR_IDENTIFIER_IS_INVALID"] = "${g.message(code:"VALIDATOR_IDENTIFIER_IS_INVALID")}";
        vms["VALIDATOR_IDENTIFIER_IS_MISSING"] = "${g.message(code:"VALIDATOR_IDENTIFIER_IS_MISSING")}";
        vms["VALIDATOR_IDENTIFIER_IS_NOT_ATOMIC"] = "${g.message(code:"VALIDATOR_IDENTIFIER_IS_NOT_ATOMIC")}";
        vms["VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE"] = "${g.message(code:"VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE")}";
        vms["VALIDATOR_IDENTIFIER_IS_VALID"] = "${g.message(code:"VALIDATOR_IDENTIFIER_IS_VALID")}";
        vms["VALIDATOR_NUMBER_IS_INVALID"] = "${g.message(code:"VALIDATOR_NUMBER_IS_INVALID")}";
        vms["VALIDATOR_NUMBER_IS_MISSING"] = "${g.message(code:"VALIDATOR_NUMBER_IS_MISSING")}";
        vms["VALIDATOR_NUMBER_IS_NOT_ATOMIC"] = "${g.message(code:"VALIDATOR_NUMBER_IS_NOT_ATOMIC")}";
        vms["VALIDATOR_NUMBER_IS_VALID"] = "${g.message(code:"VALIDATOR_NUMBER_IS_VALID")}";
        vms["VALIDATOR_STRING_IS_INVALID"] = "${g.message(code:"VALIDATOR_STRING_IS_INVALID")}";
        vms["VALIDATOR_STRING_IS_MISSING"] = "${g.message(code:"VALIDATOR_STRING_IS_MISSING")}";
        vms["VALIDATOR_STRING_IS_NOT_ATOMIC"] = "${g.message(code:"VALIDATOR_STRING_IS_NOT_ATOMIC")}";
        vms["VALIDATOR_STRING_IS_VALID"] = "${g.message(code:"VALIDATOR_STRING_IS_VALID")}";
        vms["VALIDATOR_URL_IS_INVALID"] = "${g.message(code:"VALIDATOR_URL_IS_INVALID")}";
        vms["VALIDATOR_URL_IS_MISSING"] = "${g.message(code:"VALIDATOR_URL_IS_MISSING")}";
        vms["VALIDATOR_URL_IS_NOT_ATOMIC"] = "${g.message(code:"VALIDATOR_URL_IS_NOT_ATOMIC")}";
        vms["VALIDATOR_URL_IS_VALID"] = "${g.message(code:"VALIDATOR_URL_IS_VALID")}";
        vms["UNDEFINED"] = "${g.message(code:"UNDEFINED")}";
        return vms;
    }


    function getValidationMessage(vms, key){
        return (vms[key] != null) ? vms[key] : key;
    }


    function createHiddenField(name, value){
        let input = document.createElement("input");
        input.setAttribute("type", "hidden");
        input.setAttribute("name", "fieldschanged." + name);
        input.setAttribute("value", value);
        return input;
    }


</script>