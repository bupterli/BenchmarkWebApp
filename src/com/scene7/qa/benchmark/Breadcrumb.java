package com.scene7.qa.benchmark;

import java.util.ArrayList;

public class Breadcrumb {

	/**
	 * Data structure to keep track a list of prior HTTP referrers
	 */
	protected ArrayList<String> crumbStack;

	/**
	 * Instantiate breadcrumb stack
	 */
	Breadcrumb() {
		crumbStack = new ArrayList<String>();
	}

	/**
	 * Add a URL onto current breadcrumb stack
	 * 
	 * @param url
	 *            pre-formatted URL
	 * @param depth
	 *            how many levels of breadcrumb is allowed
	 */
	public void addCrumb(String url, int depth) {

		if (url.length() == 0)
			return;

		if (crumbStack.contains(url))
			removeCrumb(url);

		if (crumbStack.size() > depth) {

			int pointer = depth - 1;
			while (pointer < crumbStack.size())
				crumbStack.remove(pointer);

		}

		crumbStack.add(url);

	}

	/**
	 * Find and pop given URL and everything on top of the breadcrumb stack
	 * 
	 * @param url
	 *            pre-formatted URL
	 */
	protected void removeCrumb(String url) {

		if (crumbStack.size() <= 0)
			return;

		int walker;
		for (walker = 0; walker < crumbStack.size(); walker++) {

			if (crumbStack.get(walker).equalsIgnoreCase(url)) {

				while (walker < crumbStack.size())
					crumbStack.remove(walker);
				break;
			}
		}
	}

	/**
	 * Remove all URLs in breadcrumb stack
	 * 
	 */
	protected void resetCrumb() {
		crumbStack.clear();
	}

	/**
	 * Helper function to get current breadcrumb stack size
	 * 
	 * @return breadcrumb stack size
	 */
	protected int getSize() {
		return crumbStack.size();
	}

	/**
	 * Helper function to print breadcrumb stack
	 * 
	 */
	protected void printStack() {
		System.out.println(crumbStack.toString());
	}
}
