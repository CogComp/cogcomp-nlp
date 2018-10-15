/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pos;

import edu.illinois.cs.cogcomp.core.datastructures.vectors.ExceptionlessInputStream;
import edu.illinois.cs.cogcomp.core.datastructures.vectors.ExceptionlessOutputStream;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;

import java.io.PrintStream;
import java.util.*;

/**
 * A poor man's Mikheev rules implementation, in that there is no morphology. Only rules involving
 * all-letter suffixes of lengths 3 and 4 of words whose length is at least 2 greater than the
 * suffix are extracted. The learned rules are divided into three categories: words that are
 * capitalized and appear first in the sentence, words that are capitalized and don't appear first
 * in the sentence, and uncapitalized, unhyphenated words. Uncapitalized, hyphenated words are hard
 * coded to a rule that produces the set <code>{ NN, JJ }</code>.
 *
 * @author Nick Rizzolo
 **/
public class MikheevLearner extends POSBaselineLearner {
    /** A map for capitalized words appearing first in the sentence. */
    protected HashMap<String, TreeMap<String, Integer>> firstCapitalized;
    /** A map for capitalized words not appearing first in the sentence. */
    protected HashMap<String, TreeMap<String, Integer>> notFirstCapitalized;


    /** Default constructor; sets the name to the empty string. */
    public MikheevLearner() {
        this("");
    }

    /**
     * Constructor setting the name of the classifier.
     *
     * @param n The name of the classifier.
     **/
    public MikheevLearner(String n) {
        super(n);
        firstCapitalized = new HashMap<>();
        notFirstCapitalized = new HashMap<>();
    }

    /** Does nothing, as there are not parameters. */
    public MikheevLearner(Parameters p) {
        this("");
    }


    /**
     * Returns a new, emtpy learner into which all of the parameters that control the behavior of
     * the algorithm have been copied. Here, "emtpy" means no learning has taken place.
     **/
    public Learner emptyClone() {
        return new MikheevLearner();
    }


    /**
     * Increments the count for the given suffix and tag.
     *
     * @param table The table in which a count should be incremented.
     * @param suffix The suffix.
     * @param tag The POS tag.
     **/
    private void increment(HashMap<String, TreeMap<String, Integer>> table, String suffix,
            String tag) {
        TreeMap<String, Integer> counts = table.get(suffix);

        if (counts == null) {
            counts = new TreeMap<>();
            table.put(suffix, counts);
        }

        Integer count = counts.get(tag);
        if (count == null)
            count = 0;
        counts.put(tag, count + 1);
    }


    /**
     * Trains the learning algorithm given an object as an example.
     *
     * @param example An example of the desired learned classifier's behavior.
     **/
    public void learn(Object example) {
        String form = extractor.discreteValue(example);
        String label = labeler.discreteValue(example);

        if (form.length() >= 5) {
            boolean allLetters = true;
            for (int i = form.length() - 3; i < form.length() && allLetters; ++i)
                allLetters = Character.isLetter(form.charAt(i));

            if (allLetters) {
                Word w = (Word) example;
                HashMap<String, TreeMap<String, Integer>> t = null;

                if (w.capitalized) {
                    if (w.previous == null)
                        t = firstCapitalized;
                    else
                        t = notFirstCapitalized;
                } else {
                    if (form.contains("-"))
                        return;
                    t = table;
                }

                form = form.toLowerCase();
                increment(t, form.substring(form.length() - 3), label);
                if (form.length() >= 6 && Character.isLetter(form.charAt(form.length() - 4)))
                    increment(t, form.substring(form.length() - 4), label);
            }
        }
    }


    /** Runs after all learning is complete. */
    public void doneLearning() {
        prune(table);
        prune(firstCapitalized);
        prune(notFirstCapitalized);
    }


    /** Clears out the table to start fresh. */
    public void forget() {
        super.forget();
        firstCapitalized.clear();
        notFirstCapitalized.clear();
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


    /**
     * Returns the set of tags that the given word's suffix has been observed with, or a reasonable
     * default if the suffix has never been observed.
     *
     * @param word The word.
     * @return The set of tags observed in association with the given word's suffix, or a reasonable
     *         default if the suffix has never been observed.
     **/
    public Set<String> allowableTags(Word word) {
        Set<String> result = allowableTags(word.form);
        if (result.size() > 0)
            return result;

        if (word.capitalized) {
            HashMap<String, TreeMap<String, Integer>> t;
            if (word.previous == null)
                t = firstCapitalized;
            else
                t = notFirstCapitalized;

            if (word.form.length() >= 6) {
                String suffix = word.form.substring(word.form.length() - 4).toLowerCase();
                if (t.containsKey(suffix))
                    result = t.get(suffix).keySet();
            }

            if (result.size() == 0 && word.form.length() >= 5) {
                String suffix = word.form.substring(word.form.length() - 3).toLowerCase();
                if (t.containsKey(suffix))
                    result = t.get(suffix).keySet();
            }

            if (result.size() == 0)
                result.add("NNP");
        } else if (word.form.contains("-")) {
            result.add("NN");
            result.add("JJ");
        } else {
            if (word.form.length() >= 6) {
                String suffix = word.form.substring(word.form.length() - 4).toLowerCase();
                if (table.containsKey(suffix))
                    result = table.get(suffix).keySet();
            }

            if (result.size() == 0 && word.form.length() >= 5) {
                String suffix = word.form.substring(word.form.length() - 3).toLowerCase();
                if (table.containsKey(suffix))
                    result = table.get(suffix).keySet();
            }

            if (result.size() == 0)
                result.add("NN");
        }

        return result;
    }


    /**
     * Writes the algorithm's internal representation as text.
     *
     * @param out The output stream.
     **/
    public void write(PrintStream out) {
        out.println("# if capitalized and first word in sentence:");
        write(out, firstCapitalized);
        out.println("\n# if capitalized and not first word in sentence:");
        write(out, notFirstCapitalized);
        out.println("\n# main table:");
        write(out, table);
    }


    /**
     * Writes the learned function's internal representation in binary form.
     *
     * @param out The output stream.
     **/
    public void write(ExceptionlessOutputStream out) {
        super.write(out);
        POSBaselineLearner.write(out, firstCapitalized);
        POSBaselineLearner.write(out, notFirstCapitalized);
    }


    /**
     * Reads the binary representation of a learner with this object's run-time type, overwriting
     * any and all learned or manually specified parameters as well as the label lexicon but without
     * modifying the feature lexicon.
     *
     * @param in The input stream.
     **/
    public void read(ExceptionlessInputStream in) {
        super.read(in);
        POSBaselineLearner.read(in, firstCapitalized);
        POSBaselineLearner.read(in, notFirstCapitalized);
    }


    /** Empty class, since there are no parameters */
    public static class Parameters extends Learner.Parameters {
        public Parameters() {}

        public Parameters(Parameters p) {
            super(p);
        }
    }
}
