<%@page language="java" import="java.util.ArrayList" %>
<%
ArrayList<String> categories = (ArrayList) request.getAttribute("categories");
%>

<!-- handles category name change by a given category name and stores modal form -->

<script>

function submitCategoryRename() {
	
	// serialize form data for $.ajax() function
	var dataString = $("#formRenameCategory").serialize();	
	
	// submit change
	$.ajax({
		type: 'post',
		url: '<%=request.getContextPath()%>/category',
			data : dataString,
			dataType : 'json',
			success : function(o) {
				// refresh page when request is successful
				document.location.reload(true);
			}
		});

	// make modal disappear
	$("#renameCategories").modal("hide");
	
	// progress indicator
	var alertString = '<div class="alert alert-info center">' +
						'Category rename in progress <span id=\"dotdotdot\"></span> ' +
						'</div>';
	
	// fade in progress indicator
	$(".alert_div").hide();
	$(".alert_div").html(alertString);
	$(".alert_div").slideDown("slow");
	var dotString = ".";
	
	// update dot dot dot every 1.5 second
	setInterval(function() {
		$("#dotdotdot").html(dotString);
		dotString += ".";
	}, 1000);
	
	return false;
}

</script>

<!--  modal -->
<div class="modal hide" id="renameCategories">

	<form id="formRenameCategory"
		class="form-horizontal"
		onsubmit="return submitCategoryRename(); ">

		<div class="modal-header">
			<button type="button" class="close" data-dismiss="modal">x</button>
			<h3>Rename Categories</h3>
		</div>
		<!-- modal header -->

		<div class="modal-body">

			<fieldset>
				
				<div class="control-group">
					<label class="control-label"> Current Category</label>
					<!-- control-label -->
					<div class="controls">
						
						<select class="span4"
								name="oldCategory">
							<% for(String category : categories) { %> 
							<option value="<%= category %>"><%= category %></option>
							<% } %>
						</select>
					</div>
					<!-- controls -->
				</div>
				<!-- control-group -->

				<div class="control-group">
					<label class="control-label"> New Category </label>
					<!-- control-label -->
					<div class="controls">
						<input type="hidden" name="type" value="renameCategory"/>
						<input type="text" class="input-xlarge"
							name="newCategory">
				</textarea>
					</div>
					<!-- controls -->
				</div>
				<!-- control-group -->

			</fieldset>
		</div>
		<!-- modal-body -->

		<div class="modal-footer">
			<a href="#" class="btn" data-dismiss="modal">Cancel</a>
			<input type="submit" class="btn btn-success"
				value="Rename" />
		</div>
		<!-- modal-footer -->

	</form>

</div>
<!-- modal hide -->