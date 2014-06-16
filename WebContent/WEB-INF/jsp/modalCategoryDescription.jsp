<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!-- handles individual test case category and description editing and stores modal form -->

<script>

// variables used for form submission and DOM manipulation
var row, testID, category, description;

// AJAX form submission for category and description update
function submitCategoryDescription() {
	
	// serialize form data for $.ajax() function
	var dataString = $("#formCategoryDescription").serialize();
	console.log(dataString);
	
	$.ajax({
		type: 'post',
		url: '<%=request.getContextPath()%>/category',
			data : dataString,
			dataType : 'json',
			success : function(o) {
				// if response is successful, then update the selected test case table element
				// this way user will not be redirected to another page
				row.find(".category").html(o.category);
				row.find(".description").html(o.description);

			}
		});

		// once done, make modal disappear
		$("#editCategoryDescription").modal("hide");
		return false;
}

	$(document).ready(function() {

		// when the "edit" button is clicked, run the following function
		$(".editTrigger").click(function() {

			// select table row element
			row = $(this).parents("tr");

			// get test ID and send to form
			testID = row.find(".testID").html();
			$("#formTestID").html(testID);
			$("#testIDVal").val(testID);

			// get current "category" and send to form
			category = row.find(".category").html();
			$("#formCategory").val(category);

			// get current "description" and send to form
			description = row.find(".description").html();
			$("#formDescription").val(description);
		});
	});
</script>

<!--  modal -->
<div class="modal hide" id="editCategoryDescription">

	<form id="formCategoryDescription" class="form-horizontal"
		onsubmit="return submitCategoryDescription(); ">

		<div class="modal-header">
			<button type="button" class="close" data-dismiss="modal">x</button>
			<h3>Edit Test Case Detail</h3>
		</div>
		<!-- modal header -->

		<div class="modal-body">

			<fieldset>
				<div class="control-group">
					<label class="control-label"> Test ID </label>
					<div class="controls">
						<label id="formTestID" class="control-label"
							style="text-align: left;"></label> <input type="hidden"
							name="type" value="update" /> <input type="hidden"
							id="testIDVal" name="testID" />
					</div>
					<!-- controls -->
				</div>
				<!-- control-group -->

				<div class="control-group">
					<label class="control-label"> Category </label>
					<!-- control-label -->
					<div class="controls">
						<textarea id="formCategory" class="input-xlarge"
							name="newCategory" placeholder="Image Rendering">
				</textarea>
					</div>
					<!-- controls -->
				</div>
				<!-- control-group -->

				<div class="control-group">
					<label class="control-label"> Description </label>
					<!-- control-label -->
					<div class="controls">
						<textarea id="formDescription" class="input-xlarge"
							name="newDescription" placeholder="Simple test 1">
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
				value="Save changes" />
		</div>
		<!-- modal-footer -->

	</form>

</div>
<!-- modal hide -->