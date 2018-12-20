<meta name="layout" content="enrichment">

<div class="row">

	<div class="col-xs-8">	
		<div class="panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title">Filter</h3>
			</div>
			<div id="statistics-filter" class="panel-body">
				
				<button type="button" class="btn btn-xs btn-default" data-class="btn-danger" data-code="VALIDATOR_IDENTIFIER_IS_NOT_ATOMIC">IDENTIFIER_IS_NOT_ATOMIC 
					<span class="badge">0</span>
					<span class="badge">0</span>
				</button>
				<button type="button" class="btn btn-xs btn-default" data-class="btn-info" data-code="VALIDATOR_IDENTIFIER_IS_MISSING">IDENTIFIER_IS_MISSING 
					<span class="badge">0</span>
					<span class="badge">0</span>
				</button>
				
				<br /><br />
				
				<button type="button" class="btn btn-xs btn-default" data-class="btn-danger" data-code="VALIDATOR_DATE_IS_INVALID">DATE_IS_INVALID 
					<span class="badge">0</span>
					<span class="badge">0</span>
				</button>
				<button type="button" class="btn btn-xs btn-default" data-class="btn-info" data-code="VALIDATOR_DATE_IS_MISSING">DATE_IS_MISSING 
					<span class="badge">0</span>
					<span class="badge">0</span>
				</button>
				
				<br /><br />
				
				<button type="button" class="btn btn-xs btn-default" data-class="btn-danger" data-code="VALIDATOR_URL_IS_NOT_ATOMIC">URL_IS_NOT_ATOMIC 
					<span class="badge">0</span>
					<span class="badge">0</span>
				</button>
				<button type="button" class="btn btn-xs btn-default" data-class="btn-info" data-code="VALIDATOR_URL_IS_MISSING">URL_IS_MISSING 
					<span class="badge">0</span>
					<span class="badge">0</span>
				</button>
				
				<br /><br />
				
				<button type="button" class="btn btn-xs btn-default" data-class="btn-warning" data-code="VALIDATOR_PUBLISHER_NOT_MATCHING">PUBLISHER_NOT_MATCHING 
					<span class="badge">0</span>
					<span class="badge">0</span>
				</button>
				<button type="button" class="btn btn-xs btn-default" data-class="btn-info" data-code="VALIDATOR_STRING_IS_MISSING">STRING_IS_MISSING 
					<span class="badge">0</span>
					<span class="badge">0</span>
				</button>
				
				<br /><br />
				
				<button type="button" class="btn btn-xs btn-default" data-class="btn-warning" data-code="VALIDATOR_TIPPURL_NOT_MATCHING">TIPPURL_NOT_MATCHING 
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
					<strong><g:message code="statistic.show.main.filename" /></strong><br />
					<span class="meta-file"></span>
					<br />
					<strong><g:message code="statistic.show.main.creation" /></strong><br />
					<span class="meta-date"></span>
					<br />
					<strong><g:message code="statistic.show.main.ygorversion" /></strong><br />
					<span class="meta-ygor"></span>
					<br />
					<strong><g:message code="statistic.show.main.hash" /></strong><br />
					${sthash}
				</p>
			</div>
		</div>
	</div>
	
	<div class="col-xs-12" id="statistics-substats">
		<div class="panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title">Status</h3>
			</div>
			<div class="panel-body">
                <span class="ignored-kbart-entries">
					<g:message code="statistic.show.sub.ignored" /> <span></span>
                </span>
                |
                <span class="processed-kbart-entries">
					<g:message code="statistic.show.sub.processed" /> <span></span>
                </span>
                |
				<span class="duplicate-key-entries">
					<g:message code="statistic.show.sub.multipleids" /> <span>0</span>
				</span>
                |
                <span class="tipps-delta">
					<g:message code="statistic.show.sub.faultytipps" /> <span></span>
                </span>
                |
                <span class="titles-delta">
					<g:message code="statistic.show.sub.faultytitles" /> <span></span>
                </span>
			</div>
		</div>
	</div>
	
	<div class="col-xs-12">
		<div id="statistics">
			<ul class="nav nav-tabs">
				<li role="presentation" class="active"><a href="#tab-package" data-toggle="tab">Package</a></li>
				<li role="presentation"><a href="#tab-titles" data-toggle="tab">Titles</a></li>
				<li role="presentation"><a href="#tab-debug" data-toggle="tab">Debug</a></li>
			</ul>
			<div class="tab-content">
				<div id="tab-package" role="tabpanel" class="tab-pane active"><br /></div>
				<div id="tab-titles" role="tabpanel" class="tab-pane"><br /></div>
				<div id="tab-debug" role="tabpanel" class="tab-pane"><br /><pre></pre></div>
			</div>
		</div>
	
	<g:if test="${json}">	
	<script>

		var statisticsController = {

			cache: {
				'VALIDATOR_IDENTIFIER_IS_NOT_ATOMIC': [],
				'VALIDATOR_IDENTIFIER_IS_MISSING': [],
				'VALIDATOR_DATE_IS_INVALID': [],
				'VALIDATOR_DATE_IS_MISSING': [],
				'VALIDATOR_URL_IS_NOT_ATOMIC': [],
				'VALIDATOR_URL_IS_MISSING': [],
				'VALIDATOR_STRING_IS_MISSING': [],
				'VALIDATOR_PUBLISHER_NOT_MATCHING': [],
				'VALIDATOR_TIPPURL_NOT_MATCHING': []
			},

			initCache: function(){

				for(key in statisticsController.cache){
					statisticsController.cache[key] = $('#statistics tr[data-code="' + key + '"]')	
				}
			},
					
			buildDomTitle: function(name, url, count){
				
				var links = ""
				for(i in url){
					if("kbart-file" == url[i]){
						links += ' <span class="glyphicon glyphicon-paste"></span>'
					}
					else{
						links += ' <a href="' + url[i] + '" target="_blank"><span class="glyphicon glyphicon-cloud-download"></span></a>'
					}
				}
				if(links){
					links = " - " + links
				}

                if (! name) {
                    name = '<span style="color:red"> faulty </span>'
                }

				return $('<p class="domTitle"><strong>' + count + '. ' + name + links + '</strong></p>')
			},
			
			buildDomTable: function(){
				
				return $('<table class="table"><thead><th>Status</th><th>Element</th><th>Vorlage</th><th>Resultat</th></thead><tbody></tbody></table>')
			},
			
			buildDomState: function(elem, row){
				
				if(elem.m.includes('INVALID')){
					$(row).attr('class', 'danger')
				}
				else if(elem.m.includes('MATCHING')){
					$(row).attr('class', 'warning')
				}
				else if(elem.m.includes('MISSING')){
					$(row).attr('class', 'info')
				}
				else if(elem.m.includes('ATOMIC')){
					$(row).attr('class', 'danger')
				}
			},
			
			buildDomValue: function(elem){
				
				if(elem && Array.isArray(elem))
					return elem.join('<br/>')

				return elem
			},
			
			buildBlockTitle: function(elem, count){

				var display = false
				var div = $('<div></div>')
				
				var table = statisticsController.buildDomTable()
				$(div).append(table)
				
				if(elem._meta.api){
					$(div).prepend(statisticsController.buildDomTitle(elem.name, elem._meta.api, count))
				}

				$(elem._meta.data).each( function(ii, ielem){
					var row = $('<tr></tr>')
					
					display = true
					$(table).append(row)
					
					$(row).append('<td>' + ielem.m + '</td><td>' + ielem.dom + '</td>')
					$(row).append('<td>' + statisticsController.buildDomValue(ielem.org) + '</td>')
					$(row).append('<td>' + statisticsController.buildDomValue(ielem.v) + '</td>')
					
					$(row).attr('data-code', ielem.m)
					
					statisticsController.buildDomState(ielem, row)
					
				})
				
				if(display){
					$('#statistics #tab-titles').append(div)
				}
				else{
					$('#statistics #tab-titles').append($('<div></div>')).append(statisticsController.buildDomTitle(elem.name, null, count))
				}
			},

			buildBlockTipp: function(elem, count){

				var display = false
				var div = $('<div></div>')
				
				var table = statisticsController.buildDomTable()
				$(div).append(table)
				
				if(elem._meta.api){
					$(div).prepend(statisticsController.buildDomTitle(elem.title.name, elem._meta.api, count))
				}
				
				$(elem._meta.data).each( function(ii, ielem){
					var row = $('<tr></tr>')

					display = true
					$(table).append(row)
					
					$(row).append('<td>' + ielem.m + '</td><td>' + ielem.dom + '</td>')
					$(row).append('<td>' + statisticsController.buildDomValue(ielem.org) + '</td')
					$(row).append('<td>' + statisticsController.buildDomValue(ielem.v) + '</td>')

					$(row).attr('data-code', ielem.m)

					statisticsController.buildDomState(ielem, row)
				})
				
				if(display){
					$('#statistics #tab-package').append(div)
				}
				else{
					$('#statistics #tab-package').append($('<div></div>')).append(statisticsController.buildDomTitle(elem.title.name, null, count))
				}
			},

			buildMarkUp: function(json){

				$('#statistics-meta .meta-file').text(json.meta.file)
				$('#statistics-meta .meta-date').text(json.meta.date)
				$('#statistics-meta .meta-ygor').text(json.meta.ygor)

                statisticsController.buildStats(json)

				var debug = JSON.stringify(json.meta.stats, null, '\t', false)
				$('#statistics #tab-debug pre').text(debug)
				
				$(json.package.tipps).sort(function(a,b){return a.title.name > b.title.name}).each( function(i, elem){
					statisticsController.buildBlockTipp(elem, i+1)
				})
			
				$(json.titles).sort(function(a,b){return a.name > b.name}).each( function(i, elem){
					statisticsController.buildBlockTitle(elem, i+1)
				})

				statisticsController.initCache()
				
				$('#statistics div table').addClass('active')
				$('#statistics div table tbody tr').addClass('active')
				
				$('#statistics-filter button').each(function(i, e){
					var filter       = $(this).attr('data-code')
					var countPackage = $('#statistics #tab-package div table tr[data-code=' + filter + ']').size()
					var countTitles  = $('#statistics #tab-titles div table tr[data-code=' + filter + ']').size()
				
					$(e).addClass('active').addClass($(e).attr('data-class'))
					$($(e).find('.badge').get(0)).text(countPackage)
					$($(e).find('.badge').get(1)).text(countTitles)
				})

				statisticsController.appendEventHandler()	
			},

            buildStats: function(json){

                var processedEntries = parseInt(json.meta.stats.general["processed kbart entries"])
                $('#statistics-substats .processed-kbart-entries > span').text(processedEntries)

                var duplicateKeyEntries = json.meta.stats.general["duplicate key entries"].join(", ")
                if (duplicateKeyEntries) {
                    $('#statistics-substats .duplicate-key-entries > span').text(duplicateKeyEntries).addClass("bg-danger")
                }

                var ignoredEntries = json.meta.stats.general["ignored kbart entries"]
                helper('#statistics-substats .ignored-kbart-entries > span', ignoredEntries.length)

                var deltaTipps  = parseInt(json.meta.stats.general["tipps before cleanUp"]) - parseInt(json.meta.stats.general["tipps after cleanUp"])
                helper('#statistics-substats .tipps-delta > span', deltaTipps)

                var deltaTitles = parseInt(json.meta.stats.general["titles before cleanUp"]) - parseInt(json.meta.stats.general["titles after cleanUp"])
                helper('#statistics-substats .titles-delta > span', deltaTitles)

                function helper(selector, val){
                    if (parseInt(val) > 0) {
                        $(selector).text(val).addClass('bg-danger')
                    } else {
                        $(selector).text(val)
                    }
                }
            },

			appendEventHandler: function(){
	
				$('#statistics-filter button').click(function(){
					$(this).toggleClass($(this).attr('data-class'))
					$(this).blur()

					var filter = $(this).attr('data-code')
					$('#statistics-filter button[data-code=' + filter + ']').toggleClass('active')
					
					var elems = statisticsController.cache[filter]
					var filterActive = $('#statistics-filter button[data-code=' + filter + ']').hasClass('active')

					elems.each(function(i, e){	
						if(filterActive){
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
