/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

/**
 * Created by zhilifeng on 11/10/16.
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import edu.illinois.cs.cogcomp.core.algorithms.LevensteinDistance;
import org.joda.time.Interval;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Phrase {
    static int flagnull = 0;
    static int comparenum = 4;
    static String constantphrase1;
    static String constantphrase2;
    static int flagonlyyear = -1;
    static int onlyyear = -1;
    static int onlymonth = -1;
    static String result1;
    static String result2;
    static String firstphrase;
    static String secondphrase;
    static int flagnum = -1;
    static int flagdaynum = -1;
    static int flagmonnum = -1;
    static String monthnum = null;
    static String year = null;
    static String day = null;
    static String month = "(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:tember)?|sept(?:ember)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?)";
    static String holiday1 = "(foundation day|new year|christmas|coming of age day|epiphany|martyrs Day|Martin Luther King's Day|Timkat|Husbands Day|National Day|The Three Holy Hierarchs|Air Force Day|Candlemas|Fiesta de la Candelaria|Constitution Day|Pre Eren Day|St. Paul's shipwreck|Unification Day|International Woman's Day|Army Day|Clean Monday|Shrove Monday|Washington's Birthday|Carnival|Ash Wednesday|Defender of the Fatherland|Independence Day|National Flag Day|Beer Day|Heroes' Day|Victory at Adwa Day|Liberation Day|The Spring Day|Saint Patrick's Day|Benito Juárez's birthday|St. Josephs day|Human Rights Day|Day of Remembrance for Truth and Justice|Annunciation|Freedom and Democracy Day|Mawlid an-Nabi|Malvinas Day|Maundy Thursday|Good Friday|Easter Saturday|Easter Monday|Easter|Juan Santamaria's day|First Day of Summer|Patriots' Day|Republic Day|Tiradentes' Day|St.George Day|ANZAC day|Freedom and Democracy Day|Liberation Day|Freedom Day|Showa DayQueen's birthday|Labour day|May Day|Bridging Holiday|General prayer day|Greenery Day|Children's Day|Victory Day|St.George Day|Bank Holiday|Victory Day|Mothers' Day|Ascension Day|Battle of Las Piedras|Navy day|Battle of Pichincha|Education, Culture and Literature Day|Saints Cyril and Methodius Day|May Revolution|Pentecost|Memorial Day|Pentecost Monday|Whit Monday|Freedom and Democracy Day|Republic Day|Corpus Christi|Sette Giugno|Chaco Armistice|Sacred Heart|Youth Day|Artigas' Birthday|Anti-Fascist Struggle Day|Midsummer Day|Victory Day|Battle of Carabobo|Johannis day|Jónsmessa|Statehood day|St. Peter and Paul day|St.Vitus' Day|St. Peter and Paul day|Saints Cyril and Methodius Day|Jan Hus Day|Republic Day|Statehood day|St.Peter's Day|Navy day|Virgin Carmen day|Birthday of Simón Bolivar|Guanacaste Day|St.Elijah's Day|Victory Day|Commerce Day|Battle of Boyacá|Independence of Quito|Assumption day|Assumption of Mary|Founding of Asunción|Day of Restoration of Independence|José de San Martín Day|St.Stephen's Day|National Uprising|Holy Rosa of Lima|National Language Day|Unification Day|Nativity of Mary|Nativity of our Lady|Enkutatash|Ramadan|Battle of San Jacinto|Respect-for-the-Aged Day|Heritage day|Meskel|St. Wenceslas Day|Battle of Angamos|Day of the Race|Health and Sports Day|Columbus Day|Revolution day|Day of the Indigenous Resistance|Our Lady of Aparecida|Eid ul-Fitr|Beatification of Mother Teresa|Ochi Day|Reformation day|All Saints|All Souls|Culture Day|Independence of Cuenca|Separation Day|Consolidation Day|October Revolution Day|St.Demetrius' Day|Armistice Day|Independence of Cartagena|Remembrance day|St. Martin's Day|Veterans Day|King's Feast|Thanksgiving Day|Self-governance Day|Foundation of Quito|St. Nicholas Day|Immaculate Conception Day|Saint Clement of Ohrid day|Day of Reconciliation|Eid ul-Adha|Emperors Birthday|Boxing Day|Day of Goodwill|St.Stephen's Day)";
    static String holiday = holiday1.toLowerCase();
    static String delims = "[(|)]";
    static String[] hotoken = holiday.split(delims);
    static String reference = "";
    static String referenceyear;
    static String referencemonth;
    static String referenceday;
    static DateTime defaultstart = new DateTime(0, 1, 1, 0, 0, 0, 0);
    static DateTime defaultfinish = new DateTime(0, 1, 1, 0, 0, 0, 0);
    static Interval overlap = new Interval(defaultstart, defaultfinish);

    //The parameters of the function fullNumerical	function
    //Chinese style: YY/MM/DD
    //USA style: MM/DD/YY
    //Europe style: DD/MM/YY
    public enum datetype {
        CHINESE, USA, EUROPE
    }

    //The type is used to define the comparison
    public enum type {
        BEFORE, AFTER, OVERLAP, BEFOREOROVERLAP, AFTEROROVERLAP, VAGUE
    }

    static type dayy = type.BEFORE;
    static datetype form = datetype.USA;


    public static boolean test2(String[] tester) {
        //first principle: Only one token length can be greater than 2
        if (tester[0].length() > 2 && tester[1].length() > 2) {
            return false;
        }
        //############################################################
        //second principle:only one token value can greater than 31
        if (Integer.parseInt(tester[1]) > 31 && Integer.parseInt(tester[0]) > 31) {
            return false;
        }

        //third principle:if there is one token length greater than 2 or the value greater than 31,it should be the year
        if (tester[0].length() > 2 || Integer.parseInt(tester[0]) > 31) {
            year = tester[0];
            day = tester[1];
            return true;
        }

        if (tester[1].length() > 2 || Integer.parseInt(tester[1]) > 31) {
            year = tester[1];
            day = tester[0];
            return true;
        }

        return false;
    }

    public static boolean isInteger(String string) {
        try {
            Integer.valueOf(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean test(String[] tester) {
        //first principle: Only one token length can be greater than 2
        if (tester[0].length() > 2 && tester[1].length() > 2) {
            return false;
        }
        if (tester[0].length() > 2 && tester[2].length() > 2) {
            return false;
        }
        if (tester[1].length() > 2 && tester[2].length() > 2) {
            return false;
        }

        if (!isInteger(tester[0]) || !isInteger(tester[1]) || !isInteger(tester[2])) {
            return false;
        }

        //######################################################################
        //second principle:if there is one token length greater than 2 or the value greater than 31,the other two tokens can not both greater than 12 and one greater than 31
        if (tester[0].length() > 2 || (isInteger(tester[0]) && Integer.parseInt(tester[0]) > 31)) {
            flagnum = 0;
            if (Integer.parseInt(tester[1]) > 12 && Integer.parseInt(tester[2]) > 12) {
                return false;
            }
            if (Integer.parseInt(tester[1]) > 31 || Integer.parseInt(tester[2]) > 31) {
                return false;
            }
            if (Integer.parseInt(tester[1]) > 12) {
                flagdaynum = 1;
                flagmonnum = 2;
                return true;
            } else {
                flagdaynum = 2;
                flagmonnum = 1;
                return true;
            }
        } else if (tester[1].length() > 2 || (isInteger(tester[1]) && Integer.parseInt(tester[1]) > 31)) {
            flagnum = 1;
            if (Integer.parseInt(tester[0]) > 12 && Integer.parseInt(tester[2]) > 12) {
                return false;
            }
            if (Integer.parseInt(tester[0]) > 31 || Integer.parseInt(tester[2]) > 31) {
                return false;
            }
            if (Integer.parseInt(tester[0]) > 12) {
                flagdaynum = 0;
                flagmonnum = 2;
                return true;
            } else {
                flagdaynum = 2;
                flagmonnum = 0;
                return true;
            }
        } else if (tester[2].length() > 2 || (isInteger(tester[2]) && Integer.parseInt(tester[2]) > 31)) {
            flagnum = 2;
            if (Integer.parseInt(tester[1]) > 12 && Integer.parseInt(tester[0]) > 12) {
                return false;
            }
            if (Integer.parseInt(tester[1]) > 31 || Integer.parseInt(tester[0]) > 31) {
                return false;
            }
            if (Integer.parseInt(tester[0]) > 12) {
                flagdaynum = 0;
                flagmonnum = 1;
                return true;
            } else {
                flagdaynum = 1;
                flagmonnum = 0;
                return true;
            }
        }
        //#########################################################################

        return false;
    }

    //First type: 3.20.1980;1998/11/12;02-10-86;1988 11 12;
    public static String fullNumerical(String phrase, datetype information) {
        String patternStr = "\\s*\\d{1,4}\\s*(?:\\s|[/\\-\\.\\,])\\s*\\d{1,4}\\s*(?:\\s|[/\\-\\.\\,])\\s*\\d{1,4}\\s*";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(phrase);
        boolean matchFound = matcher.find();
        String aim = new String();
        if (matchFound) {
            // Get all groups for this match
            String delims = "[ -/.,]+";
            String[] tokens = phrase.split(delims);

            // The Phrase should pass all the principle test of the date form
            if (test(tokens)) {
                if (flagnum != -1 && flagdaynum != -1 && flagmonnum != -1) {
                    aim = tokens[flagmonnum] + "/" + tokens[flagdaynum] + "/" + tokens[flagnum];
                    return aim;
                }
            }

            if (information == null) {
                return null;
            }
            switch (information) {
                case CHINESE:
                    return tokens[1] + "/" + tokens[2] + "/" + tokens[0];
                case USA:
                    return tokens[0] + "/" + tokens[1] + "/" + tokens[2];
                case EUROPE:
                    return tokens[1] + "/" + tokens[0] + "/" + tokens[2];

            }
        }
        return null;
    }

    public static String fullNameDate(String phrase, datetype information) {
        int i;
        int j;
        DateMapping temp = DateMapping.getInstance();
        String[] passby = new String[3];
        String patternStr = "\\s*((?:\\d{1,4}(?:st|nd|rd|th)?)|" + month + ")\\s*(?:\\s|[/\\-\\.\\,])\\s*((?:\\d{1,4}(?:st|nd|rd|th)?)|" + month + ")\\s*(?:\\s|[/\\-\\.\\,])\\s*((?:\\d{1,4}(?:st|nd|rd|th)?)|" + month + ")\\s*";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(phrase);
        boolean matchFound = matcher.find();
        boolean submatchFound;
        if (matchFound) {
            String subpatternStr = month;
            Pattern subpattern = Pattern.compile(subpatternStr);
            for (i = 1; i <= 3; i++) {
                Matcher submatcher = subpattern.matcher(matcher.group(i));
                submatchFound = submatcher.find();
                if (submatchFound) {
                    break;
                }
            }

            if (i <= 3) {
                flagmonnum = i;
                monthnum = temp.hm.get(matcher.group(flagmonnum));
            }
            String subpatternStr2 = "\\s*(\\d{1,4}(?:st|nd|rd|th))\\s*";
            Pattern subpattern2 = Pattern.compile(subpatternStr2);
            for (i = 1; i <= 3; i++) {
                Matcher submatcher2 = subpattern2.matcher(matcher.group(i));
                submatchFound = submatcher2.find();
                if (submatchFound) {
                    break;
                }
            }

            if (i <= 3) {
                flagdaynum = i;
                day = (matcher.group(flagdaynum)).split("[a-z]")[0];
            }

            if (flagdaynum != -1 && flagmonnum != -1) {
                for (j = 1; j <= 3; j++) {
                    if (j == flagdaynum || j == flagmonnum) {
                        continue;
                    }
                    flagnum = j;
                    year = matcher.group(flagnum);
                    break;
                }

                return monthnum + "/" + day + "/" + year;
            } else {
                i = 0;
                if (flagmonnum == 1) {
                    return monthnum + "/" + matcher.group(2) + "/" + matcher.group(3);
                }
                //Assume we have already get monthnum
                for (j = 1; j <= 3; j++) {
                    if (j != flagmonnum) {
                        passby[i] = matcher.group(j);
                        i++;
                    }
                }
                if (test2(passby)) {

                    return monthnum + "/" + day + "/" + year;
                } else {
                    switch (information) {
                        case CHINESE:
                            return monthnum + "/" + matcher.group(3) + "/" + matcher.group(1);
                        case USA:
                            return monthnum + "/" + matcher.group(3) + "/" + matcher.group(1);
                        case EUROPE:
                            return monthnum + "/" + matcher.group(1) + "/" + matcher.group(3);
                    }
                }

            }

        }

        return null;
    }

    public static boolean halftest(String[] tester) {
        //first principle: Only one token length can be greater than 2
        if (tester[0].length() > 2 && tester[1].length() > 2) {
            return false;
        }
        if (!isInteger(tester[0]) || !isInteger(tester[1]) || !isInteger(tester[2])) {
            return false;
        }
        //######################################################################
        //second principle:if there is one token length greater than 2 or the value greater than 31,the other one must less than 12 (year,month)
        if (tester[0].length() > 2 || Integer.parseInt(tester[0]) > 31) {
            flagnum = 0;
            if (Integer.parseInt(tester[1]) > 12) {
                return false;
            } else {
                flagmonnum = 1;
                return true;
            }
        }

        if (tester[1].length() > 2 || Integer.parseInt(tester[1]) > 31) {
            flagnum = 1;
            if (Integer.parseInt(tester[0]) > 12) {
                return false;
            } else {
                flagmonnum = 0;
                return true;
            }
        }
        //third principle:if both tokens length are less than 2 and the value less than 31,only one token can greater than 12(day,month)
        if (tester[0].length() <= 2 && Integer.parseInt(tester[0]) <= 31 && tester[1].length() <= 2 && Integer.parseInt(tester[1]) <= 31) {
            if (Integer.parseInt(tester[0]) > 12) {
                if (Integer.parseInt(tester[1]) > 12) {
                    return false;
                } else {
                    flagmonnum = 1;
                    if (tester[0].length() == 2 && Integer.parseInt(tester[0]) < 10) {
                        flagnum = 0;
                    } else {
                        flagdaynum = 0;
                    }


                    return true;
                }
            } else if (Integer.parseInt(tester[1]) > 12) {
                if (Integer.parseInt(tester[0]) > 12) {
                    return false;
                } else {
                    flagmonnum = 0;
                    if (tester[1].length() == 2 && Integer.parseInt(tester[1]) < 10) {
                        flagnum = 1;
                    } else {
                        flagdaynum = 1;
                    }
                    return true;
                }
            }
        }
        //#########################################################################

        return false;
    }

    public static String halfnumerical(String phrase, datetype information) {
        String patternStr = "\\s*\\d{1,4}\\s*(?:\\s|[/\\-\\.\\,])\\s*\\d{1,4}\\s*";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(phrase);
        boolean matchFound = matcher.find();
        String aim = new String();
        if (matchFound) {
            String delims = "[ -/.]+";
            String[] tokens = phrase.split(delims);

            // The Phrase should pass all the principle test of the date form
            if (halftest(tokens)) {

                //case1:year,month
                if (flagdaynum == -1) {
                    aim = tokens[flagmonnum] + "/XX/" + tokens[flagnum];
                    return aim;
                }
                //case 2: month and day
                else if (flagnum == -1) {
                    return aim = tokens[flagmonnum] + "/" + tokens[flagdaynum] + "/XXXX";
                }
            }

            if (information == null) {
                return null;
            }
            switch (information) {
                case CHINESE:
                    return tokens[1] + "/XX/" + tokens[0];
                case USA:
                    return tokens[0] + "/" + tokens[1] + "/XXXX";
                case EUROPE:
                    return tokens[1] + "/" + tokens[0] + "/XXXX";

            }
        }
        return null;

    }

    public static String halfnamedate(String phrase, datetype information) {

        int i;
        DateMapping temp = DateMapping.getInstance();


        String patternStr = "\\s*((?:\\d{1,4}(?:st|nd|rd|th)?)|" + month + ")\\s*(?:\\s|[/\\-\\.\\,])\\s*((?:\\d{1,4}(?:st|nd|rd|th)?)|" + month + ")\\s*";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(phrase);
        boolean matchFound = matcher.find();
        boolean submatchFound;

        if (matchFound) {

            String subpatternStr = "\\s*" + month + "\\s*";
            Pattern subpattern = Pattern.compile(subpatternStr);
            for (i = 1; i <= 2; i++) {
                Matcher submatcher = subpattern.matcher(matcher.group(i));
                submatchFound = submatcher.find();
                if (submatchFound) {
                    break;
                }
            }

            if (i <= 2) {
                flagmonnum = i;
                monthnum = temp.hm.get(matcher.group(flagmonnum));
            }
            String subpatternStr2 = "\\s*(\\d{1,4}(?:st|nd|rd|th))\\s*";
            Pattern subpattern2 = Pattern.compile(subpatternStr2);
            for (i = 1; i <= 2; i++) {
                Matcher submatcher2 = subpattern2.matcher(matcher.group(i));
                submatchFound = submatcher2.find();
                if (submatchFound) {
                    break;
                }
            }

            if (i <= 2) {
                flagdaynum = i;
                day = (matcher.group(flagdaynum)).split("[a-z]")[0];
            }

            //case 1:month,day
            if (flagdaynum != -1 && flagmonnum != -1) {

                return monthnum + "/" + day + "/XXXX";
            } else {
                //Assume we have already get monthnum
                for (i = 1; i <= 2; i++) {
                    if (i != flagmonnum) {
                        break;
                    }
                }
                String leftover = matcher.group(i);
                //first principle: if the length of the greater than 2 or the value greater than 31, it should be year
                if (leftover.length() > 2 || Integer.parseInt(leftover) > 31) {
                    return monthnum + "/XX/" + leftover;
                }
                //############################################################
                //second principle:if the the value is smaller than 31, regard it as day
                if (Integer.parseInt(leftover) < 31) {
                    if (leftover.length() == 2 && Integer.parseInt(leftover) < 10) {
                        return monthnum + "/XX/" + leftover;
                    } else {
                        return monthnum + "/" + leftover + "/XXXX";
                    }

                } else {
                    switch (information) {
                        case CHINESE:
                            return monthnum + "/XX/" + matcher.group(1);
                        case USA:
                            return monthnum + "/" + matcher.group(2) + "/XXXX";
                        case EUROPE:
                            return monthnum + "/" + matcher.group(1) + "/XXXX";
                    }
                }

            }

        }


        return null;
    }

    public static String converter(String phrase1) {
        int i;
        DateMapping temp = DateMapping.getInstance();
        //It records the number of term(Example:day,month,year)
        int numterm = 0;
        String result;
        phrase1 = phrase1.toLowerCase();
        phrase1 = phrase1.trim();

        String patternStr = "\\s*((?:\\d{1,4}(?:st|nd|rd|th)?)|" + month + ")\\s*(?:\\s|[/\\-\\.\\,])?\\s*((?:\\d{1,4}(?:st|nd|rd|th)?)|" + month + ")?\\s*(?:\\s|[/\\-\\.\\,])?\\s*((?:\\d{1,4}(?:st|nd|rd|th)?)|" + month + ")?\\s*";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(phrase1);
        boolean matchFound = matcher.find();

        if (matchFound) {
            for (i = 1; i <= 3; i++) {
                if (matcher.group(i) == null) {
                    i--;
                    break;
                }
            }
            if (i == 4) {
                i--;
            }
            numterm = i;
            //First situation: day,month,year
            if (numterm == 3) {
                result = fullNumerical(phrase1, form);
                //It is not pure numerical full date
                if (result == null) {

                    result = fullNameDate(phrase1, form);
                }
                return result;
            }

            //Second situation:(1)day,month(2)month,year
            else if (numterm == 2) {

                result = halfnumerical(phrase1, form);
                //It is not pure numerical full date
                if (result == null) {
                    result = halfnamedate(phrase1, form);

                }
                return result;
            }

            //Third situation:(1)day(st|nd|rd|th)(2)month(can only be month)(3)year
            else if (numterm == 1) {

                String patternStr1 = "\\s*((?:\\d{1,4}(?:st|nd|rd|th)?)|" + month + ")\\s*";
                Pattern pattern1 = Pattern.compile(patternStr1);
                Matcher matcher1 = pattern1.matcher(matcher.group(1));
                boolean matchFound1 = matcher1.find();
                if (matchFound1) {

                    //First case: Month
                    String subpatternStr = "\\s*" + month + "\\s*";
                    Pattern subpattern = Pattern.compile(subpatternStr);
                    Matcher submatcher = subpattern.matcher(matcher.group(1));
                    boolean submatchFound = submatcher.find();
                    if (submatchFound) {
                        monthnum = temp.hm.get(matcher.group(1));
                        result = monthnum + "/XX/XXXX";
                        return result;
                    } else {
                        //Second case:Day
                        String subpatternStr1 = "\\s*(\\d{1,4}(?:st|nd|rd|th))\\s*";
                        Pattern subpattern1 = Pattern.compile(subpatternStr1);
                        Matcher submatcher1 = subpattern1.matcher(matcher.group(1));
                        boolean submatchFound1 = submatcher1.find();
                        String tempz = matcher.group(2);
                        if (submatchFound1) {
                            day = (matcher.group(1)).split("[a-z]")[0];
                            result = "XX/" + day + "/XXXX";
                            return result;
                        }

                        //Third case:Year
                        else {

                            String subpatternStr2 = "\\s*(\\d{1,4})\\s*";
                            Pattern subpattern2 = Pattern.compile(subpatternStr2);
                            Matcher submatcher2 = subpattern2.matcher(matcher.group(1));
                            boolean submatchFound2 = submatcher2.find();
                            if (submatchFound2) {
                                year = matcher.group(1);
                                if (year.length() == 1) {
                                    result = "XX/XX/000" + year;
                                    return result;
                                } else if (year.length() == 2) {
                                    result = "XX/XX/00" + year;
                                    return result;
                                } else if (year.length() == 3) {
                                    result = "XX/XX/0" + year;
                                    return result;
                                } else {
                                    result = "XX/XX/" + year;
                                    return result;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


    public static int getLevenshteinDistance(String s, String t) {
        if (s == null || t == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

		  /*
            The difference between this impl. and the previous is that, rather
		     than creating and retaining a matrix of size s.length()+1 by t.length()+1,
		     we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
		     is the 'current working' distance array that maintains the newest distance cost
		     counts as we iterate through the characters of String s.  Each time we increment
		     the index of String t we are comparing, d is copied to p, the second int[].  Doing so
		     allows us to retain the previous cost counts as required by the algorithm (taking
		     the minimum of the cost count to the left, up one, and diagonally up and to the left
		     of the current cost count being calculated).  (Note that the arrays aren't really
		     copied anymore, just switched...this is clearly much better than cloning an array
		     or doing a System.arraycopy() each time  through the outer loop.)

		     Effectively, the difference between the two implementations is this one does not
		     cause an out of memory condition when calculating the LD over two very large strings.
		  */

        int n = s.length(); // length of s
        int m = t.length(); // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        int p[] = new int[n + 1]; //'previous' cost array, horizontally
        int d[] = new int[n + 1]; // cost array, horizontally
        int _d[]; //placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char t_j; // jth character of t

        int cost; // cost

        for (i = 0; i <= n; i++) {
            p[i] = i;
        }

        for (j = 1; j <= m; j++) {
            t_j = t.charAt(j - 1);
            d[0] = j;

            for (i = 1; i <= n; i++) {
                cost = s.charAt(i - 1) == t_j ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }

            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        // our last action in the above loop was to switch d and p, so p now
        // actually has the most recent cost counts
        return p[n];
    }

    //###################################################################################
    public static String TimeML(String phrase) throws Exception {

        String defaultyear = "2000";
        String defaultcountry = "UNITED_STATES";
        String deyear = "2000";
        String demonth = "01";
        String deday = "01";
        String dehour = "12";
        String deminute = "30";
        String desecond = "00";
        String dems = "00";
        String condition = "contain";
        phrase = phrase.toLowerCase();
        phrase = ConvertWordToNumber.ConvertWTN(phrase);

        String resulting;
        if (canonize(phrase, deyear, demonth, deday, dehour, deminute, desecond, dems, defaultyear, defaultcountry) == null) {
            return "Sorry we can not figure out the Phrase";
        } else {
            resulting = canonize(phrase, deyear, demonth, deday, dehour, deminute, desecond, dems, defaultyear, defaultcountry).toString();
        }

        return resulting;
    }

    public static void main(String[] args) throws Exception {


        String phrase1 = "two minutes ago";
        String phrase2 = "june";
        phrase1 = ConvertWordToNumber.ConvertWTN(phrase1);
        String defaultyear = "2000";
        String defaultcountry = "UNITED_STATES";
        String deyear = "2011";
        String demonth = "10";
        String deday = "01";
        String dehour = "12";
        String deminute = "30";
        String desecond = "00";
        String dems = "00";
        String condition = "contain";
        phrase1 = phrase1.toLowerCase();
        phrase2 = phrase2.toLowerCase();

        if (canonize(phrase1, deyear, demonth, deday, dehour, deminute, desecond, dems, defaultyear, defaultcountry) == null) {

            System.err.println("Sorry we can not figure out the Phrase");
        }
        //   canonizecompare(phrase1,phrase2,deyear,demonth,deday,dehour,deminute,desecond,dems,defaultyear,defaultcountry,condition);
        if (flagnull == 1) {
            System.err.println("Sorry we can not figure out the Phrase.");

        }

    }

    public static TimexChunk canonize(String phrase1, String deyear, String demonth, String deday, String dehour, String deminute, String desecond, String dems, String defaultyear, String defaultcountry) {
        TemporalPhrase temporalPhrase = new TemporalPhrase(phrase1);
        return canonize(temporalPhrase, deyear, demonth, deday, dehour, deminute, desecond, dems, defaultyear, defaultcountry);
    }

    public static TimexChunk canonize(TemporalPhrase temporalPhrase, String deyear, String demonth, String deday, String dehour, String deminute, String desecond, String dems, String defaultyear, String defaultcountry) {
        //String deyear,demonth,deday,dehour,deminute,desecond,dems are all default reference time
        //String defaultyear is the reference year of a holiday(in jollyday package)
        //String defaultcountry is the reference country of a holiday(in jollyday package)

        String phrase1 = temporalPhrase.getPhrase();
        referencemonth = demonth;
        referenceday = deday;
        referenceyear = deyear;
        int flaggy = 0;
        int i;
        String tempmain = "";
        phrase1 = phrase1.toLowerCase();
        phrase1 = phrase1.trim();
        constantphrase1 = phrase1;
        temporalPhrase.setPhrase(phrase1);
        TimexChunk tc = new TimexChunk();
        tc.setContent(phrase1);

        if (phrase1.contains("a couple of")) {
            phrase1 = phrase1.replace("a couple of", "2");
        }

        if (phrase1.contains("fiscal")) {
            phrase1 = phrase1.replace("fiscal", "");
        }

        if (phrase1.contains("couples of")) {
            phrase1 = phrase1.replace("couples of", "2");
        }

        if (phrase1.contains("the")) {
            phrase1 = phrase1.replaceAll("the", "");

        }

        if (phrase1.contains("of")) {
            phrase1 = phrase1.replaceAll("of", "this");

        }

        //special case: distinguish may and may day
        if (phrase1.equals("may")) {
            phrase1 = "this " + phrase1;
        }

        phrase1 = phrase1.trim().replaceAll(" +", " ");
        temporalPhrase.setPhrase(phrase1);

        if (phrase1.contains("mid") || phrase1.contains("middle") || phrase1.contains("mid-")) {
            tc.addAttribute(TimexNames.mod, TimexNames.MID);
        }

        DateTime start = new DateTime(Integer.parseInt(deyear), Integer.parseInt(demonth), Integer.parseInt(deday), Integer.parseInt(dehour), Integer.parseInt(deminute), Integer.parseInt(desecond), Integer.parseInt(dems));
        Interval one = new Interval(start, start);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTimeFormatter ymFmt = DateTimeFormat.forPattern("yyyy-MM");
        DateTimeFormatter yFmt = DateTimeFormat.forPattern("yyyy");
        //Get the interval of phrase1

        TimexChunk trialTc = new TimexChunk();
        if ((trialTc = RelativeDate.Relativerule(start, phrase1)) != null) {
            tc = trialTc;
        } else if ((trialTc = TimeOfDay.timeRule(start, temporalPhrase)) != null) {
            tc = trialTc;
        } else if ((trialTc = ModifiedDate.ModifiedRule(start, temporalPhrase)) != null) {
            tc = trialTc;
        } else if ((trialTc = Ordinary.Ordinaryrule(start, phrase1)) != null) {
            tc = trialTc;
        } else if ((trialTc = Period.Periodrule(start, temporalPhrase)) != null) {
            tc = trialTc;
        } else if ((trialTc = SetRule.SetRule(temporalPhrase)) != null) {
            tc = trialTc;
        } else if ((trialTc = Duration.DurationRule(start, phrase1)) != null) {
            tc = trialTc;
        } else {
            comparenum = 4;
            if (phrase1.matches(".*[0-9].*")) {

            } else {
                for (i = 1; i <= 168; i++) {
                    if (LevensteinDistance.getLevensteinDistance(hotoken[i].replaceAll(" ", ""), phrase1.replaceAll(" ", "")) < comparenum) {
                        comparenum = LevensteinDistance.getLevensteinDistance(hotoken[i].replaceAll(" ", ""), phrase1.replaceAll(" ", ""));
                        tempmain = hotoken[i];
                        flaggy = 1;
                        if (comparenum == 0) {
                            break;
                        }
                    }
                }
                if (flaggy == 1) {
                    phrase1 = tempmain;
                }
                flaggy = 0;
                comparenum = 4;
            }
            String patternstring = holiday;
            Pattern prepattern = Pattern.compile(patternstring);
            Matcher prematcher = prepattern.matcher(phrase1);
            boolean prefound = prematcher.find();

            if (prefound) {
                tc = Jollyday.test(phrase1, defaultcountry, Integer.parseInt(defaultyear));
            } else {
                flagonlyyear = -1;
                if (phrase1.contains("year")) {
                    phrase1 = phrase1.replaceAll("year", "");
                    flagonlyyear = 1;
                }
                String patternStr = "(before|after|in|during|since|from|prior to|on|early|earlier|late|later)\\s*((?:(?:\\d{1,4}(?:st|nd|rd|th)?)|" + month + ")\\s*(?:\\s|[/\\-\\.\\,])?\\s*(?:(?:\\d{1,4}(?:st|nd|rd|th)?)|" + month + ")?\\s*(?:\\s|[/\\-\\.\\,])?\\s*(?:(?:\\d{1,4}(?:st|nd|rd|th)?)|" + month + ")?)\\s*";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(phrase1);
                boolean matchFound = matcher.find();
                String preword;
                if (matchFound) {
                    onlymonth = -1;
                    onlyyear = -1;
                    flagnum = -1;
                    flagmonnum = -1;
                    flagdaynum = -1;
                    preword = matcher.group(1);

                    result1 = converter(matcher.group(2));

                    String delims = "[/]";
                    String[] token = result1.split(delims);
                    tc.addAttribute(TimexNames.type, TimexNames.DATE);
                    if (token[0].length() < 2) {
                        token[0] = "0" + token[0];
                    }
                    if (token[1].length() < 2) {
                        token[1] = "0" + token[1];
                    }

                    if (token[2].equals("XXXX")) {
                        token[2] = referenceyear;
                        // Modified by Zhili: if the sentence is past tense, and the month mentioned is
                        // after DCT, then subtract 1 from year
                        String tense = temporalPhrase.getTense();
                        if (tense.equals("past")) {
                            if (start.getMonthOfYear() < Integer.parseInt(token[0])) {
                                token[2] = String.valueOf(Integer.parseInt(referenceyear) - 1);
                            }
                        }
                    }

                    if (!token[1].equals("XX")) {
                        tc.addAttribute(TimexNames.value, token[2] + "-" + token[0] + "-" + token[1]);
                    } else if (!token[0].equals("XX")) {
                        tc.addAttribute(TimexNames.value, token[2] + "-" + token[0]);
                    } else {
                        tc.addAttribute(TimexNames.value, token[2]);
                    }

                    if (preword.matches("early|earlier")) {
                        tc.addAttribute(TimexNames.mod, TimexNames.START);
                    } else if (preword.matches("late|later")) {
                        tc.addAttribute(TimexNames.mod, TimexNames.END);
                    }

                    return tc;
                } else {
                    onlyyear = -1;
                    onlymonth = -1;
                    flagnum = -1;
                    flagmonnum = -1;
                    flagdaynum = -1;

                    result1 = converter(phrase1);
                    if (result1 == null) {
                        flagnull = 1;
                        return null;
                    }
                    String delims = "[/]";
                    String[] token = result1.split(delims);
                    tc.addAttribute(TimexNames.type, TimexNames.DATE);
                    if (token[0].length() < 2) {
                        token[0] = "0" + token[0];
                    }
                    if (token[1].length() < 2) {
                        token[1] = "0" + token[1];
                    }
                    if (token[2].equals("XXXX")) {
                        token[2] = referenceyear;
                        // Modified by Zhili: if the sentence is past tense, and the month mentioned is
                        // after DCT, then subtract 1 from year
                        String tense = temporalPhrase.getTense();
                        if (tense.equals("past") && isInteger(token[0])) {
                            if (start.getMonthOfYear() < Integer.parseInt(token[0])) {
                                token[2] = String.valueOf(Integer.parseInt(referenceyear) - 1);
                            }
                        }
                    }

                    if (!token[1].equals("XX")) {
                        tc.addAttribute(TimexNames.value, token[2] + "-" + token[0] + "-" + token[1]);
                    } else if (!token[0].equals("XX")) {
                        tc.addAttribute(TimexNames.value, token[2] + "-" + token[0]);
                    } else {
                        tc.addAttribute(TimexNames.value, token[2]);
                    }
                    return tc;
                }
            }
        }
        return tc;
    }

}
