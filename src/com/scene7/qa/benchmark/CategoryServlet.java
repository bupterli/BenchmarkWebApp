package com.scene7.qa.benchmark;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A class that handles request to view, build, reset and edit test case
 * categories and descriptions.
 * 
 * @author hewong
 * @version Jul 25, 2012
 * @todo TODO
 */
public class CategoryServlet extends BaseServlet {

	/**
	 * Default serial verision ID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Web interface view of this servlet. <br>
	 * It renders all test cases with their categories and descriptions.
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 * @TODO disable category / description disable button
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		log.debug("CategoryServlet.doGet()");

		crumb.addCrumb(Utilities.getRelativeUrl(request), 1);
		request.setAttribute("breadcrumbs", crumb.crumbStack);

		// invoke print writer
		pw = response.getWriter();

		// get html template
		prepareResponse("Category", pw, this, request, response);

		// progress bar
		getServletContext()
				.getRequestDispatcher("/WEB-INF/jsp/progressBar.jsp").include(
						request, response);

		pw.println("<div class=\"span12\">");

		// build category and description button
		pw.println("<div class=\"span3\">");
		String build = "<form class=\"inline\" id=\"buildForm\" onsubmit=\"return submitBuildReset('build');\">"
				+ "<input type=\"hidden\" name=\"type\" value=\"build\"/>"
				+ "<input type=\"submit\" class=\"btn btn-primary btn-large\" value=\"Build category and description\">"
				+ "</form>";
		pw.println(build);
		pw.println("</div>");

		// rename all categories
		String sql = q.queryMap.get("test_cases");
		rs = db.execute(sql);
		Classifier classifier = new Classifier();
		ArrayList<String> categories = classifier.getCategories(rs);
		request.setAttribute("categories", categories);

		String rename = "<a class=\"btn btn-success btn-large\" "
				+ " href=\"#renameCategories\" data-toggle=\"modal\">Rename categories</a><br>";
		pw.println("<div class=\"span3\">");
		pw.println(rename);
		getServletContext().getRequestDispatcher(
				"/WEB-INF/jsp/modalRenameCategories.jsp").include(request,
				response);
		pw.println("</div>");

		// include help text for build function
		pw.println("<div class=\"span12\"><br>");
		String helpText = "<p>To assign default category and description for each test cases</p>"
				+ "<p>This will affect all test cases that are \"null\"</p>"
				+ "<p>Update category / description manually by clicking the \"Edit\" button on right hand side</p><br>";
		pw.println(helpText);
		pw.println("</div>");

		pw.println("</div>");

		// reset category and description button in debig mode
		if (DEBUG) {
			String reset = "<form id=\"resetForm\" onsubmit=\"return submitBuildReset('reset');\">"
					+ "<input type=\"hidden\" name=\"type\" value=\"reset\"/>"
					+ "<input id=\"resetCate\" type=\"submit\" class=\"btn btn-warning btn-large\" value=\"Reset category and description\">"
					+ "</form><br>";
			pw.println("<div class=\"span3\">");
			pw.println(reset);
			pw.println("</div>");
		}

		// include modal for updating category and description
		getServletContext().getRequestDispatcher(
				"/WEB-INF/jsp/modalCategoryDescription.jsp").include(request,
				response);

		sql = q.queryMap.get("test_cases");
		rs = db.execute(sql);

		// render result table
		try {

			Utilities.renderResultSet(pw, rs, true);

		} catch (SQLException e) {

			System.err.println("CategoryServlet.doGet SQLException :" + e);
		}

		// get html template
		completeResponse(pw, response);

		// db.closeConnection();
	}

	/**
	 * Handles AJAX calls for building, resetting, and updating test case
	 * categories and descriptions. <br>
	 * Currently writing JSON responses for AJAX callback.
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// TODO: debug statement
		log.debug("CategoryServlet.doPost()");

		// type of request for categories and descriptions
		String type = request.getParameter("type");

		// used for updating a particular test case
		String testID = "";

		// retrieve sql statement from QueryHandler
		String sql = q.queryMap.get("test_cases");

		// run query
		rs = db.execute(sql);

		// used for categorizing test cases
		Classifier classifier = new Classifier();

		// used for creating ajax response
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		JSONObject json = new JSONObject();

		if (type.equals("build")) {

			// import current test cases from ResultSet object
			classifier.importURLs(rs);

			// get longest most common substring
			String category = classifier.getCategory();

			// used for progress bar
			status.reset();
			status.setNumRows(rs, db);

			// helper function to update test cases' category and corresponding
			// description
			classifier.buildCategoryDescription(category, rs, db, status);

			// used for progress bar
			status.reset();

			// set redirect path
			log.debug("CategoryServlet redirecting to /category");
			String redirect = request.getContextPath() + "/category";

			try {

				// successful response with redirecting path
				json.put("success", true);
				json.put("redirect", redirect);
				response.getWriter().write(json.toString());

			} catch (JSONException e) {

				response.getWriter().write("{\"success\" : 0}");
				System.err.println(e);
			}

		} else if (type.equals("reset")) {

			// used for progress bar
			status.reset();
			status.setNumRows(rs, db);

			// set all test case categories and descriptions back to null
			classifier.resetCategoryDescription(rs, db, status);

			// used for progress bar
			status.reset();

			// set redirect page
			log.debug("CategoryServlet redirecting to /category");
			String redirect = request.getContextPath() + "/category";

			try {

				// successful response with redirecting path
				json.put("success", true);
				json.put("redirect", redirect);
				response.getWriter().write(json.toString());

			} catch (JSONException e) {

				response.getWriter().write("{\"success\" : 0}");
				System.err.println(e);
			}

		} else if (type.equals("update")) {

			// get parameters needed to do category and description update for
			// one test case
			testID = request.getParameter("testID");
			String newCategory = request.getParameter("newCategory");
			String newDescription = request.getParameter("newDescription");
			log.debug(testID + "," + newCategory + "," + newDescription);

			// helper methods to do updates
			classifier.updateCategory(testID, newCategory, db);
			classifier.updateDescription(testID, newDescription, db);

			try {

				// response with updated test case information
				json.put("success", true);
				json.put("testID", testID);
				json.put("category", newCategory);
				json.put("description", newDescription);
				response.getWriter().write(json.toString());

			} catch (JSONException e) {

				response.getWriter().write("{\"success\" : 0}");
				System.err.println(e);

			}
		} else if (type.equals("renameCategory")) {

			// get both current and new category names
			String oldCategory = request.getParameter("oldCategory");
			String newCategory = request.getParameter("newCategory");

			// helper methods to do updates
			classifier.updateAllCategory(oldCategory, newCategory, db);

			try {

				// response with updated test case information
				json.put("success", true);
				json.put("category", newCategory);
				response.getWriter().write(json.toString());

			} catch (JSONException e) {

				response.getWriter().write("{\"success\" : 0}");
				System.err.println(e);

			}

		}

		// db.closeConnection();
	}
}
