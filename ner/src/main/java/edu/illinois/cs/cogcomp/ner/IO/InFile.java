/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.IO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;


public class InFile {
    private static boolean convertToLowerCaseByDefault = false;
    private static boolean normalize = false;
    private static boolean pruneStopSymbols = false;
    private BufferedReader in = null;
    private static String stopSymbols = "@";

    public InFile(String filename) {
        try {
            in = new BufferedReader(new FileReader(filename));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public InFile(InputStream stream) {
        try {
            in = new BufferedReader(new InputStreamReader(stream));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public String readLine() {
        try {
            String s = in.readLine();
            if (s == null)
                return null;
            if (convertToLowerCaseByDefault)
                return s.toLowerCase().trim();
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    public Vector<String> readLineTokens(String delimiters) {
        return tokenize(readLine(), delimiters);
    }

    public static Vector<String> tokenize(String s) {
        if (s == null)
            return null;
        Vector<String> res = new Vector<>();
        StringTokenizer st = new StringTokenizer(s, " \n\t");
        while (st.hasMoreTokens())
            res.addElement(st.nextToken());
        return res;
    }

    public static Vector<String> tokenize(String s, String delimiters) {
        if (s == null)
            return null;
        Vector<String> res = new Vector<>();
        StringTokenizer st = new StringTokenizer(s, delimiters);
        while (st.hasMoreTokens())
            res.addElement(st.nextToken());
        return res;
    }

    public void close() {
        try {
            this.in.close();
        } catch (Exception ignored) {
        }
    }


    public static String readFileText(String file) {
        StringBuilder text = new StringBuilder();
        InFile in = new InFile(file);
        String line = in.readLine();
        while (line != null) {
            text.append(line).append("\n");
            line = in.readLine();
        }
        in.close();
        return text.toString();
    }

}
