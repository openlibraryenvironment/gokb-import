<meta name="layout" content="enrichment">

<div class="row">

	<div class="col-xs-8">	
		<div class="panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title">Filter</h3>
			</div>
			<div id="statistics-filter" class="panel-body">
				<button type="button" class="btn btn-xs btn-default" data-class="btn-danger" data-code="VALIDATOR_DATE_IS_INVALID">DATE_IS_INVALID 
					<span class="badge">0</span>
					<span class="badge">0</span>
				</button>
				<br /><br />
				<button type="button" class="btn btn-xs btn-default" data-class="btn-warning" data-code="VALIDATOR_IDENTIFIER_IS_NOT_ATOMIC">IDENTIFIER_IS_NOT_ATOMIC 
					<span class="badge">0</span>
					<span class="badge">0</span>
				</button>
				<button type="button" class="btn btn-xs btn-default" data-class="btn-warning" data-code="VALIDATOR_URL_IS_NOT_ATOMIC">URL_IS_NOT_ATOMIC 
					<span class="badge">0</span>
					<span class="badge">0</span>
				</button>
				<br /><br />
				<button type="button" class="btn btn-xs btn-default" data-class="btn-info" data-code="VALIDATOR_DATE_IS_MISSING">DATE_IS_MISSING 
					<span class="badge">0</span>
					<span class="badge">0</span>
				</button>
				<button type="button" class="btn btn-xs btn-default" data-class="btn-info" data-code="VALIDATOR_IDENTIFIER_IS_MISSING">IDENTIFIER_IS_MISSING 
					<span class="badge">0</span>
					<span class="badge">0</span>
				</button>
				<br/><br />
				<button type="button" class="btn btn-xs btn-default" data-class="btn-info" data-code="VALIDATOR_STRING_IS_MISSING">STRING_IS_MISSING 
					<span class="badge">0</span>
					<span class="badge">0</span>
				</button>
				<button type="button" class="btn btn-xs btn-default" data-class="btn-info" data-code="VALIDATOR_URL_IS_MISSING">URL_IS_MISSING 
					<span class="badge">0</span>
					<span class="badge">0</span>
				</button>
			</div>
		</div>
	</div>
	
	<div class="col-xs-4">	
		<div class="panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title">Meta</h3>
			</div>
			<div id="statistics-meta" class="panel-body">
				<p class="text-right">
					<strong>Erstellung</strong><br />
					<span class="meta-date"></span>
					<br />
					<strong>Ygor-Version</strong><br />
					<span class="meta-ygor"></span>
					<br />
					<strong>Statistik-Hash</strong><br />
					${sthash}
				</p>
			</div>
		</div>
	</div>
	
	<div class="col-xs-12">
		<div id="statistics">
			<ul class="nav nav-tabs">
				<li role="presentation" class="active"><a href="#tab-package" data-toggle="tab">Package</a></li>
				<li role="presentation"><a href="#tab-titles" data-toggle="tab">Titles</a></li>
			</ul>
			<div class="tab-content">
				<div id="tab-package" role="tabpanel" class="tab-pane active"><br /></div>
				<div id="tab-titles" role="tabpanel" class="tab-pane"><br /></div>
			</div>
		</div>
	
	<g:if test="${json}">	
	<script>

		var statisticsController = {

			buildDomTitle: function (name, url, count){
				
				if(url){
					return $('<h4 class="domTitle"><strong>' + count + '. ' + name + ' - '
								+ '<a href="' + url + '" target="_blank"><span class="glyphicon glyphicon-new-window"></span></a>'
								+ '</strong></h4>')
				}
				else{
					return $('<h4 class="domTitle"><strong>' + count + '. ' + name + '</strong></h4>')
				}
			},
			
			buildDomTable: function (){
				
				return $('<table class="table"><thead><th>Status</th><th>Element</th><th>Vorlage</th><th>Resultat</th></thead><tbody></tbody></table>')
			},
			
			buildDomState: function (elem, row){
				
				if(elem.m.includes('INVALID')){
					$(row).attr('class', 'danger')
				}
				/*
				else if(elem.m.includes('VALIDATOR_DATE_IS_MISSING')){
					$(row).attr('class', 'default')
				}
				*/
				else if(elem.m.includes('MISSING')){
					$(row).attr('class', 'info')
				}
				else if(elem.m.includes('ATOMIC')){
					$(row).attr('class', 'warning')
				}
			},
			
			buildDomValue: function (elem){
				
				if(elem && Array.isArray(elem))
					return elem.join('<br/>')

				return elem
			},
			
			buildBlockTitle: function (elem, count){

				if(elem._meta){
					var display = false
					var div = $('<div></div>')
					
					var table = statisticsController.buildDomTable()
					$(div).append(table)
					
					$(elem._meta).each( function(ii, ielem){
						var row = $('<tr></tr>')
							
						if(ielem.api){
							$(div).prepend(statisticsController.buildDomTitle(elem.name, ielem.api, count))
						}
						else if(ielem.dom){
							display = true
							$(table).append(row)
							
							$(row).append('<td>' + ielem.m + '</td><td>' + ielem.dom + '</td>')
							$(row).append('<td>' + statisticsController.buildDomValue(ielem.org) + '</td>')
							$(row).append('<td>' + statisticsController.buildDomValue(ielem.v) + '</td>')
							//$(row).append('<td><input type="text" value="' + statisticsController.buildDomValue(ielem.v) + '"></td>')
							$(row).attr('data-code', ielem.m)
							
							statisticsController.buildDomState(ielem, row)
						}
					})

					if(display){
						$('#statistics #tab-package').append(div)
					}
					else{
						$('#statistics #tab-package').append($('<div></div>')).append(statisticsController.buildDomTitle(elem.name, null, count))
					}
				}
			},

			buildBlockTipp: function(elem, count){

				if(elem._meta){
					var display = false
					var div = $('<div></div>')
					
					var table = statisticsController.buildDomTable()
					$(div).append(table)
					
					$(elem._meta).each( function(ii, ielem){
						var row = $('<tr></tr>')
						
						if(ielem.api){
							$(div).prepend(statisticsController.buildDomTitle(elem.title.name, ielem.api, count))
						}
						else if(ielem.dom){
							display = true
							$(table).append(row)
							
							$(row).append('<td>' + ielem.m + '</td><td>' + ielem.dom + '</td>')
							$(row).append('<td>' + statisticsController.buildDomValue(ielem.org) + '</td')
							$(row).append('<td>' + statisticsController.buildDomValue(ielem.v) + '</td>')
							//$(row).append('<td><input type="text" value="' + statisticsController.buildDomValue(ielem.v) + '"></td>')
							$(row).attr('data-code', ielem.m)

							statisticsController.buildDomState(ielem, row)
						}
					})
					
					if(display){
						$('#statistics #tab-titles').append(div)
					}
					else{
						$('#statistics #tab-titles').append($('<div></div>')).append(statisticsController.buildDomTitle(elem.title.name, null, count))
					}
				}
			},

			buildMarkUp: function(json){

				$('#statistics-meta .meta-date').text(json.meta.date)
				$('#statistics-meta .meta-ygor').text(json.meta.ygor)
				
				$(json.package.tipps).sort(function(a,b){return a.title.name > b.title.name}).each( function(i, elem){
					statisticsController.buildBlockTipp(elem, i+1)
				})
			
				$(json.titles).sort(function(a,b){return a.name > b.name}).each( function(i, elem){
					statisticsController.buildBlockTitle(elem, i+1)
				})

				$('#statistics div table').addClass('active')
				$('#statistics div table tbody tr').addClass('active')
				
				$('#statistics-filter button').each(function(i, e){
					var filter = '[data-code=' + $(this).attr('data-code') + ']'
					var countPackage = $('#statistics #tab-package div table tr' + filter).size()
					var countTitles  = $('#statistics #tab-titles div table tr' + filter).size()
				
					$(e).addClass('active').addClass($(e).attr('data-class'))
					$($(e).find('.badge').get(0)).text(countPackage)
					$($(e).find('.badge').get(1)).text(countTitles)
				})

				statisticsController.appendEventHandler()
			},

			appendEventHandler: function(){
	
				$('#statistics-filter button').click(function(){
					$(this).toggleClass($(this).attr('data-class'))
					$(this).blur()
						
					var filter = '[data-code=' + $(this).attr('data-code') + ']'
	
					$('#statistics-filter button' + filter).toggleClass('active')
					
					$('#statistics div table tr' + filter).each(function(i, e){
						
						if($('#statistics-filter button' + filter).hasClass('active')){
							$(e).addClass('active')
						}
						else {
							$(e).removeClass('active')
						}
					})
					
					$('#statistics div table tbody tr').each(function(i, e){

						if(0 == $(e).parent('tbody').find('tr.active').size()) {
							$(e).parent('tbody').parent('table').removeClass('active')
						}
						else {
							$(e).parent('tbody').parent('table').addClass('active')
						}
					})
				})
			}
		}

		statisticsController.buildMarkUp(${raw(json)})

	</script>
	</g:if>

	</div>
</div>
