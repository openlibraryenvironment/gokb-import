<%@ page import="de.hbznrw.ygor.iet.export.structure.FixedValues" %>
<meta name="layout" content="enrichment">

<div class="row">

	<div class="col-xs-10 col-xs-offset-1">
		
		<h3>Datenflüsse</h3>
		
		<table class="table table-striped table-hover">			
			<thead>
				<th>API</th>
				<th>Title</th>
				<th>TIPP</th>
			</thead>
			<tbody>
				<tr>
					<td>Ygor <em>, Formular</em></td>
					<td></td>
					<td>packageHeader.name</td>
				</tr>
				<tr>
					<td>Ygor <em>, Formular</em></td>
					<td></td>
					<td>packageHeader.nominalProvider</td>
				</tr>
				<tr>
					<td>Ygor <em>, Formular</em></td>
					<td></td>
					<td>packageHeader.nominalPlatform</td>
				</tr>
				<tr>
					<td>Ygor <em>, Formular</em></td>
					<td></td>
					<td>packageHeader.variantNames</td>
				</tr>
				<tr>
					<td>Ygor <em>, Formular</em></td>
					<td></td>
					<td>packageHeader.curatoryGroups</td>
				</tr>
							
				<tr>
					<td>KBART <em>, Datei</em></td>
					<td></td>
					<td><strong>tipps.coverage</strong></td>
				</tr>
				<tr>
					<td>KBART <em>, Datei</em></td>
					<td></td>
					<td>tipps.url <strong data-toggle="tooltip" data-placement="top" title="Falls tipps.url gegen packageHeader.nominalPlatform matcht">[?]</strong></td>
				</tr>
				<tr>
					<td>KBART <em>, Datei</em></td>
					<td></td>
					<td>tipps.platform <strong data-toggle="tooltip" data-placement="bottom" title="URI-Authority von tipps.url, falls diese gegen packageHeader.nominalPlatform matcht">[?]</strong></td>
				</tr>
				
				<tr>
					<td>ZDB / GBV</td>
					<td>title.name</td>
					<td>tipps.title.name</td>
				</tr>
				
				<tr>
					<td>ZDB / GBV</td>
					<td>title.identifiers.zdb</td>
					<td>tipps.title.identifiers.zdb</td>
				</tr>
				<tr>
					<td>ZDB / GBV</td>
					<td>title.identifiers.eissn</td>
					<td>tipps.title.identifiers.eissn</td>
				</tr>
				<tr>
					<td>ZDB / GBV</td>
					<td>title.identifiers.issn</td>
					<td></td>
				</tr>
				
				<tr>
					<td>ZDB / GBV</td>
					<td>title.publishedFrom</td>
					<td></td>
				</tr>
				<tr>
					<td>ZDB / GBV</td>
					<td>title.publishedTo</td>
					<td></td>
				</tr>
				<tr>
					<td>ZDB / GBV</td>
					<td><strong>title.publisher_history</strong></td>
					<td></td>
				</tr>
				
				<tr>
					<td>ZDB / GBV</td>
					<td><strong>title.history_events</strong></td>
					<td></td>
				</tr>
				
				<tr>
					<td>EZB</td>
					<td>title.identifiers.ezb</td>
					<td></td>
				</tr>
			</tbody>
		</table>
		
		<br />
		
		<h3>Hart kodierte Werte</h3>
		
		<table class="table table-striped table-hover">	
			<thead>
				<th></th>
				<th>Title</th>
				<th>TIPP</th>
				<th>Wert</th>
			</thead>
			<tbody>		
				<tr>
					<td><em>fixed</em></td>
					<td></td>
					<td>packageHeader.breakable</td>
					<td>${FixedValues.packageHeader_breakable}</td>
				</tr>
				<tr>
					<td><em>fixed</em></td>
					<td></td>
					<td>packageHeader.consistent</td>
					<td>${FixedValues.packageHeader_consistent}</td>
				</tr>
				<tr>
					<td><em>fixed</em></td>
					<td></td>
					<td>packageHeader.fixed</td>
					<td>${FixedValues.packageHeader_fixed}</td>
				</tr>
				<tr>
					<td><em>fixed</em></td>
					<td></td>
					<td>packageHeader.global</td>
					<td>${FixedValues.packageHeader_global}</td>
				</tr>
				<tr>
					<td><em>fixed</em></td>
					<td></td>
					<td>packageHeader.listStatus</td>
					<td>${FixedValues.packageHeader_listStatus}</td>
				</tr>

				<tr>
					<td><em>fixed</em></td>
					<td></td>
					<td>tipps.medium</td>
					<td>${FixedValues.tipp_medium}</td>
				</tr>
				<tr>
					<td><em>fixed</em></td>
					<td></td>
					<td>tipps.status</td>
					<td>${FixedValues.tipp_status}</td>
				</tr>
				<tr>
					<td><em>fixed</em></td>
					<td></td>
					<td>tipps.coverage.coverageDepth</td>
					<td>${FixedValues.tipp_coverage_coverageDepth}</td>
				</tr>
				<tr>
					<td><em>fixed</em></td>
					<td></td>
					<td>tipps.title.type</td>
					<td>${FixedValues.tipp_title_type}</td>
				</tr>

				<tr>
					<td><em>fixed</em></td>
					<td>title.editStatus</td>
					<td></td>
					<td>${FixedValues.title_editStatus}</td>
				</tr>
				<tr>
					<td><em>fixed</em></td>
					<td>title.medium</td>
					<td></td>
					<td>${FixedValues.title_medium}</td>
				</tr>
				<tr>
					<td><em>fixed</em></td>
					<td>title.status</td>
					<td></td>
					<td>${FixedValues.title_status}</td>
				</tr>
				<tr>
					<td><em>fixed</em></td>
					<td>title.type</td>
					<td></td>
					<td>${FixedValues.title_type}</td>
				</tr>
				
				<tr>
					<td><em>fixed</em></td>
					<td>title.publisher_history.status</td>
					<td></td>
					<td>${FixedValues.title_publisher_history_status}</td>
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