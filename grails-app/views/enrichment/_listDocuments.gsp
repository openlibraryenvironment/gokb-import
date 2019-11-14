<!-- _listDocuments.gsp -->

<%@ page
        import="ygor.Enrichment"
        import="ygor.GokbService"
        import="de.hbznrw.ygor.export.structure.TitleStruct"
        import="de.hbznrw.ygor.export.structure.PackageStruct"
        import="de.hbznrw.ygor.bridges.*"
        import="de.hbznrw.ygor.connectors.KbartConnector"
%>

<g:each in="${enrichments}" var="e">
    <g:form controller="enrichment" action="process">
        <g:hiddenField name="originHash" value="${e.value.originHash}"/>
        <g:hiddenField name="resultHash" value="${e.value.resultHash}"/>
        <g:hiddenField name="dataTyp" value="${session.lastUpdate?.dataTyp}"/>

        <div class="row" xmlns="http://www.w3.org/1999/html">
            <div class="col-xs-12">

                <br /><br />

                <ul class="list-group content-list">
                    <li class="list-group-item">

                        <div class="input-group">
                            <span class="input-group-addon">Datei</span>
                            <span class="form-control" title="${e.value.originHash}">
        ${e.value.originName}

        <span><em>
            <g:if test="${e.value.status == Enrichment.ProcessingState.PREPARE}">
                &rarr; <g:message code="listDocuments.state.prepare"/>
            </g:if>
            <g:if test="${e.value.status == Enrichment.ProcessingState.UNTOUCHED}">
                &rarr; <g:message code="listDocuments.state.untouched"/>
            </g:if>
            <g:if test="${e.value.status == Enrichment.ProcessingState.WORKING}">
                &rarr; <g:message code="listDocuments.state.working"/>
            </g:if>
            <g:if test="${e.value.status == Enrichment.ProcessingState.ERROR}">
                &rarr; <g:message code="listDocuments.state.error"/>
            </g:if>
            <g:if test="${e.value.status == Enrichment.ProcessingState.FINISHED}">
                &rarr; <g:message code="listDocuments.state.finished"/>
            </g:if>
        </em></span>

        </span>
    </div>

        <br/>

        <div class="input-group">
            <span class="input-group-addon"><g:message code="listDocuments.key.mediatype"/></span>
            <span class="form-control">
                <g:if test="${session.lastUpdate?.dataTyp == "journals"}">
                    <g:message code="listDocuments.mediatype.journal"/>
                </g:if>

                <g:if test="${session.lastUpdate?.dataTyp == "database"}">
                    <g:message code="listDocuments.mediatype.database"/>
                </g:if>

                <g:if test="${session.lastUpdate?.dataTyp == "ebooks"}">
                    <g:message code="listDocuments.mediatype.ebook"/>
                </g:if>
            </span>
        </div>

        <br/>

        <div id="progress-${e.value.resultHash}" class="progress">
            <g:if test="${e.value.status == Enrichment.ProcessingState.FINISHED}">
                <div class="progress-bar" role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100"
                     style="width:100%;">100%</div>
            </g:if>
            <g:else>
                <div class="progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="0"
                     aria-valuemin="0" aria-valuemax="100" style="width:0%;">0%</div>
            </g:else>
        </div>



        <g:if test="${e.value.status == Enrichment.ProcessingState.PREPARE}">

            <div class="input-group">
                <span class="input-group-addon"><g:message code="listDocuments.key.title"/></span>
                <g:if test="${session.lastUpdate?.parameterMap?.pkgTitle}">
                    <g:textField name="pkgTitle" size="48" value="${session.lastUpdate.parameterMap.pkgTitle[0]}"
                                 class="form-control"/>
                </g:if>
                <g:else>
                    <g:textField name="pkgTitle" size="48" placeholder="Munchhausen Verlag: Science Journals: hbz: 1999"
                                 class="form-control"/>
                </g:else>
            </div>

            <br/>

            <div class="input-group">
                <span class="input-group-addon"><g:message code="listDocuments.key.isil"/></span>
                <g:if test="${session.lastUpdate?.parameterMap?.pkgIsil}">
                    <g:textField name="pkgIsil" size="48" value="${session.lastUpdate.parameterMap.pkgIsil[0]}"
                                 class="form-control"/>
                </g:if>
                <g:else>
                    <g:textField name="pkgIsil" size="48" value="" class="form-control"/>
                </g:else>
            </div>

            <br/>

            <div class="input-group">
                <span class="input-group-addon"><em>GOKb</em> <g:message code="listDocuments.key.platform"/></span>
                <select name="pkgNominalPlatform" id="pkgNominalPlatform"></select>
            </div>

            <br/>

            <div class="input-group">
                <span class="input-group-addon"><em>GOKb</em> <g:message code="listDocuments.key.provider"/></span>
                <select name="pkgNominalProvider" id="pkgNominalProvider"></select>
            </div>

            <br/>

            <script>
                var platform = null;
                if (${false != session.lastUpdate?.parameterMap?.pkgNominalPlatform?.getAt(0)}) {
                    platform = "${session.lastUpdate?.parameterMap?.pkgNominalPlatform?.getAt(0)}";
                }
                var provider = null;
                if (${false != session.lastUpdate?.parameterMap?.pkgNominalProvider?.getAt(0)}) {
                    provider = "${session.lastUpdate?.parameterMap?.pkgNominalProvider?.getAt(0)}";
                }
                $(document).ready(function () {
                    $('#pkgNominalPlatform').select2({
                        allowClear: true,
                        placeholder: '${message(code:"listDocuments.js.placeholder.platform")}',
                        debug: true,
                        templateSelection: function (data) {
                            // Add custom attributes to the <option> tag for the selected option
                            $(data.element).attr('value', data.findFilter);
                            return data.text;
                        },
                        ajax: {
                            url: '/ygor/enrichment/suggestPlatform',
                            data: function (params) {
                                var query = {
                                    q: params.term
                                }
                                return query;
                            },
                            processResults: function (data) {
                                return {
                                    results: data.items
                                }
                            }
                        }
                    });
                    $('#pkgNominalPlatform').append($('<option></option>').attr('value', platform).text(platform));
                    $('#pkgNominalProvider').select2({
                        allowClear: true,
                        placeholder: '${message(code:"listDocuments.js.placeholder.provider")}',
                        debug: true,
                        ajax: {
                            url: '/ygor/enrichment/suggestProvider',
                            data: function (params) {
                                var query = {
                                    q: params.term
                                }
                                return query;
                            },
                            processResults: function (data) {
                                return {
                                    results: data.items
                                }
                            }
                        }
                    });
                    $('#pkgNominalProvider').append($('<option></option>').attr('value', provider).text(provider));
                });
            </script>

            <g:if test="${namespaces?.size() > 0}">
                <div class="input-group">
                    <span class="input-group-addon"><g:message code="listDocuments.key.namespace"/></span>
                    <g:select name="namespace_title_id" id="namespace_title_id" from="${namespaces}" optionKey="text"
                              optionValue="id"
                              noSelection="${['': message(code: 'listDocuments.js.placeholder.namespace')]}"
                              class="form-control"></g:select>
                </div>
                <br>
            </g:if>

            <div class="input-group">
                <span class="input-group-addon"><em>GOKb</em> Curatory Group 1</span>
                <g:if test="${session.lastUpdate?.parameterMap?.pkgCuratoryGroup1}">
                    <g:textField name="pkgCuratoryGroup1" size="24"
                                 value="${session.lastUpdate.parameterMap.pkgCuratoryGroup1[0]}" class="form-control"/>
                </g:if>
                <g:else>
                    <g:textField name="pkgCuratoryGroup1" size="24" value="LAS:eR" class="form-control"/>
                </g:else>
            </div>

            <div class="input-group">
                <span class="input-group-addon"><em>GOKb</em> Curatory Group 2</span>
                <g:if test="${session.lastUpdate?.parameterMap?.pkgCuratoryGroup2}">
                    <g:textField name="pkgCuratoryGroup2" size="24"
                                 value="${session.lastUpdate.parameterMap.pkgCuratoryGroup2[0]}" class="form-control"/>
                </g:if>
                <g:else>
                    <g:textField name="pkgCuratoryGroup2" size="24" value="hbz" class="form-control"/>
                </g:else>
            </div>

            <!--
            <div class="row">
                <div class="col-xs-6 col-xs-offset-6">
                    GOKb Source Name:
                    <br /><br />
            <g:textField name="pkgSourceName" size="24" value="LAS:eR"/>
            &nbsp;
            <g:textField name="pkgSourceUrl" size="24" value="hbz"/>
            <br />
            <br />
        </div>
    </div> .row -->

        </g:if>


        <g:if test="${e.value.status == Enrichment.ProcessingState.UNTOUCHED}">

            ${e.value.dataContainer.pkg.packageHeader.v.nominalPlatform.name ? '' : raw('<div class="alert alert-danger" role="alert">') +
                    message(code: 'listDocuments.js.message.noplatformname') +
                    raw('</div>')}

            <div class="input-group custom-control">
                <span class="input-group-addon"><em>GOKb</em> <g:message code="listDocuments.key.platformname"/></span>
                <span class="form-control">${e.value.dataContainer.pkg.packageHeader.v.nominalPlatform.name}</span>
            </div>

            <br/>

            ${e.value.dataContainer.pkg.packageHeader.v.nominalPlatform.url ? '' : raw('<div class="alert alert-danger" role="alert">') +
                    message(code: 'listDocuments.js.message.noplatformurl') +
                    raw('</div>')}
            <div class="input-group">
                <span class="input-group-addon"><em>GOKb</em>  <g:message code="listDocuments.key.platformurl"/></span>
                <span class="form-control">${e.value.dataContainer.pkg.packageHeader.v.nominalPlatform.url}</span>
            </div>

            <br/>

            ${e.value.dataContainer.pkg.packageHeader.v.nominalProvider.v ? '' : raw('<div class="alert alert-danger" role="alert">') +
                    message(code: 'listDocuments.js.message.noprovider') +
                    raw('</div>')}

            <div class="input-group">
                <span class="input-group-addon"><em>GOKb</em> <g:message code="listDocuments.key.provider"/></span>
                <span class="form-control">${e.value.dataContainer.pkg.packageHeader.v.nominalProvider.v}</span>
            </div>

            <br/>

            <g:message code="listDocuments.enrichment.apis"/>
            <br/><br/>

            <div class="input-group">
                <span class="input-group-addon"><g:message code="listDocuments.key.sources"/></span>
                <span class="form-control">
                    <div class="checkbox">
                        <label>
                            <g:checkBox name="processOption" required="true" checked="true"
                                        value="${KbartBridge.IDENTIFIER}"/>
                            KBART <code><g:message code="listDocuments.enrichment.file"/></code>
                        </label>
                    &nbsp;
                        <g:if test="${session.lastUpdate?.dataTyp == "journals" || session.lastUpdate?.dataTyp == "database"}">
                            <label>
                                <g:checkBox name="processOption" required="true" checked="true"
                                            value="${ZdbBridge.IDENTIFIER}"/>
                                ZDB <em>@GBV</em> <code>API</code>
                            </label>
                            &nbsp;
                            <label>
                                <g:if test="${session.lastUpdate?.pmOptions == null || session.lastUpdate?.pmOptions?.contains(EzbBridge.IDENTIFIER)}">
                                    <g:checkBox name="processOption" checked="true" value="${EzbBridge.IDENTIFIER}"/>
                                </g:if>
                                <g:else>
                                    <g:checkBox name="processOption" checked="false" value="${EzbBridge.IDENTIFIER}"/>
                                </g:else>
                                EZB <code>API</code>
                            </label>
                            <!--
                            &nbsp;
                            <label>
                            <g:checkBox name="processOption" checked="false" disabled="true"
                                        value="${ZdbBridge.IDENTIFIER}"/>
                            ZDB <code>API</code>
                        </label>
                        -->
                        </g:if>
                    </div>
                </span>
            </div>

            <br/>
            <g:message code="listDocuments.enrichment.entry"/>
            <br/><br/>

            <div class="input-group">
                <span class="input-group-addon"><g:message code="listDocuments.key.key"/></span>
                <span class="form-control">
                    <div class="radio">
                        <g:message code="listDocuments.enrichment.sequence"/>
                    </div>
                </span>
            </div>

        </g:if>

        </li>
    </ul>

        <ul class="list-group content-list">
        <li class="list-group-item">

            <g:if test="${e.value.status == Enrichment.ProcessingState.UNTOUCHED}">
            <g:actionSubmit action="deleteFile" value="${message(code:'listDocuments.button.deletefile')}" class="btn btn-danger"/>
            <g:actionSubmit action="correctFile" value="${message(code:'listDocuments.button.correctfile')}" class="btn btn-warning"/>
            <g:actionSubmit action="processFile" value="${message(code:'listDocuments.button.processfile')}" class="btn btn-success"/>
            </g:if>
            <g:if test="${e.value.status == Enrichment.ProcessingState.PREPARE}">
            <g:actionSubmit action="deleteFile" value="${message(code:'listDocuments.button.deletefile')}" class="btn btn-danger"/>
            <g:actionSubmit action="correctFile" value="${message(code:'listDocuments.button.correctfile')}" class="btn btn-warning"/>
            <g:actionSubmit action="prepareFile" value="${message(code:'listDocuments.button.processfile')}" class="btn btn-success"/>
            </g:if>
            <g:if test="${e.value.status == Enrichment.ProcessingState.WORKING}">
            <g:actionSubmit action="stopProcessingFile" value="${message(code:'listDocuments.button.stopprocessingfile')}" class="btn btn-danger"/>
            </g:if>
            <g:if test="${e.value.status == Enrichment.ProcessingState.ERROR}">
            <g:actionSubmit action="deleteFile" value="${message(code:'listDocuments.button.deletefile')}" class="btn btn-danger"/>
            <g:actionSubmit action="correctFile" value="${message(code:'listDocuments.button.correctfile')}" class="btn btn-warning"/>
            </g:if>
            <g:if test="${e.value.status == Enrichment.ProcessingState.FINISHED}">

                <g:link controller="statistic" action="show" params="[resultHash: e?.value?.resultHash]" target="_blank"
                        class="btn btn-info"><g:message code="listDocuments.button.showstatistics"/></g:link>

                <g:actionSubmit action="downloadTitlesFile"
                                value="${message(code: 'listDocuments.button.downloadtitlesfile')}"
                                class="btn btn-info"/>
                <g:actionSubmit action="downloadPackageFile"
                                value="${message(code: 'listDocuments.button.downloadpackagefile')}"
                                class="btn btn-info"/>

                <g:if test="${grailsApplication.config.ygor.enableGokbUpload}">
                    <button type="button" class="btn btn-success" data-toggle="modal" gokbdata="titles"
                            data-target="#credentialsModal"><g:message code="listDocuments.button.titles"/></button>
                    <button type="button" class="btn btn-success" data-toggle="modal" gokbdata="package"
                            data-target="#credentialsModal"><g:message code="listDocuments.button.package"/></button>

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

                                        <div align="right">
                                            <button type="button" class="btn btn-default"
                                                    data-dismiss="modal"><g:message
                                                    code="listDocuments.button.cancel"/></button>
                                            <g:actionSubmit action=""
                                                            value="${message(code: 'listDocuments.button.send')}"
                                                            class="btn btn-success"
                                                            name="cred-modal-btn-send" data-toggle="tooltip"
                                                            data-placement="top"
                                                            title="JavaScript ${message(code: 'technical.required')}."/>
                                        </div>
                                    </g:form>
                                </div>
                            </div>
                        </div>
                    </div>
                    <script>
                        $('#credentialsModal').on('show.bs.modal', function (event) {
                            var uri = $(event.relatedTarget)[0].getAttribute("gokbdata");
                            if (uri.localeCompare('package') == 0) {
                                $(this).find('.modal-body .btn.btn-success').attr('name', '_action_sendPackageFile');
                            } else if (uri.localeCompare('titles') == 0) {
                                $(this).find('.modal-body .btn.btn-success').attr('name', '_action_sendTitlesFile');
                            }
                        })
                    </script>
                </g:if>
                <g:else>
                    <g:actionSubmit action="sendPackageFile"
                                    value="${message(code: 'listDocuments.button.sendPackageFile')}"
                                    class="btn btn-success disabled"
                                    data-toggle="tooltip" data-placement="top"
                                    title="Deaktiviert: ${grailsApplication.config.gokbApi.xrPackageUri}"
                                    disabled="disabled"/>
                    <g:actionSubmit action="sendTitlesFile"
                                    value="${message(code: 'listDocuments.button.sendTitlesFile')}"
                                    class="btn btn-success disabled"
                                    data-toggle="tooltip" data-placement="top"
                                    title="Deaktiviert: ${grailsApplication.config.gokbApi.xrTitleUri}"
                                    disabled="disabled"/>
                </g:else>

                <g:if test="${grailsApplication.config.ygor.enableDebugDownload}">
                    </li>
                    <li class="list-group-item">
                    <g:actionSubmit action="downloadRawFile"
                                    value="${message(code: 'listDocuments.button.downloadRawFile')}" class="btn"/>
                </g:if>

                </li>
                <li class="list-group-item">
                <g:actionSubmit action="deleteFile" value="${message(code: 'listDocuments.button.deletefile')}"
                                class="btn btn-danger"/>
                <g:actionSubmit action="correctFile" value="${message(code: 'listDocuments.button.correctfile')}"
                                class="btn btn-warning"/>

            </g:if>

        </li>
        </ul>

        <br/>

        <script>
            $(function () {
                $('[data-toggle="tooltip"]').tooltip()
            })
        </script>

        <g:if test="${e.value.status == Enrichment.ProcessingState.WORKING}">
            <script>
                $(function () {
                    var ygorDocumentStatus${e.value.resultHash} = function () {
                        jQuery.ajax({
                            type: 'GET',
                            url: '${grailsApplication.config.grails.app.context}/enrichment/ajaxGetStatus',
                            data: 'originHash=${e.value.originHash}',
                            data: 'resultHash=${e.value.resultHash}',
                            success: function (data, textStatus) {

                                data = jQuery.parseJSON(data)
                                console.log("OH: ${e.value.originHash}");
                                console.log("RH: ${e.value.resultHash}");
                                var status = data.status;
                                var progress = data.progress;

                                jQuery('#progress-${e.value.resultHash} > .progress-bar').attr('aria-valuenow', progress);
                                jQuery('#progress-${e.value.resultHash} > .progress-bar').attr('style', 'width:' + progress + '%');
                                jQuery('#progress-${e.value.resultHash} > .progress-bar').text(progress + '%');

                                if (status == 'FINISHED') {
                                    window.location = '${grailsApplication.config.grails.app.context}/enrichment/process';
                                }
                                if (status == 'ERROR') {
                                    window.location = '${grailsApplication.config.grails.app.context}/enrichment/process';
                                }

                            },
                            error: function (XMLHttpRequest, textStatus, errorThrown) {
                                clearInterval(ygorDocumentStatus${e.value.resultHash});
                            }
                        });
                    }

                    var ygorInterval${e.value.resultHash} = setInterval(ygorDocumentStatus${e.value.resultHash}, 1500);
                })
            </script>
        </g:if>
    </g:form>
</g:each>
