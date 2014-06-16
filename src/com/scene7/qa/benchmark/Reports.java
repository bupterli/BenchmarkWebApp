package com.scene7.qa.benchmark;

import java.sql.ResultSet;
import java.util.HashMap;

/**
 * A class to encapsulate each report's detail, such as description, SQL
 * statement and chart type.
 * 
 * @author hewong
 * @version Jul 25, 2012
 * @todo TODO
 */
public class Reports {

	public static double DIFF_THRESHOLD = 0.0;
	// public static double DIFF_THRESHOLD = config.diffThreshold;

	/**
	 * Data structure that stores names of all reports. <br>
	 * Each report maps to a ReportNode which has report description, sql
	 * statement and etc. <br>
	 * Example: name -> report details
	 * 
	 * <pre>
	 * { 'today_yesterday' : 
	 * 		{	// ReportNode
	 * 			'description' : ..., 
	 * 			'sql' : ...,
	 * 			'chartType' : ...
	 * 		}
	 * }
	 * </pre>
	 */
	public HashMap<String, ReportNode> reportsMap;

	Reports(QueryHandler q) {

		reportsMap = new HashMap<String, ReportNode>();

		// get threshold value from config file
		DIFF_THRESHOLD = ConfigHandler.diffThreshold;

		// import all queries from QueryHandler
		populateReports(q);
	}

	/**
	 * An inner class to store each report's metadata, including name,
	 * description and SQL statement.
	 * 
	 * @author hewong
	 * @version Jul 25, 2012
	 * @todo TODO
	 */
	public class ReportNode {

		/**
		 * report name
		 */
		public String name;

		/**
		 * report description
		 */
		public String description;

		/**
		 * sql statement to generate result set for this report
		 */
		public String sql;

		/**
		 * ResultSet of this report
		 */
		public ResultSet rs;

		/**
		 * chart type used for this report (Google Visualization API)
		 */
		public String chartType;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getChartType() {
			return chartType;
		}

		public void setChartType(String chartType) {
			this.chartType = chartType;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getSql() {
			return sql;
		}

		public void setSql(String sql) {
			this.sql = sql;
		}

		@Override
		public String toString() {
			return "ReportNode [name=" + name + ", description=" + description
					+ ", sql=" + sql + ", chartType=" + chartType + "]";
		}
	}

	/**
	 * Method to add report object to data structure.
	 * 
	 * @param name
	 *            report name
	 * @param desc
	 *            report description
	 * @param sql
	 *            sql statement to generate this report
	 * @param chartType
	 *            chart type used for this report (Google Visualization API)
	 * @return ReportNode object
	 */
	public ReportNode addReport(String name, String desc, String sql,
			String chartType) {

		// currently only comparison for today&yesterday's build is using bar
		// chart
		if (name.equals("Today_vs_Yesterday_Builds")
				|| name.equals("Today_Yesterday_Builds_vs_Last_Release")
				|| name.equals("Last_Build_vs_Last_Release"))
			chartType = "bar";

		else
			// default to line chart
			chartType = "line";

		// create ReportNode object to store report meta data
		ReportNode node = new ReportNode();
		node.setName(name);
		node.setDescription(desc);
		node.setSql(sql);
		node.setChartType(chartType);

		if (!reportsMap.containsKey(name))
			reportsMap.put(name, node);

		return node;
	}

	/**
	 * Getter for report object by report name.
	 * 
	 * @param name
	 *            report name used in data structure (see QueryHandler)
	 * @return ReportNode
	 */
	public ReportNode getReport(String name) {

		if (reportsMap.containsKey(name))
			return reportsMap.get(name);
		else
			return null;
	}

	/**
	 * Populate report objects from predefined queries. <br>
	 * see QueryHandler.class.
	 * 
	 * @param q
	 *            QueryHandler object
	 */
	private void populateReports(QueryHandler q) {

		for (String name : q.queryMap.keySet()) {

			String sql = q.queryMap.get(name);
			addReport(name, "Request report for " + name, sql, "");
		}
	}

}
