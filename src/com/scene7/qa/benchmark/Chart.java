package com.scene7.qa.benchmark;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class that encapsulates header information and data string used for chart
 * rendering operating with Google Visualization API <br>
 * Data structure illustration:
 * 
 * <pre>
 * [ column0, column1, column2, ...]
 * 	
 *  column0 : { 'type': ..., 'value': ... }
 * </pre>
 * 
 * @author hewong
 * @version Jul 25, 2012
 * @todo TODO
 */
public class Chart {

	/**
	 * A list of header names, each element links to a HashMap that contains
	 * header type and name for a column. <br>
	 * Example:
	 * 
	 * <pre>
	 * [ header1, header2, ...]
	 * 
	 * 	header1 -> { "string" : "Description" }
	 * </pre>
	 */
	public ArrayList<HashMap<String, String>> header;

	/**
	 * Serialized chart data rows stored in a string used for Google
	 * Visualization API.
	 */
	public String data;

	/**
	 * Initialize header data structure and serialized data row variables.
	 */
	Chart() {

		header = new ArrayList<HashMap<String, String>>();
		data = "";
	}

	/**
	 * Create new element that stores the chart header type and name, then add
	 * to header data structure. <br>
	 * Example:
	 * 
	 * <pre>
	 * setHeader("string", "Description");
	 * setHeader("tooltip", "/is/image/....")
	 * </pre>
	 * 
	 * @param type
	 *            column header type
	 * @param value
	 *            name of this column
	 */
	public void setHeader(String type, String value) {

		HashMap<String, String> column = new HashMap<String, String>();
		column.put("type", type);
		column.put("value", value);
		header.add(column);
	}

	/**
	 * Stores pre-formatted chart data in a string used for Google Visualization
	 * API.
	 * 
	 * @param value
	 *            serialized data row
	 */
	public void setData(String value) {

		this.data = value;
	}

	/**
	 * Getter for header data structure.
	 * 
	 * @return list of headers that links to column type and name
	 */
	public ArrayList<HashMap<String, String>> getHeader() {

		return this.header;
	}

	/**
	 * Getter for chart data rows (not including header).
	 * 
	 * @return data row as a String
	 */
	public String getData() {

		return this.data;
	}
}
