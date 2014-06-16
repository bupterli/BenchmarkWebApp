package com.scene7.qa.benchmark;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * A class contains methods to categorize test cases using "category" and
 * "description".
 * 
 * @author hewong
 * @version Jul 25, 2012
 * @todo TODO
 */
public class Classifier extends BaseServlet {

	/**
	 * Default serial verision ID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Store result set URLs into memory from queries for faster string
	 * manipulation.
	 */
	private static ArrayList<String> urls;

	/**
	 * Substring (or term) frequency table to find most common substring used
	 * for string manipulation.
	 */
	private static HashMap<String, Integer> substringFreq;

	/**
	 * A list of existing categories
	 */
	protected static ArrayList<String> categories;

	/**
	 * Variables used to find most common substring.
	 */
	int maxLen, count, s1Len, s2Len, walker;
	String commonSubstring, tempString;

	/**
	 * Initialize local data structures and variables.
	 */
	Classifier() {

		urls = new ArrayList<String>();
		substringFreq = new HashMap<String, Integer>();
		categories = new ArrayList<String>();

		maxLen = 0;
		count = 0;
		commonSubstring = "";
	}

	/**
	 * Read in all the test cases URLs from database to memory.
	 * 
	 * @param rs
	 *            test case ResultSet
	 */
	protected void importURLs(ResultSet rs) {

		// reusable String object
		String url = "";

		try {

			// rewind pointer
			rs.beforeFirst();

			while (rs.next()) {

				log.debug(rs.getInt("id") + " : " + rs.getString("url"));

				// get URL from test case
				url = rs.getString("url");

				// only consider URL for category classification when category
				// is null
				if (rs.getString("category") == null) {

					// add to arraylist
					urls.add(url);
				}
			}

		} catch (SQLException e) {

			System.err.println("Classifier.importURLs() SQLException : " + e);
		}
	}

	/**
	 * Get a list of existing categories
	 * 
	 * @param rs
	 *            Query result set
	 * @return A list of categories
	 */
	protected ArrayList<String> getCategories(ResultSet rs) {

		// reusable String object
		String category = "";

		try {

			// rewind pointer
			rs.beforeFirst();

			while (rs.next()) {

				// only consider URL for category classification when category
				// is null
				if (rs.getString("category") != null) {

					// get // get URL from test case
					category = rs.getString("category");
					if (categories.size() == 0) {

						categories.add(category);

					} else if (!categories.contains(category)) {

						// add to arraylist
						categories.add(category);
					}
				}
			}

			return categories;

		} catch (SQLException e) {

			System.err.println("Classifier.importURLs() SQLException : " + e);
		}
		return null;
	}

