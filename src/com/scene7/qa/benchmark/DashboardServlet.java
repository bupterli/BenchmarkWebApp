package com.scene7.qa.benchmark;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class DashboardServlet
 */
public class DashboardServlet extends BaseServlet {

	/**
	 * Default serial verision ID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Reusable ChartHandler object.
	 */
	private ChartHandler chart;

	/**
	 * Initialize Chart object.
	 */
	public void init() {
		chart = null;
	}

	/**
	 * Renders top 3 pre-defined reports.
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		log.debug("DashboardServlet.doGet()");

		try {

			// create report node instance
			String report = "";
			Reports.ReportNode node = null;

			// invoke print writer
			pw = response.getWriter();

			// get html template
			prepareResponse("Dashboard", pw, this, request, response);

			// template to hold global variables
			request.setAttribute("report1", "Today_vs_Yesterday_Builds");
			request.setAttribute("report2", "Last_Build_vs_Last_Release");
			request.setAttribute("report3",
					"Today_Yesterday_Builds_vs_Last_Release");
			r.includeTemplate("/WEB-INF/jsp/dashboard.jsp");

			// Report 1
			report = "Today_vs_Yesterday_Builds";
			request.setAttribute("report", report);
			node = reports.getReport(report);
			renderPage(node, request, response);

			// Report 2
			report = "Last_Build_vs_Last_Release";
			request.setAttribute("report", report);
			node = reports.getReport(report);
			renderPage(node, request, response);

			// Report 3
			report = "Today_Yesterday_Builds_vs_Last_Release";
			request.setAttribute("report", report);
			node = reports.getReport(report);
			renderPage(node, request, response);

			crumb.resetCrumb();
			crumb.addCrumb(Utilities.getRelativeUrl(request), 0);
			request.setAttribute("breadcrumbs", crumb.crumbStack);

			// get html template
			completeResponse(pw, response);

		} catch (SQLException e) {

			System.err.println(e);
			System.exit(-1);
		}

	}

	/**
	 * Not in use.
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * Helper function that renders ResultSet to chart.
	 * 
	 * @param node
	 *            report node object
	 * @param request
	 *            caller servlet's request object
	 * @param response
	 *            caller servlet's response object
	 * @throws IOException
	 * @throws SQLException
	 *             if result set object contains bad queries
	 * @throws ServletException
	 */
	private void renderPage(Reports.ReportNode node,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, SQLException, ServletException {

		// TODO: debug statement
		log.debug("Dashboard.renderPage()");

		// result set from executed sql
		String sql = node.getSql();
		rs = db.execute(sql);

		// chart properties
		String title = node.getName().replace("_", " ");
		String y = "time_process";
		String x = "";

		// render query result in google chart
		// print message when data is not time series based
		// Note: current hard-coded to render pie charts
		chart = new ChartHandler();
		if (!chart.renderChart(title, x, y, "pie", rs, db, this, request,
				response)) {
			pw.println("<h3>No chart view available for non-time-series based data set</h3>");
		}

		// db.closeConnection();

	}

}
