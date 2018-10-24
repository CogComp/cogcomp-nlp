/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nesim.data;

import java.io.IOException;
import java.util.*;

import com.wcohen.secondstring.JaroWinkler;
import com.wcohen.secondstring.SoftTFIDF;
import com.wcohen.secondstring.tokens.SimpleTokenizer;

public class NameParser {
	// Constant
	public static final float TYPOTHRES = 0.95f;
	// Variables
	private String honorific;
	private String firstName;
	private String middleName;
	private String lastName;
	private String extraName;
	// if there are more than one names, the last one is chosen to be parsed.
	// Rest are put in this
	// variable.
	// to keep track of an extra entity in the string ("spouse" name problem)
	private HonorificManager honoMan;
	private NickNameManager nickMan;
	private JaroWinkler jrwk = new JaroWinkler();;
	SimpleTokenizer tokenizer = new SimpleTokenizer(true, true);
	SoftTFIDF soft = new SoftTFIDF(tokenizer, jrwk, 0.92);
	private ArrayList<String> arrNameTokens;

	/**
	 * Constructor to initialize a NameParser object. Used in scoring strings of
	 * type PER.
	 * 
	 * @param honorificFileName
	 *            File name to obtain list of name strings
	 * @param nicknameFileName
	 *            File name to obtain list of nickname strings
	 * @param nickThres
	 *            Threshold at which a name is considered a nickname
	 * @throws IOException
	 */
	public NameParser(String honorificFileName, String nicknameFileName, float nickThres) throws IOException {
		resetAll();
		honoMan = new HonorificManager(honorificFileName);
		nickMan = new NickNameManager(nicknameFileName, nickThres);
	}

	/**
	 * Sets all variables to empty strings.
	 */
	public void resetAll() {
		honorific = "";
		firstName = "";
		middleName = "";
		lastName = "";
		extraName = "";

		arrNameTokens = new ArrayList<String>();
	}

	/**
	 * Parses a given string of type PER, identifying the parts of the name -
	 * first name, last name, honorific, title, etc.
	 * 
	 * @param name
	 *            String to be parsed using type PER
	 */
	public void parsing(String name) {
		resetAll();
		name = name.trim();
		String[] tokens = name.split("\\s+");

		ArrayList<String> arrTokens = new ArrayList<String>();

		int reversedNamePosn = -1;
		// -1 indicates not reversed, 0 or more indicates index where the last
		// name ends
		for (int i = 0; i < tokens.length; ++i) {
			String tok = tokens[i];
			tok = tok.trim();
			if (tok.length() == 0)
				continue;
			if (tok.equals("and") || tok.equals("&")) {
				for (int j = 0; j < arrTokens.size(); ++j) {
					if (j > 0)
						extraName += " ";
					extraName += arrTokens.get(j);
				}
				arrTokens.clear();
				continue;
			}

			char endChar = tok.charAt(tok.length() - 1);
			tok = tok.replaceAll("[^A-Za-z]", "");
			if (tok.length() > 0) {
				arrTokens.add(tok);
				if (i < tokens.length - 1 && endChar == ',') {
					reversedNamePosn = arrTokens.size() - 1;
				}
			}
		}

		if (arrTokens.size() == 0) {
			firstName = "";
			lastName = "";
			return;
		} else {
			String nameTokens[] = new String[arrTokens.size()];
			nameTokens = arrTokens.toArray(nameTokens);
			int index = (honoMan.isHonorific(nameTokens[0])) ? 1 : 0;
			int lastNameLen;

			if (reversedNamePosn >= 0) {
				lastNameLen = reversedNamePosn + 1 - index;
				parseName(nameTokens, index, lastNameLen, true);
			} else {
				lastNameLen = 1;
				parseName(nameTokens, index, lastNameLen, false);
			}
			for (int i = 0; i < nameTokens.length; ++i)
				arrNameTokens.add(nameTokens[i]);
		}
	}

	/**
	 * Helper function to parse a string. Identifies honorifics, first/last
	 * names, and suffixes.
	 * 
	 * @param tokens
	 *            Array of all words in the string
	 * @param index
	 *            Array index at which the first or last name begins
	 * @param lastNameLen
	 *            Length of the last name
	 * @param swapNameOrder
	 *            Whether the first/last name order is swapped
	 */
	private void parseName(String tokens[], int index, int lastNameLen, boolean swapNameOrder) {
		if (swapNameOrder) {
			String[] tok = new String[lastNameLen];
			int i, j;
			for (i = 0; i < lastNameLen; ++i)
				tok[i] = tokens[index + i];
			for (i = lastNameLen + index, j = index; i < tokens.length; ++i, ++j)
				tokens[j] = tokens[i];
			for (i = 0; i < lastNameLen; ++i, ++j)
				tokens[j] = tok[i];
		}

		if (index > 0)
			honorific = tokens[0];

		if (index < tokens.length) {
			firstName = tokens[index];
		} else {
			index = 0;
			honorific = "";
			firstName = tokens[0];
		}

		if (tokens.length == index + 1)
			lastName = tokens[index];
		else {
			int n = tokens.length;
			for (int i = n - lastNameLen; i < n; ++i) {
				if (i > n - lastNameLen)
					lastName += " ";
				lastName += tokens[i];
			}
			for (int i = index + 1; i < n - lastNameLen; ++i) {
				if (i > index + 1)
					middleName += " ";
				middleName += tokens[i];
			}
		}
	}

