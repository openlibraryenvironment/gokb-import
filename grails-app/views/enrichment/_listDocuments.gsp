<!-- _listDocuments.gsp -->

<%@ page
        import="ygor.Enrichment"
        import="ygor.GokbService"
        import="de.hbznrw.ygor.export.structure.TitleStruct"
        import="de.hbznrw.ygor.export.structure.PackageStruct"
        import="de.hbznrw.ygor.readers.*"
%>

<g:form controller="enrichment" action="process">
    <g:hiddenField name="originHash" value="${enrichment?.originHash}" />
    <g:hiddenField name="resultHash" value="${enrichment?.resultHash}" />
    <g:hiddenField name="dataTyp" value="${session.lastUpdate?.dataTyp}" />
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
                        <div class="input-group">
                            <span class="input-group-addon"><g:message code="listDocuments.key.mediatype" /></span>
                            <span class="form-control" >
                                <g:if test="${session.lastUpdate?.dataTyp == "journals"}">
                                    <g:message code="listDocuments.mediatype.journal" />
                                </g:if>

                                <g:if test="${session.lastUpdate?.dataTyp == "database"}">
                                    <g:message code="listDocuments.mediatype.database" />
                                </g:if>

                                <g:if test="${session.lastUpdate?.dataTyp == "ebooks"}">
                                    <g:message code="listDocuments.mediatype.ebook" />
                                </g:if>
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
                                <g:if test="${session.lastUpdate?.parameterMap?.pkgTitle}">
                                    <g:textField name="pkgTitle" size="48" value="${session.lastUpdate.parameterMap.pkgTitle[0]}" class="form-control" />
                                </g:if>
                                <g:else>
                                    <g:textField name="pkgTitle" size="48" placeholder="Munchhausen Verlag: Science Journals: hbz: 1999" class="form-control" />
                                </g:else>
                            </div>
                            <br />
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
                                <span class="input-group-addon"><em>GOKb</em> <g:message code="listDocuments.key.platform" /></span>
                                <select name="pkgNominalPlatform" id="pkgNominalPlatform"></select>
                            </div>
                            <br />
                            <div class="input-group">
                                <span class="input-group-addon"><em>GOKb</em> <g:message code="listDocuments.key.provider" /></span>
                                <select name="pkgNominalProvider" id="pkgNominalProvider"></select>
                            </div>
                            <br />
                            <g:if test="${namespaces?.size() > 0}">
                                <div class="input-group">
                                    <span class="input-group-addon"><g:message code="listDocuments.key.namespace" /></span>
                                    <g:if test="${session.lastUpdate?.parameterMap?.pkgTitleId}">
                                        <g:select name="pkgTitleId" id="pkgTitleId" from="${namespaces}" optionKey="text" optionValue="id" value="${session.lastUpdate?.parameterMap?.pkgTitleId.getAt(0)}" class="form-control"></g:select>
                                    </g:if>
                                    <g:else>
                                        <g:select name="pkgTitleId" id="pkgTitleId" from="${namespaces}" optionKey="text" optionValue="id" noSelection="${['':message(code:'listDocuments.js.placeholder.namespace')]}" class="form-control"></g:select>
                                    </g:else>
                                </div>
                                <br>
                            </g:if>

                            <script>
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
                                $(document).ready(function() {
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
                                    $('#pkgTitleId').append($('<option></option>').attr('value', titleId).text(titleId));
                                });
                            </script>

                            <div class="input-group">
                                <span class="input-group-addon"><em>GOKb</em> Curatory Group 1</span>
                                <g:if test="${session.lastUpdate?.parameterMap?.pkgCuratoryGroup1}">
                                    <g:textField name="pkgCuratoryGroup1" size="24" value="${session.lastUpdate.parameterMap.pkgCuratoryGroup1[0]}" class="form-control" />
                                </g:if>
                                <g:else>
                                    <g:textField name="pkgCuratoryGroup1" size="24" class="form-control" />
                                </g:else>
                            </div>

                            <div class="input-group">
                                <span class="input-group-addon"><em>GOKb</em> Curatory Group 2</span>
                                <g:if test="${session.lastUpdate?.parameterMap?.pkgCuratoryGroup2}">
                                    <g:textField name="pkgCuratoryGroup2" size="24" value="${session.lastUpdate.parameterMap.pkgCuratoryGroup2[0]}" class="form-control" />
                                </g:if>
                                <g:else>
                                    <g:textField name="pkgCuratoryGroup2" size="24" class="form-control" />
                                </g:else>
                            </div>

                        </g:if>


                        <g:if test="${enrichment.status == Enrichment.ProcessingState.PREPARE_2}">

                            ${enrichment.dataContainer.pkg.packageHeader.v.nominalPlatform.name ? '' : raw('<div class="alert alert-danger" role="alert">') +
                                    message(code:'listDocuments.js.message.noplatformname') +
                                    raw('</div>')}
                            <div class="input-group custom-control">
                                <span class="input-group-addon"><em>GOKb</em> <g:message code="listDocuments.key.platformname" /></span>
                                <span class="form-control" >${enrichment.dataContainer.pkg.packageHeader.v.nominalPlatform.name}</span>
                            </div>
                            <br />

                            ${enrichment.dataContainer.pkg.packageHeader.v.nominalPlatform.url ? '' : raw('<div class="alert alert-danger" role="alert">') +
                                    message(code:'listDocuments.js.message.noplatformurl') +
                                    raw('</div>')}
                            <div class="input-group">
                                <span class="input-group-addon"><em>GOKb</em>  <g:message code="listDocuments.key.platformurl" /></span>
                                <span class="form-control" >${enrichment.dataContainer.pkg.packageHeader.v.nominalPlatform.url}</span>
                            </div>
                            <br />

                            ${enrichment.dataContainer.pkg.packageHeader.v.nominalProvider.v ? '' : raw('<div class="alert alert-danger" role="alert">') +
                                    message(code:'listDocuments.js.message.noprovider') +
                                    raw('</div>')}
                            <div class="input-group">
                                <span class="input-group-addon"><em>GOKb</em> <g:message code="listDocuments.key.provider" /></span>
                                <span class="form-control" >${enrichment.dataContainer.pkg.packageHeader.v.nominalProvider.v}</span>
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
                                            KBART <code><g:message code="listDocuments.enrichment.file" /></code>
                                        </label>
                                    &nbsp;
                                        <g:if test="${session.lastUpdate?.dataTyp == "journals" || session.lastUpdate?.dataTyp == "database"}">
                                            <label>
                                                <g:checkBox name="processOption" required="true" checked="true" value="${ZdbReader.IDENTIFIER}"/>
                                                ZDB <em>@GBV</em> <code>API</code>
                                            </label>
                                            &nbsp;
                                            <label>
                                                <g:if test="${session.lastUpdate?.pmOptions == null || session.lastUpdate?.pmOptions?.contains(EzbReader.IDENTIFIER)}">
                                                    <g:checkBox name="processOption" checked="true" value="${EzbReader.IDENTIFIER}" />
                                                </g:if>
                                                <g:else>
                                                    <g:checkBox name="processOption" checked="false" value="${EzbReader.IDENTIFIER}" />
                                                </g:else>
                                                EZB <code>API</code>
                                            </label>
                                            <!--
                                            &nbsp;
                                            <label>
                                            <g:checkBox name="processOption" checked="false" disabled="true" value="${ZdbReader.IDENTIFIER}"/>
                                            ZDB <code>API</code>
                                        </label>
                                        -->
                                        </g:if>
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
                    <g:actionSubmit action="deleteFile" value="${message(code:'listDocuments.button.deletefile')}" class="btn btn-danger"/>
                    <g:actionSubmit action="correctFile" value="${message(code:'listDocuments.button.correctfile')}" class="btn btn-warning"/>
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
                    <g:actionSubmit action="deleteFile" value="${message(code:'listDocuments.button.deletefile')}" class="btn btn-danger"/>
                    <g:actionSubmit action="correctFile" value="${message(code:'listDocuments.button.correctfile')}" class="btn btn-warning"/>
                </g:if>

                <br/>

                <script>
                    $(function(){
                        $('[data-toggle="tooltip"]').tooltip()
                    })
                </script>

                <g:if test="${enrichment.status == Enrichment.ProcessingState.WORKING}">
                    <script>
                        $(function(){
                            var ygorDocumentStatus${enrichment.resultHash} = function(){
                                jQuery.ajax({
                                    type:       'GET',
                                    url:         '${grailsApplication.config.grails.app.context}/enrichment/ajaxGetStatus',
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
