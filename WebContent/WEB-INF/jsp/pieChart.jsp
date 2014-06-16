<%@page language="java" import="java.util.ArrayList" %>
<%@page language="java" import="java.util.HashMap" %>
<% 
	String data = (String)request.getAttribute("data");
	String title = (String)request.getAttribute("title");
	String y = (String)request.getAttribute("y");
	String reportName = (String)request.getAttribute("report");
	ArrayList<HashMap<String, String>> header = (ArrayList)request.getAttribute("header"); 
%>

<!-- renders report summary charts on dashboard -->

<script type="text/javascript">

// reset reused objects
array = []; headers = []; data = []; 

// build local data structure for this chart / report
baseUrl = '<%= request.getContextPath() %>/report?name=<%= reportName %>';
var changeDict_<%= reportName %> = { 
					'Slower' : {'val': 0, 'url': baseUrl+"&filter=neg"}, 
					'Faster' : {'val': 0, 'url': baseUrl+"&filter=pos"}, 
					'Unchanged' : {'val': 0, 'url': baseUrl+"&filter=unchanged"},
					'New' : {'val': 0, 'url': baseUrl+"&filter=new"},
				 };
				 
google.setOnLoadCallback(drawChart_<%= reportName %>);

function drawChart_<%= reportName %>() {
	
	$("#title_<%= reportName %>").html('<a href="<%= request.getContextPath() %>/report?name=<%= reportName %>">'
								+'Performance of <%= title %></a>');

	// Create datatable
	data = new google.visualization.DataTable();
	<% 	for(HashMap<String, String> column : header) {
			if(column.get("value").length() > 0) { %>
				data.addColumn('<%= column.get("type") %>', '<%= column.get("value") %>');
				headers.push('<%= column.get("value") %>');
			<% } else { %>
				data.addColumn({type: 'string', role: 'tooltip'});	// date
				data.addColumn({type: 'string', role: 'tooltip'});	// url
				data.addColumn({type: 'number', role: 'tooltip'});	// time process
				data.addColumn({type: 'string', role: 'tooltip'});	// category
	<% 	} } %>
	
	// add serialized data
	// show test cases with biggest performance decrease first
	data.addRows([<%= data %>]);
	data.sort({column: data.getNumberOfColumns()-1, desc: true});
	
	// build change dictionary
	var delta = 0.0;
	for(var i=0; i<data.getNumberOfRows(); i++) {
		
		delta = data.getValue(i, data.getNumberOfColumns()-1);
		
		if(delta < 100 && delta > -100){
			if(delta > 0) {
				changeDict_<%= reportName %>['Slower']['val'] =
					changeDict_<%= reportName %>['Slower']['val']+1;
					
			}else if (delta == 0) {
				changeDict_<%= reportName %>['Unchanged']['val'] = 
					changeDict_<%= reportName %>['Unchanged']['val']+1;
					
			} else if (delta < 0){
				changeDict_<%= reportName %>['Faster']['val'] = 
					changeDict_<%= reportName %>['Faster']['val']+1;
			}	
			
		} else {
			changeDict_<%= reportName %>['New']['val'] = 
				changeDict_<%= reportName %>['New']['val']+1;	
		}
		
	}
	
	// construct test case metadata
	var total = data.getNumberOfRows();
	var summary = '<ul class="unstyled inline">' +
			'<li><a href="'+changeDict_<%= reportName %>['Faster']['url']+'">'
				+ '<h1 class="inline">'+ changeDict_<%= reportName %>['Faster']['val'] 
				+ '</h1> of '+total+' are faster </a></li>' +
			'<li><a href="'+changeDict_<%= reportName %>['Slower']['url']+'">'
				+ '<h1 class="inline">'+ changeDict_<%= reportName %>['Slower']['val'] 
				+ '</h1> of '+total+' are slower </a></li>';
				
	// display new test cases	
	if(changeDict_<%= reportName %>['New']['val'] > 0) {
		summary += '<li><a href="'+changeDict_<%= reportName %>['New']['url']+'">'
		+ '<h1 class="inline">'+ changeDict_<%= reportName %>['New']['val'] 
		+ '</h1> of '+total+' are new test cases</a></li>';
	}
	
	// display test cases with same performance 
	if(changeDict_<%= reportName %>['Unchanged']['val'] > 0) {
		summary += '<li><a href="'+changeDict_<%= reportName %>['Unchanged']['url']+'">'
		+ '<h1 class="inline">'+ changeDict_<%= reportName %>['Unchanged']['val'] 
		+ '</h1> of '+total+' have same performance (<%= request.getAttribute("threshold") %>% Threshold )'
		+ '</a></li>';
	}
	summary += '</ul>';
	
	// show test cases metadata
	//$('#numRecords_<%= reportName %>').html(data.getNumberOfRows() + " records");
	$('#info_div_<%= reportName %>').html(summary);
	
	// push to global dictionary
	globalDict['<%= reportName %>'] = changeDict_<%= reportName %>;
	
	// build data table
	var dataTable = [['Performance change', 'Percentage']];
	for (var key in changeDict_<%= reportName %>) {
		dataTable.push([key, changeDict_<%= reportName %>[key].val]);
	}
	
	// convert to google dataTable
	data = google.visualization.arrayToDataTable(dataTable);
  	
  	// instantiate chart then call draw function
	var chart = new google.visualization.PieChart(document.getElementById('chart_div_<%= reportName %>'));
	chart.draw(data, options);
	
	// add listener to handler redirect when chart is clicked
	google.visualization.events.addListener(chart, 'select', function() {
		
		var selectedItem = chart.getSelection()[0];
        if (selectedItem) {
          var key = data.getValue(selectedItem.row, 0);
          
          // redirect to the report that this pie chart represents
          window.location.href = globalDict['<%= reportName %>'][key].url;
		}
	});
	
}


</script>
	
<div id="dashboard_<%= reportName %>" class="span12" style="margin-top:10px;">

	<div class="row">
	
		<div class="span9">
			<h3 id="title_<%= reportName %>"></h3><br>
		</div><!-- /span9 -->
		
		<div class="span2 right">
			<h4 id="numRecords_<%= reportName %>" class="inline"></h4>
			<br>
		</div><!-- /span2 -->
	
	</div>
	
	<div class="row">
		<div id="info_div_<%= reportName %>" class="span5"></div>
		<div class="span7">
			<div id="chart_div_<%= reportName %>" class="pie"></div>
		</div>
		
	</div>
	
</div>

