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

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * A class that handles a set or predefined queries.
 * 
 * @author hewong, cbrann
 * @version Jul 25, 2012
 * @todo TODO
 */
public class QueryHandler {

	/**
	 * Data structure that maps each query type to a SQL statement. <br>
	 * Example:
	 * 
	 * <pre>
	 * 	{ 'type' : 'sql statement' }
	 * </pre>
	 */
	protected HashMap<String, String> queryMap;

	/**
	 * Reusable String object.
	 */
	private String sql;

	/**
	 * Reusable ResultSet object
	 */
	private ResultSet result;

	/**
	 * Default query limit. Note: Not in use.
	 */
	private int limit;

	/**
	 * DatabaseHandler for query ResultSet validation
	 */
	private DatabaseHandler db;

	/**
	 * ConfigHandler to get last release version
	 */
	private ConfigHandler config;

	/**
	 * Log4j object for debug statements.
	 */
	private static Logger log = BaseServlet.log;
	
	/**
	 * Latest builds that were imported into db
	 */
	public static String lastBuildIDs;
	
	/**
	 * Next to latest builds that were imported into db
	 */
	public static String previousBuildIDs;
	
	/**
	 * Initialize local data structures and variables with default query result
	 * limit, and create all predefined queries and store in data structures.
	 * 
	 * @param lmt
	 *            default query result limit
	 */
	QueryHandler(int lmt, DatabaseHandler database, ConfigHandler confighandler) {

		queryMap = new HashMap<String, String>();
		sql = "";
		limit = lmt;
		db = database;
		config = confighandler;

		// create all pre-defined query and store in memory
		populateQueryMap();
	}

	/**
	 * Append query result limit to end of each query. Note: Not in use.
	 * 
	 * @param type
	 *            requested query type
	 * @return SQL statement as String
	 */
	protected String addLimit(String type) {

		// edge case error checking
		if (!queryMap.containsKey(type))
			return "";

		// get sql statement
		sql = queryMap.get(type);

		// append limit
		sql += "LIMIT " + limit;

		return sql;
	}

