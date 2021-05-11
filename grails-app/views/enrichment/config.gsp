<meta name="layout" content="enrichment">

<div class="row">

	<div class="col-xs-10 col-xs-offset-1">
	</div>
				
	<div id="config-view" class="col-xs-10 col-xs-offset-1">
		<br />
		<p class="lead"><g:message code="config.heading.ejournal" /></p>
		
		<table class="table table-striped table-hover">			
			<thead>
				<th>API</th>
				<th>Title</th>
				<th>TIPP</th>
				<th><g:message code="config.table.field" /></th>
			</thead>
			<tbody>
				<tr>
					<td>Ygor <em>, <g:message code="config.table.form" /></em></td>
					<td></td>
					<td>packageHeader.name</td>
					<td>Titel</td>
				</tr>
				<tr>
					<td>Ygor <em>, <g:message code="config.table.form" /></em></td>
					<td></td>
					<td>packageHeader.nominalProvider</td>
					<td>Knowledge Base Provider</td>
				</tr>
				<tr>
					<td>Ygor <em>, <g:message code="config.table.form" /></em></td>
					<td></td>
					<td>packageHeader.nominalPlatform</td>
					<td>Knowledge Base Plattform</td>
				</tr>

				<tr>
					<td>KBART <em>, <g:message code="config.table.file" /></em></td>
					<td></td>
					<td><strong>tipps.coverage</strong></td>
					<td>
						<ul>
							<li>date_first_issue_online</li>
							<li>date_last_issue_online</li>
							<li>num_first_vol_online</li>
							<li>num_last_vol_online</li>
							<li>num_first_issue_online</li>
							<li>num_last_issue_online</li>
							<li>title_url</li>
							<li>embargo_info</li>
							<li>coverage_depth</li>
							<li>notes</li>
						</ul>
					</td>
				</tr>
				<tr>
					<td>KBART <em>, <g:message code="config.table.file" /></em></td>
					<td></td>
					<td>tipps.access</td>
					<td>
						<ul>
							<li>access_start_date</li>
							<li>access_end_date</li>
						</ul>
					</td>
				</tr>
				<tr>
					<td>KBART <em>, <g:message code="config.table.file" /></em></td>
					<td></td>
					<td>tipps.url</td>
					<td>title_url</td>
				</tr>
				<tr>
					<td>KBART <em>, <g:message code="config.table.file" /></em></td>
					<td></td>
					<td>tipps.platform</td>
					<td>title_url</td>
				</tr>
				
				<tr>
					<td>ZDB / GBV</td>
					<td>title.name</td>
					<td>tipps.title.name</td>
					<td>025@:a, alt. 021A:a</td>
				</tr>
				
				<tr>
					<td>ZDB / GBV</td>
					<td>title.identifiers.zdb</td>
					<td>tipps.title.identifiers.zdb</td>
					<td>006Z:0</td>
				</tr>
				<tr>
					<td>ZDB / GBV</td>
					<td>title.identifiers.eissn</td>
					<td>tipps.title.identifiers.eissn</td>
					<td>005A:0</td>
				</tr>
				
				<tr>
					<td>ZDB / GBV</td>
					<td>title.publishedFrom</td>
					<td></td>
					<td>011@:a</td>
				</tr>
				<tr>
					<td>ZDB / GBV</td>
					<td>title.publishedTo</td>
					<td></td>
					<td>011@:b</td>
				</tr>
				<tr>
					<td>ZDB / GBV</td>
					<td><strong>title.publisher_history</strong></td>
					<td></td>
					<td>
						<ul>
							<li>033A:n</li>
							<li>033A:h</li>
						</ul>
					</td>
				</tr>
				<tr>
					<td>ZDB / GBV</td>
					<td><strong>title.historyEvents</strong></td>
					<td></td>
					<td>
						<ul>
							<li>039E:c</li>
							<li>039E:a, alt 039E:t</li>
							<li>039E:C</li>
							<li>039E:6</li>
						</ul>
					</td>
				</tr>
				
				<tr>
					<td>EZB</td>
					<td>title.identifiers.ezb</td>
					<td></td>
					<td>journal[@jourid]</td>
				</tr>
			</tbody>
		</table>

		<br />

		<p class="lead"><g:message code="config.heading.ebook" /></p>

		<table class="table table-striped table-hover">
			<thead>
			<th>API</th>
			<th>Title</th>
			<th>TIPP</th>
			<th><g:message code="config.table.field" /></th>
			</thead>
			<tbody>
			<tr>
				<td>Ygor <em>, <g:message code="config.table.form" /></em></td>
				<td></td>
				<td>packageHeader.name</td>
				<td>Titel</td>
			</tr>
			<tr>
				<td>Ygor <em>, <g:message code="config.table.form" /></em></td>
				<td></td>
				<td>packageHeader.nominalProvider</td>
				<td>GOKb Provider</td>
			</tr>
			<tr>
				<td>Ygor <em>, <g:message code="config.table.form" /></em></td>
				<td></td>
				<td>packageHeader.nominalPlatform</td>
				<td>GOKb Plattform</td>
			</tr>

			<tr>
				<td>KBART <em>, <g:message code="config.table.file" /></em></td>
				<td></td>
				<td><strong>tipps.coverage</strong></td>
				<td>
					<ul>
						<li>date_first_issue_online</li>
						<li>date_last_issue_online</li>
						<li>num_first_vol_online</li>
						<li>num_last_vol_online</li>
						<li>num_first_issue_online</li>
						<li>num_last_issue_online</li>
						<li>title_url</li>
						<li>embargo_info</li>
						<li>coverage_depth</li>
						<li>notes</li>
					</ul>
				</td>
			</tr>
			<tr>
				<td>KBART <em>, <g:message code="config.table.file" /></em></td>
				<td></td>
				<td>tipps.access</td>
				<td>
					<ul>
						<li>access_start_date</li>
						<li>access_end_date</li>
					</ul>
				</td>
			</tr>
			<tr>
				<td>KBART <em>, <g:message code="config.table.file" /></em></td>
				<td></td>
				<td>tipps.url</td>
				<td>title_url</td>
			</tr>
			<tr>
				<td>KBART <em>, <g:message code="config.table.file" /></em></td>
				<td></td>
				<td>tipps.platform</td>
				<td>title_url</td>
			</tr>

			<tr>
				<td>KBART <em>, <g:message code="config.table.file" /></em></td>
				<td>title.name</td>
				<td>tipps.title.name</td>
				<td>publication_title</td>
			</tr>

			<tr>
				<td>KBART <em>, <g:message code="config.table.file" /></em></td>
				<td>title.identifiers.doi</td>
				<td>tipps.title.identifiers.doi</td>
				<td>doi_identifier</td>
			</tr>
			<tr>
				<td>KBART <em>, <g:message code="config.table.file" /></em></td>
				<td>title.identifiers.eissn</td>
				<td>tipps.title.identifiers.eissn</td>
				<td>online_identifier</td>
			</tr>
			<tr>
				<td>KBART <em>, <g:message code="config.table.file" /></em></td>
				<td>title.identifiers.issn</td>
				<td></td>
				<td>print_identifier</td>
			</tr>

			<tr>
				<td>KBART <em>, <g:message code="config.table.file" /></em><br>
					Ygor <em>, Formular</em>
				</td>
				<td>title.identifiers.%namespace%</td>
				<td>title.identifiers.%namespace%</td>
				<td>title_id <br>Liste von Namespaces</td>
			</tr>

			<tr>
				<td>KBART <em>, <g:message code="config.table.file" /></em></td>
				<td>title.firstAuthor</td>
				<td></td>
				<td>first_author</td>
			</tr>
			<tr>
				<td>KBART <em>, <g:message code="config.table.file" /></em></td>
				<td>title.firstEditor </td>
				<td></td>
				<td>first_editor </td>
			</tr>
			<tr>
				<td>KBART <em>, <g:message code="config.table.file" /></em></td>
				<td>title.dateFirstInPrint</td>
				<td></td>
				<td>
					date_monograph_published_print
				</td>
			</tr>
			<tr>
				<td>KBART <em>, <g:message code="config.table.file" /></em></td>
				<td>title.dateFirstOnline</td>
				<td></td>
				<td>
					date_monograph_published_online
				</td>
			</tr>
			<tr>
				<td>KBART <em>, <g:message code="config.table.file" /></em></td>
				<td>title.monographEdition<br>title.editionNumber<br>title.editionDifferentiator<br>title.editionStatement</td>
				<td></td>
				<td>
					monograph_edition
				</td>
			</tr>
			<tr>
				<td>KBART <em>, <g:message code="config.table.file" /></em></td>
				<td>title.monographVolume</td>
				<td></td>
				<td>
					monograph_volume
				</td>
			</tr>

			</tbody>
		</table>
		
		<br />
		
		<p class="lead"><g:message code="config.heading.hard" /></p>
		
		<table class="table table-striped table-hover">	
			<thead>
				<th>API</th>
				<th>Title</th>
				<th>TIPP</th>
				<th><g:message code="config.table.value" /></th>
			</thead>
			<tbody>		
				<tr>
					<td><em>fixed</em></td>
					<td></td>
					<td>tipps.title.type</td>
					<td>Serial / Database / Ebook</td>
				</tr>
				<tr>
					<td><em>fixed</em></td>
					<td>title.medium</td>
					<td></td>
					<td>Journal / Database / Book</td>
				</tr>
				<tr>
					<td><em>fixed</em></td>
					<td>title.type</td>
					<td></td>
					<td>Serial / Database / Ebook</td>
				</tr>
			</tbody>
		</table>
		
		<script>
			$(function(){
			  $('[data-toggle="tooltip"]').tooltip()
			})
		</script>					
	</div>
</div>
