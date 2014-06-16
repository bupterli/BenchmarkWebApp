<%@page trimDirectiveWhitespaces="true"%>
<% String data = (String)request.getAttribute("data"); %>
<% String title = (String)request.getAttribute("title"); %>
<% String y = (String)request.getAttribute("y"); %>

<!-- handles single test case performance report to show its progress over time. -->

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">

$(document).ready(function() {
	
	// hide then fade in chart sequentially with delay to buffer render time
	$("#chart_div").hide();
	$("#chart_div").fadeIn(300);
	
});

// color pallet
var color_array = ['#0F59A9','#E07804','#609D09','#D12112','#813AA7',
               '#056F73','#EDBD0C','#B9256F','#2F2D9D','#9F4F1A',
               '#507609','#922D3D','#592993','#695153','#C1893E'];

// load Google Visualization library
google.load("visualization", "1", {packages:["corechart"]});

// draw chart on load
google.setOnLoadCallback(drawChart);

// serialized data to data table
var array = [<%= data %>];	

// chart options
var options = {
		title: '<%= title %>',
		colors: color_array,
		lineWidth : 3,
		curveType : 'function',
		legend : { position : 'none', maxLines: 0 }, // disable legend
		interpolateNulls: true,
		chartArea : { top: 30, width : "80%", }
};

function drawChart() {
	
	var data = google.visualization.arrayToDataTable(array);
	
	// create chart object and draw 
	var chart = new google.visualization.LineChart(document.getElementById('chart_div'))
					.draw(data, options);
	
	// draw legend using canvas
	drawLegend(array[0], "line");
	
	// detect and redraw when screen size changes
	autoResizeChart(chart, data, options);
}
</script>

<h6><%@include file='breadcrumb.jsp' %></h6>

<div id="chart_div" class="span12" style="height:400px;"></div>