	/**
	 * Populate a set of predefined queries and store in local data structure.
	 * 
	 */
	private void populateQueryMap() {

		setLastBuildIDs();
//		lastBuildIDs = "125";
		setPreviousBuildIDs();
		
		// get all requests over time
		queryMap.put(
				"All_Tests",
				"SELECT c.id, t.time_parse, t.time_fetch, t.time_process, o.server_revision,  c.url, o.date "
						+ "FROM benchmark_testcase_times t, benchmark_testcases c, benchmark_container o "
						+ "WHERE t.testcases_id = c.id  and t.benchmark_container_id = o.id ORDER BY o.date  ");

		// get requests that have keyword "alpha"
		queryMap.put(
				"Has_Alpha",
				"SELECT c.id, t.time_parse, t.time_fetch, t.time_process, o.server_revision,  c.url, o.date "
						+ "FROM benchmark_testcase_times t, benchmark_testcases c, benchmark_container o "
						+ "WHERE t.testcases_id = c.id  and t.benchmark_container_id = o.id and url like '%alpha%' ORDER BY c.id, o.server_revision  ");

		// get requests that have keyword "cmyk"
		queryMap.put(
				"Has_CMYK",
				"SELECT c.id, t.time_parse, t.time_fetch, t.time_process, o.server_revision, c.url, o.date "
						+ "FROM benchmark_testcase_times t, benchmark_testcases c, benchmark_container o "
						+ "WHERE t.testcases_id = c.id  and t.benchmark_container_id = o.id and url like '%cmyk%' ORDER BY c.id, o.server_revision ");

		// get all requests comparing today and yesterday's result
		// Modify query to retrieve multiple runs on the same date
		queryMap.put(
				"Today_vs_Yesterday_Builds",
				"SELECT container.id as container_id, cases.id as case_id, times.time_process, cases.url, container.imageserving_builddate, "
						+ "		cases.category as category, cases.description as description "
						+ "FROM benchmark_testcase_times as times "
						+ "LEFT JOIN benchmark_testcases as cases ON times.testcases_id = cases.id "
						+ "INNER JOIN benchmark_container as container ON times.benchmark_container_id=container.id "
						+ "WHERE container.id in "
						+ "("
						+ lastBuildIDs
						+ ") "
						+ "OR container.id in "
						+ "("
						+ previousBuildIDs
						+ ") "
						+ "ORDER BY container_id DESC ");

		// get all requests comparing today and yesterday's, and last release's
		// result
		// Note: diff is comparing yesterday's and last release's performance
		// Modify query to retrieve multiple runs on the same date
		queryMap.put(
				"Today_Yesterday_Builds_vs_Last_Release",
				"SELECT container.id as container_id, cases.id as case_id, times.time_process, cases.url, container.imageserving_builddate, "
						+ "		cases.category as category, cases.description as description "
						+ "FROM benchmark_testcase_times as times "
						+ "LEFT JOIN benchmark_testcases as cases ON times.testcases_id = cases.id "
						+ "INNER JOIN benchmark_container as container ON times.benchmark_container_id=container.id "
						+ "WHERE container.id in "
						+ "("
						+ lastBuildIDs
						+ ") "
						+ "OR container.id in "
						+ "("
						+ previousBuildIDs
						+ ") "
						+ "OR container.id in "
						+ "("
						+ getLastReleaseID()
						+ ") " + "ORDER BY container.date DESC ");

		// compare most current build and last release's performance
		// Modify query to retrieve multiple runs on the same date
		queryMap.put(
				"Last_Build_vs_Last_Release",
				"SELECT container.id as container_id, cases.id as case_id, times.time_process, cases.url, container.imageserving_builddate, "
						+ "		cases.category as category, cases.description as description "
						+ "FROM benchmark_testcase_times as times "
						+ "LEFT JOIN benchmark_testcases as cases ON times.testcases_id = cases.id "
						+ "INNER JOIN benchmark_container as container ON times.benchmark_container_id=container.id "
						+ "WHERE container.id in "
						+ "("
						+ lastBuildIDs
						+ ") "
						+ "OR container.id = "
						+ "("
						+ getLastReleaseID()
						+ ") "
						+ "ORDER BY str_to_date(container.imageserving_builddate, '%W %M %d %T PDT %Y') "
						+ "DESC ");

		// get all test cases
		queryMap.put("test_cases", "SELECT id, category, description, url "
				+ "FROM benchmark_testcases ");
	}

	/**
	 * Calls and includes HTML form template for user submitted query.
	 * 
	 * @param pw
	 *            PrintWriter from caller servlet
	 * @param servlet
	 *            caller servlet
	 * @param request
	 *            HttpServletRequest object from caller servlet
	 * @param response
	 *            HttpServletResponse object from caller servlet
	 */
	protected void renderQueryForm(PrintWriter pw, HttpServlet servlet,
			HttpServletRequest request, HttpServletResponse response) {

		try {
			// include user query form
			servlet.getServletContext()
					.getRequestDispatcher("/WEB-INF/jsp/userQuery.jsp")
					.include(request, response);

		} catch (ServletException e) {

			System.err.println("Servlet exception in renderQueryForm()");
			System.err.println(e);
			System.exit(-1);

		} catch (IOException e) {

			System.err.println("IO exception in renderQueryForm()");
			System.err.println(e);
			System.exit(-1);
		}
	}

	/**
	 * Renders predefined query set as HTML hyperlinks.
	 * 
	 * @param pw
	 *            PrintWriter of caller servlet
	 */
	protected void renderQueryTypesKeys(PrintWriter pw) {

		pw.println("<ul>");

		for (Entry<String, String> e : queryMap.entrySet()) {
			pw.println("<li><a href=\"/BenchmarkWebApp/report?name="
					+ e.getKey() + "\">" + e.getKey() + "</a></li>");
		}

		pw.println("</ul>");
	}

