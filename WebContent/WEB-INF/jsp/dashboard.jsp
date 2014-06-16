<!-- holds global javascript variables used for pieChart.jsp on dashboard page -->

<script>

// global variables used along all charts
var globalDict = {
	'<%= request.getAttribute("report1") %>':'',
	'<%= request.getAttribute("report2") %>':'',
	'<%= request.getAttribute("report3") %>':''
}

$(document).ready(function() {
	
	// hide all reports upon load
	$('#dashboard_<%= request.getAttribute("report1") %>')
		.add($('#dashboard_<%= request.getAttribute("report2") %>'))
		.add($('#dashboard_<%= request.getAttribute("report3") %>'))
		.hide();
	
	// then fade in each report sequentially with delay to buffer render time
	$('#dashboard_<%= request.getAttribute("report1") %>').fadeIn(300);
	$('#dashboard_<%= request.getAttribute("report2") %>').fadeIn(500);
	$('#dashboard_<%= request.getAttribute("report3") %>').fadeIn(800);
	
});

// chart data and stats
var numRows; var array = []; var headers = [];
var data; var reportName = ''; var baseUrl = '';

// color pallet
var color_array = ['#D12112', // red
                   '#507609', // greeb
                   '#EDBD0C', // yellow
                   '#0F59A9'  // blue
                   ];

// load Google Visualization Library
google.load("visualization", "1", {packages:["corechart"]});

// chart options
var options = {
		colors: color_array,
		fontSize: 11,
		legend: { position: 'none'},
		pieSliceText: 'label',
		tooltip : { showColorCode: true },
		is3D : true,
		forceIFrame: false,	// change cursor when hover on	
		chartArea : {
			left : 300,
			top : 0,
			width : "40%",
			height : "90%"
		}
};
</script>