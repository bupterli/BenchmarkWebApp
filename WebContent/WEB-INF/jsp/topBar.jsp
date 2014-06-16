<%@page trimDirectiveWhitespaces="true"%>
<%@page language="java" import="java.util.HashMap" %>
<%@page language="java" import="java.util.Map.Entry" %>
<% HashMap<String, String> reports = (HashMap)request.getAttribute("queryMap"); %>
<% String fullUrl = (String)request.getAttribute("fullUrl"); %>
<% int debug = (Integer)request.getAttribute("DEBUG"); %>

<!-- renders top navigation bar to show links to reports and all pages -->

<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container">
		
			<a class="btn btn-navbar" data-toggle="collapse"
				data-target=".nav-collapse"> <span class="icon-bar"></span> <span
				class="icon-bar"></span> <span class="icon-bar"></span>
			</a>
			<a class="brand" href="<%= request.getContextPath() %>/">Scene7 Benchmark Reporting</a>
			<div class="nav-collapse">
				
				<ul class="nav">
				
					<!-- Menu item 1 -->
					<li class="dropdown">
					<a href="#" class=dropdown-toggle" data-toggle="dropdown">Report <b class="caret"></b></a>
					<ul class="dropdown-menu">
					<% for (Entry<String, String> e : reports.entrySet()) { %>
						<li>
							<a href="<%= request.getContextPath() %>/report?name=<%= e.getKey() %>">
									<%= e.getKey().replace("_"," ") %>
							</a>
						</li>
						<% } %>
					</ul>
					</li>
					
					<!-- Menu item 2 -->
					<li><a href="#runUserQuery" data-toggle="modal">Query</a>
					
					<!-- Menu item 3 -->
					<li><a href="<%= request.getContextPath() %>/category">Category</a></li>
				
				</ul> <!-- /nav -->
				
				<ul class="nav pull-right">
				
					<!-- Menu item 4 -->
					<% if (debug == 1) { %>
					<li><a href="#"><span class="label label-warning">debug mode</span></a></li>
					<% } %>
				
					<!-- Menu item 5 -->
					<form class="navbar-search pull-right" method="get" action="<%= request.getContextPath() %>/report">
						<input type="hidden" name="name" value="byId" />
						<input type="text" class="search-query span2" 
								name="id" value="" placeholder="Search Test ID" />
					</form>
				
				</ul>
			</div> <!-- /nav-collapse -->
			
		</div> <!-- /container -->
	</div> <!-- /navbar-inner -->
</div> <!-- navbar navbar-fixed-top -->


<div class="alert_div"> </div>