	/**
	 * Method to find the longest substring in common among all URLs, and return
	 * it as "category".
	 * 
	 * @return longest substring in common
	 */
	protected String getCategory() {

		// if no URLs, return nothing
		if (urls.isEmpty())
			return "";

		// reusable String object
		String s1 = "";

		// populate term frequency dictionary
		Iterator<String> it = urls.iterator();
		while (it.hasNext()) {

			s1 = (String) it.next();
			if (s1.length() > 0)
				setCommonSubstring(s1);

		}

		// convert to a list structure
		// sort by value (term frequency), then sort by key (length of
		// substring) using custom comparator
		List<Entry<String, Integer>> sorted = new ArrayList<Entry<String, Integer>>(
				substringFreq.entrySet());
		Collections.sort(sorted, new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> arg0,
					Entry<String, Integer> arg1) {

				if (arg1.getValue() - arg0.getValue() != 0) {
					return arg1.getValue() - arg0.getValue();
				} else {
					return arg1.getKey().compareTo(arg0.getKey());
				}
			}
		});

		// return the element that has highest term frequency and longest string
		// length
		return sorted.get(0).getKey();
	}

	/**
	 * Check string and put all substring permutations into term frequency
	 * table. <br>
	 * Algorithm steps:
	 * 
	 * <pre>
	 * String A = "Hello world";
	 * 1) put key "H" to count value =  1
	 * 2) put key "He" to count value =  1
	 * 3) put key"Hel" to count value =  1
	 * ...
	 * 
	 * String B = "Hell boy";
	 * 1) update key "H" to count value =  2
	 * 2) update key "He" to count value =  2
	 * 3) update key "Hel" to count value =  2
	 * </pre>
	 * 
	 * @param s
	 *            current string to check against term frequency table
	 */
	protected void setCommonSubstring(String s) {

		// edge case check
		if (s == null || s.length() == 0)
			return;

		// please see javadoc above for algorithm illustration
		count = 0;
		for (int i = 0; i < s.length(); i++) {

			tempString = s.substring(0, i);
			if (tempString.length() > 0) {

				if (!substringFreq.containsKey(tempString)) {

					substringFreq.put(tempString, 1);

				} else {

					count = substringFreq.get(tempString);
					count++;

					substringFreq.put(tempString, count);
				}
			}
		}
	}

	/**
	 * Update categories and descriptions of all test cases in database. <br>
	 * Category will be pre-determined, description will be the substring
	 * remaining after subtracting the category string. <br>
	 * Example:
	 * 
	 * <pre>
	 * String category = "www.s7IsAwesome.com";
	 * String url = "www.s7IsAwesome.com/is/imaging/test1.php";
	 * then description would be "/is/imaging/test1.php";
	 * </pre>
	 * 
	 * <br>
	 * When rebuilding categories and descriptions, it would only affect test
	 * cases that do not have category and description.
	 * 
	 * @param category
	 *            desired category name
	 * @param rs
	 *            affected test cases result set
	 * @param db
	 *            database handler
	 * @param status
	 *            used for progress bar
	 */
	protected void buildCategoryDescription(String category, ResultSet rs,
			DatabaseHandler db, Status status) {

		int id = 0;
		int current = 0;
		String description = "";
		String url = "";
		String sql = "";

		try {

			// rewind result set pointer
			rs.beforeFirst();

			while (rs.next()) {

				// update pointer for progress bar
				current++;
				status.increProgress(current);

				// check for null category and description
				if (nullCategoryDescription(rs)) {

					// get test case ID from result set record
					id = rs.getInt("id");

					// get test case URL from result set record
					url = rs.getString("url");

					// get test case description from subtracting the category
					// from URL
					description = url.substring(category.length());

					// build sql statement
					sql = "UPDATE benchmark_testcases " + "SET category='"
							+ category + "', description='" + description
							+ "' " + "WHERE id=" + id;

					// do update
					db.update(sql);
					// db.closeConnection();

					log.debug(sql);
				}
			}

		} catch (SQLException e) {

			System.err
					.println("Classifier.buildCategoryDescription() SQLException : "
							+ e);
		}
	}

	/**
	 * Helper function to check and see if category or description of a given
	 * test case has not been set.
	 * 
	 * @param rs
	 *            test case result set pointer
	 * @return if category or description is null
	 */
	private boolean nullCategoryDescription(ResultSet rs) {

		try {
			if (rs.getString("category") == null
					|| rs.getString("description") == null) {
				return true;
			}
			if (rs.getString("category").equals("null")
					|| rs.getString("description").equals("null")
					|| rs.getString("category").length() == 0
					|| rs.getString("description").length() == 0) {

				// one of the conditions above is null
				return true;

			}
		} catch (SQLException e) {
			System.err
					.println("Classifier.nullCategoryDescription() SQLException : "
							+ e);
		}

		// not null
		return false;
	}

	/**
	 * Reset all test case categories and descriptions back to null. <br>
	 * Used for internal testing purpose. <br>
	 * Note: Currently disabled. To enable this function, need to enable "reset"
	 * button in CategoryServlet.class.
	 * 
	 * @param rs
	 *            result set of given test case
	 * @param db
	 *            database handler
	 * @param status
	 *            progress bar object
	 */
	protected void resetCategoryDescription(ResultSet rs, DatabaseHandler db,
			Status status) {

		int id = 0;
		int current = 0;
		String sql = "";

		try {
			// rewind result set pointer
			rs.beforeFirst();

			while (rs.next()) {

				// progress bar object
				current++;
				status.increProgress(current);

				// build sql statement
				id = rs.getInt("id");
				sql = "UPDATE benchmark_testcases "
						+ "SET category='null', description='null' "
						+ "WHERE id=" + id;

				// do update
				db.update(sql);
				// db.closeConnection();

				// TODO: debug statement
				// log.debug(sql);
			}

		} catch (SQLException e) {
			System.err
					.println("Classifier.resetCategoryDescription() SQLException : "
							+ e);
		}
	}

	/**
	 * Method to update description of a test case by test case ID.
	 * 
	 * @param testID
	 *            test case ID
	 * @param newDescription
	 *            desire test case description
	 * @param db
	 *            database handler
	 */
	protected void updateDescription(String testID, String newDescription,
			DatabaseHandler db) {

		// edge cases error checking
		if (testID.length() == 0 || newDescription.length() == 0
				|| testID == null || newDescription == null)
			return;

		// build sql statement
		String sql = "UPDATE benchmark_testcases " + "SET description='"
				+ newDescription + "' " + "WHERE id=" + testID;

		// do update
		db.update(sql);
		// db.closeConnection();
	}

	/**
	 * Method to update category of a test case by its test case ID.
	 * 
	 * @param testID
	 *            test case ID
	 * @param newDescription
	 *            desire test case category
	 * @param db
	 *            database handler
	 */
	protected void updateCategory(String testID, String newCategory,
			DatabaseHandler db) {

		// edge cases error checking
		if (testID.length() == 0 || newCategory.length() == 0 || testID == null
				|| newCategory == null)
			return;

		// build sql statement
		String sql = "UPDATE benchmark_testcases " + "SET category='"
				+ newCategory + "' " + "WHERE id=" + testID;

		// do update
		db.update(sql);
		// db.closeConnection();
	}

	/**
	 * Method to update all categories given an existing and new category names
	 * 
	 * @param oldCategory
	 *            existing category name
	 * @param newCategory
	 *            new category name
	 * @param db
	 *            database handler
	 */
	protected void updateAllCategory(String oldCategory, String newCategory,
			DatabaseHandler db) {

		// edge cases error checking
		if (oldCategory.length() == 0 || newCategory.length() == 0
				|| oldCategory == null || newCategory == null)
			return;

		// build sql statement
		String sql = "UPDATE benchmark_testcases " + "SET category='"
				+ newCategory + "' " + "WHERE category='" + oldCategory + "'";

		// do update
		db.update(sql);
		// db.closeConnection();
	}
}
