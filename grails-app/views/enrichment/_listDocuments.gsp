<!-- _listDocuments.gsp -->

<%@ page
        import="ygor.Enrichment"
        import="ygor.GokbService"
        import="de.hbznrw.ygor.export.structure.TitleStruct"
        import="de.hbznrw.ygor.readers.*"
%>

<g:form controller="enrichment" action="process">
    <g:hiddenField name="originHash" value="${enrichment?.originHash}" />
    <g:hiddenField name="resultHash" value="${enrichment?.resultHash}" />
    <g:if test="${enrichment?.status != null}">
        <div class="row" xmlns="http://www.w3.org/1999/html">
            <div class="col-xs-12">
                <br /><br />
                <ul class="list-group content-list">
                    <li class="list-group-item">
                        <div class="input-group">
                            <span class="input-group-addon">Datei</span>
                            <span class="form-control" title="${enrichment.originHash}">
                                ${enrichment.originName}
                                <span><em>
                                    <g:if test="${enrichment.status == Enrichment.ProcessingState.PREPARE_1 || enrichment.status == Enrichment.ProcessingState.PREPARE_2}">
                                        &rarr; <g:message code="listDocuments.state.prepare" />
                                    </g:if>
                                    <g:if test="${enrichment.status == Enrichment.ProcessingState.WORKING}">
                                        &rarr; <g:message code="listDocuments.state.working" />
                                    </g:if>
                                    <g:if test="${enrichment.status == Enrichment.ProcessingState.ERROR}">
                                        &rarr; <g:message code="listDocuments.state.error" />
                                    </g:if>
                                    <g:if test="${enrichment.status == Enrichment.ProcessingState.FINISHED}">
                                        &rarr; <g:message code="listDocuments.state.finished" />
                                    </g:if>
                                </em></span>
                            </span>
                        </div>
                        <br />
                        <div id="progress-${enrichment.resultHash}" class="progress">
                            <g:if test="${enrichment.status == Enrichment.ProcessingState.FINISHED}">
                                <div class="progress-bar" role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width:100%;">100%</div>
                            </g:if>
                            <g:else>
                                <div class="progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width:0%;">0%</div>
                            </g:else>
                        </div>
                        <g:if test="${enrichment.status == Enrichment.ProcessingState.PREPARE_1}">
                            <div class="input-group">
                                <span class="input-group-addon"><g:message code="listDocuments.key.title" /></span>
                                <select class="dynamic-options form-control" name="pkgTitle" id="pkgTitle">
                                    <option></option>
                                </select>
                            </div>
                            <span class="checkbox">
                                <label>
                                    <g:if test="${session.lastUpdate != null && session.lastUpdate?.addOnly == true}">
                                        <g:checkBox name="addOnly" checked="true" value="${addOnly}"/>
                                    </g:if>
                                    <g:else>
                                        <g:checkBox name="addOnly" checked="false" value="${addOnly}"/>
                                    </g:else>
                                    <g:message code="uploadFile.addOnly" />
                                </label>
                                <span class="info-icon">
                                    <div class="info-icon-text">${message(code: 'uploadFile.addOnly.tooltip')}</div>
                                </span>
                            </span>
                            <div class="input-group">
                                <span class="input-group-addon"><g:message code="listDocuments.key.isil" /></span>
                                <g:if test="${session.lastUpdate?.parameterMap?.pkgIsil}">
                                    <g:textField name="pkgIsil" size="48" value="${session.lastUpdate.parameterMap.pkgIsil[0]}" class="form-control" />
                                </g:if>
                                <g:else>
                                    <g:textField name="pkgIsil" size="48" value="" class="form-control" />
                                </g:else>
                            </div>
                            <br />
                            <div class="input-group">
                                <span class="input-group-addon"><g:message code="listDocuments.key.pkgid" /></span>
                                <g:select
                                        name="pkgIdNamespace" id="pkgIdNamespace" from="${pkg_namespaces}" optionKey="id" optionValue="text" class="form-control">
                                </g:select>
                                <g:if test="${session.lastUpdate?.parameterMap?.pkgIdNamespace}">
                                    <script>
                                        $('#pkgIdNamespace').val('${session.lastUpdate?.parameterMap?.pkgIdNamespace[0]}').select();
                                    </script>
                                </g:if>
                                <g:if test="${session.lastUpdate?.parameterMap?.pkgId}">
                                    <g:textField name="pkgId" size="48" value="${session.lastUpdate.parameterMap.pkgId[0]}" class="form-control" />
                                </g:if>
                                <g:else>
                                    <g:textField name="pkgId" size="48" value="" class="form-control" />
                                </g:else>
                            </div>
                            <br />
                            <div class="input-group">
                                <span class="input-group-addon"><em>GOKb</em> <g:message code="listDocuments.key.platform" /></span>
                                <select name="pkgNominalPlatform" id="pkgNominalPlatform" required></select>
                            </div>
                            <br />
                            <div class="input-group">
                                <span class="input-group-addon"><em>GOKb</em> <g:message code="listDocuments.key.provider" /></span>
                                <select name="pkgNominalProvider" id="pkgNominalProvider" required></select>
                            </div>
                            <br />
                            <g:if test="${record_namespaces?.size() > 0}">
                                <div class="input-group">
                                    <span class="input-group-addon"><g:message code="listDocuments.key.namespace" /></span>
                                    <g:select name="pkgTitleId" id="pkgTitleId" from="${record_namespaces}" optionKey="id"
                                              optionValue="text" class="form-control"/>
                                    <g:if test="${session.lastUpdate?.parameterMap?.pkgTitleId}">
                                        <script>
                                            $('#pkgTitleId').val('${session.lastUpdate?.parameterMap?.pkgTitleId[0]}').select();
                                        </script>
                                    </g:if>
                                </div>
                                <br>
                            </g:if>
                            <g:if test="${curatoryGroups?.size() > 0}">
                                <div class="input-group">
                                    <span class="input-group-addon"><em>GOKb</em> Curatory Group</span>
                                    <g:select name="pkgCuratoryGroup" id="pkgCuratoryGroup" from="${curatoryGroups}"
                                              optionKey="text" optionValue="text" class="form-control"
                                              required="required"
                                              noSelection="${['':message(code:'listDocuments.js.placeholder.curatorygroup')]}"/>
                                    <g:if test="${session.lastUpdate?.parameterMap?.pkgCuratoryGroup}">
                                        <script>
                                            $('#pkgCuratoryGroup').val('${session.lastUpdate?.parameterMap?.pkgCuratoryGroup[0]}').select();
                                        </script>
                                    </g:if>
                                </div>
                            </g:if>

                            <script>
                                var title = null;
                                if (${false != session.lastUpdate?.parameterMap?.pkgTitle?.getAt(0)}){
                                    title = "${session.lastUpdate?.parameterMap?.pkgTitle?.getAt(0)}";
                                }
                                var platform = null;
                                if (${false != session.lastUpdate?.parameterMap?.pkgNominalPlatform?.getAt(0)}){
                                    platform = "${session.lastUpdate?.parameterMap?.pkgNominalPlatform?.getAt(0)}";
                                }
                                var provider = null;
                                if (${false != session.lastUpdate?.parameterMap?.pkgNominalProvider?.getAt(0)}){
                                    provider = "${session.lastUpdate?.parameterMap?.pkgNominalProvider?.getAt(0)}";
                                }
                                var titleId = null;
                                if (${false != session.lastUpdate?.parameterMap?.pkgTitleId?.getAt(0)}){
                                    titleId = "${session.lastUpdate?.parameterMap?.pkgTitleId?.getAt(0)}";
                                }
                                var curatoryGroup = null;
                                if (${false != session.lastUpdate?.parameterMap?.pkgCuratoryGroup?.getAt(0)}){
                                    curatoryGroup = "${session.lastUpdate?.parameterMap?.pkgCuratoryGroup?.getAt(0)}";
                                }
                                $(document).ready(function() {
                                    $('#pkgTitle').select2({
                                        allowClear: true,
                                        placeholder: '${message(code:"listDocuments.js.placeholder.platform")}',
                                        debug: true,
                                        tags: true,
                                        templateSelection: function (data) {
                                            // Add custom attributes to the <option> tag for the selected option
                                            $(data.element).attr('value', data.findFilter);
                                            return data.text;
                                        },
                                        ajax: {
                                            url: '/ygor/enrichment/suggestTitle',
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
                                    $('#pkgTitle').append($('<option></option>').attr('value', title).text(title));
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
                        </g:if>


                        <g:if test="${enrichment.status == Enrichment.ProcessingState.PREPARE_2}">

                            ${enrichment.dataContainer?.pkgHeader?.nominalPlatform.name ? '' : raw('<div class="alert alert-danger" role="alert">') +
                                    message(code:'listDocuments.js.message.noplatformname') +
                                    raw('</div>')}
                            <div class="input-group custom-control">
                                <span class="input-group-addon"><em>GOKb</em> <g:message code="listDocuments.key.platformname" /></span>
                                <span class="form-control" >${enrichment.dataContainer?.pkgHeader?.nominalPlatform.name}</span>
                            </div>
                            <br />

                            ${enrichment.dataContainer?.pkgHeader?.nominalPlatform.url ? '' : raw('<div class="alert alert-danger" role="alert">') +
                                    message(code:'listDocuments.js.message.noplatformurl') +
                                    raw('</div>')}
                            <div class="input-group">
                                <span class="input-group-addon"><em>GOKb</em>  <g:message code="listDocuments.key.platformurl" /></span>
                                <span class="form-control" >${enrichment.dataContainer?.pkgHeader?.nominalPlatform.url}</span>
                            </div>
                            <br />

                            ${enrichment.dataContainer?.pkgHeader?.nominalProvider ? '' : raw('<div class="alert alert-danger" role="alert">') +
                                    message(code:'listDocuments.js.message.noprovider') +
                                    raw('</div>')}
                            <div class="input-group">
                                <span class="input-group-addon"><em>GOKb</em> <g:message code="listDocuments.key.provider" /></span>
                                <span class="form-control" >${enrichment.dataContainer?.pkgHeader?.nominalProvider.name}</span>
                            </div>
                            <br />

                            <g:message code="listDocuments.enrichment.apis" />
                            <br /><br />

                            <div class="input-group">
                                <span class="input-group-addon"><g:message code="listDocuments.key.sources" /></span>
                                <span class="form-control">
                                    <div class="checkbox">
                                        <label>
                                            <g:checkBox name="processOption" required="true" checked="true" value="${KbartReader.IDENTIFIER}"/>
                                            KBart <code><g:message code="listDocuments.enrichment.file" /></code>
                                        </label>
                                    &nbsp;
                                        <label>
                                            <g:if test="${session.lastUpdate?.pmOptions?.contains(ZdbReader.IDENTIFIER)}">
                                                <g:checkBox name="processOption" checked="true" value="${ZdbReader.IDENTIFIER}" />
                                            </g:if>
                                            <g:else>
                                                <g:checkBox name="processOption" checked="false" value="${ZdbReader.IDENTIFIER}" />
                                            </g:else>
                                            ZDB <em>@GBV</em> <code>API</code>
                                        </label>
                                        &nbsp;
                                        <label>
                                            <g:if test="${session.lastUpdate?.pmOptions?.contains(EzbReader.IDENTIFIER)}">
                                                <g:checkBox name="processOption" checked="true" value="${EzbReader.IDENTIFIER}" />
                                            </g:if>
                                            <g:else>
                                                <g:checkBox name="processOption" checked="false" value="${EzbReader.IDENTIFIER}" />
                                            </g:else>
                                            EZB <code>API</code>
                                        </label>
                                    </div>
                                </span>
                            </div>

                            <br />
                            <g:message code="listDocuments.enrichment.entry" />
                            <br /><br />

                            <div class="input-group">
                                <span class="input-group-addon"><g:message code="listDocuments.key.key" /></span>
                                <span class="form-control">
                                    <div class="radio">
                                        <g:message code="listDocuments.enrichment.sequence" />
                                    </div>
                                </span>
                            </div>
                        </g:if>
                    </li>
                </ul>

                <g:if test="${enrichment.status == Enrichment.ProcessingState.PREPARE_1 || enrichment.status == Enrichment.ProcessingState.PREPARE_2}">
                    <g:actionSubmit action="deleteFile" value="${message(code:'listDocuments.button.deleteFile')}" class="btn btn-danger"/>
                    <g:actionSubmit action="correctFile" value="${message(code:'listDocuments.button.correctFile')}" class="btn btn-warning"/>
                    <g:if test="${enrichment.status == Enrichment.ProcessingState.PREPARE_1}">
                        <g:actionSubmit action="prepareFile" value="${message(code:'listDocuments.button.processfile')}" class="btn btn-success"/>
                    </g:if>
                    <g:else>
                        <g:actionSubmit action="processFile" value="${message(code:'listDocuments.button.processfile')}" class="btn btn-success"/>
                    </g:else>
                </g:if>
                <g:if test="${enrichment.status == Enrichment.ProcessingState.WORKING}">
                    <g:actionSubmit action="stopProcessingFile" value="${message(code:'listDocuments.button.stopprocessingfile')}" class="btn btn-danger"/>
                </g:if>
                <g:if test="${enrichment.status == Enrichment.ProcessingState.ERROR}">
                    <g:actionSubmit action="deleteFile" value="${message(code:'listDocuments.button.deleteFile')}" class="btn btn-danger"/>
                    <g:actionSubmit action="correctFile" value="${message(code:'listDocuments.button.correctFile')}" class="btn btn-warning"/>
                </g:if>

                <br/>

                <script>
                    $(function(){
                        $('[data-toggle="tooltip"]').tooltip()
                    })
                </script>

                <g:if test="${enrichment.status in [Enrichment.ProcessingState.WORKING,
                                                    Enrichment.ProcessingState.FINISHED,
                                                    Enrichment.ProcessingState.ERROR]}">
                    <script>
                        $(function(){
                            var ygorDocumentStatus${enrichment.resultHash} = function(){
                                jQuery.ajax({
                                    type:       'GET',
                                    url:         '${grailsApplication.config.grails.app.context}/statistic/ajaxGetStatus',
                                    data:        'originHash=${enrichment.originHash}',
                                    data:        'resultHash=${enrichment.resultHash}',

                                    success:function(data, textStatus){
                                        data = jQuery.parseJSON(data)
                                        console.log("OH: ${enrichment.originHash}");
                                        console.log("RH: ${enrichment.resultHash}");
                                        var status = data.status;
                                        var progress = data.progress;

                                        jQuery('#progress-${enrichment.resultHash} > .progress-bar').attr('aria-valuenow', progress);
                                        jQuery('#progress-${enrichment.resultHash} > .progress-bar').attr('style', 'width:' + progress + '%');
                                        jQuery('#progress-${enrichment.resultHash} > .progress-bar').text(progress + '%');

                                        if(status == 'FINISHED') {
                                            console.log("FINISHED: ${enrichment.originHash}");
                                            window.location = '${grailsApplication.config.grails.app.context}/statistic/show'
                                                + '?originHash=${enrichment.originHash}&resultHash=${enrichment.resultHash}';
                                        }
                                        if(status == 'ERROR') {
                                            window.location = '${grailsApplication.config.grails.app.context}/enrichment/process';
                                        }
                                    },
                                    error:function(XMLHttpRequest, textStatus, errorThrown){
                                        clearInterval(ygorDocumentStatus${enrichment.resultHash});
                                    }
                                });
                            }
                            var ygorInterval${enrichment.resultHash} = setInterval(ygorDocumentStatus${enrichment.resultHash}, 1500);
                        })
                    </script>
                </g:if>
            </div>
        </div>
    </g:if>
</g:form>
