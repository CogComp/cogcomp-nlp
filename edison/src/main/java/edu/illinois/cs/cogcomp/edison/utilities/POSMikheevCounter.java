/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.PennTreebankPOSReader;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.Gson;

/**
 * A poor man's Mikheev rules implementation, in that there is no morphology. Only rules involving
 * all-letter suffixes of lengths 3 and 4 of words whose length is at least 2 greater than the
 * suffix are extracted. The learned rules are divided into three categories: words that are
 * capitalized and appear first in the sentence, words that are capitalized and don't appear first
 * in the sentence, and uncapitalized, unhyphenated words.
 * 
 * @author Xinbo Wu
 */
public class POSMikheevCounter extends POSBaseLineCounter {
    /** A map for capitalized words appearing first in the sentence. */
    protected HashMap<String, TreeMap<String, Integer>> firstCapitalized;
    /** A map for capitalized words not appearing first in the sentence. */
    protected HashMap<String, TreeMap<String, Integer>> notFirstCapitalized;

    public POSMikheevCounter(String corpusName) {
        super(corpusName);
        firstCapitalized = new HashMap<String, TreeMap<String, Integer>>();
        notFirstCapitalized = new HashMap<String, TreeMap<String, Integer>>();
    }


    /**
     * A table is built from either a given source corpus file or source corpus directory by
     * counting the number of times that each suffix-POS association in a source corpus.
     * 
     * @param home file name or directory name of the source corpus
     * @throws Exception
     **/
    public void buildTable(String home) throws Exception {
        if (IOUtils.isFile(home))
            this.buildTableHelper(home);
        else if (IOUtils.isDirectory(home)) {
            String[] files = IOUtils.lsFiles(home);
            for (String file : files) {
                // logger.info(file);
                this.buildTableHelper(home + "\\" + file);
            }
        }
    }

    /**
     * A table is built from a given source corpus file by counting the number of times that each
     * suffix-POS association in a source corpus.
     * 
     * @param fileName file name of the source corpus
     * @throws Exception
     **/
    private void buildTableHelper(String fileName) throws Exception {
        PennTreebankPOSReader reader = new PennTreebankPOSReader(this.corpusName);
        reader.readFile(fileName);
        List<TextAnnotation> tas = reader.getTextAnnotations();
        for (TextAnnotation ta : tas) {

            for (int tokenId = 0; tokenId < ta.size(); tokenId++) {
                String form = ta.getToken(tokenId);
                String tag = ((SpanLabelView) ta.getView(ViewNames.POS)).getLabel(tokenId);

                if (form.length() >= 5) {
                    boolean allLetters = true;
                    for (int i = form.length() - 3; i < form.length() && allLetters; ++i)
                        allLetters = Character.isLetter(form.charAt(i));

                    if (allLetters) {
                        // Word w = (Word) example;
                        HashMap<String, TreeMap<String, Integer>> t = null;

                        if (WordHelpers.isCapitalized(ta, tokenId)) {
                            int headOfSentence =
                                    ta.getSentence(ta.getSentenceId(tokenId)).getStartSpan();
                            if (tokenId == headOfSentence)
                                t = firstCapitalized;
                            else
                                t = notFirstCapitalized;
                        } else {
                            if (form.contains("-"))
                                return;
                            t = table;
                        }

                        form = form.toLowerCase();
                        count(t, form.substring(form.length() - 3), tag);
                        if (form.length() >= 6
                                && Character.isLetter(form.charAt(form.length() - 4)))
                            count(t, form.substring(form.length() - 4), tag);
                    }
                }

            }
        }
    }

    /**
     * Increments the count for the given suffix and tag.
     *
     * @param table The table in which a count should be incremented.
     * @param suffix The suffix.
     * @param tag The POS tag.
     **/
    public void count(HashMap<String, TreeMap<String, Integer>> table, String suffix, String tag) {
        TreeMap<String, Integer> counts = table.get(suffix);

        if (counts == null) {
            counts = new TreeMap<String, Integer>();
            table.put(suffix, counts);
        }

        Integer count = counts.get(tag);
        if (count == null)
            count = 0;
        counts.put(tag, count + 1);
    }

    /** Runs after all learning is complete. */
    public void doneLearning() {
        prune(table);
        prune(firstCapitalized);
        prune(notFirstCapitalized);
    }

