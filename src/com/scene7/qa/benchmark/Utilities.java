/*
 * Copyright 2012 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 ***************************************************************************/

package com.scene7.qa.benchmark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.mysql.jdbc.ResultSetMetaData;

/**
 * Miscellaneous helper functions used for all classes in this project.
 * 
 * @author hewong, cbrann
 * @version Jul 25, 2012
 * @todo TODO
 */
public class Utilities extends BaseServlet {

	/**
	 * Default serial verision ID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Log4j object for debug statements.
	 */
	private static Logger log = BaseServlet.log;

	/**
	 * toString method that serializes data set as Google Visualization API
	 * accepted input. <br>
	 * Example:
	 * 
	 * <pre>
	 * notation:
	 * hashmap = { key : value }
	 * arraylist = [1,2,3, ..., n]
	 * 
	 * input format: ( request ID -> list of time data )
	 * { '349' : [123.45, 234.56, 345.67, ...],
	 *   '1001' : [456.78, ...],
	 *   '1002' : [ ... ],
	 *   ... }
	 * 
	 * output format:
	 * [ 'x', '349', '1001', '1002', ...],
	 * [ '' , 123.45, 456.78, ..., ...],
	 * [ '' , 234.56, ..., ..., ...]
	 * </pre>
	 * 
	 * @param map
	 *            test case IDs that links to a list its process time data
	 * @return serialized String data set
	 */
	@SuppressWarnings("unchecked")
	private static <T, K, V> String buildLinearDataTable(HashMap<K, V> map,
			ArrayList<String> dates) {

		// TODO: debug statement
		log.debug("Utilities.buildDataTable()");

		// a list of test case IDs used for data structure conversion
		ArrayList<String> indexes = new ArrayList<String>();

		// Header row used as return value
		String dataTable = "['x'";

		// first pass:
		// 1) add each test case ID to a list (indexes)
		// 2) append each test case to the first row of data table (header row)
		for (Object k : map.keySet()) {

			indexes.add(k.toString());
			dataTable += ", '" + k.toString() + "'";

		}
		dataTable += "],\n";

		/**
		 * number of test cases
		 */
		int numIndexes = indexes.size();

		/**
		 * maximum number of data points of all test cases
		 */
		int max = maxRecords(map);

		/**
		 * reusable String to serialize data set
		 */
		String eachRow = "";

		/**
		 * temporary list to store all data point of a given test case then
		 * include the toString result in data table
		 */
		ArrayList<T> eachData;

		for (int row = 0; row < max; row++) {

			eachRow = "['" + dates.get(row) + "' ";

			for (int col = 0; col < numIndexes; col++) {

				// get pointer to test case data points list
				String index = indexes.get(col);

				// serialize data points of this test case
				eachData = (ArrayList<T>) map.get(index);
				eachRow += ", ";

				// get corresponding test case data point if available
				// if out of bound, then append "null" so that Google
				// Visualization API would not crash
				eachRow += ((row < eachData.size()) ? eachData.get(row)
						: "null");
			}

			dataTable += eachRow + "],\n";
		}

		return dataTable;
	}

