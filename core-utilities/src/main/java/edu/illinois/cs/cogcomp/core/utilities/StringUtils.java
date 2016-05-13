/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
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

    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd_HH-mm-ss";

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

    public static String normalizeUnicodeDiacritics(String text) {
        text = Normalizer.normalize(text, Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        text = pattern.matcher(text).replaceAll("");

        return text;
    }
}
