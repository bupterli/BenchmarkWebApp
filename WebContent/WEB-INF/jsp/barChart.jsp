<%@page trimDirectiveWhitespaces="true" %>
<%@page language="java" import="java.util.ArrayList" %>
<%@page language="java" import="java.util.HashMap" %>
<%
	String data = (String) request.getAttribute("data");
	String title = (String) request.getAttribute("title");
	String y = (String) request.getAttribute("y");
	ArrayList<HashMap<String, String>> header = (ArrayList) request.getAttribute("header");
	ArrayList<String> categories = (ArrayList) request.getAttribute("categories");
%>

<!-- handles performance variation reports. e.g. Today vs Yesterday's builds, Last Build vs Last Release -->

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">

$(document).ready(function() {
	
	// hide chart then fade in through out 2 seconds to buffer render time
	$('#chart_div').hide();
	$('#chart_div').fadeIn(2000);
	
	// reset all filters
//	resetFilters();

});


//Added by Weihua on 31.03.2014
var urls_array = [];

// chart instance
var barChart; var dashboard;

// chart filters
var slider; var stringFilter; var categoryFilter; var descriptionFilter;

// chart data structures
var array = []; var headers = []; var data;

// chart stats
var totalNumRows = 0; var numRows = 0;

// color pallet
var color_array = ['#0F59A9','#E07804','#609D09','#D12112','#813AA7',
               '#056F73','#EDBD0C','#B9256F','#2F2D9D','#9F4F1A',
               '#507609','#922D3D','#592993','#695153','#C1893E', '#808284'];
               
// chart options
var options = {
		colors: color_array,
		axisTitlesPosition: 'in',
		fontSize: 11,
		legend: { position: 'none'},
		bar: { groupWidth : "60%" },
		chartArea : {
			left : 350,
			top : 0,
			width : "100%",
			height : "95%"
		},
		vAxis : {
			title: 'Test Case ',
			titleTextStyle: {fontSize: 9 },
		},
		hAxis : {
			title: 'Milliseconds',
			titleTextStyle: {fontSize: 9 },
			gridlines : { color: "#F4F4F4", count : 10}
		},
		tooltip : { textStyle : { fontSize: 13 } },
		forceIFrame: false,	// change cursor when hover on
};

// load google visualization library and render chart on load
google.load("visualization", "1", {packages:["controls", "corechart"]});
google.setOnLoadCallback(drawChart);


