/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author Vivek Srikumar
 */
public class StringUtils {

    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd_HH-mm-ss";
    static Pattern diacritPattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public static String getFormattedString(double d, int numDecimalPlaces) {
        StringBuilder sb = new StringBuilder();
        sb.append("##.");

        for (int i = 0; i < numDecimalPlaces; i++)
            sb.append("#");

        DecimalFormat df = new DecimalFormat(sb.toString());
        return df.format(d);
    }

    public static String getFormattedTwoDecimal(double d) {
        DecimalFormat df = new DecimalFormat("##.##");
        return df.format(d);
    }

    /**
     * Get the current date and time in a format so that sorting the string will sort the date.
     */
    public static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());

    }

    public static String join(String separator, Object... objects) {
        StringBuilder sb = new StringBuilder();

        for (Object o : objects) {
            sb.append(o.toString()).append(separator);
        }
        return sb.toString().trim();
    }

    public static String join(String separator, List<String> tokens) {
        if (tokens.size() == 1)
            return tokens.get(0);

        StringBuilder sb = new StringBuilder();

        for (String token : tokens) {
            sb.append(token).append(separator);
        }

        return sb.toString().trim();
    }

    public static String join(String separator, String[] tokens) {
        return join(separator, Arrays.asList(tokens));
    }

    public static String generateUniqueString() {
        return UUID.randomUUID().toString();
    }

    public static boolean isNumeric(String s) {
        return s.matches("-?\\d+(.\\d+)?");
    }

    /**
     * Uses 3rd party library to convert UTF8 string characters from non-English forms to English. Mainly useful
     *    for removing diacritics.
     * WARNING: NOT guaranteed to preserve character offsets.
     * @param text String to normalize.
     * @return normalized version of string.
     */
    public static String normalizeUnicodeDiacritics(String text) {
        text = Normalizer.normalize(text, Form.NFD);
        text = diacritPattern.matcher(text).replaceAll("");

        return text;
    }


    public static String normalizeUnicodeDiacriticChar(Character ch) {
        String text = Normalizer.normalize(String.valueOf(ch), Form.NFD);
        text = diacritPattern.matcher(text).replaceAll("");

        return text;
    }

}
