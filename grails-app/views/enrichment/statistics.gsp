<meta name="layout" content="enrichment">

<div class="row">
	<div class="col-xs-12">
		<br />
		<br />
	</div>
	<div class="col-xs-12">
	
		<div class="panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title">Filter</h3>
			</div>
			<div id="statistics-filter" class="panel-body">
				<button type="button" class="btn btn-xs btn-default" data-class="btn-danger" data-code="VALIDATOR_DATE_IS_INVALID">DATE_IS_INVALID <span class="badge">0</span></button>
				<br /><br />
				<button type="button" class="btn btn-xs btn-default" data-class="btn-warning" data-code="VALIDATOR_IDENTIFIER_IS_NOT_ATOMIC">IDENTIFIER_IS_NOT_ATOMIC <span class="badge">0</span></button>
				<button type="button" class="btn btn-xs btn-default" data-class="btn-warning" data-code="VALIDATOR_URL_IS_NOT_ATOMIC">URL_IS_NOT_ATOMIC <span class="badge">0</span></button>
				<br /><br />
				<button type="button" class="btn btn-xs btn-default" data-class="btn-info" data-code="VALIDATOR_DATE_IS_MISSING">DATE_IS_MISSING <span class="badge">0</span></button>
				<button type="button" class="btn btn-xs btn-default" data-class="btn-info" data-code="VALIDATOR_IDENTIFIER_IS_MISSING">IDENTIFIER_IS_MISSING <span class="badge">0</span></button>
				<button type="button" class="btn btn-xs btn-default" data-class="btn-info" data-code="VALIDATOR_STRING_IS_MISSING">STRING_IS_MISSING <span class="badge">0</span></button>
				<button type="button" class="btn btn-xs btn-default" data-class="btn-info" data-code="VALIDATOR_URL_IS_MISSING">URL_IS_MISSING <span class="badge">0</span></button>
			</div>
		</div>

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

		var json = ${raw(json)}


		function domTitle(name, url, count){
			if(url){
				return $('<h4 class="domTitle"><strong>' + count + '. <a href="' + url + '" target="_blank">' + name + '</a></strong></h4>')
			}
			else{
				return $('<h4 class="domTitle"><strong>' + count + '. ' + name + '</strong></h4>')
			}
		}
		
		function domTable(){
			return $('<table class="table"><thead><th>Status</th><th>Element</th><th>Vorlage</th><th>Resultat</th></thead><tbody></tbody></table>')
		}

		function domState(elem, row){
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
		}

		function domValue(elem){
			if(elem && Array.isArray(elem))
				return elem.join('<br/>')

			return elem
		}

		function blockTitle(elem, count){

			if(elem._meta){
				var display = false
				var div = $('<div></div>')
				
				var table = domTable()
				$(div).append(table)
				
				$(elem._meta).each( function(ii, ielem){
					var row = $('<tr></tr>')
					$(table).append(row)
						
					if(ielem.api){
						$(div).prepend(domTitle(elem.name, ielem.api, count))
					}
					else if(ielem.dom){
						display = true
						
						$(row).append('<td>' + ielem.m + '</td><td>' + ielem.dom + '</td>')
						$(row).append('<td>' + domValue(ielem.org) + '</td><td>' + domValue(ielem.v) + '</td>')
						$(row).attr('data-code', ielem.m)
						
						domState(ielem, row)
					}
				})

				if(display){
					$('#statistics #tab-package').append(div)
				}
				else{
					$('#statistics #tab-package').append($('<div></div>')).append(domTitle(elem.name, null, count))
				}
			}
		}

		function blockTipp(elem, count){

			if(elem._meta){
				var display = false
				var div = $('<div></div>')
				
				var table = domTable()
				$(div).append(table)
				
				$(elem._meta).each( function(ii, ielem){
					var row = $('<tr></tr>')
					$(table).append(row)
					
					if(ielem.api){
						$(div).prepend(domTitle(elem.title.name, ielem.api, count))
					}
					else if(ielem.dom){
						display = true
						
						$(row).append('<td>' + ielem.m + '</td><td>' + ielem.dom + '</td>')
						$(row).append('<td>' + domValue(ielem.org) + '</td><td>' + domValue(ielem.v) + '</td>')
						$(row).attr('data-code', ielem.m)

						domState(ielem, row)
					}
				})
				
				if(display){
					$('#statistics #tab-titles').append(div)
				}
				else{
					$('#statistics #tab-titles').append($('<div></div>')).append(domTitle(elem.title.name, null, count))
				}
			}
		}

		// output 
		
		$(json.package.tipps).sort(function(a,b){return a.title.name > b.title.name}).each( function(i, elem){
			blockTipp(elem, i+1)
		})
		
		$(json.titles).sort(function(a,b){return a.name > b.name}).each( function(i, elem){
			blockTitle(elem, i+1)
		})
		
		// filter
		
		$('#statistics-filter button').each(function(i, e){
			var filter = '[data-code=' + $(this).attr('data-code') + ']'
			var counts = $('#statistics div table tr' + filter).size()
			
			$(e).addClass('active').addClass($(e).attr('data-class'))
			$(e).find('.badge').text(counts)
		})
		
		$('#statistics-filter button').click(function(){
			
			var filter = '[data-code=' + $(this).attr('data-code') + ']'

			$('#statistics-filter button' + filter).toggleClass('active')
			
			$('#statistics div table tr' + filter).each(function(i, e){
				
				if($('#statistics-filter button' + filter).hasClass('active')){
					$(e).show().addClass('active')
				}
				else {
					$(e).hide().removeClass('active')
				}
			})
			
			$(this).toggleClass($(this).attr('data-class'))
			$(this).blur()
		})
		
	</script>
	</g:if>

	</div>
</div>
