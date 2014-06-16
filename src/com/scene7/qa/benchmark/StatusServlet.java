package com.scene7.qa.benchmark;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Serves API calls to check query execution status when running a large set of
 * queries. <br>
 * Currently used for progress bar.
 * 
 * @author hewong
 * @version Jul 25, 2012
 * @todo TODO
 */
public class StatusServlet extends BaseServlet {

	/**
	 * Default serial verision ID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Progress in raw number (e.g. 0.31).
	 */
	private double progress;

	/**
	 * Formatted progress (e.g. 31%).
	 */
	private String progressString;

	/**
	 * Initialize progress and progressString values.
	 */
	public void init() {

		progress = 0.0;
		progressString = "";
	}

	/**
	 * Write JSON format respond to AJAX request with calculated current
	 * progress from Status.class
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// create ajax response
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		// use json format for response
		JSONObject json = new JSONObject();

		try {

			progress = status.getProgress();
			progressString = String.valueOf(progress * 100 + 25) + "%";

			// save in json object
			json.put("success", true);
			json.put("progressString", progressString);
			json.put("progress", progress);

			// write to response
			response.getWriter().write(json.toString());

		} catch (JSONException e) {

			response.getWriter().write("{\"success\" : 0}");
			System.err.println(e);
		}
	}

	/**
	 * Method current not in use.
	 * 
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// not in use
	}

}
