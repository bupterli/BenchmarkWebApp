$(document).ready(function() {

	// reset all button
	$('#resetAll').html('<button class="btn btn-mini filter" onclick="resetFilters();">Reset all </button>');
	
	// create a "selected" button when a button is clicked
	$('.btn-toolbar button, .filter, #resetAll').on('click change', function(){
		
		$('.btn-toolbar button').removeAttr('disabled');
		$(this).attr('disabled','');
		
	});
	
	// when select a category, reset all performance change filters
	$('#categorySelector').on('click change', function(){
		$('.btn-toolbar button').removeAttr('disabled');
	});
	
});


// helper function to get url parameter
$.urlParam = function(name) {
	var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
	if(results)
		return results[1];
	else
		return 0;
}


// create custom event when screen size change is complete
$(window).resize(function(){
	
	if(this.resizeTO) clearTimeout(this.resizeTO);
    this.resizeTO = setTimeout(function() {
        $(this).trigger('resizeEnd');
    }, 500);
    
});


// detect and redraw when screen size changes
function autoResizeChart(chart, data, options) {
	
	$(window).bind('resizeEnd', function() { chart.draw(data, options);	});
	
}


// helper function to manually draw chart legend
function drawLegend(data, chartType) {
	
	//draw legend
	var requests = $.extend(true, [], data);
	requests.splice(0,1); // pop first 2 elem
	
	if(chartType == "bar") {
		requests.splice(-1,1); // pop last	
	}
	
	// collapse legend if too many labels
	if(requests.length > 5){
		$("#chart_legend").closest('.collapse').removeClass('in');
	}

	$.each(requests, function(i){
		
		// populate HTML
		var area = '<canvas id="legend_'+i+'" width="20" height="20"></canvas>';
		var label = area + '<h4 style="margin-left:10px;display:inline">'+requests[i]+'</h4><br>';
		$('#chart_legend').append(label);
		
		// fill with color
		var colorIndex = i % color_array.length;
		var canvas = document.getElementById("legend_"+i);
		var ctx = canvas.getContext('2d');
		ctx.fillStyle = color_array[colorIndex];
		ctx.fillRect(0,0,20,20);
		
	});
	
}