	/**
	 * toString method that serializes data set as Google Visualization API (bar
	 * chart) accepted data input. Example:
	 * 
	 * <pre>
	 * Notation:
	 * HashMap = { key : value }
	 * ArrayList = [1,2,3, ..., n]
	 * 
	 * Input format:
	 * containerNames = ['today', 'yesterday', ...]
	 * urls = { 'url1' : id1, 'url2': id2 }
	 * containers = {'today' : {'url1':123.45, 'url2':456.78, },
	 * 			  'yesteday' : {'url1':123.45, 'url2':456.78, } }
	 * 
	 * output format:
	 * [ 'test id', 'today','tooltip1','tooltip2','yesterday','tooltip1','tooltip2', 'diff'],
	 * [ 1, 3.25, 3.25, 'url1', 3.01, 3.01, 'url2', 0.05],
	 * </pre>
	 * 
	 * @param containerNames
	 *            a list of container names
	 * @param urls
	 *            set of urls that maps to their test IDs
	 * @param containers
	 *            meta data structure that holds a list of containers, each
	 *            container is a pointer to a map of URLs that points to process
	 *            time
	 * @param categories
	 *            test case URLs that maps to their categories
	 * @param descriptions
	 *            test case URLs that maps to their descriptions
	 * @return chart object
	 */
	private static Chart buildClusterDataTable(
			ArrayList<String> containerNames_ProcessTime, ArrayList<String> containerNames, HashMap<String, Integer> urls,
			ArrayList<HashMap<String, Double>> containers,
			HashMap<String, String> categories,
			HashMap<String, String> descriptions) {

		log.debug("Utilities.buildClusterDataTable()");

		Chart chart = new Chart();
		chart.setHeader("string", "Description");

		// for each test case performance, add date and time
		for (String containerName : containerNames) {
			chart.setHeader("number", containerName);
			chart.setHeader("tooltip", "");
		}

		// set performance difference
		chart.setHeader("number", "diff");

		// data rows
		int i = 0;
		double data = 0.0, diff = 0.0;
		double today = 0.0, yesterday = 0.0;
		String chartData = "";

		for (String url : urls.keySet()) {

			// add test case description
//			chartData += "[\n'" + descriptions.get(url) + "',\n";
			chartData += "[\n'" + url + "',\n";
			i = 0;

			for (HashMap<String, Double> contner : containers) {

				if (i > 0)
					today = yesterday;

				// in millisecond
				if (contner.get(url) != null) {
					data = contner.get(url);
				} else {
					data = 0.0;
				}
				yesterday = data;

				chartData += data;

//				Modified by Weihua 03.18.2014     exchange the sequence 2)process time <---> 3)test case url, and add  process time to url, integrate progress time with url, so if the process time is too short to show in the Chart bar, we could get the process time in the url annotationText!!!
				// tooltip 1 - 3
				// 1) date -- build date  append with process time
				// 2) process time
				// 3) test case url
//				chartData += ",\n'" + containerNames.get(i) + "'";
				chartData += ",\n'" + containerNames_ProcessTime.get(i) + data +  "'";
				
//				chartData += ",'" + url + "',\n" + data + ",\n";
				chartData += ",'" + "Process Time: " + data + "  Full Request URL: " + url + "',\n" + data + ",\n";

				// 4) add test case category
				if (categories.get(url).length() > 0) {
					chartData += "'" + categories.get(url) + "',\n";
				} else {
					chartData += "'General',\n";
				}
				
				i++;
			}

			// calculate difference
			diff = ((today / yesterday) - 1) * 100;

			// add thresdhold
			if (diff > Reports.DIFF_THRESHOLD * -1
					&& diff < Reports.DIFF_THRESHOLD * 1) {
				diff = 0;
			} else if (diff > 100 || diff < -100 || today == 0.0
					|| yesterday == 0.0) {
				diff = -1000;
			}

			chartData += "" + diff;

			// TODO: using chart object
			chartData += "\n],";
		}

		// TODO: data rows with chart object
		chart.setData(chartData);
		return chart;
	}

