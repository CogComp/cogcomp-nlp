/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nesim.compare;

import edu.illinois.cs.cogcomp.nesim.data.*;
import edu.illinois.cs.cogcomp.nesim.utils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EntityComparison {
	// Constants
	public static final String SCORE = "SCORE";
	public static final String REASON = "REASON";
	public static final String TERMINATION = "_----_";
	public static final float NICKNAMES_THRES = 0.4f;
	public static final float RATIO_THRES = 0.f;
	public static final String NICKNAMES_LIST = "nicknames.txt";
	public static final String ACRONYMS_LIST = "acronyms.txt";
	public static final String SHORTCUTS_LIST = "shortcuts.txt";
	public static final String HONORIFIC_LIST = "honorifics.txt";
	public static final String COUNTRYLANG_LIST = "countrylanguage.txt";
	public static final String PEOPLE_LIST = "people.txt";
	public static final String LOCATION_LIST = "locations.txt";
	// Variables
	private AcronymManager acroMan;
	private ShortcutManager shortMan;
	private NameParser name1Parser;
	private NameParser name2Parser;
	private NameParser extraName1Parser;
	private NameParser extraName2Parser;
	private float score, scoreShingle;
	private String reason;
	private CountryLanguageManager countryMan;
	private PeopleManager peopleMan;
	private LocationManager locMan;
	private OrganizationManager orgMan;
	private DameraoLevenstein ed = new DameraoLevenstein();

	/**
	 * No-arg constructor that initializes all helper classes with appropriate
	 * files and sets the default values for score and reason.
	 * 
	 * @throws IOException
	 */
	public EntityComparison() throws IOException {
		acroMan = new AcronymManager(ACRONYMS_LIST);
		shortMan = new ShortcutManager(SHORTCUTS_LIST);
		name1Parser = new NameParser(HONORIFIC_LIST, NICKNAMES_LIST, NICKNAMES_THRES);
		name2Parser = new NameParser(HONORIFIC_LIST, NICKNAMES_LIST, NICKNAMES_THRES);
		extraName1Parser = new NameParser(HONORIFIC_LIST, NICKNAMES_LIST, NICKNAMES_THRES);
		extraName2Parser = new NameParser(HONORIFIC_LIST, NICKNAMES_LIST, NICKNAMES_THRES);
		countryMan = new CountryLanguageManager(COUNTRYLANG_LIST);
		peopleMan = new PeopleManager(PEOPLE_LIST);
		locMan = new LocationManager(LOCATION_LIST);
		orgMan = new OrganizationManager(ACRONYMS_LIST);
		score = 0.0f;
		scoreShingle = 0.0f;
		reason = "";
	}

	/**
	 * Helper function invoked by HashMap<String, String> compare. It maps the
	 * score (0-1) to "SCORE" and the reason to "REASON". Scoring based on the
	 * shingling method is mapped to "SHINGLE," although this has been merged to
	 * limited extent into the original scoring methods.
	 * 
	 * @param name1
	 *            First of the two names being compared
	 * @param name2
	 *            Second of the two names being compared
	 * @return HashMap<String, String> with corresponding scores and reasons
	 *         mapped
	 */
	public HashMap<String, String> compareNames(String name1, String name2) {
		compare(name1, name2);
		HashMap<String, String> result = new HashMap<String, String>();
		result.put("SCORE", Float.toString(score));
		result.put("SHINGLE", Float.toString(scoreShingle));
		result.put("REASON", reason);
		return result;
	}

	/**
	 * Primary helper compare function that converts the comparable strings into
	 * NameInfo objects. If no type if given by the user, the type
	 * (LOC/PER/ORG/GEN) is inferred without context and scored based on the
	 * inferred type. Types that are not GEN and are different are not
	 * considered comparable and given a score of 0.
	 * 
	 * @param name1
	 *            First of the two names being compared
	 * @param name2
	 *            Second of the two names being compared
     * @param type1
                  Type of the first name or null if type is unknown
     * @param type2
                  Type of the second name or null if type is unknown
	 */
	public void compare(String name1, String name2, String type1, String type2) {
		score = 0.0f;
		reason = "";

		if (type1 == null) {
			type1 = NameInfo.GENERIC_TYPE;
		}
		if (type2 == null) {
			type2 = NameInfo.GENERIC_TYPE;
		}
		NameInfo name1Info = new NameInfo(name1);
		NameInfo name2Info = new NameInfo(name2);
		name1Info.setType(type1);
		name2Info.setType(type2);

		type1 = name1Info.getType();
		type2 = name2Info.getType();

		// If enforced types are different and neither is generic
		if (!type1.equals(NameInfo.GENERIC_TYPE) && !type2.equals(NameInfo.GENERIC_TYPE) && !type2.equals(type1)) {
			score = 0.0f;
			scoreShingle = 0.0f;
			reason = "Types " + type1 + " and " + type2 + " cannot be compared";
			return;
		}

		fixType(name1Info, name2Info);

		type1 = name1Info.getType();
		type2 = name2Info.getType();

		if (!type1.equals(NameInfo.GENERIC_TYPE) && !type2.equals(NameInfo.GENERIC_TYPE) && !type2.equals(type1)) {
			score = 0.0f;
			scoreShingle = 0.0f;
			reason = "Types " + type1 + " and " + type2 + " cannot be compared";
			return;
		}

		int flag = 0;

		if (type1.equals("PER") && type1.equals(type2))
			flag = 1;
		else if (type1.equals("LOC") && type1.equals(type2))
			flag = 2;
		else if (type1.equals("ORG") && type1.equals(type2))
			flag = 3;
		else if (type1.equals("GEN") && type1.equals(type2))
			flag = 4;
		else
			flag = 5;

		scoring(name1Info, name2Info, flag);
	}

	/**
	 * Primary helper compare function that converts the comparable strings into
	 * NameInfo objects. Since no type is given by the user, the types are
     * inferred.
	 * 
	 * @param name1
	 *            First of the two names being compared
	 * @param name2
	 *            Second of the two names being compared
	 */
    public void compare(String name1, String name2) {
		String type1 = null;
		String type2 = null;
		compare(name1, name2, type1, type2);
    }

	/**
	 * Primary method invoked by the user to initiate comparison of two strings.
	 * Invokes other helper functions for comparison task.
	 * 
	 * @param mapNames
	 *            HashMap passed in by the wrapper to access the strings
	 * @return HashMap<String, String> containing score and reason for the
	 *         strings being compared
	 */
	public HashMap<String, String> compare(HashMap<String, String> mapNames) {
		HashMap<String, String> mapResults = compareNames(mapNames.get("FIRST_STRING"), mapNames.get("SECOND_STRING"));
		return mapResults;
	}

	/**
	 * The strings converted to NameInfo objects are assumed to have been not
	 * given explicit types by the user. This function is responsible for
	 * deducing the appropriate type for the strings being compared so that the
	 * best and most appropriate scoring can be used.
	 * 
	 * @param name1Info
	 *            NameInfo object containing information on the first string
	 * @param name2Info
	 *            NameInfo object containing information on the second string
	 */
	private void fixType(NameInfo name1Info, NameInfo name2Info) {
		String type1 = name1Info.getType();
		String type2 = name2Info.getType();
		// String name1 = name1Info.getName();
		// String name2 = name2Info.getName();

		if (!name1Info.isEnforced() && !name2Info.isEnforced()) {
			fixLoc(name1Info);
			fixLoc(name2Info);
			fixOrg(name1Info);
			fixOrg(name2Info);
			fixPeople(name1Info);
			fixPeople(name2Info);
			String longName = "";
			String shortName = "";
			if ((name1Info.getName()).length() >= (name2Info.getName()).length()) {
				longName = name1Info.getName();
				shortName = name2Info.getName();
			} else {
				longName = name2Info.getName();
				shortName = name1Info.getName();
			}
			if (acroMan.isAcronymFirstLetters(shortName, longName)) {
				name1Info.setType("ORG");
				name2Info.setType("ORG");
			}
		}

		type1 = name1Info.getType();
		type2 = name2Info.getType();

		if (!type1.equals(NameInfo.GENERIC_TYPE) && !type2.equals(NameInfo.GENERIC_TYPE) && !type1.equals(type2)) {
			scoreShingle = 0.0f;
			score = 0.0f;
			reason = "Types " + type1 + " and " + type2 + " cannot be compared";
			return;
		}

		if (type1.equals(NameInfo.GENERIC_TYPE) && !type2.equals(NameInfo.GENERIC_TYPE)) {
			type1 = type2;
			name1Info.setType(type1);
		} else if (type2.equals(NameInfo.GENERIC_TYPE) && !type1.equals(NameInfo.GENERIC_TYPE)) {
			type2 = type1;
			name2Info.setType(type2);
		}
	}

	/**
	 * This function checks NameInfo object to determine if the string should be
	 * designated as type PER.
	 * 
	 * @param nameInfo
	 *            NameInfo object that requires a type (LOC/ORG/PER) to be
	 *            assigned
	 */
	private void fixPeople(NameInfo nameInfo) {
		String type = nameInfo.getType();
		if (type.equals(NameInfo.GENERIC_TYPE)) {
			String name = nameInfo.getName();
			String tokens[] = name.split("\\s+");
			boolean isPeople = false;
			for (String token : tokens) {
				if (peopleMan.isValidPeopleName(token)) {
					isPeople = true;
					break;
				}
			}
			if (isPeople == true)
				nameInfo.setType("PER");
		}
	}

	/**
	 * This function checks NameInfo object to determine if the string should be
	 * designated as type LOC.
	 * 
	 * @param nameInfo
	 *            NameInfo object that requires a type (LOC/ORG/PER) to be
	 *            assigned
	 */
	private void fixLoc(NameInfo nameInfo) {
		String type = nameInfo.getType();
		if (type.equals(NameInfo.GENERIC_TYPE)) {
			String name = nameInfo.getName();
			if (locMan.isValidLocation(name))
				nameInfo.setType("LOC");
		}
	}

	/**
	 * This function checks NameInfo object to determine if the string should be
	 * designated as type ORG.
	 * 
	 * @param nameInfo
	 *            NameInfo object that requires a type (LOC/ORG/PER) to be
	 *            assigned
	 */
	private void fixOrg(NameInfo nameInfo) {
		String type = nameInfo.getType();
		if (type.equals(NameInfo.GENERIC_TYPE)) {
			String name = nameInfo.getName();
			if (orgMan.isValidOrg(name))
				nameInfo.setType("ORG");
		}
	}

	/**
	 * This is the primary function used in determining which type of scoring to
	 * use based on the types of the strings. If no type was assigned to the
	 * strings being compared, then the highest score is used among all the
	 * scoring methods, shingling being a viable option as well.
	 * 
	 * @param name1Info
	 *            First NameInfo object to be compared
	 * @param name2Info
	 *            Second NameInfo object to be compared
	 * @param flag
	 *            Determines which type-specific scoring to use (LOC/ORG/PER).
	 *            If none of the types are appropriate then it uses all the
	 *            scoring types, shingling included, and the highest score is
	 *            considered the actual score
	 */
	private void scoring(NameInfo name1Info, NameInfo name2Info, int flag) {
		String name1 = name1Info.getName();
		String name2 = name2Info.getName();
		String type1 = name1Info.getType();
		String type2 = name2Info.getType();
		if (name1.length() == 0 || name2.length() == 0) {
			score = 0.0f;
			scoreShingle = 0.0f;
			reason = "There is an empty name.";
			return;
		}
		switch (flag) {

		case 1: // PER
			scoreShingle = (float) LSHUtils.dice(name1, name2);
			float scorePER = getPERScore(name1Info, name2Info);
			reason = "Type: " + name1Info.getType() + ". metric";
			if (score < scorePER) {
				score = scorePER;
			}
			break;

		case 2: // LOC
			scoreShingle = (float) LSHUtils.dice(name1, name2);
			score = shortMan.scoring(name1, name2);
			reason = "Type: " + name1Info.getType() + ". metric; Scored by JaroWinkler metric with Shortcut.";
			float countryLangScore = countryMan.scoring(name1, name2);
			if (score < countryLangScore) {
				score = countryLangScore;
				reason = "Type: " + name1Info.getType()
						+ ". metric; Scored by JaroWinkler metric with Country-Language list.";
				if (score < scoreShingle) {
					score = scoreShingle;
					reason = "Type: " + name1Info.getType() + ". metric; Scored by Shingling";
				}
			}
			break;

		case 3: // ORG
			scoreShingle = (float) LSHUtils.dice(name1, name2);
			score = acroMan.scoring(name1Info, name2Info);
			reason = "Type: " + name1Info.getType() + ". metric; Scored by JaroWinkler metric with Acronym.";
			if (scoreShingle > score) {
				score = scoreShingle;
				reason = "Type: " + name1Info.getType() + ". metric; Scored by Shingling";
			}
			break;

		case 4: // GEN
			scoreShingle = (float) LSHUtils.dice(name1, name2);
			float scorePer = getPERScore(name1Info, name2Info);
			float scoreShort = shortMan.scoring(name1, name2);
			float scoreCL = countryMan.scoring(name1, name2);
			float scoreAcro = acroMan.scoring(name1Info, name2Info);
			if (scoreAcro > score) {
				score = scoreAcro;
				reason = "Type: " + name1Info.getType() + ". metric; Scored by JaroWinkler metric with Acronym.";
			}
			if (scoreCL > score) {
				score = scoreCL;
				reason = "Type: " + name1Info.getType()
						+ ". metric; Scored by JaroWinkler metric with Country-Language list.";
			}
			if (scoreShort > score) {
				score = scoreShort;
				reason = "Type: " + name1Info.getType() + ". metric; Scored by JaroWinkler metric with Shortcut.";
			}
			if (scorePer > score) {
				score = scorePer;
				reason = "Type: " + name1Info.getType() + ". metric";
			}
			if (scoreShingle > score) {
				score = scoreShingle;
				reason = "Type: " + name1Info.getType() + ". metric; Scored by Shingling";
			}
			if (acroMan.isSpecificCase()) {
				score = scoreAcro;
				reason = "Type: " + name1Info.getType() + ". metric; Scored by JaroWinkler metric with Acronym.";
			}
			break;

		case 5: // Two different types
			if (type1.equals("PER") || type2.equals("PER")) {
				score = 0.0f;
				reason = "Type: " + type1 + ". and " + type2 + ". cannot be compared";
			} else if (type1.equals("LOC") || type2.equals("LOC")) {
				score = shortMan.scoring(name1, name2);
				reason = "Type: LOC. metric; Scored by JaroWinkler metric with Shortcut.";
			} else if (type1.equals("ORG") || type2.equals("ORG")) {
				float orgScore = acroMan.scoring(name1Info, name2Info);
				if (score < orgScore) {
					score = orgScore;
					reason = "Type: ORG. metric; Scored by JaroWinkler metric with Acronym.";
				}
			} else {
				score = 0.0f;
				reason = "Type: " + type1 + ". and " + type2 + ". cannot be compared";
			}
			break;

		default:
			score = 0.0f;
			reason = "#######SHOULD NOT HAPPEN#######";
		}
		if (name1.equalsIgnoreCase(name2))
			score = 1.0f;
		if (!name1Info.getType().equals("PER") && !name2Info.getType().equals("PER"))
			handleLargeRatio(name1, name2);
	}

	/**
	 * This function is only invoked if the type is not designated PER. If the
	 * strings being compared have a very significant difference in length, as
	 * determined by the ratio threshold, then a score of 0 is assigned as they
	 * are too different in length to be comparable.
	 * 
	 * @param name1
	 *            First of the names being compared
	 * @param name2
	 *            Second of the names being compared
	 */
	private void handleLargeRatio(String name1, String name2) {
		int n1 = name1.length();
		int n2 = name2.length();
		int longN = (n1 > n2) ? n1 : n2;
		int shortN = (n1 > n2) ? n2 : n1;
		float ratio = (float) shortN / (float) longN;
		if (ratio < RATIO_THRES)
			if (score != 1.0f) {
				score = 0.0f;
				scoreShingle = 0.0f;
				reason = "Two names have a very large difference in length, and were not captured by other handlers.";
			}
	}

	/**
	 * Helper function invoked by the scoring method to score strings designated
	 * as type PER only.
	 * 
	 * @param name1Info
	 *            First NameInfo object to be compared
	 * @param name2Info
	 *            Second NameInfo object to be compared
	 * @return A floating point value between 0 and 1 inclusive
	 */
	private float getPERScore(NameInfo name1Info, NameInfo name2Info) {
		// initials compared with full name
		String acroName1 = name1Info.getName();
		String acroName2 = name2Info.getName();
		if (acroName1.length() == acroName2.length()) { // similar names,
														// perhaps with
														// misspelling
														// or apostrophe
			if (ed.score(acroName1, acroName2) <= 1.0f)
				return 1.0f;
		}
		String shortName = "";
		if (acroName1.length() > acroName2.length()) {
			shortName = acroMan.findAcronym(acroName1);
			if (shortName.equalsIgnoreCase(acroName2))
				return 1.0f;
		} else {
			shortName = acroMan.findAcronym(acroName2);
			if (shortName.equalsIgnoreCase(acroName1))
				return 1.0f;
		}

		// last name compared with full name
		String longName = "";
		shortName = "";
		if ((name1Info.getName()).length() >= (name2Info.getName()).length()) {
			longName = name1Info.getName();
			shortName = name2Info.getName();
		} else {
			longName = name2Info.getName();
			shortName = name1Info.getName();
		}
		String[] tokensLong = longName.split("\\s+");
		for (String word : tokensLong) {
			if (shortName.equalsIgnoreCase(word))
				return 1.0f;
		}

		// first and last names are too similar for NameParser to distinguish
		String Name1 = name1Info.getName(), Name2 = name2Info.getName();
		String[] tokens1 = (Name1.trim()).split("\\s+");
		String[] tokens2 = (Name2.trim()).split("\\s+");
		if (tokens1.length == tokens2.length && tokens1.length == 2) {
			String[] shortened = removeSameWords(Name1, Name2);
			String shortName1 = shortened[0], shortName2 = shortened[1];
			float preciseScore = (float) LSHUtils.dice(shortName1, shortName2);
			if (preciseScore < 0.5f)
				return 0.0f;
			else
				return 1.0f;
		}

		// score using NameParser
		float maxScore = 0.0f;
		float maxExtraScore = 0.0f;
		String name1Extra, name2Extra;
		float perScore;

		ArrayList<String> candidate1 = name1Info.getCandidates();
		ArrayList<String> candidate2 = name2Info.getCandidates();
		int n = candidate1.size();
		int m = candidate2.size();

		for (int i = 0; i < n; i++) {
			String name1 = candidate1.get(i);
			name1Parser.parsing(name1);

			name1Extra = name1Parser.getExtraName();
			if (!name1Extra.isEmpty())
				extraName1Parser.parsing(name1Extra);

			for (int j = 0; j < m; j++) {
				String name2 = candidate2.get(j);
				name2Parser.parsing(name2);

				name2Extra = name2Parser.getExtraName();
				if (!name2Extra.isEmpty())
					extraName2Parser.parsing(name2Extra);

				if (name1.equals(name2))
					perScore = 1.0f;
				else {
					perScore = name1Parser.compare(name2Parser);
					float extraPerScore;
					if (name1Extra.isEmpty() || name2Extra.isEmpty())
						extraPerScore = (name1Extra.isEmpty() && name2Extra.isEmpty()) ? perScore : 0.9f;
					else
						extraPerScore = (name1Extra.equals(name2Extra)) ? 1.0f
								: extraName1Parser.compare(extraName2Parser);

					perScore = (perScore + extraPerScore) / 2.0f;
				}
				if (perScore > maxScore)
					maxScore = perScore;

				if (!name1Extra.isEmpty()) {
					perScore = extraName1Parser.compare(name2Parser);
					if (perScore > maxExtraScore)
						maxExtraScore = perScore;
				}
				if (!name2Extra.isEmpty()) {
					perScore = name1Parser.compare(extraName2Parser);
					if (perScore > maxExtraScore)
						maxExtraScore = perScore;
				}
			}
		}
		return (maxExtraScore > maxScore) ? maxExtraScore : maxScore;
	}

	private String[] removeSameWords(String name1, String name2) {
		ArrayList<String> tmp1 = new ArrayList<String>();
		ArrayList<String> tmp2 = new ArrayList<String>();
		ArrayList<String> sameWords = new ArrayList<String>();
		String[] tokens1 = name1.split("\\s+");
		String[] tokens2 = name2.split("\\s+");
		for (String a : tokens1)
			tmp1.add(a);
		for (String b : tokens2)
			tmp2.add(b);
		for (int i = 0; i < tmp1.size(); i++) {
			String a = tmp1.get(i);
			for (int j = 0; j < tmp2.size(); j++) {
				String b = tmp2.get(j);
				if (a.equalsIgnoreCase(b)) {
					tmp1.remove(i);
					tmp2.remove(j);
					if (!sameWords.contains(a))
						sameWords.add(a);
					i--;
					break;
				}
			}
		}
		for (int i = 0; i < tmp1.size(); i++) {
			if (sameWords.contains(tmp1.get(i))) {
				tmp1.remove(i);
				i--;
			}
		}
		for (int i = 0; i < tmp2.size(); i++) {
			if (sameWords.contains(tmp2.get(i))) {
				tmp2.remove(i);
				i--;
			}
		}
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		for (int i = 0; i < tmp1.size(); i++) {
			sb1.append(tmp1.get(i) + " ");
		}
		for (int i = 0; i < tmp2.size(); i++) {
			sb2.append(tmp2.get(i) + " ");
		}
		name1 = (sb1.toString()).trim();
		name2 = (sb2.toString()).trim();
		String[] shortened = { name1, name2 };
		return shortened;
	}

	/**
	 * Gets score of the two strings currently being compared.
	 * 
	 * @return Score from comparing two strings
	 */
	public float getScore() {
		return score;
	}

	/**
	 * Gets reason of the two strings currently being compared.
	 * 
	 * @return Reason for scoring two strings
	 */
	public String getReason() {
		return reason;
	}

	public static void main(String[] args) {
		try {
			EntityComparison entityComp = new EntityComparison();
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String str = "";
			while (str != null) {
				str = in.readLine();
				if (str == null) {
					System.out.println("0");
					System.out.println("ERROR (no input): Please input names: TYPE#name1 and TYPE#name2");
					System.out.println();
					System.out.println();
					continue;
				}
				if (str.equals(TERMINATION))
					break;
				String[] names = parseInput(str);
				if (names == null) {
					System.out.println("0");
					System.out.println("ERROR (incorrect input format): Please input names: TYPE#name1----TYPE#name2");
					System.out.println();
					System.out.println();
					continue;
				}
				entityComp.compare(names[0], names[1]);
				float score = entityComp.getScore();
				String reason = entityComp.getReason();
				System.out.println(score);
				System.out.println(reason);
				System.out.println();
				System.out.println();
			}
		} catch (IOException e) {
		}
	}

	// ============
	public static String[] parseInput(String str) {
		String[] names = str.split("----");
		if (names.length != 2)
			return null;
		else {
			String name1 = names[0];
			String name2 = names[1];
			if (name1.length() == 0 || name2.length() == 0)
				return null;
		}
		return names;
	}

	// ============
	public List<String> getLongForms(String acronym) {
		List<String> longForms = acroMan.getLongForm(acronym);
		List<String> longForms2 = shortMan.getLongForm(acronym);
		for (int i = 0; i < longForms2.size(); i++)
			longForms.add(longForms2.get(i));
		return longForms;
	}

	// ============
	public List<String> getAbbreviations(String longName) {
		List<String> shortForms = acroMan.getShortForm(longName);
		List<String> shortForms2 = shortMan.getShortForm(longName);
		for (int i = 0; i < shortForms2.size(); i++)
			shortForms.add(shortForms2.get(i));

		String acronym = acroMan.findAcronym(longName);
		if (!acronym.equals("")) {
			boolean exists = false;
			for (int i = 0; i < shortForms.size(); i++) {
				if (shortForms.get(i).equals(acronym)) {
					exists = true;
					break;
				}
			}
			if (!exists)
				shortForms.add(acronym);
		}
		return shortForms;
	}
}
