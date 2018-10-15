/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nesim.data;

import java.util.ArrayList;

public class NameInfo {

	public static final String GENERIC_TYPE = "GEN";
	// Variables
	private String origName;
	private String name;
	private String type;
	private ArrayList<String> candidates;
	private ArrayList<String> origCandidates;
	private boolean enforced;

	/**
	 * Constructor that takes one of the two compared strings as a parameter. It
	 * converts the string into a NameInfo object.
	 * 
	 * @param inputName
	 *            One of the two strings being compared
	 */
	public NameInfo(String inputName) {
		origName = inputName;
		name = "";
		type = "";
		enforced = false;
		candidates = new ArrayList<String>();
		origCandidates = new ArrayList<String>();
		parseName(inputName);
	}

	/**
	 * 
	 * @return The string being compared, with any additional delimiters (#)
	 */
	public String getOriginalName() {
		return origName;
	}

	/**
	 * 
	 * @return The string being compared, from a NameInfo object
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return The type (LOC/ORG/PER/GEN) of the NameInfo object
	 */
	public String getType() {
		return type;
	}

	/**
	 * 
	 * @return A boolean value (true/false) determining whether the string
	 *         passed in was given a type by the user.
	 */
	public boolean isEnforced() {
		return enforced;
	}

	/**
	 * 
	 * @param type
	 *            The type (LOC/ORG/PER/GEN) to set the NameInfo object as
	 */
	public void setType(String type) {
		this.type = type;
	}

	// ==========
	public ArrayList<String> getCandidates() {
		return candidates;
	}

	// ==========
	public ArrayList<String> getOriginalCandidates() {
		return origCandidates;
	}

	/**
	 * Parses the string parameter and separates each part from the
	 * delimiter(s). This function checks whether the user provided a type to be
	 * used. If not, the generic type GEN is the default and is later corrected
	 * in EntityComparison.
	 * 
	 * @param inputName
	 *            One of two compared strings
	 * @return Irrelevant, should be void return type instead
	 */
	public boolean parseName(String inputName) {
		String[] tokens = inputName.split("#");
		if (tokens.length != 4) {
			if (tokens.length == 1) { // string
				type = GENERIC_TYPE;
				name = tokens[0];
				enforced = false;
				origName = fixNameString(name, false);
				origCandidates.add(origName);
				name = fixNameString(name, true);
				candidates.add(name);
				return true;
			} else if (tokens.length == 2) { // TYPE#string
				type = tokens[0];
				name = tokens[1];
				if (!type.equals(GENERIC_TYPE))
					enforced = true;

				origName = fixNameString(name, false);
				origCandidates.add(origName);
				name = fixNameString(name, true);
				candidates.add(name);
				return true;
			} else if (tokens.length == 3) { // string#start#end
				type = GENERIC_TYPE;
				String str = tokens[0];
				enforced = false;
				int beginBoundary = Integer.parseInt(tokens[1]);
				int endBoundary = Integer.parseInt(tokens[2]);
				String[] nameTokens = str.split("\\s+");
				return makeCandidates(beginBoundary, endBoundary, nameTokens);
			} else {
				return false;
			}
		} else { // TYPE#string#start#end
			type = tokens[0];
			String str = tokens[1];
			enforced = true;
			int beginBoundary = Integer.parseInt(tokens[2]);
			int endBoundary = Integer.parseInt(tokens[3]);
			String[] nameTokens = str.split("\\s+");
			return makeCandidates(beginBoundary, endBoundary, nameTokens);
		}
	}

	// ==========
	private boolean makeCandidates(int beginBoundary, int endBoundary, String[] nameTokens) {
		int n = nameTokens.length;
		String stringName = "";
		String stringLeft = "";
		String stringRight = "";
		for (int i = beginBoundary; i <= endBoundary && i < n; i++) {
			if (i > beginBoundary)
				stringName += " ";
			stringName += nameTokens[i];
		}
		if (stringName.length() == 0)
			return false;

		for (int i = 0; i < beginBoundary && i < n; i++) {
			if (i > 0)
				stringLeft += " ";
			stringLeft += nameTokens[i];
		}
		for (int i = endBoundary + 1; i < n; i++) {
			if (i > endBoundary + 1)
				stringRight += " ";
			stringRight += nameTokens[i];
		}

		origName = fixNameString(stringName, false);
		origCandidates.add(stringName);
		stringName = fixNameString(stringName, true);
		stringLeft = fixNameString(stringLeft, true);
		stringRight = fixNameString(stringRight, true);
		String candidate;
		if (stringLeft.length() > 0) {
			candidate = stringLeft + " " + stringName;
			candidates.add(candidate);
		}
		if (stringRight.length() > 0) {
			candidate = stringName + " " + stringRight;
			candidates.add(candidate);
		}
		if (stringLeft.length() > 0 && stringRight.length() > 0) {
			candidate = stringLeft + " " + stringName + " " + stringRight;
			candidates.add(candidate);
		}
		name = stringName;
		candidates.add(stringName);
		return true;
	}

	private String fixNameString(String nameIn, boolean makeLowerCase) {
		if (makeLowerCase)
			nameIn = nameIn.toLowerCase();
		return nameIn.replace('-', ' ');
	}
}