	/**
	 * Serialize result set to a string. <br>
	 * Used for bar chart from Google Visualization API. <br>
	 * Also see helper method Utilities.buildClusterDataTable().
	 * 
	 * @param rs
	 *            desire result set
	 * @return chart object
	 */
	public static Chart serializeClusterResult(ResultSet rs) {

		// TODO: debug statement
		log.debug("Utilities.serializeClusterResult()");

		// header info
		HashMap<String, Integer> urls = new HashMap<String, Integer>();
		ArrayList<String> containerNames = new ArrayList<String>();
		ArrayList<String> containerNames_ProcessTime = new ArrayList<String>();
		HashMap<String, String> descriptions = new HashMap<String, String>();
		HashMap<String, String> categories = new HashMap<String, String>();

		// data
		ArrayList<HashMap<String, Double>> containers = new ArrayList<HashMap<String, Double>>();
		HashMap<String, Double> container = new HashMap<String, Double>();

		try {

			int current = 0;
			int last = 0;

			int containerId = 0;
			int testId = 0;
			double timeProcess = 0.0;
			String url = "";
			String containerName = "";
			String containerName_ProcessTime = "";
			String category = "";
			String description = "";
			// Handle multiple runs on same date
			String latestIDs[] = QueryHandler.lastBuildIDs.split(",");
			int lastBuild = Integer.parseInt(latestIDs[0]);
			// Handle multiple runs on same date
			String previousIDs[] = QueryHandler.previousBuildIDs.split(",");
			int yesterday = Integer.parseInt(previousIDs[0]);
			int lastRelease = q.getLastReleaseID();

			while (rs.next()) {

				// each row
				url = String.valueOf(rs.getObject("url"));
				containerId = rs.getInt(1);
				testId = rs.getInt(2);
				timeProcess = rs.getDouble(3);
				category = rs.getString("category");
				description = rs.getString("description");

				// add to url->test_id map
				if (!urls.containsKey(url)) {
					urls.put(url, testId);

					// default category to url
					if (category == null || category.equals("null")
							|| category.length() == 0)
						category = url;

					// default description to url
					if (description == null || description.equals("null")
							|| description.length() == 0)
						description = url;

					// save category and description
					categories.put(url, category);
					descriptions.put(url, description);
				}

				current = containerId;

				// initialize last pointer
				if (last == 0)
					last = current;

				if (current != last) {

					// end of a container
					containerNames.add(containerName);
					containerNames_ProcessTime.add(containerName_ProcessTime);
					containers.add(container);
					container = new HashMap<String, Double>();

				} else if (rs.isLast()) {

					// no more result to iterate
					containerNames.add(containerName);
					containerNames_ProcessTime.add(containerName_ProcessTime);
					containers.add(container);
				}

				container.put(url, timeProcess);
				last = current;

				// build more verbose container names
				containerName = "";
				containerName_ProcessTime = "";
				int id = rs.getInt("container_id");

				if (id == lastRelease) {

					// 1) Last Release
					containerName += "Last Release - ";
					containerName_ProcessTime += "Last Release - Process time: ";

				}

				if (id == lastBuild) {

					// 2) Last Build
					containerName += "Last build(s) - ";
					containerName_ProcessTime += "Last build(s) - Process time: ";


				} else if (id == yesterday) {

					// 4) Yesterday
					containerName += "Previous runs(s) - ";
					containerName_ProcessTime += "Previous runs(s) - Process time: ";

				}

				// Change display from date imported to date of build
				containerName += " imageServer.buildDate: "
						+ rs.getString("imageserving_builddate").substring(0, 19);
			}

			// rewind rs pointer
			rs.first();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return buildClusterDataTable(containerNames_ProcessTime, containerNames, urls, containers,
				categories, descriptions);
	}

	/**
	 * Convert query returned result set data to google chart api compatible
	 * data structure. <br>
	 * Formatting: A HashMap that each value is a pointer to an ArrayList. Map
	 * key is request id, and value is a List of time series data <br>
	 * This is used for "all_test", "cmyk", and "alpha" queries.
	 * 
	 * @param rs
	 *            desire result set
	 * @param colName
	 *            column name
	 * @return serialized result set as a String
	 * @throws SQLException
	 *             if result set sql statement cannot be executed
	 */
	@SuppressWarnings("unchecked")
	public static <T> String serializeLinearResult(ResultSet rs, String colName)
			throws SQLException {

		// TODO: debug statement
		log.debug("Utilities.serializeLinearResult()");

		HashMap<String, ArrayList<T>> allData = new HashMap<String, ArrayList<T>>();
		ArrayList<T> eachData = new ArrayList<T>();
		ArrayList<String> dates = new ArrayList<String>();

		// use current and last pointer to detect end of each request on a large
		// result set
		String last = "";
		String current;

		// Some result sets (like benchmark_testcases) don't have a date, so check before asking
		boolean hasDateValue = true;
		try {
			rs.findColumn("date");
		} catch (java.sql.SQLException se) {
			log.debug("No date in ResultSet, so column will not be populated");
			hasDateValue = false;
		}
		
		while (rs.next()) {

			current = rs.getString("url");

			if (hasDateValue) {
				log.debug("date in ResultSet, " + rs.getString("date").substring(0, 10));
				dates.add(rs.getString("date").substring(0, 10));
			}

			// initialize last pointer
			if (last.equals(""))
				last = current;

			if (!current.equals(last)) {

				// end of a request
				// map the list of time data to request id and start a new list
				allData.put(last, eachData);
				eachData = new ArrayList<T>();

			} else if (rs.isLast()) {

				// no more result to iterate, close the listS
				allData.put(last, eachData);
			}

			try {

				// put time data into list
				eachData.add((T) rs.getObject(colName));
			} catch (SQLException e) {
				return null;
			}

			// update last pointer
			last = current;
		}

		// rewind result set pointer
		rs.first();
		return buildLinearDataTable(allData, dates);
	}

	/**
	 * Helper function to find the maximum number of records of all given test
	 * cases.
	 * 
	 * @param map
	 *            meta data structure that holds all test cases and their data
	 *            points
	 * @return max test case number
	 */
	@SuppressWarnings("unchecked")
	private static <T, K, V> int maxRecords(HashMap<K, V> map) {

		// TODO: debug statement
		log.debug("Utilities.maxRecords()");

		ArrayList<T> tmp;
		int size = 0;

		for (Object k : map.keySet()) {

			tmp = (ArrayList<T>) map.get(k);
			if (tmp.size() > size) {

				size = tmp.size();
			}
		}

		return size;
	}

	/**
	 * Helper function to print converted data structure.
	 * 
	 * @param map
	 *            meta data structure that holds all test cases and their values
	 * @return Not in use
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	private static <T, K, V> int printData(HashMap<K, V> map) {

		log.debug("Utilities.printData()");

		ArrayList<T> tmp;
		int size = 0;

		for (Object k : map.keySet()) {

			tmp = (ArrayList<T>) map.get(k);

			System.out.println(k.toString());
			System.out.println(tmp.toString());

		}
		return size;
	}

	/**
	 * Helper function that renders given ResultSet as HTML table to servlet.
	 * 
	 * @param pw
	 *            caller servlet PrintWriter object
	 * @param result
	 *            desire result set
	 * @param editable
	 *            whether this table will include an "action" button to edit
	 *            test case metadata current only used in CategoryServlet
	 * @throws SQLException
	 *             if result set has sql statements that cannot be executed
	 */
	protected static void renderResultSet(PrintWriter pw, ResultSet result,
			Boolean editable) throws SQLException {

		// TODO: debug statement
		log.debug("Utilities.renderResultSet()");

		// Collapse table toggle button
		pw.println("<div class=\"span12\">");

		// Print column header
		pw.println("<div id=\"result_set\" class=\"visible\">"); // collapse
																	// table by
																	// default
		pw.println("<table class=\"table table-striped table-bordered table-condensed\">");
		pw.println("<thead><tr>");
		pw.print("<th class=\"span1\">#</th>");

		ResultSetMetaData meta = (ResultSetMetaData) result.getMetaData();
		int numCol = meta.getColumnCount();
		int i;

		for (i = 1; i <= numCol; i++) {

			// limit width of columns with long string
			if (meta.getColumnLabel(i).equals("id")) {

				pw.print("<th class=\"span1\">");

			} else if (meta.getColumnLabel(i).equals("url")) {

				pw.print("<th class=\"url\">");

			} else {
				pw.print("<th>");
			}

			pw.print(meta.getColumnLabel(i));
			pw.print("</th>");
		}

		// include action button
		if (editable)
			pw.println("<th>actions</th>");

		pw.println("</thead></tr><tbody>");

		// Print row content
		int row = 0;
		while (result.next()) {

			pw.println("<tr>");
			pw.print("<td>" + row++ + "</td>");
			for (i = 1; i <= numCol; i++) {

				// add html classes to columns
				if (meta.getColumnLabel(i).equals("id")
						|| meta.getColumnLabel(i).equals("case_id")) {

					pw.print("<td class=\"testID\">");

				} else if (meta.getColumnLabel(i).equals("category")) {

					pw.print("<td class=\"category\">");

				} else if (meta.getColumnLabel(i).equals("description")) {

					pw.print("<td class=\"description\">");

				} else if (meta.getColumnLabel(i).equals("url")) {

					pw.print("<td class=\"url\">");

				} else {

					pw.print("<td>");
				}

				pw.print(result.getObject(i));
				pw.print("</td>");
			}

			// include action button
			if (editable) {
				pw.println("<td><a class=\"btn btn-primary editTrigger\" "
						+ "data-toggle=\"modal\" href=\"#editCategoryDescription\">Edit</a></td>");
			}

			pw.println("</tr>");
		}

		pw.println("</tbody></table>");
		pw.println("</div></div>");
	}

	/**
	 * Helper function that convert an InputStream object to a String object. <br>
	 * Used for importJSONConfig(), but this method is not currently in use.
	 * 
	 * @param in
	 *            JSON formatted file InputStream
	 * @return serialized configuration
	 * @throws IOException
	 *             if file cannot be read or opened
	 */
	public static String configToString(InputStream in) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String output = "";
		String s = "";
		while ((s = br.readLine()) != null) {
			output += s.replaceAll("\\s", "");
		}

		br.close();
		return output;
	}

