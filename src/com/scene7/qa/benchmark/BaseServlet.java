package com.scene7.qa.benchmark;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * A class that holds all global objects and is extended by all servlets.
 * 
 * @author hewong
 * @version Jul 25, 2012
 * @todo TODO
 */
public class BaseServlet extends HttpServlet {

	/**
	 * Default serial verision ID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Log4j object used for debug messages.
	 * 
	 */
	protected static Logger log;

	/**
	 * Parser that holds configuration variables (e.g. db url, username,
	 * password).
	 */
	protected static ConfigHandler config;

	/**
	 * Connector that holds connection, execute and update methods.
	 */
	protected static DatabaseHandler db;

	/**
	 * Stores predefined queries.
	 */
	protected static QueryHandler q;

	/**
	 * Stores report details such has chart type and description.
	 */
	protected static Reports reports;

	/**
	 * Calculate current position in relation to total queries will be executed.
	 */
	protected static Status status;

	/**
	 * Handles javascript, css template files includes.
	 */
	protected static ResourceHandler r;

	/**
	 * Handles breadcrumb style navigation
	 */
	protected static Breadcrumb crumb;

	/**
	 * Debug flag to control log4j log level and rendering of result set html
	 * tables
	 */
	protected static boolean DEBUG;

	/**
	 * ResultSet object to encapsulate test case records.
	 */
	protected ResultSet rs;

	/**
	 * PrintWriter object to render HTML code.
	 */
	protected PrintWriter pw;

	/**
	 * Initialize all objects needed for server. <br>
	 * Including logger, configuration parser, database handler, chart, report
	 * and query handlers.
	 * 
	 * @throws InterruptedException
	 */
	public BaseServlet() {

		log = Logger.getLogger(BaseServlet.class);
		log.setLevel(Level.ERROR);
		DEBUG = false;

		config = new ConfigHandler();
		db = new DatabaseHandler();
		q = new QueryHandler(DatabaseHandler.LIMIT, db, config);
		reports = new Reports(q);
		status = new Status();
		r = null;
		crumb = new Breadcrumb();

	}

	/**
	 * A function that appends HTML closing tags to PrintWriter object.
	 * 
	 * @param pw
	 *            PrintWriter object passed from servlet
	 * @param response
	 *            HttpServletResponse passed from servlet
	 */
	protected void completeResponse(PrintWriter pw, HttpServletResponse response) {

		log.debug("BaseServlet.completeResponse()\n\n");

		pw.println("</div>");
		pw.println("</body>");
		pw.println("</html>");
	}

	/**
	 * A function that prepends a chunk of HTML code (opening tags, static file
	 * includes) to each page.
	 * 
	 * @param title
	 *            page title in meta tag
	 * @param pw
	 *            PrintWriter object
	 * @param servlet
	 *            HttpServlet object used for ResourceHandler to include
	 *            templates
	 * @param request
	 *            HttpServletRequest object
	 * @param response
	 *            HttpServletResponse object
	 * @return PrintWriter object
	 * @throws ServletException
	 * @throws IOException
	 *             if any included templates do not exist or cannot be opened
	 */
	protected PrintWriter prepareResponse(String title, PrintWriter pw,
			HttpServlet servlet, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		log.debug("BaseServlet.prepareResponse()");

		// Setup flag for debug mode
		setupDebugFlag(request);

		// Setup performance threshold
		request.setAttribute("threshold", Reports.DIFF_THRESHOLD);

		response.setContentType("text/html");
		response.setCharacterEncoding("utf-8");

		pw.println("<!DOCTYPE html>");
		pw.println("<html>");

		pw.println("<head>");
		pw.println("<meta name=\"title\" content=\"" + title + "\" />");
		pw.println("<meta name=\"description\" content=\"Scene7 QA Benchmark Reporting\" />");
		pw.println("<meta name=\"keywords\" content=\"\" />");
		pw.println("<meta name\"author\" content=\"Ian Wong\" />");
		pw.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");

		// include resources
		r = new ResourceHandler(servlet, request, response, pw);
		r.includejQuery(); // jquery library
		r.includeJs("functions.js"); // common javascript function
		r.includeBootstrap(); // twitter bootstrap library
		r.includeTemplate("/WEB-INF/jsp/css.jsp"); // custom css
		r.includeGoogleChart();

		pw.println("</head>");
		pw.println("<body data-spy=\"scroll\" data-target=\".subnav\" data-offset=\"50\">");

		// top bar template
		request.setAttribute("queryMap", q.queryMap);
		r.includeTemplate("/WEB-INF/jsp/topBar.jsp");

		// include modal to run user defined query
		getServletContext().getRequestDispatcher("/WEB-INF/jsp/userQuery.jsp")
				.include(request, response);

		pw.println("<div class=\"container\" id=\"top\">");

		return pw;
	}

	/**
	 * Wrapper method to setup global debug flag
	 * 
	 * @param request
	 *            HTTP servlet request
	 */
	private void setupDebugFlag(HttpServletRequest request) {

		Object debug = getDebugMode(request);
		HttpSession session = request.getSession(false);

		if (session == null) {
			session = request.getSession();
		}

		if (session.getAttribute("debug") != null) {

			if (debug != null
					&& debug != (Boolean) session.getAttribute("debug")) {

				session.setAttribute("debug", debug);
			}
			debug = (Boolean) session.getAttribute("debug");
			setDebugMode((Boolean) debug, request);
			DEBUG = (Boolean) debug;

		} else {

			session.setAttribute("debug", debug);
			debug = (Boolean) session.getAttribute("debug");
			setDebugMode(false, request);
			DEBUG = false;
		}

	}

	/**
	 * Turn debug mode on and off by adding ?debug=1 or ?debug=0 as url
	 * parameter
	 * 
	 * @param request
	 *            HTTP request that contains debug parameter
	 */
	private void setDebugMode(boolean debug, HttpServletRequest request) {

		if (debug) {
			log.debug("Debug is on");
			log.setLevel(Level.DEBUG);
			request.setAttribute("DEBUG", 1);

		} else {
			log.setLevel(Level.ERROR);
			request.setAttribute("DEBUG", 0);
		}
		request.setAttribute("fullUrl", Utilities.getFullUrl(request));
	}

	/**
	 * Get debug parameter from URL
	 * 
	 * @param request
	 *            HTTP servlet request
	 * @return true, false or null
	 */
	private Object getDebugMode(HttpServletRequest request) {

		// get debug flag from URL
		String debugMode = request.getParameter("debug");

		if (debugMode != null) {

			if (debugMode.equals("on") || debugMode.equals("1")) {

				return true;

			} else {
				return false;
			}
		}

		return null;
	}
}
