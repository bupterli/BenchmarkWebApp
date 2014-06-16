package com.scene7.qa.benchmark;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A class that handles renders result set as charts and HTML tables.
 * 
 * @author hewong
 * @version Jul 25, 2012
 * @todo TODO
 */
public class ReportServlet extends BaseServlet {

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
	 * Handles requested test type from predefined SQL statement sets.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException {

		// TODO: debug statement
		log.debug("ReportServlet.doGet()");

		try {

			// get report name from url
			String name = request.getParameter("name");

			// default query type
			if (name == null)
				name = "Today_Yesterday_Builds_vs_Last_Release";

			// create report node instance
			Reports.ReportNode node = reports.getReport(name);

			// query by description or test case id
			if (name.equals("custom")) {

				String description = request.getParameter("description");
//				String sql = "SELECT c.id, t.time_parse, t.time_fetch, t.time_process, o.server_revision, c.url, o.date "
//						+ "FROM benchmark_testcase_times t, benchmark_testcases c, benchmark_container o WHERE t.testcases_id = c.id  and t.benchmark_container_id = o.id and "
//						+ "c.description like '"
//						+ description
//						+ "' ORDER BY o.date ";
				String sql = "SELECT c.id, t.time_parse, t.time_fetch, t.time_process, o.server_revision, c.url, o.date "
						+ "FROM benchmark_testcase_times t, benchmark_testcases c, benchmark_container o WHERE t.testcases_id = c.id  and t.benchmark_container_id = o.id and "
						+ "c.url like '"
						+ description
						+ "' ORDER BY o.date ";

				node = reports.addReport("", "", sql, "");

			} else if (name.equals("byId")) {

				String id = request.getParameter("id");
				String sql = "SELECT c.id, t.time_parse, t.time_fetch, t.time_process, o.server_revision, c.url, o.date "
						+ "FROM benchmark_testcase_times t, benchmark_testcases c, benchmark_container o WHERE t.testcases_id = c.id  and t.benchmark_container_id = o.id and "
						+ "c.id = " + id + " ORDER BY o.date ";

				node = reports.addReport("", "", sql, "");
			}

			// create breadcrumb navigation here
			if (node.chartType.equalsIgnoreCase("pie")) {

			} else if (node.chartType.equalsIgnoreCase("bar")) {

				// Get a list of existing categories for filtering
				String sql = q.queryMap.get("test_cases");
				rs = db.execute(sql);
				Classifier classifier = new Classifier();
				ArrayList<String> categories = classifier.getCategories(rs);
				request.setAttribute("categories", categories);

				// bar chart is used on report page
				// set level limit to 2, assuming that referer is dashboard page
				crumb.addCrumb(Utilities.getRelativeUrl(request), 1);

			} else {

				// line chart is used on individual test case report page
				// set level limit to 3, assuming that referes are dashboard,
				// and report page
				crumb.addCrumb(Utilities.getRelativeUrl(request), 2);

			}

			// pass breadcrumb stack to jsp template for rendering
			request.setAttribute("breadcrumbs", crumb.crumbStack);

			// helper method to render chart and table
			renderPage(node, request, response);

		} catch (IOException e) {

			System.err.println(e);
			System.exit(-1);

		} catch (SQLException e) {

			System.err.println(e);
			System.exit(-1);
		}
	}

	/**
	 * Handles user defined query from HTML POST form submission.
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) {

		// TODO: debug statement
		log.debug("ReportServlet.doPost()");

		try {

			// get sql statement from user form submission
			String sql = request.getParameter("sql");
			System.out.println(sql);

			// create temporary report instance
			Reports.ReportNode node = reports.addReport("", "", sql, "");

			crumb.addCrumb(Utilities.getRelativeUrl(request), 1);
			request.setAttribute("breadcrumbs", crumb.crumbStack);

			// helper method to render chart and table
			renderPage(node, request, response);

		} catch (SQLException e) {

			System.err.println(e);
			System.exit(-1);

		} catch (ServletException e) {

			System.err.println(e);
			System.exit(-1);

		} catch (IOException e) {

			System.err.println(e);
			System.exit(-1);
		}
	}

	/**
	 * Helper function that renders ResultSet to chart and HTML table.
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
		log.debug("ReportServlet.renderPage()");

		// result set from executed sql
		String sql = node.getSql();
		rs = db.execute(sql);

		// invoke print writer
		pw = response.getWriter();

		// get html template
		prepareResponse("Report", pw, this, request, response);

		// chart properties
		String title = node.getName().replace("_", " ");
		String y = "time_process";
		String x = "";

		// use accordion to collapse 1) report type, 2) query executed and 3)
		// run user defined query
		String accordion = "<div class=\"row\"><div class=\"accordion\" id=\"accordion\">";
		pw.println(accordion);

		// 1) sql query information
		accordion = "<div class=\"accordion-group\">"
				+ "		<div class=\"accordion-heading\">"
				+ "			<a class=\"accordion-toggle\" data-toggle=\"collapse\" data-parent=\"#accordion\" href=\"#collapseTwo\">"
				+ "			<h4>Query executed</h4>" + "			</a>" + "		</div>"
				+ "	<div id=\"collapseTwo\" class=\"accordion-body collapse\">"
				+ "			<div class=\"accordion-inner\">";
		pw.println(accordion);
		pw.println("<pre class=\"prettyprint linenums\">" + sql + "</pre>");
		accordion = " </div></div></div>";
		pw.println(accordion);

		// 2) print chart legend
		accordion = "<div class=\"accordion-group\">"
				+ "		<div class=\"accordion-heading\">"
				+ "			<a class=\"accordion-toggle\" data-toggle=\"collapse\" data-parent=\"#accordion\" href=\"#collapseFour\">"
				+ "			<h4>Chart legend</h4>"
				+ "			</a>"
				+ "		</div>"
				+ "	<div id=\"collapseFour\" class=\"accordion-body collapse in\">"
				+ "			<div class=\"accordion-inner\">";
		pw.println(accordion);
		pw.println("<div id=\"chart_legend\" class=\"span12\"></div>");
		accordion = " </div></div></div></div>";
		pw.println(accordion);

		// close accordion div
		accordion = "</div>";
		pw.println(accordion);

		// render query result in google chart
		// print message when data is not time series based
		chart = new ChartHandler();
		if (!chart.renderChart(title, x, y, node.chartType, rs, db, this,
				request, response)) {

			pw.println("<h3>No chart view available for non-time-series based data set</h3>");

		}

		// TODO: disabled render query result in html table
		if (DEBUG)
			Utilities.renderResultSet(pw, rs, false);

		// get html template
		completeResponse(pw, response);

		// close db connection when result set is no longer in use
		// db.closeConnection();
	}
}
