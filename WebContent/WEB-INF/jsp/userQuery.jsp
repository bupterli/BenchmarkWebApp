<!-- stores modal form to submit user query -->

<div class="modal hide" id="runUserQuery">

	<form
		class="form-inline" 
		method="POST" 
		action="<%= request.getContextPath() %>/report">
		
		<div class="modal-header">
			<button type="button" class="close" data-dismiss="modal">x</button>
			<h3>Run custom query</h3>
		</div>
		<!-- modal header -->
		
		<div class="modal-body">

			<fieldset>
				<div class="control-group">
					<label class="control-label"> SQL </label>
					<!-- control-label -->
					<div class="controls">

					
					<textarea style="width:500px; height:90px;"
							name="sql"></textarea>
					</div>
					<!-- controls -->
				</div>
				<!-- control-group -->

			</fieldset>
			
		</div>
		<!-- modal-body -->
		
		<div class="modal-footer">
			<a href="#" class="btn" data-dismiss="modal">Cancel</a> 
			<input type="submit" class="btn btn-primary"
				value="Execute" />
		</div>
		<!-- modal-footer -->
	
	</form>
</div>
<!-- modal hide -->