function drawChart() {
	console.log("google onload");

	// Create datatable
	data = new google.visualization.DataTable();
	<%for (HashMap<String, String> column : header) {%>
		<%if (column.get("value").length() > 0) {%>
			data.addColumn('<%=column.get("type")%>', '<%=column.get("value")%>');
			headers.push('<%=column.get("value")%>');
		<%} else {%>
			data.addColumn({type: 'string', role: 'annotation'});	// date: build_date
			data.addColumn({type: 'string', role: 'annotationText'});	// url: full requet string
			data.addColumn({type: 'number', role: 'tooltip'});	// time: process time
			data.addColumn({type: 'string', role: 'interval'});	// category
		<%}	}%>
	
	array =  [<%=data%>];
	data.addRows(array);
	totalNumRows = data.getNumberOfRows();
	
	// Disable sort because of adding copy url button
	// show test cases with biggest performance decrease first
//	data.sort({column: data.getNumberOfColumns()-1, desc: true});
	
	// Number range filter
  	slider = new google.visualization.ControlWrapper({
	    'controlType': 'NumberRangeFilter',
	    'containerId': 'chart_slider',
	    'options': {
	      'filterColumnLabel': 'diff',
	      'minValue': -1000,
	      'maxValue': 100,
	      'ui' : {
	    	  'label' : 'Filter by variation',
	    	  'labelSeparator' : '(%)',
	    	  'labelStacking' : 'verical'
	      }
	    }
	  });
	
 	// hide number range filter, use buttons instead
 	$("#chart_slider").hide();
 	
	// String filters
	// 1) Category filter
	categoryFilter = new google.visualization.ControlWrapper({
		'controlType' : 'StringFilter',
  		'containerId' : 'chart_string_filter1',
  		'options' : {
  			'filterColumnIndex' : '5', // filter by request URL
  			'matchType' : 'exact',
  			'ui' : { 'label' : 'Filter by Category' }
  		}
	});
	google.visualization.events.addListener(categoryFilter, 'ready', function(){
		console.log('filter loaded');
		
	});
	
	// 2) Description filter
	descriptionFilter = new google.visualization.ControlWrapper({
		'controlType' : 'StringFilter',
  		'containerId' : 'chart_string_filter2',
  		'options' : {
  			'filterColumnIndex' : '0', // filter by request URL
  			'ui' : { 'label' : 'Filter by Description' }
  		}
	});
	
	// 3) URL filter
  	var urlFilter = new google.visualization.ControlWrapper({
  		'controlType' : 'StringFilter',
  		'containerId' : 'chart_string_filter3',
  		'options' : {
  			'filterColumnIndex' : '4', // filter by request URL
  			'matchType' : 'any',
  			'ui' : { 'label' : 'Filter by URL string' }
  		}
  	});
	

	//Modified on 2014.03.17
	// visible columns, hide last column  
  	var visibleColumns = [];
  	for (i=0; i<data.getNumberOfColumns()-1; i++) { visibleColumns.push(i); }
  	//var visibleColumns_new = [0,1,2,3,5,6,7,8];
  	
	// create chart wrapper
	barChart = new google.visualization.ChartWrapper({
		'chartType' : 'BarChart',
		'containerId' : 'chart_div',
		'options' : options,
		'view' : { 'columns' : visibleColumns} // hide diff column
	});
	
	// Modified on 01.04.2014
	google.visualization.events.addListener(barChart, 'ready', function(){
		console.log('barchart loaded');
		
		urls_array  =  barChart.getDataTable();
		
		d3.selectAll('#chart_div svg > g:nth-of-type(1) > g:nth-of-type(3) > g').each(function(d,i){if(i>=9){d3.select(this).append("foreignObject") 
			.attr("x", 0)     
	 		.attr("y", 57*(i-9)+40) 
	  		.attr("width", 120) 
	  		.attr("height", 30) 
	  		.append("xhtml:body") 
			.style("padding",0) 
			.html('<div style="display:none" id="clipboard_text_' + i +'">' + urls_array.getValue(i-9, 0) +'</div><button class="btn btn-button" data-clipboard-target="clipboard_text_' + i +'">Copy URL</button>'); 
		}});
		 
		var client = new ZeroClipboard($('button.btn.btn-button'));

		 client.on('load', function(client) {
			  client.on('complete', function(client, args) {
	  		  alert("Copied Full Requet URL to clipboard: \n\n" + args.text);
		 	});
	 	 });

		 client.on('wrongflash noflash', function() {
			  ZeroClipboard.destroy();
		 });
		 
	});

	// render chart
	dashboard = new google.visualization.Dashboard(document.getElementById('dashboard'))
		.bind([slider, urlFilter, descriptionFilter, categoryFilter], barChart);
	dashboard.draw(data);
	
	// chart Height Auto-resizing
	google.visualization.events.addListener(dashboard, 'ready', function(){
		
		var numRows = barChart.getDataTable().getNumberOfRows();
		var expectedHeight = numRows * 60;
		if(numRows == 0) expectedHeight = 60;
		
		if(parseInt(barChart.getOption('height'), 10) != expectedHeight) {
			
			$("#numRecords").html(numRows + " / " +  totalNumRows + " records");
			barChart.setOption('height', expectedHeight);
			barChart.draw();
		}
		
	});

	// enable clicking on chart to view test case detail
	google.visualization.events.addListener(barChart, 'select', function (){
		var chartObject = barChart.getChart();
		var selectedItem = chartObject.getSelection()[0];
		var selectedData = barChart.getDataTable();
		
		if(selectedItem) {
			
			var description = selectedData.getValue(selectedItem.row, 0);
			
			// redirect when click
			window.location.href = '<%=request.getContextPath()%>/report?name=custom&description='
					+ escape(description);
		}
	});

	// draw legend using canvas
	drawLegend(headers, "bar");

	// detect when screen  
	autoResizeChart(barChart, data, options);

	// apply default filter
	defaultFilter(slider);
}

