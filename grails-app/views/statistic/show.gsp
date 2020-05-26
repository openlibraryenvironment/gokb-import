<%@ page import="ygor.Enrichment" %>

<meta name="layout" content="enrichment">

<p class="lead">${packageName}</p>

<g:each in="${jobIds}" var="jobId">
    <div id="uploadResult-${jobId}" class="showUploadResults">
        <button type="button" class="btn btn-info response-button" data-toggle="collapse" data-target="#btn-accord">
                <g:message code="listDocuments.gokb.response"/>
        </button>
        <button type="button" class="btn btn-success response-remove" onclick="removeJobId('${jobId}')">
            <g:message code="listDocuments.gokb.response.remove"/>
        </button>
        <g:set var="nrOfRecords" value="${greenRecords == null && yellowRecords == null ? 0 :
                greenRecords?.size() + yellowRecords?.size()}"/>
        <div class="collapse in" id="progress-section-${jobId}">
            <div id="progress-${jobId}" class="progress">
                <div class="progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="${nrOfRecords}" style="width:0%;">0%</div>
            </div>
        </div>
        <table class="table" id="feedbackTable-${jobId}">
            <tbody>
            </tbody>
        </table>
    </div>
    <br/>
</g:each>

<script>
    // format buttons width
    var responseButtons = $(".response-button");
    console.log("responseButtons: "+responseButtons);
    console.log("responseButtons[0]: "+responseButtons[0]);
    console.log("responseButtons[0].parentNode: "+responseButtons[0].parentNode);
    console.log("responseButtons[0].parentNode.parentElement: "+responseButtons[0].parentNode.parentElement);
    var responseRemovers = $(".response-remove");
    if (responseButtons.length > 0){
        var divWidth = responseButtons[0].parentNode.parentElement.clientWidth;
        console.log("width: "+divWidth);
        var toggleWidth = (divWidth-5) * 5 / 6;
        var removeWidth = (divWidth-5) / 6;
        for (var i = 0, max = responseButtons.length; i < max; i++) {
            responseButtons[i].style.width = toggleWidth+"px";
            responseRemovers[i].style.width = removeWidth+"px";
        }
    }

    var data = {}
    function removeJobId(uid) {
        jQuery.ajax({
            method: "POST",
            url: '${grailsApplication.config.grails.app.context}/statistic/removeJobId',
            dataType: "json",
            timeout: 500,
            async: true,
            data: {
                uid: uid
            },
            success: function (data) {
                jQuery('#uploadResult-'+uid).attr('hidden', 'hidden');
            },
            error: function (deXMLHttpRequest, textStatus, errorThrown) {
                jQuery('#uploadResult-'+uid).attr('hidden', 'hidden');
            }
        });
    }


    var jobIdsAsList = "${jobIds}".replace("[", "").replace("]", "").split(", ");
    var intervals = new Map();
    var jobStatus;

    $.each(jobIdsAsList, function (i, uid) {
        if (uid != null && uid != ""){
            var interval = setInterval(function () {
                processJobStatus(uid);
            }, 1000);
            intervals.set(uid, interval);
        }
    });


    function processJobStatus(uid) {
        jQuery.ajax({
            method: "GET",
            url: '${grailsApplication.config.grails.app.context}/statistic/getJobStatus?uid=' + uid,
            timeout: 500,
            success: function (data) {
                var json = JSON.parse(data);
                jobStatus = json["status"];
            },
            error: function (deXMLHttpRequest, textStatus, errorThrown) {
                jobStatus = null;
            }
        });
        if (jobStatus == 'STARTED') {
            // show progress bar
            jQuery('#progress-' + uid).removeAttr('hidden');
            jQuery.ajax({
                method: "GET",
                url: '${grailsApplication.config.grails.app.context}/statistic/getJobProgress?uid=' + uid,
                timeout: 500,
                success: function (data) {
                    var json = JSON.parse(data);
                    var max = jQuery('#progress-'+ uid + ' > .progress-bar').attr('aria-valuemax');
                    var percentNow = json["count"] / max * 100;
                    jQuery('#progress-'+ uid + ' > .progress-bar').attr('aria-valuenow', percentNow);
                    jQuery('#progress-'+ uid + ' > .progress-bar').attr('style', 'width:' + percentNow + '%');
                    jQuery('#progress-'+ uid + ' > .progress-bar').text(percentNow + '%');
                },
                error: function (deXMLHttpRequest, textStatus, errorThrown) {}
            });
        }
        else if (jobStatus == 'SUCCESS' || jobStatus == 'ERROR' || jobStatus == 'FINISHED_UNDEFINED') {
            // remove progress bar
            jQuery('#progress-' + uid).attr('hidden', 'hidden');
            // fill result table
            var table = null;
            var data = null;
            jQuery.ajax({
                method: "GET",
                url: '${grailsApplication.config.grails.app.context}/statistic/getResultsTable?uid=' + uid,
                timeout: 500,
                success: function (data) {
                    table = document.getElementById("feedbackTable-" + uid).getElementsByTagName('tbody')[0];
                    fillTable(table, data);
                    clearInterval(intervals.get(uid));
                },
                error: function (deXMLHttpRequest, textStatus, errorThrown) {
                }
            });
        }
    }


    function fillTable(tableElement, data){
        var json = JSON.parse(data);
        if (json.length > 0){
            json.forEach((entry) => {
                Object.keys(entry).forEach(function(key) {
                    var value = entry[key];
                    let row = tableElement.insertRow();
                    row.insertCell(0).appendChild(document.createTextNode(key));
                    row.insertCell(1).appendChild(document.createTextNode(value));
                });
            });
        }
    }
