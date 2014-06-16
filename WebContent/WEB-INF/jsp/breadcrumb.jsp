<%@page trimDirectiveWhitespaces="true"%>
<%@page language="java" import="java.util.ArrayList" %>    
<% ArrayList<String> crumbs = (ArrayList)request.getAttribute("breadcrumbs"); %>
<!-- renders breadcrumb navigation object -->
<% for(String crumb : crumbs) { %>
	<a href="<%= crumb %>"><%= crumb %></a>
	<i class=" icon-chevron-right"></i>
<% } %>