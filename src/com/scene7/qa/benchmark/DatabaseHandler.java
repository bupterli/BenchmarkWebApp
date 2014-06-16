package com.scene7.qa.benchmark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * A class that holds database connection, execute and update methods.
 * 
 * @author hewong
 * @version Jul 25, 2012
 * @todo TODO
 */
public class DatabaseHandler {

	/**
	 * Default query result limit. <br>
	 * Note: Not in use
	 */
	protected final static int LIMIT = 300;

	/**
	 * Database connection variable.
	 */
	private static Connection co;

	/**
	 * Prepared statement object used to prevent injection attack.
	 */
	private static PreparedStatement ps;

	/**
	 * Reusable database result set object.
	 */
	private static ResultSet rs;

	/**
	 * Database variables, updated by ConfigHandler.class.
	 */
	private static String url;
	private static String user;
	private static String password;

	/**
	 * Object to encapsulate username and password.
	 */
	private static Properties property;

	/**
	 * Database connection status.
	 */
	private static Boolean status;

	/**
	 * Log4j object for debug statements.
	 */
	private static Logger log = BaseServlet.log;

	DatabaseHandler() {

		// obtain database configurations from ConfigHandler upon instantiation
		url = ConfigHandler.dbUrl;
		user = ConfigHandler.dbUser;
		password = ConfigHandler.dbPassword;

		property = new Properties();
		property.put("user", user);
		property.put("password", password);

		co = null;
		ps = null;
		rs = null;

		status = false;

		// load JDBC driver
		if (loadDriver() == false) {

			System.err
					.println("Database driver cannot be loaded. Now exiting.");
			System.exit(-1);
		}

		// obtain database connection
		// if unable to get DB connection during startup, give 10 tries within
		// 30 seconds
		int retry = 0;
		do {

			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				System.err
						.println("Database connection re-establishing is interrupted."
								+ e);
			}

			if (retry > 10) {
				System.err
						.println("Database connection cannot be obtained during startup. Now exiting.");
				System.exit(-1);
				closeConnection();
			}

			// System.err.println("Obtaining database connection retry #"+retry);
			retry++;

		} while (getConnection() == false);

	}

	/**
	 * Close database connection, prepared statement and result set objects.
	 * 
	 * @return if database connection can be closed
	 */
	public Boolean closeConnection() {

		// TODO: debug statement
		log.debug("DatabaseHandler.closeConnection()");

		try {

			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (co != null)
				co.close();
			return true;

		} catch (SQLException ex) {

			System.out.println("Could not close connection. " + ex);
		}
		return false;
	}

	/**
	 * Upon query execution, check if current connection is dead
	 * 
	 * @return whether connection is dead
	 */
	public Boolean checkDeadConnection() {

		try {
			if ((rs != null && rs.isClosed()) || (ps != null && ps.isClosed())
					|| (co != null && co.isClosed())) {

				return true;
			}

		} catch (SQLException e) {
			System.out.println("Could not close connection. " + e);
			return true;

		}
		return false;
	}

	/**
	 * Prepare given SQL statement then execute it.<br>
	 * Obtain a new database connection when current one is dead.
	 * 
	 * @param sql
	 *            sql statment to be executed
	 * @return result set of query executed
	 */
	public ResultSet execute(String sql) {

		log.debug("DatabaseHandler.execute(" + sql + ")");

		try {

			// re-establish connection if disconnected
			if (checkDeadConnection()) {

				System.err.println("Database connection is dead.\n"
						+ "Now re-establishing connection...");
				closeConnection();
				getConnection();

			}

			ps = co.prepareStatement(sql);
			rs = ps.executeQuery();

		} catch (SQLException e) {

			System.err.println("Query error: " + sql);
			System.err.println(e);
		}
		return rs;
	}

	/**
	 * Prepare given SQL update statement then execute it.<br>
	 * Obtain a new database connection when current one is dead.
	 * 
	 * @param sql
	 *            sql update statement
	 * @return result set of query executed
	 */
	public ResultSet update(String sql) {

		log.debug("DatabaseHandler.update(" + sql + ")");

		try {

			// re-establish connection if disconnected
			if (checkDeadConnection()) {

				System.err.println("Database connection is dead.\n"
						+ "Now re-establishing connection...");
				closeConnection();
				getConnection();

			}

			ps = co.prepareStatement(sql);
			ps.executeUpdate();

		} catch (SQLException e) {

			System.err.println("Query error: " + sql);
			System.err.println(e);
		}
		return rs;
	}

	/**
	 * Establish global database connection when BaseServlet.class is invoked.
	 * 
	 * @return if connection is can established
	 */
	public Boolean getConnection() {

		// TODO: debug statement
		log.debug("DatabaseHandler.getConnection()");
		status = false;

		try {

			co = DriverManager.getConnection(url, property);
			status = true;

		} catch (SQLException ex) {

			System.err.println("Could not connect to database. " + ex);
		}
		return status;
	}

	/**
	 * Loads JDBC driver when BaseServlet.class is invoked.
	 * 
	 * @return if JDBC can be obtained
	 */
	private Boolean loadDriver() {

		// TODO: debug statement
		log.debug("DatabaseHandler.loadDriver()");

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			status = true;

		} catch (Exception e) {

			e.printStackTrace();
		}
		return status;
	}

	/**
	 * Helper function that prints result set to console for debug purposes. <br>
	 * Note: This function does not render result set to html layer, please see
	 * Utilities.renderResultSet() to render result set.
	 * 
	 * @param result
	 *            result set to be printed
	 * @throws SQLException
	 *             if query of result set does not execute properly
	 */
	protected void printResultSet(ResultSet result) throws SQLException {

		// TODO: debug statement
		log.debug("DatabaseHandler.printResultSet()");

		ResultSetMetaData meta = result.getMetaData();
		int numCol = meta.getColumnCount();
		int i;

		// Print column header
		System.out.println();
		for (i = 1; i <= numCol - 1; i++) {
			System.out.print(meta.getColumnName(i).toUpperCase() + ", ");
		}
		System.out.print(meta.getColumnName(i++).toUpperCase());
		System.out.println();

		// Print row content
		while (rs.next()) {

			for (i = 1; i <= numCol - 1; i++)
				System.out.print(result.getObject(i) + ", ");

			System.out.print(result.getObject(i++));
			System.out.println();
		}
	}

	/**
	 * Helper function to get total number of rows.
	 * 
	 * @param result
	 *            desire result set
	 * @return number of rows
	 */
	protected int getNumRows(ResultSet result) {

		int numRows = 0;

		try {
			// rewind result set pointer
			result.beforeFirst();

			while (result.next())
				numRows++;

		} catch (SQLException e) {

			System.err.println("DatabaseHandler.getNumRows() SQLException : "
					+ e);
		}
		return numRows;
	}
}
