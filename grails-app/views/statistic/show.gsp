<%@ page import="ygor.Enrichment" %>

<meta name="layout" content="enrichment">

<p class="lead">${packageName}</p>

<g:if test="${runningJobIds != null || finishedJobIds != null}">
    <g:each in="${runningJobIds + finishedJobIds}" var="jobId">
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
                <div id="progress-${jobId}" class="progress" hidden="hidden">
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
</g:if>

<script>
    var rkm = getResponseKeyMap();
    var rvm = getResponseValueMap();

    // format buttons width
    var responseButtons = $(".response-button");
    var responseRemovers = $(".response-remove");
    if (responseButtons.length > 0){
        var divWidth = responseButtons[0].parentNode.parentElement.clientWidth;
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


    var runningJobIdsAsList = "${runningJobIds}".replace("[", "").replace("]", "").split(", ");
    var intervals = new Map();

    $.each(runningJobIdsAsList, function (i, uid) {
        if (uid != null && uid != ""){
            var interval = setInterval(function () {
                processJobStatus(uid);
                updateJobStatus(uid);
            }, 1000);
            intervals.set(uid, interval);
        }
    });


    function processJobStatus(uid) {
        jQuery.ajax({
            method: "GET",
            url: '${grailsApplication.config.grails.app.context}/statistic/getJobStatus?uid=' + uid,
            timeout: 60000,
            success: function (data) {
                if (JSON.parse(data)["status"] == 'STARTED') {
                    showProgressBar(uid);
                }
                else {
                    let interval = intervals.get(uid);
                    if (interval != null) {
                        clearInterval(interval);
                        intervals.delete(uid);
                    }
                    showFinishedJobResult(uid);
                }
            },
            error: function (deXMLHttpRequest, textStatus, errorThrown) {}
        });
    }


    function updateJobStatus(uid) {
        jQuery.ajax({
            method: "GET",
            url: '${grailsApplication.config.grails.app.context}/statistic/refreshJobStatus?uid=' + uid,
            timeout: 60000,
            success: function (data) {},
            error: function (deXMLHttpRequest, textStatus, errorThrown) {}
        });
    }


    function showProgressBar(uid){
        jQuery('#progress-' + uid).removeAttr('hidden');
        jQuery.ajax({
            method: "GET",
            url: '${grailsApplication.config.grails.app.context}/statistic/getJobProgress?uid=' + uid,
            timeout: 60000,
            success: function (data) {
                var json = JSON.parse(data);
                var max = jQuery('#progress-' + uid + ' > .progress-bar').attr('aria-valuemax');
                var percentNow = json["count"] / max * 100;
                jQuery('#progress-' + uid + ' > .progress-bar').attr('aria-valuenow', percentNow);
                jQuery('#progress-' + uid + ' > .progress-bar').attr('style', 'width:' + percentNow + '%');
                jQuery('#progress-' + uid + ' > .progress-bar').text(percentNow + '%');
            },
            error: function (deXMLHttpRequest, textStatus, errorThrown) {
                console.error("Could not get job info for job " + uid);
            }
        });
    }

    // show former upload results
    var finishedJobIdsAsList = "${finishedJobIds}".replace("[", "").replace("]", "").split(", ");
    $.each(finishedJobIdsAsList, function (i, uid) {
        if (uid != null && uid != ""){
            showFinishedJobResult(uid);
        }
    });

    function showFinishedJobResult(uid) {
        let jobStatus;
        jQuery.ajax({
            method: "GET",
            url: '${grailsApplication.config.grails.app.context}/statistic/getJobStatus?uid=' + uid,
            timeout: 60000,
            success: function (data) {
                jobStatus = JSON.parse(data)["status"];
                if (jobStatus == 'FINISHED_UNDEFINED' || jobStatus == 'SUCCESS' || jobStatus == 'ERROR') {
                    // remove progress bar
                    jQuery('#progress-' + uid).attr('hidden', 'hidden');
                    // fill result table
                    jQuery.ajax({
                        method: "GET",
                        url: '${grailsApplication.config.grails.app.context}/statistic/getResultsTable?uid=' + uid,
                        timeout: 60000,
                        success: function (data) {
                            let table = document.getElementById("feedbackTable-" + uid).getElementsByTagName('tbody')[0];
                            fillTable(table, data);
                        },
                        error: function (deXMLHttpRequest, textStatus, errorThrown) {
                            console.error("Could not get results for job "+uid);
                        }
                    });
                }
            },
            error: function (deXMLHttpRequest, textStatus, errorThrown) {
                jobStatus = null;
            }
        });
    }

    function fillTable(tableElement, data){
        let json = JSON.parse(data);
        if (json.length > 0){
            json.forEach((entry) => {
                for (let key in entry){
                    appendRow(tableElement, key, entry[key]);
                }
            });
        }
    }

    function appendRow(tableElement, key, value) {
        let row = tableElement.insertRow();
        let codedKey = rkm[key];
        if (codedKey != undefined){
            key = codedKey
        }
        let codedValue = rvm[value];
        if (codedValue != undefined){
            value = codedValue
        }
        row.insertCell(0).appendChild(document.createTextNode(key));
        row.insertCell(1).appendChild(document.createTextNode(value));
    }

    function getResponseKeyMap() {
        const rkm = new Object();
        rkm["listDocuments.gokb.response.type"] = "${g.message(code:"listDocuments.gokb.response.type")}";
        rkm["listDocuments.gokb.response.ok"] = "${g.message(code:"listDocuments.gokb.response.ok")}";
        rkm["listDocuments.gokb.response.error"] = "${g.message(code:"listDocuments.gokb.response.error")}";
        rkm["listDocuments.gokb.response.message"] = "${g.message(code:"listDocuments.gokb.response.message")}";
        rkm["listDocuments.gokb.response.status"] = "${g.message(code:"listDocuments.gokb.response.status")}";
        return rkm;
    }

    function getResponseValueMap() {
        const rvm = new Object();
        rvm["listDocuments.gokb.response.titles"] = "${g.message(code:"listDocuments.gokb.response.titles")}";
        rvm["listDocuments.gokb.response.package"] = "${g.message(code:"listDocuments.gokb.response.package")}";
        return rvm;
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
                        data-target="#credentialsModal" onclick="assignSendTargetToModal()">
                    <g:message code="listDocuments.button.sendTitlesFile"/>
                </button>
                <button type="button" class="btn btn-success btn-same-width" data-toggle="modal" gokbdata="package"
                        data-target="#credentialsModal" onclick="assignSendTargetToModal()">
                    <g:message code="listDocuments.button.sendPackageFile"/>
                </button>

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
                                                        id="cred-modal-btn-send" data-toggle="tooltip"
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
                    function assignSendTargetToModal(){
                        $('#credentialsModal').on('show.bs.modal', function (event) {
                            var uri = $(event.relatedTarget)[0].getAttribute("gokbdata");
                            if (uri.localeCompare('package') == 0) {
                                $(this).find('#cred-modal-btn-send').attr('name', '_action_sendPackageFile');
                            }
                            else if (uri.localeCompare('titles') == 0) {
                                $(this).find('#cred-modal-btn-send').attr('name', '_action_sendTitlesFile');
                            }
                        });
                    }
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