	/**
	 * 
	 * @return THe honorific of the name
	 */
	public String getHonorific() {
		return honorific;
	}

	/**
	 * 
	 * @return First name of the parsed string
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * 
	 * @return Middle name of the parsed string
	 */
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * 
	 * @return Last name of the parsed string
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * 
	 * @return A second name, if one exists
	 */
	public String getExtraName() {
		return extraName;
	}

	/**
	 * Compares two strings of type PER that are converted into NameParser
	 * objects. Primary function called by EntityComparison.
	 * 
	 * @param nameParser
	 *            The other string of type PER to be compared with
	 * @return A float value between 0 and 1 inclusive as the score
	 */
	public float compare(NameParser nameParser) {
		int matchType = 0;
		int firstNameMatchType = 0;
		float score = 0.0f;
		// Check to see if all the conditions are satisfied; return false if
		// there is any condition
		// failed.
		// 0. Matching special cases
		matchType = specialCasesMatch(nameParser);
		if (matchType == 0) {
			if (lastNameMatch(nameParser.getLastName())) {
				// 1. Match last name AND 2. Match first name (with or without
				// nicknames)
				firstNameMatchType = firstNameMatch(nameParser.getFirstName());
				switch (firstNameMatchType) {
				case 1:
					score = 1.0f;
					break; // both last and first names match exactly
				case 2:
					score = 0.75f;
					break; // last name matches and first names match as
							// nicknames of a common
							// name
				default:
					score = 0.0f; // last name matches, first name exists and
									// doesn't match
				}
			}
			return score;
		} else if (matchType == 3) {
			// name is subsumed; see if you can do better
			if (lastNameMatch(nameParser.getLastName())) {
				// 1. Match last name AND 2. Match first name (with or without
				// nicknames)
				firstNameMatchType = firstNameMatch(nameParser.getFirstName());
				switch (firstNameMatchType) {
				case 1:
					score = 1.0f;
					break; // both last and first names match exactly
				default:
					score = 0.9f; // last name matches, first name exists
									// (subsumption), but not
									// recognized as one
					// case 2 : score = 0.75f ; break ; // last name matches and
					// first names
					// match as nicknames of a common name
				}
			}
			return score;
		}
		// 3. Matching honorific
		if (!honorificMatching(nameParser.getHonorific()))
			score = 0.0f; // As of now honorifics are not handled. So, instead
							// of returning here,
							// just continue
		switch (matchType) {
		case 1:
			score = 0.93f;
			break; // match first name
		case 2:
			score = 0.95f;
			break; // match last name
		case 4:
			score = 0.75f;
			break; // match first names that are both nicknames
		case 3:
			score = 0.90f;
			break; // name subsumed
		}
		return score;
	}

	private boolean isSubsumed(List<String> shortList, List<String> longList) {
		HashSet<String> tokens = new HashSet<String>();
		synchronized (longList) {
			for (String token : longList)
				tokens.add(token);
		}

		for (int i = 0; i < shortList.size(); i++) {
			if (!tokens.contains(shortList.get(i)))
				return false;
		}
		return true;
	}

	// ==========
	private int specialCasesMatch(NameParser nameParser) {
		float score1, score2;
		boolean firstNameNicknameMatch = false;

		if (this.firstName.equals(this.lastName) || nameParser.getFirstName().equals(nameParser.getLastName())) {

			score1 = firstNameMatchScore(this.firstName, nameParser.getFirstName());
			score2 = lastNameMatchScore(this.lastName, nameParser.getLastName());

			if (score1 < 0.0f) {
				firstNameNicknameMatch = true;
				score1 *= -1;
			}

			if (score1 >= TYPOTHRES || score2 >= TYPOTHRES) {
				// return 2. if last name match is higher
				// return 4 if first name matched only on nicknames
				// else return 1
				return (score2 >= score1) ? 2 : (firstNameNicknameMatch) ? 4 : 1;
			}
		}

		// Check if one name is subsumed in another
		List<String> thisTokens = Collections.synchronizedList(this.arrNameTokens);
		List<String> thatTokens = Collections.synchronizedList(nameParser.getArrNameTokens());

		if (thisTokens.size() >= thatTokens.size() && isSubsumed(thatTokens, thisTokens)
				|| (thisTokens.size() < thatTokens.size() && isSubsumed(thisTokens, thatTokens)))
			return ((thisTokens.size() == thatTokens.size()) ? 0 : 3);
		return 0;
	}

	public ArrayList<String> getArrNameTokens() {
		return arrNameTokens;
	}

