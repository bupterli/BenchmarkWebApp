package com.scene7.qa.benchmark;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A class that handles chart rendering. <br>
 * Currently handles line chart for time series data, and bar chart for
 * comparing two data sets
 * 
 * @author hewong
 * @version Jul 25, 2012
 * @todo TODO
 */
public class ChartHandler {

	/**
	 * Data structure to store chart type (line / bar) by their report names. <br>
	 * Example:
	 * 
	 * <pre>
	 * {
	 * 	 "Today_Yesterday_Builds" : "bar",
	 * 	 "Has_Alpha" : "line",
	 * }
	 * </pre>
	 */
	protected HashMap<String, String> reportTypes;

	/**
	 * A set to store all available bar chart report names.
	 */
	protected HashSet<String> barChartNames;

	/**
	 * A set to store all available line chart report name.
	 */
	protected HashSet<String> lineChartNames;

	/**
	 * Initialize reportType data structure and create default report names with
	 * chart types.
	 */
	ChartHandler() {

		reportTypes = new HashMap<String, String>();
		populateChartTypes();
	}

	/**
	 * Create default report name and chart types and store in local data
	 * structures.
	 */
	private void populateChartTypes() {

		// report to compare performance of today's and yesterday's build
		reportTypes.put("Today_vs_Yesterday_Builds", "bar");

		// report to compare all test cases that have keyword "cymk" in its URL
		reportTypes.put("Has_CMYK", "line");

		// report to compare all test cases that have keyword "alpha" in its URL
		reportTypes.put("Has_Alpha", "line");

		// report to compare all test cases
		reportTypes.put("All_Tests", "line");

		// initialize line chart names set
		lineChartNames = new HashSet<String>();
		lineChartNames.add("Has_CMYK");
		lineChartNames.add("Has_Alpha");
		lineChartNames.add("All_Tests");

		// initialize bar chart names set
		barChartNames = new HashSet<String>();
		barChartNames.add("Today_vs_Yesterday_Builds");
		barChartNames.add("Today_Yesterday_Builds_vs_Last_Release");
		barChartNames.add("Last_Build_vs_Last_Release");

	}

	/**
	 * Handles data structure conversion and detect if given data set can be
	 * rendered as chart, then call Google Visualization API to render chart in
	 * either barChart.jsp or lineChart.jsp.
	 * 
	 * @param title
	 *            chart title
	 * @param xAxis
	 *            x axis name (currently not in use)
	 * @param yAxis
	 *            y axis name
	 * @param type
	 *            chart type
	 * @param rs
	 *            result set that will be rendered
	 * @param serv
	 *            caller servlet
	 * @param req
	 *            caller servlet request to include templates
	 * @param res
	 *            caller servlet response to include templates
	 * @return if provided data set can be rendered as chart or not
	 * @throws SQLException
	 *             if result set cannot be retrieved
	 * @throws ServletException
	 *             if templates cannot be included
	 * @throws IOException
	 *             if templates are not found or cannot be read
	 */
	public Boolean renderChart(String title, String xAxis, String yAxis,
			String type, ResultSet rs, DatabaseHandler db, HttpServlet serv,
			HttpServletRequest req, HttpServletResponse res)
			throws SQLException, ServletException, IOException {

		// data structure to store list of column headers
		ArrayList<HashMap<String, String>> header = null;

		// serialized data rows
		String data = "";

		if (type.equalsIgnoreCase("bar")) { // bar chart

			// serialize header and data and store them in Chart object
			Chart chart = Utilities.serializeClusterResult(rs);

			// get serialized data rows
			data = chart.getData();

			// get a list of headers
			header = chart.getHeader();

		} else if (type.equalsIgnoreCase("pie")) {

			// serialize header and data and store them in Chart object
			Chart chart = Utilities.serializeClusterResult(rs);

			// get serialized data rows
			data = chart.getData();

			// get a list of headers
			header = chart.getHeader();

		} else { // default to line chart

			// serialize both header and data in single String
			data = Utilities.serializeLinearResult(rs, yAxis);
		}

		// handle data set that cannot make chart
		if (data == null)
			return false;

		// set parameters to pass to JSP page
		req.setAttribute("data", data);
		req.setAttribute("title", title);
		req.setAttribute("y", yAxis);

		if (type.equalsIgnoreCase("bar")) { // bar chart

			// header information required for bar chart
			req.setAttribute("header", header);

			// call bar chart template
			serv.getServletContext()
					.getRequestDispatcher("/WEB-INF/jsp/barChart.jsp")
					.include(req, res);

		} else if (type.equalsIgnoreCase("pie")) {

			// header information required for pie chart
			req.setAttribute("header", header);

			// call bar chart template
			serv.getServletContext()
					.getRequestDispatcher("/WEB-INF/jsp/pieChart.jsp")
					.include(req, res);

		} else { // line chart

			// call line chart template
			serv.getServletContext()
					.getRequestDispatcher("/WEB-INF/jsp/lineChart.jsp")
					.include(req, res);
		}

		return true;
	}
}