// filtering functions
function resetFilters() {
	
	// reset performance difference filter
	slider.setState({ 'lowValue' : -1000, 'highValue' : 100 });
	slider.draw();

	// reset category filter
	$("#categorySelector").val("");
	categoryFilter.setState({'value': ''});
	categoryFilter.draw();
	
	//barChart.draw();
}

function filterCategory(category) {
		
	// reset performance difference filter
	slider.setState({ 'lowValue' : -1000, 'highValue' : 100	});
	slider.draw();
	
	categoryFilter.setState({'value': category});
	categoryFilter.draw();
	
	//barChart.draw();
}

function filterResult(low, high) {
	
	slider.setState({ 'lowValue' : low, 'highValue' : high});
	slider.draw();
}

function defaultFilter() {

	var filter = $.urlParam('filter');

	if (filter == 'pos') {
		
		filterResult(-100, 0);
		$("#pos").attr("disabled", "disabled");
		
	} else if (filter == 'neg') {
		
		filterResult(0, 100);
		$("#neg").attr("disabled", "disabled");
		
	} else if (filter == 'unchanged') {
		
		filterResult(0, 0);
		$("#unchanged").attr("disabled", "disabled");
		
	} else if (filter == 'new') {
		filterResult(-1000, -1000);
	}

}


$(document).ready(function() { 
	console.log("dom ready");
});

window.onload = function(){
	console.log("window onload");
}


</script>



<div class="row">

<div id="dashboard" class="span12">


	<h6><%@include file='breadcrumb.jsp'%></h6>
	<div class="row">

		<div class="span8">
			<h3><%=title%>
				(sorted by most performance decreased)
			</h3>
			<br>
		</div>
		<!-- /span8 -->

		<div class="span3 right">
			<h3 id="numRecords" class="inline"></h3>
			<br>
		</div>
		<!-- /span3 -->
	</div><!-- /row -->
	<div class="row">
		<div class="span8">
			<div class="btn-toolbar">
			
				<h5>Category</h5>
				
				<select id="categorySelector" 
						class="span6" 
						onchange="filterCategory(this.options[this.selectedIndex].value);">
					<option value="">All test cases</option>
					<% for(String category : categories) { %> 
					<option value="<%= category %>"><%= category %></option>
					<% } %>
				</select>
				
				<br>

				<h5>Filter by performance change</h5>
				<div class="btn-group">
					<button class="btn btn-mini btn-success" onclick="filterResult(-100,0);"
						id="pos">All faster</button>
					<button class="btn btn-mini btn-success" onclick="filterResult(-100,-5);">Faster
						by 5% or more</button>
					<button class="btn btn-mini btn-success" onclick="filterResult(-100,-10);">Faster
						by 10% or more</button>
					<button class="btn btn-mini btn-success" onclick="filterResult(-100,-20);">Faster
						by 20% or more</button>
					<button id="unchanged" class="btn btn-mini btn-success" onclick="filterResult(0,0);">Same performance</button>
				</div>
				
				<br>

				<div class="btn-group">
					<button class="btn btn-mini btn-danger" onclick="filterResult(0,100);"
						id="neg">All slower</button>
					<button class="btn btn-mini btn-danger" onclick="filterResult(5,100);">Slower
						by 5% or more</button>
					<button class="btn btn-mini btn-danger" onclick="filterResult(10,100);">Slower
						by 10% or more</button>
					<button class="btn btn-mini btn-danger" onclick="filterResult(20,100);">Slower
						by 20% or more</button>
					<button id="unchanged" class="btn btn-mini btn-danger" onclick="filterResult(0,0);">Same performance</button>
						
				</div>
				
				<br>
				
				<div id="resetAll"></div>

			</div>

		</div>
		<!-- /span8 -->

		<div class="span3">
			<div class="filters">
				<div id="chart_string_filter1"></div>
				<div id="chart_string_filter2"></div>
				<div id="chart_string_filter3"></div>
				<div id="chart_slider" class="line_break"></div>
			</div>
		</div>
		<!-- /span3 -->
	</div>
	
	

	<div class="row">
		<div id="chart_div" class="span12"></div>
	</div>

</div>

</div><!-- /row -->