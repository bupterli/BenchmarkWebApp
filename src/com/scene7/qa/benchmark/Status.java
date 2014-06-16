package com.scene7.qa.benchmark;

import java.sql.ResultSet;

/**
 * A class that handles calculation of current query execution progress. <br>
 * Currently used for progress bar when building or resetting test case
 * categories.
 * 
 * @author hewong
 * @version Jul 25, 2012
 * @todo TODO
 */
public class Status {

	/**
	 * Number of total result set records.
	 */
	public static int numRows;

	/**
	 * Current progress from 0.0 to 1.00.
	 */
	public static double progress;

	/**
	 * Initialize numRows and progress.
	 */
	Status() {

		numRows = 0;
		progress = 0.0;
	}

	/**
	 * Reset calculation variables. <br>
	 * This should be called at beginning and end of each progress calculation,
	 * think of profiling a block of code.
	 */
	protected void reset() {

		numRows = 0;
		progress = 0.0;
	}

	/**
	 * Setter for total number of rows.
	 * 
	 * @param result
	 *            desire result set
	 * @param db
	 *            database handler
	 */
	protected void setNumRows(ResultSet result, DatabaseHandler db) {

		numRows = db.getNumRows(result);
	}

	/**
	 * Getter for total number of rows.
	 * 
	 * @return number of rows
	 */
	protected int getNumRows() {
		return numRows;
	}

	/**
	 * Getter for calculated progress.
	 * 
	 * @return current progress
	 */
	protected double getProgress() {
		return progress;
	}

	/**
	 * Update and calculate current progress.
	 * 
	 * @param current
	 *            current pointer
	 */
	protected void increProgress(int current) {

		if (current <= 0)
			return;
		progress = (double) current / numRows;

	}
}
