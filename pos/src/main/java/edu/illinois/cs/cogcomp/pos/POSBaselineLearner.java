/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pos;

import java.util.*;
import java.io.*;

import edu.illinois.cs.cogcomp.core.datastructures.vectors.ExceptionlessInputStream;
import edu.illinois.cs.cogcomp.core.datastructures.vectors.ExceptionlessOutputStream;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;


/**
 * This learning algorithm simply counts the number of times that each word appears in training data
 * associated with each possible part of speech tag. It assumes that its feature extractor
 * classifier is <code>discrete</code>, i.e. that in the LBJ source file, a single
 * <code>discrete</code> classifier is named in the learning classifier expression's
 * <code>using</code> clause. That classifier is assumed to return the form of the word.
 *
 * <p>
 * <b>Note:</b> This learner will not work if feature pre-extraction is enabled for it.
 *
 * @author Nick Rizzolo
 **/
public class POSBaselineLearner extends Learner {
    /**
     * This map associates words with maps that associate POS tags with counts.
     **/
    protected HashMap<String, TreeMap<String, Integer>> table;


    /** Default constructor; sets the name to the empty string. */
    public POSBaselineLearner() {
        this("");
    }

    /**
     * Constructor setting the name of the classifier.
     *
     * @param n The name of the classifier.
     **/
    public POSBaselineLearner(String n) {
        super(n);
        table = new HashMap<>();
    }

    /** Does nothing, as there are not parameters. */
    public POSBaselineLearner(Parameters p) {
        this("");
    }


    /**
     * Returns a new, emtpy learner into which all of the parameters that control the behavior of
     * the algorithm have been copied. Here, "emtpy" means no learning has taken place.
     **/
    public Learner emptyClone() {
        return new POSBaselineLearner();
    }


    /**
     * Returns a string describing the input type of this classifier.
     *
     * @return <code>"edu.illinois.cs.cogcomp.lbjava.nlp.Word"</code>
     **/
    public String getInputType() {
        return "edu.illinois.cs.cogcomp.lbjava.nlp.Word";
    }


    /**
     * Trains the learning algorithm given an object as an example.
     *
     * @param example An example of the desired learned classifier's behavior.
     **/
    public void learn(Object example) {
        String form = extractor.discreteValue(example);
        TreeMap<String, Integer> counts = table.get(form);

        if (counts == null) {
            counts = new TreeMap<>();
            table.put(form, counts);
        }

        String l = labeler.discreteValue(example);
        Integer count = counts.get(l);
        if (count == null)
            count = 0;
        counts.put(l, count + 1);
    }

    /**
     * Dummy implementation of the abstract <code>learn(int[],double[],int[],double[])</code>
     * signature for the compiler. Not to be used by the programmer.
     *
     * @param exampleFeatures Not used.
     * @param exampleValues Not used.
     * @param exampleLabels Not used.
     * @param labelValues Not used.
     **/
    public void learn(int[] exampleFeatures, double[] exampleValues, int[] exampleLabels,
            double[] labelValues) {}


    /** Clears out the table to start fresh. */
    public void forget() {
        table.clear();
    }


    /**
     * Determines if the input word looks like a number of some sort.
     *
     * @param form The form of the word.
     * @return <code>true</code> iff the word contains only characters in ".,-" and at least one
     *         digit.
     **/
    public static boolean looksLikeNumber(String form) {
        boolean containsDigit = false;

        for (int i = 0; i < form.length(); ++i) {
            if (Character.isDigit(form.charAt(i)))
                containsDigit = true;
            else if (".,-".indexOf(form.charAt(i)) == -1)
                return false;
        }

        return containsDigit;
    }


    /**
     * Returns the value of the discrete prediction that this learner would make, given an example.
     *
     * @param example The example object.
     * @return The discrete value.
     **/
    public String discreteValue(Object example) {
        return computePrediction(example);
    }


    /**
     * Computes the prediction for this example. This method only exists because of the way that
     * generated code calls {@link #discreteValue(Object)}, {@link #featureValue(Object)}, and
     * {@link #classify(Object)}.
     *
     * @param example The example object.
     * @return The discrete value.
     **/
    protected String computePrediction(Object example) {
        String form = extractor.discreteValue(example);
        TreeMap<String, Integer> counts = table.get(form);
        String l = null;

        if (counts == null) {
            if (form.equals(";"))
                l = ":";
            else if (looksLikeNumber(form))
                l = "CD";
            else
                l = "UNKNOWN";
        } else {
            int best = 0;

            for (Map.Entry<String, Integer> e : counts.entrySet()) {
                int c = e.getValue();
                if (c > best) {
                    best = c;
                    l = e.getKey();
                }
            }
        }

        return l;
    }