	/**
	 * Helper function to get last release version's container ID
	 * 2012-12-10 - changed identifier to the server.buildNumber
	 * Displayed in the build.txt as "ps = 5.1-170020-61"
	 * This will enable us to identify the exact build that was released
	 * NOTE: this value is set in the src/config.xml file. A run with this value
	 * must be in the database prior to using it 
	 * 
	 * @return last release version container ID
	 */
	protected int getLastReleaseID() {

//		sql = "SELECT id FROM benchmark_container WHERE imageserving_version LIKE '"
//				+ config.lastReleaseVersion + "%' ORDER BY id DESC LIMIT 1";
		sql = "SELECT id FROM benchmark_container WHERE server_buildnumber = '"
				+ config.lastReleaseBuildNumber + "' ORDER BY id DESC LIMIT 1";

		try {

			result = db.execute(sql);
			result.first();
			int id = result.getInt("id");
			// db.closeConnection();

			return id;

		} catch (SQLException e) {

			System.err
					.println("QueryHandler.getLastReleaseID() SQL Exception "
							+ "while looking for Last Release Build Number " 
							+ config.lastReleaseBuildNumber
							+ e);
		}
		return -1;
	}

	/**
	 * Helper function to get last build's container ID sort query result by
	 * imageserving_buildate
	 * 
	 * @return last build's container ID
	 */
	protected int getLastBuildID() {

		sql = "SELECT id FROM `benchmark_container` ORDER BY STR_TO_DATE(imageserving_builddate, '%W %M %d %T PDT %Y') DESC LIMIT 1";

		try {

			result = db.execute(sql);
			result.first();
			int id = result.getInt("id");
			// db.closeConnection();

			return id;
		} catch (SQLException e) {

			System.err
					.println("QueryHandler.getLastReleaseID() SQL Exception. "
							+ e);
		}
		return -1;

	}

	/**
	 * Helper function to get today log's container ID
	 * 
	 * @return today log's container ID
	 */
	protected int getTodayID() {

		sql = "SELECT id FROM benchmark_container WHERE date LIKE '"
				+ Utilities.getToday() + "%' ORDER BY id DESC LIMIT 1";
		try {

			result = db.execute(sql);
			result.first();
			int id = result.getInt("id");
			// db.closeConnection();
			return id;

		} catch (SQLException e) {

			System.err.println("QueryHandler.getTodayID() SQL Exception. " + e);
		}
		return -1;

	}
	
	/**
	 * Helper function to get all container IDs (runs) for today
	 * 
	 * @return  container IDs from today's logs
	 */
	protected String getTodayIDs() {

		sql = "SELECT id FROM benchmark_container WHERE date LIKE '"
				+ Utilities.getToday() + "%' ORDER BY id DESC";
		StringBuffer ids = new StringBuffer();
		try {

			result = db.execute(sql);
			boolean oneId = true;
			while (result.next()) {
				if (oneId) {
					oneId = false;
				} else{
					ids.append(",");
				}
				ids.append(result.getString("id"));
			}
			result.first();
			return ids.toString();

		} catch (SQLException e) {

			System.err.println("QueryHandler.getTodayIDs() SQL Exception. " + e);
		}
		return ids.toString();

	}

	/**
	 * Helper function to get yesterday log's container ID
	 * 
	 * @return yesterday log's container ID
	 */
	protected int getYesterdayID() {

		sql = "SELECT id FROM benchmark_container WHERE date LIKE '"
				+ Utilities.getYesterday() + "%' ORDER BY id DESC LIMIT 1";
		result = db.execute(sql);
		try {

			result = db.execute(sql);
			result.first();
			int id = result.getInt("id");
			// db.closeConnection();
			return id;

		} catch (SQLException e) {

			System.err.println("QueryHandler.getYesterdayID() SQL Exception. "
					+ e);
		}
		return -1;
	}
	
