package com.scene7.qa.benchmark;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * A class that handles static resource and template includes.
 * 
 * @author hewong
 * @version Jul 25, 2012
 * @todo TODO
 */
public class ResourceHandler {

	/**
	 * Objects inherited caller servlet.
	 */
	private static HttpServletRequest request;
	private static HttpServletResponse response;
	private static HttpServlet servlet;
	private static PrintWriter pw;

	private static Logger log = BaseServlet.log;

	private static String jsPath;
	private static String bootstrapPath;

	ResourceHandler(HttpServlet s, HttpServletRequest req,
			HttpServletResponse res, PrintWriter pwter) {

		servlet = s;
		request = req;
		response = res;
		pw = pwter;
		jsPath = "javascript/";
		bootstrapPath = "bootstrap/";
	}

	/**
	 * Include Twitter Bootstrap stylesheets and Javascript libraries.
	 */
	protected void includeBootstrap() {

		// Bootstrap css
		pw.println("<link href=\"" + bootstrapPath
				+ "css/bootstrap.css\" rel=\"stylesheet\">");

		// fix top padding css bug when switching between desktop and tablet
		// view
		pw.println("<style type=\"text/css\">" + "body {"
				+ "	padding-top: 60px; " + " padding-bottom: 40px; " + "  }"
				+ "</style>");

		// Bootstrap responsive css
		pw.println("<link href=\"" + bootstrapPath
				+ "css/bootstrap-responsive.css\" rel=\"stylesheet\">");

		// Bootstrap javascript libraries
		pw.println("<script type=\"text/javascript\" src=\"" + bootstrapPath
				+ "js/bootstrap.min.js\"></script>");
				
	}

	/**
	 * Include JQuery library.
	 */
	protected void includejQuery() {

		pw.println("<script type=\"text/javascript\" src=\"" + jsPath
				+ "jquery-1.11.1.min.js\"></script>");
		
		
		// Modified in 28.03.2014   zeroclipboard lib & d3 lib
		pw.println("<script type=\"text/javascript\" src=\"http://dl.dropboxusercontent.com/u/46302658/tmpfile/ZeroClipboard.min.js\"></script>");
		pw.println("<script type=\"text/javascript\" src=\"http://mbostock.github.com/d3/d3.js?2.6.0\"></script>");
		
//		pw.println("<script type=\"text/javascript\" src=\"" + jsPath
//				+ "zeroclipboard.min.js\"></script>");
//		pw.println("<script type=\"text/javascript\" src=\"" + jsPath
//				+ "d3.v2.js\"></script>");	

	}

	/**
	 * Include Javascript files.
	 * 
	 * @param jsFiles
	 *            an array of javascript files
	 */
	protected void includeJs(String... jsFiles) {

		for (String jsFile : jsFiles) {
			log.debug(jsFile);
			pw.println("<script type=\"text/javascript\" src=\"" + jsPath
					+ jsFile + "\"></script>");
		}
	}

	/**
	 * Include a given template.
	 * 
	 * @param template
	 *            filepath to template
	 * @throws ServletException
	 * @throws IOException
	 *             if template cannot be open or does not exist
	 */
	protected void includeTemplate(String template) throws ServletException,
			IOException {

		servlet.getServletContext().getRequestDispatcher(template)
				.include(request, response);
	}

	/**
	 * Include Google Visualization library.
	 */
	protected void includeGoogleChart() {

		pw.println("<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>");
	}
}
