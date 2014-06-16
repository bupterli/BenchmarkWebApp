package com.scene7.qa.benchmark;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A class that contains web server configuration parser and variables
 * 
 * @author hewong
 * @version Jul 25, 2012
 * @todo TODO
 */
public class ConfigHandler {

	/**
	 * Log4j object for debug statements.
	 */
	private static Logger log;

	/**
	 * Configuration file used (XML).
	 */
	private static String configFile;

	/**
	 * Database related configuration variables.
	 */
	protected static String dbUrl;
	protected static String dbUser;
	protected static String dbPassword;

	/**
	 * JSP files folder path variable. <br>
	 * Note: Not in use
	 */
	protected static String jspPath;

	/**
	 * Javascript files folder path variable. <br>
	 * Note: Not in use
	 */
	protected static String jsPath;

	/**
	 * String variable to indicate the last release version number
	 */
	protected String lastReleaseVersion;

	/**
	 * String variable to indicate the last release build number
	 */
	protected String lastReleaseBuildNumber;

	/**
	 * String variable to indicate the last build version number
	 */
	protected static String lastBuildVersion;

	/**
	 * Noise tolerance for performance difference
	 */
	protected static double diffThreshold;

	/**
	 * Initialize configuration variables then import them from file upon
	 * instantiation.
	 */
	ConfigHandler() {

		log = BaseServlet.log;

		configFile = "projectConfig.xml";
		dbUrl = "";
		dbUser = "";
		dbPassword = "";
		jspPath = "";
		jsPath = "";
		lastReleaseVersion = "";
		lastBuildVersion = "";
		diffThreshold = 0.0;

		// import when class is instantiated
		importXMLConfig(configFile);
	}

	/**
	 * XML format configuration file parser.
	 * 
	 * @param configFile
	 *            filepath to XML configuration file
	 */
	protected void importXMLConfig(String configFile) {

		log.debug("ConfigHandler.importXMLConfig()");

		try {

			log.debug(configFile);

			// read configuration file into memory
			// get file from "src/" directory
			URL filepath = getClass().getResource("/" + configFile);
			File file = new File(filepath.toURI());
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);

			NodeList nodeList = doc.getElementsByTagName("db");

			// parse nested elements for db variables
			for (int i = 0; i < nodeList.getLength(); i++) {

				Node node = nodeList.item(i);
				Element elem = (Element) node;

				dbUrl = getNestedElemValue(elem, "url");
				dbUser = getNestedElemValue(elem, "user");
				dbPassword = getNestedElemValue(elem, "password");

				log.debug("url = " + getNestedElemValue(elem, "url"));
				log.debug("user = " + getNestedElemValue(elem, "user"));
				log.debug("password = " + getNestedElemValue(elem, "password"));
			}

			// JSP and JS folder path variables (not in use)
			jspPath = getNestedDocValue(doc, "jsp");
			jsPath = getNestedDocValue(doc, "js");

			// last build version number
			lastReleaseVersion = getNestedDocValue(doc, "last_release_version");

			// last release build number
			lastReleaseBuildNumber = getNestedDocValue(doc, "last_release_server_buildnumber");
			System.out.println("Last Release Build Number: " + lastReleaseBuildNumber);
			
			// get performance threshold
			diffThreshold = Double.valueOf(getNestedDocValue(doc,
					"performance_threshold"));

			log.debug("jsp = " + getNestedDocValue(doc, "jsp"));
			log.debug("js = " + getNestedDocValue(doc, "js"));
			log.debug("css = " + getNestedDocValue(doc, "css"));

		} catch (NullPointerException e) {

			System.err.println("XML configuration file is null.");
			System.err.println(e);
			System.exit(-1);

		} catch (SAXException e) {

			System.err.println("XML configuration file cannot be parsed.");
			System.err.println(e);
			System.exit(-1);

		} catch (IOException e) {

			System.err.println("XML configuration file cannot be imported.");
			System.err.println(e);
			System.exit(-1);

		} catch (ParserConfigurationException e) {

			System.err.println("XML configuration file cannot be parsed.");
			System.err.println(e);
			System.exit(-1);

		} catch (URISyntaxException e) {

			System.err.println("XML configuration file cannot be parsed.");
			System.err.println(e);
			System.exit(-1);
		}

	}

	/**
	 * Helper function to parse nested XML nodes for importXMLConfig().
	 * 
	 * @param elem
	 *            XML node element
	 * @param name
	 *            parent node name
	 * @return node value
	 */
	private String getNestedElemValue(Element elem, String name) {

		NodeList list = elem.getElementsByTagName(name);
		Element nestedElem = (Element) list.item(0);
		NodeList nestedList = nestedElem.getChildNodes();
		return nestedList.item(0).getNodeValue();
	}

	/**
	 * Helper function to parse flat XML nodes for importXMLConfig().
	 * 
	 * @param doc
	 *            XML node
	 * @param name
	 *            node name
	 * @return node value
	 */
	private String getNestedDocValue(Document doc, String name) {

		NodeList nodeList = doc.getElementsByTagName(name);
		Element elem = (Element) nodeList.item(0);
		NodeList nestedList = elem.getChildNodes();
		return nestedList.item(0).getNodeValue();
	}

	/**
	 * Parse and import JSON based configuration file. <br>
	 * Note: Not in use
	 * 
	 * @param configFile
	 *            JSON based configuration filepath
	 */
	protected void importJSONConfig(String configFile) {

		// TODO: debug statement
		log.debug("ConfigHandler.importConfig()");

		try {

			// convert json file as InputStream
			InputStream configFileInputStream = this.getClass()
					.getResourceAsStream(configFile);

			// serialize json object
			String jsonString = Utilities.configToString(configFileInputStream);

			// create json object
			JSONObject json = new JSONObject(jsonString);

			// set database variables
			dbUrl = json.getJSONObject("db").getString("url");
			dbUser = json.getJSONObject("db").getString("user");
			dbPassword = json.getJSONObject("db").getString("password");

			// set jsp and js filepath variables
			jspPath = json.getString("jsp");
			jsPath = json.getString("js");

			// TODO: debug statement
			log.debug("dbUrl = " + dbUrl);
			log.debug("dbUser = " + dbUser);
			log.debug("dbPassword = " + dbPassword);
			log.debug("jspPath = " + jspPath);
			log.debug("jsPath = " + jsPath);

		} catch (IOException e) {

			System.err.println("JSON configuration file cannot be parsed.");
			System.err.println(e);
			System.exit(-1);

		} catch (JSONException e) {

			System.err.println("JSON configuration file cannot be parsed.");
			System.err.println(e);
			System.exit(-1);
		}
	}
}