</script>

<div class="row">
    <g:set var="lineCounter" value="${0}"/>
    <g:set var="displayZDB" value="true"/>

    <g:if test="${redRecords != null && !(redRecords.isEmpty())}">
        <div class="col-xs-12">
            <div class="panel panel-default">
                <div class="panel-heading-red">
                    <h3 class="panel-title">${redRecords.size} <g:message code="statistic.show.records.red"/></h3>
                </div>

                <div class="statistics-data">
                    <table id="red-records" class="statistics-details">
                        <thead><tr>
                            <th>Title</th>
                            <g:if test="${displayZDB}">
                                <th>ZDB</th>
                                <th>ZDB ID</th>
                            </g:if>
                            <th>eISSN</th>
                        </tr></thead>
                        <tbody>
                        <g:set var="lineCounter" value="${0}"/>
                        <g:each in="${redRecords}" var="record">
                            <tr class="${(lineCounter % 2) == 0 ? 'even hover' : 'odd hover'}">
                                <td class="statistics-cell">
                                    <g:link action="edit" params="[resultHash: resultHash]"
                                            id="${record.value.getAt(4)}">${org.apache.commons.lang.StringUtils.isEmpty(record.value.getAt(0)) ?
                                            "<"+message(code: 'missing')+">" : record.value.getAt(0)}</g:link>
                                </td>
                                <g:if test="${displayZDB}">
                                    <td><g:if test="${record.value.getAt(1)}">
                                        <a href="${record.value.getAt(1)}" class="link-icon"></a>
                                    </g:if></td>
                                    <td class="statistics-cell"><g:if test="${record.value.getAt(2) != null}">
                                        ${record.value.getAt(2)}<br/>
                                    </g:if></td>
                                </g:if>
                                <td class="statistics-cell"><g:if test="${record.value.getAt(3) != null}">
                                    ${record.value.getAt(3)}<br/>
                                </g:if></td>
                            </tr>
                            <g:set var="lineCounter" value="${lineCounter + 1}"/>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </g:if>

    <g:if test="${displayZDB && yellowRecords != null && !(yellowRecords.isEmpty())}">
        <div class="col-xs-12">
            <div class="panel panel-default">
                <div class="panel-heading-yellow">
                    <h3 class="panel-title">${yellowRecords.size} <g:message code="statistic.show.records.yellow"/></h3>
                </div>
                <div class="statistics-data">
                    <table id="yellow-records"class="statistics-details">
                        <thead><tr>
                            <th>Title</th>
                            <th>ZDB</th>
                            <th>ZDB ID</th>
                            <th>eISSN</th>
                        </tr></thead>
                        <tbody>
                        <g:set var="lineCounter" value="${0}"/>
                        <g:each in="${yellowRecords}" var="record">
                            <tr class="${(lineCounter % 2) == 0 ? 'even hover' : 'odd hover'}">
                                <td class="statistics-cell">
                                    <g:link action="edit" params="[resultHash: resultHash]"
                                            id="${record.value.getAt(4)}">${org.apache.commons.lang.StringUtils.isEmpty(record.value.getAt(0)) ?
                                            "<"+message(code: 'missing')+">" : record.value.getAt(0)}</g:link>
                                </td>
                                <td><g:if test="${record.value.getAt(1)}">
                                    <a href="${record.value.getAt(1)}" class="link-icon"></a>
                                </g:if></td>
                                <td class="statistics-cell"><g:if test="${record.value.getAt(2) != null}">
                                    ${record.value.getAt(2)}<br/>
                                </g:if></td>
                                <td class="statistics-cell"><g:if test="${record.value.getAt(3) != null}">
                                    ${record.value.getAt(3)}<br/>
                                </g:if></td>
                            </tr>
                            <g:set var="lineCounter" value="${lineCounter + 1}"/>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </g:if>

    <g:if test="${greenRecords != null && !(greenRecords.isEmpty())}">
        <div class="col-xs-12">
            <div class="panel panel-default">
                <div class="panel-heading-green">
                    <h3 class="panel-title">${greenRecords.size} <g:message code="statistic.show.records.green"/></h3>
                </div>

                <div class="statistics-data">
                    <table id="green-records" class="statistics-details">
                        <thead><tr>
                            <th>Title</th>
                            <th>ZDB</th>
                            <th>ZDB ID</th>
                            <th>eISSN</th>
                        </tr></thead>
                        <tbody>
                        <g:set var="lineCounter" value="${0}"/>
                        <g:each in="${greenRecords}" var="record">
                            <tr class="${(lineCounter % 2) == 0 ? 'even hover' : 'odd hover'}">
                            <td class="statistics-cell">
                                <g:link action="edit" params="[resultHash: resultHash]"
                                id="${record.value.getAt(4)}">${org.apache.commons.lang.StringUtils.isEmpty(record.value.getAt(0)) ?
                                        "<"+message(code: 'missing')+">" : record.value.getAt(0)}</g:link>
                                <g:if test="${displayZDB}">
                                    <td><g:if test="${record.value.getAt(1)}">
                                        <a href="${record.value.getAt(1)}" class="link-icon"></a>
                                    </g:if></td>
                                    <td class="statistics-cell"><g:if test="${record.value.getAt(2) != null}">
                                        ${record.value.getAt(2)}<br/>
                                    </g:if></td>
                                </g:if>
                                <td class="statistics-cell"><g:if test="${record.value.getAt(3) != null}">
                                    ${record.value.getAt(3)}<br/>
                                </g:if></td>
                            </tr>
                            <g:set var="lineCounter" value="${lineCounter + 1}"/>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </g:if>

    <g:form>
        <g:hiddenField name="originHash" value="${originHash}"/>
        <g:hiddenField name="resultHash" value="${resultHash}"/>
        <div class="col-xs-12" style="margin-bottom: 20px">
            <g:if test="${grailsApplication.config.ygor.enableGokbUpload}">
                <button type="button" class="btn btn-success btn-same-width" data-toggle="modal" gokbdata="titles"
                        data-target="#credentialsModal"><g:message code="listDocuments.button.sendTitlesFile"/></button>
                <button type="button" class="btn btn-success btn-same-width" data-toggle="modal" gokbdata="package"
                        data-target="#credentialsModal"><g:message code="listDocuments.button.sendPackageFile"/></button>

                <div class="modal fade" id="credentialsModal" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal">&times;</button>
                                <h4 class="modal-title"><g:message code="listDocuments.gokb.credentials"/></h4>
                            </div>

                            <div class="modal-body">
                                <g:form>
                                    <div class="input-group">
                                        <span class="input-group-addon"><g:message
                                                code="listDocuments.gokb.username"/></span>
                                        <g:textField name="gokbUsername" size="24" class="form-control"/>
                                    </div>

                                    <div class="input-group">
                                        <span class="input-group-addon"><g:message
                                                code="listDocuments.gokb.password"/></span>
                                        <g:passwordField name="gokbPassword" size="24" class="form-control"/>
                                    </div>
                                    <br/>
                                    <div align="right">
                                        <button type="button" class="btn btn-default btn-same-width" data-dismiss="modal"><g:message
                                                code="listDocuments.button.cancel"/></button>
                                        <g:actionSubmit action="" value="${message(code: 'listDocuments.button.send')}"
                                                        class="btn btn-success btn-same-width"
                                                        name="cred-modal-btn-send" data-toggle="tooltip"
                                                        data-placement="top"
                                                        title="JavaScript ${message(code: 'technical.required')}."/>
                                    </div>
                                </g:form>
                            </div>
                        </div>
                    </div>
                </div>
                <br/>
                <br/>
                <script>
                    $('#credentialsModal').on('show.bs.modal', function (event) {
                        var uri = $(event.relatedTarget)[0].getAttribute("gokbdata");
                        if (uri.localeCompare('package') == 0) {
                            $(this).find('.modal-body .btn.btn-success').attr('name', '_action_sendPackageFile');
                        } else if (uri.localeCompare('titles') == 0) {
                            $(this).find('.modal-body .btn.btn-success').attr('name', '_action_sendTitlesFile');
                        }
                    });

                </script>
            </g:if>
            <g:else>
                <g:actionSubmit action="sendPackageFile"
                                value="${message(code: 'listDocuments.button.sendPackageFile')}"
                                class="btn btn-success disabled btn-same-width"
                                data-toggle="tooltip" data-placement="top"
                                title="Deaktiviert: ${grailsApplication.config.gokbApi.xrPackageUri}"
                                disabled="disabled"/>
                <g:actionSubmit action="sendTitlesFile" value="${message(code: 'listDocuments.button.sendTitlesFile')}"
                                class="btn btn-success disabled btn-same-width"
                                data-toggle="tooltip" data-placement="top"
                                title="Deaktiviert: ${grailsApplication.config.gokbApi.xrTitleUri}"
                                disabled="disabled"/>
                <br/>
                <br/>
            </g:else>
            <g:actionSubmit action="downloadTitlesFile"
                            value="${message(code: 'listDocuments.button.downloadTitlesFile')}"
                            class="btn btn-default btn-same-width"/>
            <g:actionSubmit action="downloadPackageFile"
                            value="${message(code: 'listDocuments.button.downloadPackageFile')}"
                            class="btn btn-default btn-same-width"/>
            <g:actionSubmit action="downloadRawFile" value="${message(code: 'listDocuments.button.downloadRawFile')}"
                            class="btn btn-default btn-same-width"/>
            <br/>
            <br/>
            <g:actionSubmit action="correctFile" value="${message(code: 'listDocuments.button.correctFile')}"
                            class="btn btn-warning btn-same-width"/>
            <g:actionSubmit action="deleteFile" value="${message(code: 'listDocuments.button.deleteFile')}"
                            class="btn btn-danger btn-same-width"/>
        </div>
        <script>
            var bwidth=0
            $(".btn-same-width").each(function(i,v){
                if($(v).width()>bwidth) bwidth=$(v).width();
            });
            $(".btn-same-width").width(bwidth);
        </script>
    </g:form>

    <div class="col-xs-12">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title"><g:message code="statistic.show.meta"/></h3>
            </div>

            <div id="statistics-meta" class="panel-body">
                <table>
                    <tr class="${(lineCounter % 2) == 0 ? 'active' : 'info active'}">
                        <td class="statistics-cell"><strong><g:message code="statistic.show.main.filename"/></strong>
                        </td>
                        <td class="statistics-cell">${filename}</td>
                    </tr>
                    <tr class="${(lineCounter % 2) == 0 ? 'active' : 'info active'}">
                        <td class="statistics-cell"><strong><g:message code="statistic.show.main.creation"/></strong>
                        </td>
                        <td class="statistics-cell">${date}</td>
                    </tr>
                    <tr class="${(lineCounter % 2) == 0 ? 'active' : 'info active'}">
                        <td class="statistics-cell"><strong><g:message code="statistic.show.main.ygorversion"/></strong>
                        </td>
                        <td class="statistics-cell">${ygorVersion}<br/></td>
                    </tr>
                    <tr class="${(lineCounter % 2) == 0 ? 'active' : 'info active'}">
                        <td class="statistics-cell"><strong><g:message code="statistic.show.main.hash"/></strong></td>
                        <td class="statistics-cell">${resultHash}</td>
                    </tr>
                </table>
            </div>
        </div>
    </div>
