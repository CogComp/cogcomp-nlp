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
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;


import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;

import java.io.PrintStream;
import java.util.*;

/**
 * @author Vivek Srikumar
 */
public class TextAnnotationUtilities {
    public final static Comparator<Constituent> constituentStartComparator =
            new Comparator<Constituent>() {
                public int compare(Constituent arg0, Constituent arg1) {
                    int start0 = arg0.getStartSpan();
                    int start1 = arg1.getStartSpan();
                    if (start0 < start1)
                        return -1;
                    else if (start0 == start1)
                        return 0;
                    else
                        return 1;
                }
            };
    public final static Comparator<Sentence> sentenceStartComparator = new Comparator<Sentence>() {

        @Override
        public int compare(Sentence o1, Sentence o2) {
            return constituentStartComparator.compare(o1.sentenceConstituent,
                    o2.sentenceConstituent);
        }
    };

    public final static Comparator<Constituent> constituentEndComparator =
            new Comparator<Constituent>() {

                @Override
                public int compare(Constituent arg0, Constituent arg1) {
                    int end0 = arg0.getEndSpan();
                    int end1 = arg1.getEndSpan();

                    if (end0 < end1)
                        return -1;
                    else if (end0 > end1)
                        return 1;
                    else
                        return 0;
                }
            };

    public final static Comparator<Constituent> constituentLengthComparator =
            new Comparator<Constituent>() {

                @Override
                public int compare(Constituent arg0, Constituent arg1) {
                    int size0 = arg0.size();
                    int size1 = arg1.size();

                    if (size0 < size1)
                        return -1;
                    else if (size0 > size1)
                        return 1;
                    else
                        return 0;
                }
            };

    public static TextAnnotation createFromTokenizedString(String text) {
        return BasicTextAnnotationBuilder.createTextAnnotationFromTokens(Collections
                .singletonList(text.split(" ")));
    }

    public static String getTokenSequence(TextAnnotation ta, int start, int end) {
        return new Constituent("", "", ta, start, end).toString();
    }

    public static List<String> getSentenceList(TextAnnotation ta) {
        List<String> sentencesList = new ArrayList<>();

        for (Sentence sentence : ta.sentences()) {
            String sentenceString = sentence.getTokenizedText().trim();

            sentencesList.add(sentenceString);
        }
        return sentencesList;
    }

    static public void printTextAnnotation(PrintStream out, TextAnnotation ta) {
        out.println("TextAnnotation with id: " + ta.getId());

        String rawText = ta.getText();
        out.println("Raw Text: " + rawText);

        out.println(getLineFill());

        out.println("TextAnnotation Views:");

        for (String name : ta.getAvailableViews()) {
            out.println("View Name: " + name);
            out.println(ta.getView(name).toString());
            out.println(getLineFill());
        }
    }

    private static String getLineFill() {
        return "------------------------------------";
    }
}
