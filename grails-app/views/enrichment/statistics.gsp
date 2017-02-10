<meta name="layout" content="enrichment">

<div class="row">
	<div class="col-xs-12">
		<br />
		<br />
	</div>
	<div class="col-xs-12">

		<div id="statistics"></div>
	
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
						
						domState(ielem, row)
					}
				})

				if(display){
					$('#statistics').append(div)
				}
				else{
					$('#statistics').append($('<div></div>')).append(domTitle(elem.name, null, count))
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

						domState(ielem, row)
					}
				})
				
				if(display){
					$('#statistics').append(div)
				}
				else{
					$('#statistics').append($('<div></div>')).append(domTitle(elem.title.name, null, count))
				}
			}
		}

		$('#statistics').append('<h3>Tipps</h3>')
		
		$(json.package.tipps).sort(function(a,b){return a.title.name > b.title.name}).each( function(i, elem){
			blockTipp(elem, i+1)
		})
		
		$('#statistics').append('<h3>Titles</h3>')
		
		$(json.titles).sort(function(a,b){return a.name > b.name}).each( function(i, elem){
			blockTitle(elem, i+1)
		})
		
	</script>
	</g:if>

	</div>
</div>
