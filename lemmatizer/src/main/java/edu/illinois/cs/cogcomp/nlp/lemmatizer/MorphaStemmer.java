/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.lemmatizer;

/*
 * Copyright (C) 2012 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */



/*
 * author: Michael Schmitz <schmmd@cs.washington.edu>
 * 
 * This is a light wrapper for the JFLEX-generated code from the original lex morpha stemmer. It
 * provides a nicer interface and handles exceptions.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.Scanner;
import java.util.regex.Pattern;

public class MorphaStemmer {
    private static final Pattern whitespace = Pattern.compile("\\s+");
    private static Logger logger = LoggerFactory.getLogger(MorphaStemmer.class);

    /***
     * Stem the supplied text, splitting on whitespace to break it into words.
     **/
    public static String stem(String text) {
        return morpha(cleanText(text), false);
    }

    /***
     * Stem the supplied token.
     *
     * @throws IllegalArgumentException token contains whitespace
     **/
    public static String stemToken(String token) {
        if (whitespace.matcher(token).find()) {
            throw new IllegalArgumentException("Token may not contain a space: " + token);
        }
        return morpha(cleanText(token), false);
    }

    /***
     * Stem the supplied token using supplemental postag information.
     *
     * @throws IllegalArgumentException token contains whitespace
     **/
    public static String stemToken(String token, String postag) {
        if (whitespace.matcher(token).find()) {
            throw new IllegalArgumentException("Token may not contain a space: " + token);
        }
        return morpha(cleanText(token) + "_" + postag, true);
    }

    private static String cleanText(String text) {
        return text.replaceAll("_", "-");
    }

    /***
     * Run the morpha algorithm on the specified string.
     **/
    public static String morpha(String text, boolean tags) {
        if (text.isEmpty()) {
            return "";
        }

        String[] textParts = whitespace.split(text);

        StringBuilder result = new StringBuilder();
        try {
            for (String textPart : textParts) {
                Morpha morpha = new Morpha(new StringReader(textPart), tags);

                if (result.length() != 0) {
                    result.append(" ");
                }

                result.append(morpha.next());
            }
        }
        // yes, Morpha is cool enough to throw Errors
        // usually when the text contains underscores
        catch (Error e) {
            return text;
        } catch (java.io.IOException e) {
            return text;
        }

        return result.toString();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            System.out.println(MorphaStemmer.stem(line));
        }
    }
}
