<meta name="layout" content="enrichment">

<div class="row">

    <div class="col-xs-12">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title"><g:message code="statistic.edit.record"/> - ${record.multiFields.get("publicationTitle")?.getPrioValue()}</h3>
            </div>
            <div class="statistics-data">
                <table class="statistics-details">
                    <tr>
                        <th><g:message code="statistic.edit.field"/></th>
                        <th><g:message code="statistic.edit.value"/></th>
                        <th><g:message code="statistic.edit.source"/></th>
                        <th><g:message code="statistic.edit.status"/></th>
                    </tr>
                    <g:set var="lineCounter" value="${0}" />
                    <g:each in="${record.multiFields}" var="multiField">
                        <g:if test="${multiField?.value?.getPrioValue()}">
                            <tr class="${ (lineCounter % 2) == 0 ? 'even hover' : 'odd hover'}">
                                <td class="statistics-cell">${multiField.key}</td>
                                <td contenteditable="true" class="statistics-cell">${multiField.value.getPrioValue()}</td>
                                <td class="statistics-cell">${multiField.value.getPrioSource()}</td>
                                <td class="statistics-cell"><g:message code="${multiField.value.status}"/></td>
                            </tr>
                            <g:set var="lineCounter" value="${lineCounter + 1}"/>
                        </g:if>
                    </g:each>
                </table>
            </div>
        </div>
        <!-- <ul class="list-group content-list">
            <input type="submit" value="${message(code:'statistic.edit.cancel')}" class="btn btn-warning"/>
            <input type="submit" value="${message(code:'statistic.edit.save')}" class="btn btn-success"/>
        </ul> -->
    </div>

</div>

<script>
    var valueFields = document.querySelectorAll('[contenteditable]');
    valueFields.forEach((valueField) => {
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
                else if (event.which == 13 /* that is "enter" */) {
                    // save
                    data[target.getAttribute('data-name')] = target.innerHTML;
                    // send update
                    jQuery.ajax({
                        type:   'POST',
                        url:    '${grailsApplication.config.grails.app.context}/statistic/update',
                        data:   {value: 'updated frontendly'},
                        success:function(data){
                            console.log("successly");
                            data = jQuery.parseJSON(data);
                            console.log(data);
                            const status = data.status;
                            if(status == 'FINISHED') {
                                window.location = '${grailsApplication.config.grails.app.context}/statistic/edit';
                            }
                            if(status == 'ERROR') {
                                window.location = '${grailsApplication.config.grails.app.context}/statistic/edit';
                            }

                        },
                        error:function(XMLHttpRequest, textStatus, errorThrown){
                            // clearInterval(ygorDocumentStatus$ {e.key});
                            console.log("errorly");
                        }
                    });

                    target.blur();
                    event.preventDefault();
                }
            }
        }, true);
    });
</script>