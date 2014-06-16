<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<!-- renders progress indicator for building and resetting test case categories and description -->

<script>

// progress bar variables
var done = false; var progressString; var progress = 0.0; var redirect = "";

// ping StatusServlet to retrieve current status
function getProgress() {
	$.ajax({
		url: '<%= request.getContextPath() %>/status',
		dataType: 'json',
		success: function(o) {
			progress = o.progress;
			progressString = o.progressString;
		}
	});
	return false;
};

// form submission for test case category and description build / reset request
function buildCategoryDescription(type) {
	
	var dataString 
	if(type == 'build') {
		dataString = $("#buildForm").serialize();
	}else if (type == 'reset'){
		dataString = $("#resetForm").serialize();
	}
	
	$.ajax({
		type: 'post',
		url: '<%= request.getContextPath() %>/category',
		data: dataString,
		dataType: 'json',
		success: function(o) {
			// upon sucessul response, do redirect
			done = true;
			redirect = o.redirect;
			window.location.replace(o.redirect);
		},
	});
	
	return false;
}

function updateProgress(current) {
	
	$(".bar").attr("style", "width: "+current+";");
	
}

function submitBuildReset(type) {
	
	// reveal progress bar
	$(".progress").slideDown("slow");
	
	// make ajax call for category build / reset
	buildCategoryDescription(type);
	
	// busy-wait updating progress bar until buildCategoryDescription() is complete
	// then do redirect
	i = 0;
	setInterval(function(){	
		i++;
		if(done) i = -1;
		
		if(i > 0) { 
			getProgress(); // get current progress from StatusServlet
			updateProgress(progressString);	// update the progress bar
			if(redirect.length > 0 && progress > 1) {
				window.location.replace(redirect);
			}
		}
	}, 1);
	
	return false;
}

</script>

<div class="span12 progress progress-striped progress-success active" style="display: none;">

	<div class="bar" style="width: 0%;"></div>

</div> 