    /**
     * Prunes the specified table.
     *
     * @param table The table.
     **/
    public void prune(HashMap<String, TreeMap<String, Integer>> table) {
        for (Iterator<Map.Entry<String, TreeMap<String, Integer>>> I = table.entrySet().iterator(); I
                .hasNext();) {
            Map.Entry<String, TreeMap<String, Integer>> e = I.next();
            int sum = 0;
            for (Integer count : e.getValue().values())
                sum += count;

            for (Iterator<Map.Entry<String, Integer>> J = e.getValue().entrySet().iterator(); J
                    .hasNext();) {
                int count = J.next().getValue();

                if (count * 100 < sum) {
                    sum -= count;
                    J.remove();
                }
            }

            if (sum <= 10)
                I.remove();
        }
    }

    /** Clears out the table to start fresh. */
    public void forget() {
        super.forget();
        firstCapitalized.clear();
        notFirstCapitalized.clear();
    }

    /** Give a tag for a given form */
    public String tag(int tokenId, TextAnnotation ta) {
        String form = ta.getToken(tokenId);

        if (form.length() >= 5) {
            boolean allLetters = true;
            for (int i = form.length() - 3; i < form.length() && allLetters; ++i)
                allLetters = Character.isLetter(form.charAt(i));

            if (allLetters) {

                if (WordHelpers.isCapitalized(ta, tokenId)) {
                    int headOfSentence = ta.getSentence(ta.getSentenceId(tokenId)).getStartSpan();
                    if (tokenId == headOfSentence)
                        return tagHelper(form, firstCapitalized);
                    else
                        return tagHelper(form, notFirstCapitalized);
                } else {
                    return super.tag(tokenId, ta);
                }
            }
        } else {

        }
        return "UNKNOWN";
    }


    private String tagHelper(String form, HashMap<String, TreeMap<String, Integer>> table) {
        form = form.toLowerCase();
        ArrayList<String> forms = new ArrayList<String>();

        forms.add(form.substring(form.length() - 3));
        if (form.length() >= 6 && Character.isLetter(form.charAt(form.length() - 4)))
            forms.add(form.substring(form.length() - 4));

        String l = null;
        int best = 0;

        for (String element : forms) {
            TreeMap<String, Integer> counts = table.get(element);

            if (counts == null) {
                if (best == 0)
                    l = "UNKNOWN";
            } else {

                for (Map.Entry<String, Integer> e : counts.entrySet()) {
                    int c = e.getValue();
                    if (c > best) {
                        best = c;
                        l = e.getKey();
                    }
                }
            }
        }

        return l;
    }

    /**
     * Returns the set of tags that the given word's suffix has been observed with, or a reasonable
     * default if the suffix has never been observed.
     *
     * @return The set of tags observed in association with the given word's suffix, or a reasonable
     *         default if the suffix has never been observed.
     **/
    public Set<String> allowableTags(TextAnnotation ta, int tokenId) {
        String form = ta.getToken(tokenId);

        Set<String> result = allowableTags(form);
        if (result.size() > 0)
            return result;

        if (WordHelpers.isCapitalized(ta, tokenId)) {
            HashMap<String, TreeMap<String, Integer>> t;

            int headOfSentence = ta.getSentence(ta.getSentenceId(tokenId)).getStartSpan();
            if (tokenId == headOfSentence)
                t = firstCapitalized;
            else
                t = notFirstCapitalized;

            if (form.length() >= 6) {
                String suffix = form.substring(form.length() - 4).toLowerCase();
                if (t.containsKey(suffix))
                    result = t.get(suffix).keySet();
            }

            if (result.size() == 0 && form.length() >= 5) {
                String suffix = form.substring(form.length() - 3).toLowerCase();
                if (t.containsKey(suffix))
                    result = t.get(suffix).keySet();
            }

            if (result.size() == 0)
                result.add("NNP");
        } else if (form.contains("-")) {
            result.add("NN");
            result.add("JJ");
        } else {
            if (form.length() >= 6) {
                String suffix = form.substring(form.length() - 4).toLowerCase();
                if (table.containsKey(suffix))
                    result = table.get(suffix).keySet();
            }

            if (result.size() == 0 && form.length() >= 5) {
                String suffix = form.substring(form.length() - 3).toLowerCase();
                if (table.containsKey(suffix))
                    result = table.get(suffix).keySet();
            }

            if (result.size() == 0)
                result.add("NN");
        }

        return result;
    }

    /** Write an instance of POSMikheevCounter class to JSON format */
    public static String write(POSMikheevCounter counter) {
        return POSBaseLineCounter.write(counter);
    }

    /** Read the an instance of POSMikheevCounter class from JSON format */
    public static POSMikheevCounter read(String json) {
        return new Gson().fromJson(json, POSMikheevCounter.class);
    }
}