	/**
	 * Helper function to get today's date. <br>
	 * Used for comparing today's and yesterday's build.
	 * 
	 * @param db
	 *            Global DatabaseHandler
	 * @return today's date as string
	 */
	public static String getToday() {

		return getDateString(0);
	}

	/**
	 * Helper function to get yesterday's date. <br>
	 * Used for comparing today's and yesterday's build.
	 * 
	 * @param db
	 *            Global DatabaseHandler
	 * @return yesteday's date as string
	 */
	public static String getYesterday() {

		return getDateString(-1);

	}

	/**
	 * Helper function to get last release's version defined in config file.
	 * 
	 * @return last release version
	 */
	public static String getLastRelease() {

		return config.lastReleaseVersion;
	}

	/**
	 * Helper function to validate today and yesterday's results are not null. <br>
	 * If null, keep rolling back until there is result.
	 * 
	 * @param db
	 *            Global DatabaseHandler
	 * @param lookBehind
	 *            number of days rolling back
	 * @return date toString
	 */
	private static String getDateString(int lookBehind) {

		String dateString = "";
		Calendar current = Calendar.getInstance();

		// get today
		dateString = findDate(current, lookBehind);

		// adjust for yesterday's date
		if (lookBehind < 0) {
			current.add(Calendar.DATE, lookBehind);
			dateString = findDate(current, lookBehind);
		}

		return dateString;
	}