	/**
	 * Corrects spelling errors
	 * 
	 * @param thisName
	 *            First of the two compared strings
	 * @param thatName
	 *            Second of the two compared strings
	 * @return
	 */
	private float lastNameMatchScore(String thisName, String thatName) {
		float score = (float) jrwk.score(thisName, thatName);
		if (score < TYPOTHRES) {
			score = 0.0f;
			ArrayList<String> thisLastNames = new ArrayList<String>();
			ArrayList<String> thatLastNames = new ArrayList<String>();
			String tokens[] = thisName.split("\\s+");
			for (String tok : tokens)
				thisLastNames.add(tok);
			tokens = thatName.split("\\s+");
			for (String tok : tokens)
				thatLastNames.add(tok);

			if ((thisLastNames.size() >= thatLastNames.size() && isSubsumed(thatLastNames, thisLastNames))
					|| (thisLastNames.size() < thatLastNames.size() && isSubsumed(thisLastNames, thatLastNames)))
				score = 0.9f; // If name match fails, check for subsumption and
								// allow token subsets
		}
		return score;
	}

	/**
	 * True if the last name matches a last name string in the ArrayList.
	 * 
	 * @param lastName
	 *            Last name parsed from the string
	 * @return A boolean value (true/false) determining whether the last name is
	 *         valid
	 */
	private boolean lastNameMatch(String lastName) {
		return (lastNameMatchScore(this.lastName, lastName) > 0.0f);
	}

	/**
	 * Checks if the two given names are compatible first names. Returns 0 if
	 * the names are not compatible, > 0 if names are compatible and above
	 * threshold, and < 0: if names are both nicknames, but of a common name,
	 * and pass threshold
	 * 
	 * @param thisName
	 *            First of the two first name strings
	 * @param thatName
	 *            Second of the two first name strings
	 * @return A float value determining whether the two first names are
	 *         compatible
	 */
	private float firstNameMatchScore(String thisName, String thatName) {
		if (thisName.length() == 0 || thatName.length() == 0)
			return 0.0f;

		if (isAbbreviation(thisName) || isAbbreviation(thatName)) {
			if (thisName.charAt(0) == thatName.charAt(0))
				return 1.0f;
			return 0.0f;
		} else {
			ArrayList<String> thisFirstNames = nickMan.replaceNickNames(thisName);
			ArrayList<String> thatFirstNames = nickMan.replaceNickNames(thatName);

			// First check if one is a nickname of other
			float score = getMaxScore(thisName, thatFirstNames);
			if (score >= TYPOTHRES)
				return score;

			score = getMaxScore(thatName, thisFirstNames);
			if (score >= TYPOTHRES)
				return score;

			// If that fails, check if both are nicknames of a common name
			score = getMaxScore(thisFirstNames, thatFirstNames);
			if (score >= TYPOTHRES)
				return -1 * score;
			return 0.0f;
		}
	}

	/**
	 * Checks for matching first names in the ArrayList. Returns 0 if no match,
	 * 1 if names are compatible, and 2 if both are nicknames for a common name.
	 * 
	 * @param firstName
	 *            First name parsed from the string
	 * @return An integer 0, 1, or 2 determining if names are compatible
	 */
	private int firstNameMatch(String firstName) {
		float score = firstNameMatchScore(this.firstName, firstName);
		return (score == 0.0f) ? 0 : (score > 0.0f) ? 1 : 2;
	}

	private float getMaxScore(String name, ArrayList<String> firstNames) {
		float score, maxScore = 0.0f;
		int n = firstNames.size();
		for (int i = 0; i < n; i++) {
			String someFirstName = firstNames.get(i);
			score = (float) jrwk.score(someFirstName, name);
			if (score > maxScore) {
				maxScore = score;
			}
		}
		return maxScore;
	}

	private float getMaxScore(ArrayList<String> thisFirstNames, ArrayList<String> thatFirstNames) {
		float score, maxScore = 0.0f;
		int n = thisFirstNames.size();
		for (int i = 0; i < n; i++) {
			String thisFirstName = thisFirstNames.get(i);
			score = getMaxScore(thisFirstName, thatFirstNames);
			if (score > maxScore)
				maxScore = score;
		}
		return maxScore;
	}

	// =========
	private boolean isAbbreviation(String name) {
		if (name.length() == 1 || (name.length() == 2 && name.charAt(1) == '.'))
			return true;
		return false;
	}

	/**
	 * Checks if the string parameter is a valid honorific.
	 * 
	 * @param honorific
	 *            String to be checked for honorific
	 * @return A boolean value (true/false) determining whether the string is a
	 *         honorific
	 */
	public boolean honorificMatching(String honorific) {
		if (this.honorific.length() == 0 || honorific.length() == 0)
			return true;
		return honoMan.isMatching(this.honorific, honorific);
	}

	/**
	 * Checks if the string parameter is a valid nickname.
	 * 
	 * @param text
	 *            String to be checked for nickname
	 * @return A boolean value (true/false) determining whether the string is a
	 *         nickname
	 */
	public boolean isNickName(String text) {
		return nickMan.isNickName(text);
	}

	/**
	 * Finds all possible nicknames for the string parameter.
	 * 
	 * @param text
	 *            String to find the nickname mapping for from the HashMap
	 * @return An ArrayList of all possible nicknames for a certain name
	 */
	public ArrayList<String> getNickNameMapping(String text) {
		return nickMan.getNickNameMapping(text);
	}
}