	/**
	 * Helper function to get all container IDs (runs) for previous day
	 * 
	 * @return container IDs from previous day's logs
	 */
	protected String getPreviousdayIDs() {

		sql = "SELECT id FROM benchmark_container WHERE date LIKE '"
				+ Utilities.getYesterday() + "%' ORDER BY id DESC";
		StringBuffer ids = new StringBuffer();
		try {

			result = db.execute(sql);
			boolean oneId = true;
			while (result.next()) {
				if (oneId) {
					oneId = false;
				} else{
					ids.append(",");
				}
				ids.append(result.getString("id"));
			}
			return ids.toString();

		} catch (SQLException e) {

			System.err.println("QueryHandler.getPreviousdayIDs() SQL Exception. " + e);
		}
		return ids.toString();

	}
	
	/**
	 * Helper function to get all container IDs (runs) for today. If no runs were
	 * loaded today keep going back until we have at least 1 run.
	 * 
	 * @return  container IDs from today's logs
	 */
	protected void setLastBuildIDs() {

		StringBuffer ids = new StringBuffer();
		try {
			int lastRun = 0;
			String getIDs = "SELECT id FROM benchmark_container WHERE date >= CURDATE() ORDER BY id DESC;";
			ResultSet rsCount;
			// Check that we have at least one test run. If no test runs for the current dat
			// go back until we find a date with a test run.
			boolean found = false;
			do {
				rsCount = db.execute(getIDs);
				if (!rsCount.next()){
					lastRun++;
					getIDs = "SELECT id, date FROM benchmark_container WHERE date >= DATE_SUB(CURDATE(), INTERVAL " + Integer.toString(lastRun) + " DAY) ORDER BY id DESC;";
					log.debug("setLastBuildIDs:" + getIDs);
				} else {
					found = true;
					boolean oneId = true;
					rsCount.beforeFirst();
					while (rsCount.next()) {
						if (oneId) {
							oneId = false;
						} else{
							ids.append(",");
						}
						ids.append(rsCount.getString("id"));
					}
				}
			} while (!found);

		} catch (SQLException e) {
			System.err.println("QueryHandler.getTodayIDs() SQL Exception. " + e);
		}
		lastBuildIDs = ids.toString();
		log.debug("lastBuildIDs: " + lastBuildIDs);
	}
	
	/**
	 * Helper function to get all container IDs (runs) for today. If no runs were
	 * loaded today keep going back until we have at least 1 run.
	 * 
	 * @return  container IDs from today's logs
	 */
	protected void setPreviousBuildIDs() {

		StringBuffer ids = new StringBuffer();
		try {

			int lastRun = 1;
			String getIDs = "SELECT id FROM benchmark_container WHERE date >= DATE_SUB(CURDATE(), INTERVAL " + Integer.toString(lastRun) + " DAY) AND id NOT IN (" + lastBuildIDs + ") ORDER BY id DESC;";
			ResultSet rsCount;
			// Check that we have at least one test run. If no test runs for the current dat
			// go back until we find a date with a test run.
			boolean found = false;
			do {
				rsCount = db.execute(getIDs);
				if (!rsCount.next()){
					lastRun++;
					getIDs = "SELECT id, date FROM benchmark_container WHERE date >= DATE_SUB(CURDATE(), INTERVAL " + Integer.toString(lastRun) + " DAY) AND id NOT IN (" + lastBuildIDs + ") ORDER BY id DESC;";
					log.debug("setPreviousBuildIDs:" + getIDs);
				} else {
					found = true;
					boolean oneId = true;
					rsCount.beforeFirst();
					while (rsCount.next()) {
						if (oneId) {
							oneId = false;
						} else{
							ids.append(",");
						}
						ids.append(rsCount.getString("id"));
					}
				}
			} while (!found);

		} catch (SQLException e) {
			System.err.println("QueryHandler.getTodayIDs() SQL Exception. " + e);
		}
		previousBuildIDs = ids.toString();
		log.debug("previousBuildIDs: " + previousBuildIDs);
	}

}