	/**
	 * Helper function to find last value log file
	 * 
	 * @param current
	 *            pointer to current date (log file)
	 * @param lookBehind
	 *            how many days prior to current pointer to look back for
	 * @return validate log file given of date
	 */
	private static String findDate(Calendar current, int lookBehind) {

		int daysBehind = lookBehind;
		Date date = new Date(current.getTimeInMillis());

		String sql = "SELECT id FROM benchmark_container WHERE date LIKE '"
				+ date.toString() + "%' ORDER BY id DESC";
		while (validateResultIsNull(db, sql)) {

			daysBehind--;
			current.add(Calendar.DATE, daysBehind);
			date = new Date(current.getTimeInMillis());
			sql = "SELECT id FROM benchmark_container WHERE date LIKE '"
					+ date.toString() + "%' ORDER BY id DESC";

		}

		return date.toString();
	}

	/**
	 * Validate ResultSet is null
	 * 
	 * @param sql
	 *            query for not null validation
	 * @return if query is null or not
	 */
	private static boolean validateResultIsNull(DatabaseHandler db, String sql) {

		try {

			ResultSet result = db.execute(sql);

			if (result == null || result.isLast() || !result.next()) {
				return true;
			}

			// db.closeConnection();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Construct full URL with parameters from HttpServletRequest.
	 * 
	 * @param req
	 *            Http request
	 * @return full URL string
	 */
	protected static String getFullUrl(HttpServletRequest req) {

		String url = req.getRequestURL().toString();
		if (req.getQueryString() != null)
			url += "?" + req.getQueryString();

		return url;
	}

	/**
	 * Construct relative URL (with parameters) from HttpServletRequest.
	 * 
	 * @param req
	 *            Http request
	 * @return relative URL string
	 */
	protected static String getRelativeUrl(HttpServletRequest req) {

		String url = req.getRequestURL().toString();
		if (req.getQueryString() != null)
			url += "?" + req.getQueryString();

		String relativeUrl = url.substring(req.getRequestURL().toString()
				.indexOf(req.getContextPath()));
		return relativeUrl;
	}

}
