<!-- _uploadFile.gsp -->
<%@ page import="ygor.Enrichment" %>

<g:if test="${enrichments.size() == 0}">

	<div class="row">
		<div class="col-xs-12">

			<br /><br />

			<g:set var="filename" value="${message(code:'uploadFile.file.nofile')}" scope="page" />
			<g:if test="${session.lastUpdate?.file?.originalFilename != null}">
				<g:set var="filename" value="${session.lastUpdate?.file?.originalFilename}" scope="page" />
			</g:if>

			<g:uploadForm name="uploadFile" action="uploadFile">

				<ul class="list-group content-list">
					<li class="list-group-item">

						<div class="input-group">
							<span class="input-group-addon"><g:message code="uploadFile.file.label" /></span>
							<input class="form-control" type="text" id="uploadFileLabel" name="uploadFileLabel" readonly value="${filename}" size="46"/>
						</div>

						<br />

						<div class="input-group">
							<span class="input-group-addon"><g:message code="uploadFile.separator.label" /></span>
							<span class="form-control">
								<div class="radio">
									<label>
										<g:if test="${session.lastUpdate?.foDelimiter == null || session.lastUpdate?.foDelimiter == "tab"}">
											<g:radio name="formatDelimiter" checked="true" value="tab" />
										</g:if>
										<g:else>
											<g:radio name="formatDelimiter" value="tab" />
										</g:else>
										<g:message code="uploadFile.separator.tabulator" />  <code>(\t)</code>
									</label>
									&nbsp;
									<label>
										<g:if test="${session.lastUpdate?.foDelimiter == "comma"}">
											<g:radio name="formatDelimiter" checked="true" value="comma" />
										</g:if>
										<g:else>
											<g:radio name="formatDelimiter" value="comma" />
										</g:else>
										<g:message code="uploadFile.separator.comma" /> <code>(,)</code>
									</label>
									&nbsp;
									<label>
										<g:if test="${session.lastUpdate?.foDelimiter == "semicolon"}">
											<g:radio name="formatDelimiter" checked="true" value="semicolon" />
										</g:if>
										<g:else>
											<g:radio name="formatDelimiter" value="semicolon" />
										</g:else>
										<g:message code="uploadFile.separator.semicolon" />  <code>(;)</code>
									</label>
								</div>
							</span>
						</div>

						<br />
						<div class="input-group">
							<span class="input-group-addon"><g:message code="uploadFile.mediatype.label" /></span>
							<span class="form-control">
								<div class="radio">
									<label>
										<g:if test="${session.lastUpdate?.dataTyp == null || session.lastUpdate?.dataTyp == "journals"}">
											<g:radio name="dataTyp" checked="true" value="journals" />
										</g:if>
										<g:else>
											<g:radio name="dataTyp" value="journals" />
										</g:else>
										<g:message code="uploadFile.mediatype.serial" />
									</label>
									%{--&nbsp;
                                    <label>
                                        <g:if test="${session.lastUpdate?.dataTyp == "database"}">
                                            <g:radio name="dataTyp" checked="true" value="database" />
                                        </g:if>
                                        <g:else>
                                            <g:radio name="dataTyp" value="database" />
                                        </g:else>
                                        Database
                                    </label>--}%
									&nbsp;
									<label>
										<g:if test="${session.lastUpdate?.dataTyp == "ebooks"}">
											<g:radio name="dataTyp" checked="true" value="ebooks" />
										</g:if>
										<g:else>
											<g:radio name="dataTyp" value="ebooks" />
										</g:else>
										<g:message code="uploadFile.mediatype.monograph" />
									</label>
								</div>
							</span>
						</div>

						<!-- <br />
					<div class="input-group">
						<span class="input-group-addon">Zeichenbegrenzer:</span>
						<span class="form-control">
							<div class="radio">
								<label>
									<g:radio name="formatQuoteMode" checked="true" value="none" />
									Ohne
								</label>
								&nbsp;
								<label>
									<g:radio name="formatQuoteMode" value="nonnumeric" />
									Nur alphanumerische Felder begrenzen
								</label>
								&nbsp;
								<label>
									<g:radio name="formatQuoteMode" value="all" />
									Alle Felder begrenzen
								</label>
							</div>
						</span>
					</div> -->
					</li>
				</ul>

				<ul class="list-group content-list">
					<li class="list-group-item">
						<label class="btn btn-link btn-file">
							<input type="file" accept=".csv,.tsv,.ssv,.txt" name="uploadFile" style="display: none;"/><g:message code="uploadFile.button.select" />
						</label>
						<input type="submit" value="${message(code:'uploadFile.button.upload')}" class="btn btn-success" />
						<script>
							jQuery(document).on('change', "[name='uploadFile']", function() {
								var input = $(this),
									files = input.get(0).files;
								if (files){
									var file0 = files[0],
										filename = file0.name,
										replaced = filename.replace(/\\/g, '/'),
										label = replaced.replace(/.*\//, '');
									jQuery('#uploadFileLabel')[0].setAttribute("value", label)
								}
							});
						</script>
					</li>
				</ul>

			</g:uploadForm>
		</div>
	</div><!-- .row -->

	<div class="row">
		<div class="col-xs-12">

			<br/><br/>
			<ul class="list-group how-to-list">
				<li class="list-group-item">
					<span class="glyphicon glyphicon-share-alt" aria-hidden="true"></span>
					&nbsp; <g:message code="howtostep1.uploadRaw" />
				</li>
			</ul>
			<br/>

			<g:set var="filenameRaw" value="${message(code:'uploadFile.raw.file.nofile')}" scope="page" />
			<g:uploadForm id="uploadRawFile" action="uploadRawFile">

				<ul class="list-group content-list">
					<li class="list-group-item">
						<div class="input-group">
							<span class="input-group-addon"><g:message code="uploadFile.raw.file.label" /></span>
							<input class="form-control" type="text" id="uploadRawFileLabel" name="uploadRawFileLabel" readonly value="${filenameRaw}" size="46"/>
						</div>
					</li>
				</ul>

				<ul class="list-group content-list">
					<li class="list-group-item">
						<label class="btn btn-link btn-file">
							<input type="file" accept=".raw.json" name="uploadRawFile" style="display: none;"/><g:message code="uploadFile.button.select" />
						</label>
						<input type="submit" value="${message(code:'uploadFile.button.upload')}" class="btn btn-success" />
						<script>
							jQuery(document).on('change', "[name='uploadRawFile']", function() {
								var input = $(this),
										files = input.get(0).files;
								if (files){
									var file0 = files[0],
											filename = file0.name,
											replaced = filename.replace(/\\/g, '/'),
											label = replaced.replace(/.*\//, '');
									jQuery('#uploadRawFileLabel')[0].setAttribute("value", label)
								}
							});
						</script>
					</li>
				</ul>
			</g:uploadForm>
		</div>
	</div><!-- .row -->
</g:if>