</div>
<asset:javascript src="jquery.dataTables.js"/>
<asset:stylesheet src="jquery.dataTables.css"/>

<script>
    $(document).ready(function () {
        $('#red-records').DataTable( {
            ajax: {
                url: "/ygor/statistic/records?resultHash=${resultHash}&colour=RED"
            },
            serverSide: true, stateSave: true, order: [1, "asc"], length: 10,
            draw: 1,
            language: {
                lengthMenu: "${message(code: 'datatables.lengthMenu')}",
                zeroRecords: "",
                info: "${message(code: 'datatables.pageOfPages')}",
                infoEmpty: "${message(code: 'datatables.noRecordsAvailable')}",
                infoFiltered: "${message(code: 'datatables.filteredFromMax')}",
                search: "${message(code: 'datatables.search')}",
                paginate: {
                    first: "${message(code: 'datatables.first')}",
                    last: "${message(code: 'datatables.last')}",
                    next: "${message(code: 'datatables.next')}",
                    previous: "${message(code: 'datatables.previous')}"
                }
            },
            "columns": [
                {
                    "type": "string",
                    "width": "67%"
                },
                {
                    "type": "html",
                    "width": "8%"
                },
                {
                    "type": "string",
                    "width": "11%"
                },
                {
                    "type": "string",
                    "width": "14%"
                }
            ]
        });
        $('#yellow-records').DataTable( {
            ajax: {
                url: "/ygor/statistic/records?resultHash=${resultHash}&colour=YELLOW"
            },
            serverSide: true, stateSave: true, order: [1, "asc"], length: 10,
            draw: 1,
            language: {
                lengthMenu: "${message(code: 'datatables.lengthMenu')}",
                zeroRecords: "",
                info: "${message(code: 'datatables.pageOfPages')}",
                infoEmpty: "${message(code: 'datatables.noRecordsAvailable')}",
                infoFiltered: "${message(code: 'datatables.filteredFromMax')}",
                search: "${message(code: 'datatables.search')}",
                paginate: {
                    first: "${message(code: 'datatables.first')}",
                    last: "${message(code: 'datatables.last')}",
                    next: "${message(code: 'datatables.next')}",
                    previous: "${message(code: 'datatables.previous')}"
                }
            },
            "columns": [
                {
                    "type": "string",
                    "width": "67%"
                },
                {
                    "type": "html",
                    "width": "8%"
                },
                {
                    "type": "string",
                    "width": "11%"
                },
                {
                    "type": "string",
                    "width": "14%"
                }
            ]
        });
        $('#green-records').dataTable( {
            ajax: {
                url: "/ygor/statistic/records?resultHash=${resultHash}&colour=GREEN"
            },
            serverSide: true, stateSave: true, order: [1, "asc"], length: 10,
            draw: 1,
            language: {
                lengthMenu: "${message(code: 'datatables.lengthMenu')}",
                zeroRecords: "",
                info: "${message(code: 'datatables.pageOfPages')}",
                infoEmpty: "${message(code: 'datatables.noRecordsAvailable')}",
                infoFiltered: "${message(code: 'datatables.filteredFromMax')}",
                search: "${message(code: 'datatables.search')}",
                paginate: {
                    first: "${message(code: 'datatables.first')}",
                    last: "${message(code: 'datatables.last')}",
                    next: "${message(code: 'datatables.next')}",
                    previous: "${message(code: 'datatables.previous')}"
                }
            },
            "columns": [
                {
                    "type": "string",
                    "width": "67%"
                },
                {
                    "type": "html",
                    "width": "8%"
                },
                {
                    "type": "string",
                    "width": "11%"
                },
                {
                    "type": "string",
                    "width": "14%"
                }
            ]
        });
    });
</script>