    /**
     * Returns the classification of the given example object as a single feature instead of a
     * {@link FeatureVector}.
     *
     * @param example The object to classify.
     * @return The classification of <code>example</code> as a feature.
     **/
    public Feature featureValue(Object example) {
        return new DiscretePrimitiveStringFeature(containingPackage, name, "",
                computePrediction(example));
    }


    /**
     * This function makes one or more decisions about a single object, returning those decisions as
     * <code>Feature</code>s in a vector.
     *
     * @param example The object to make decisions about.
     * @return A vector of <code>Feature</code>s about the input object.
     **/
    public FeatureVector classify(Object example) {
        return new FeatureVector(featureValue(example));
    }

    /**
     * Dummy implementation of the abstract <code>learn(int[],double[],int[],double[])</code>
     * signature for the compiler. Not to be used by the programmer.
     *
     * @param exampleFeatures Not used.
     * @param exampleValues Not used.
     * @return An empty feature vector.
     **/
    public FeatureVector classify(int[] exampleFeatures, double[] exampleValues) {
        return new FeatureVector();
    }


    /** Returns <code>null</code>. */
    public ScoreSet scores(int[] features, double[] values) {
        return null;
    }


    /**
     * Indicates whether the input word was observed while training this learner.
     *
     * @param form The form of the word.
     * @return <code>true</code> if this learner contains statistics for the input word.
     **/
    public boolean observed(String form) {
        return table.containsKey(form);
    }


    /** Returns the number of times the given form has been observed. */
    public int observedCount(String form) {
        if (!table.containsKey(form))
            return 0;
        int result = 0;
        for (Integer count : table.get(form).values())
            result += count;
        return result;
    }


    /**
     * Returns the set of tags that the given word has been observed with.
     *
     * @param form The form of the word.
     * @return The set of tags observed in association with the given word.
     **/
    public Set<String> allowableTags(String form) {
        if (!table.containsKey(form)) {
            HashSet<String> result = new HashSet<>();
            if (form.equals(";"))
                result.add(":");
            else if (looksLikeNumber(form))
                result.add("CD");
            return result;
        }

        return table.get(form).keySet();
    }


    /**
     * Writes the algorithm's internal representation as text.
     *
     * @param out The output stream.
     **/
    public void write(PrintStream out) {
        write(out, table);
    }


    /**
     * Writes the algorithm's internal representation as text.
     *
     * @param out The output stream.
     * @param table The table.
     **/
    public static void write(PrintStream out, HashMap<String, TreeMap<String, Integer>> table) {
        String[] keys = table.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        for (String key : keys) {
            out.print(key + ":");
            TreeMap<String, Integer> counts = table.get(key);
            String[] tags = counts.keySet().toArray(new String[0]);
            final Integer[] values = counts.values().toArray(new Integer[0]);
            Integer[] indexes = new Integer[tags.length];
            for (int j = 0; j < tags.length; ++j)
                indexes[j] = j;

            Arrays.sort(indexes, new Comparator<Integer>() {
                public int compare(Integer i1, Integer i2) {
                    return values[i2] - values[i1];
                }
            });

            for (Integer indexe : indexes)
                out.print(" " + tags[indexe] + "(" + values[indexe] + ")");
            out.println();
        }
    }


    /**
     * Writes the learned function's internal representation in binary form.
     *
     * @param out The output stream.
     **/
    public void write(ExceptionlessOutputStream out) {
        super.write(out);
        write(out, table);
    }


    /**
     * Writes the binary representation of the specific form of map used by this class.
     *
     * @param out The output stream.
     * @param map The map to write.
     **/
    protected static void write(ExceptionlessOutputStream out,
            HashMap<String, TreeMap<String, Integer>> map) {
        out.writeInt(map.size());

        for (Map.Entry<String, TreeMap<String, Integer>> e : map.entrySet()) {
            out.writeString(e.getKey());
            out.writeInt(e.getValue().size());
            for (Map.Entry<String, Integer> ee : e.getValue().entrySet()) {
                out.writeString(ee.getKey());
                out.writeInt(ee.getValue());
            }
        }
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
        forget();
        read(in, table);
    }


    /**
     * Reads in the binary representation of the specific form of map used by this class.
     *
     * @param in The input stream.
     * @param map The map to read into.
     **/
    protected static void read(ExceptionlessInputStream in,
            HashMap<String, TreeMap<String, Integer>> map) {
        int tableSize = in.readInt();

        for (int i = 0; i < tableSize; ++i) {
            String tableKey = in.readString();
            TreeMap<String, Integer> tableValue = new TreeMap<>();
            map.put(tableKey, tableValue);

            int countsSize = in.readInt();
            for (int j = 0; j < countsSize; ++j)
                tableValue.put(in.readString(), in.readInt());
        }
    }


    /** Empty class, since there are no parameters */
    public static class Parameters extends Learner.Parameters {
        public Parameters() {}

        public Parameters(Parameters p) {
            super(p);
        }
    }
}
