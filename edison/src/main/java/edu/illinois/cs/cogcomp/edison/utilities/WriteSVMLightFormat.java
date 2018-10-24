/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.Lexicon;
import edu.illinois.cs.cogcomp.edison.features.Feature;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;

/**
 * Writes Edison Features into SVMLight format for use by non-CCG learning frameworks SVMLight
 * format is: [label] ([featid]:[featvalue])+
 *
 * -- only active features are represented. -- feature ids must be given in ascending numerical
 * order.
 *
 * WriteSVMLightFormat maintains a Lexicon to track feature ids, and label ids if binary classifier
 * is not specified at construction. Binary classification tasks: expects
 * WriteSVMLightFormat.TRUE_LAB and WriteSVMLightFormat.FALSE_LAB as labels for positive, negative
 * examples respectively.
 *
 * @author mssammon
 */


public class WriteSVMLightFormat {
    private static final String NAME = WriteSVMLightFormat.class.getName();
    private Lexicon labelLex;
    private Lexicon featureLex;
    private boolean isBinaryTask;

    public static final String TRUE_LAB = Boolean.toString(true);
    public static final String FALSE_LAB = Boolean.toString(false);

    /**
     * default constructor: binary task, no bias feature, don't store string values
     */
    public WriteSVMLightFormat() {
        this(true, false, false);
    }

    public WriteSVMLightFormat(boolean isBinaryTask, boolean hasBias, boolean storeStringValues) {
        this.isBinaryTask = isBinaryTask;
        labelLex = new Lexicon(hasBias, storeStringValues);
        featureLex = new Lexicon(hasBias, storeStringValues);
    }


    /**
     * return a String representation of the lexicon (tab-separated key/value pairs, where the key
     * is an integer and the value is a feature's text representation
     *
     * @return
     */
    public String getLexiconContentsAsString() throws IOException {
        StringBuilder bldr = new StringBuilder();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        writeLexiconToOutputStream(baos);

        return bldr.toString();
    }


    /**
     * write the lexicon out to an outputstream. Note that for non-binary tasks, there may be
     * duplicate entries for integer ids: labels may have the same id as features.
     *
     * @param out where the lexicon will be written
     * @throws IOException
     */
    public void writeLexiconToOutputStream(OutputStream out) throws IOException {
        if (!isBinaryTask)
            writeLexicon(labelLex, out);
        writeLexicon(featureLex, out);
    }

    private void writeLexicon(Lexicon lex, OutputStream out) throws IOException {
        lex.writeIntegerToFeatureStringFormat(new PrintStream(out));
    }

    /**
     * Generate a String value corresponding to a SVMLight format example from Edison feature
     * representation (String label and Collection of Feature). Stores a mapping between features
     * and integer ids so that when a new example is passed in, if a feature is the same as an
     * example that has already been processed, that feature will get the same integer id as it did
     * previously. From SVMLight documentation, binary classification labels must be {-1,1} for
     * binary problem, but otherwise labels are just integers -- one integer per class for
     * multi-class problem integer rank for ranking problem Apparently, no problem for label to have
     * same integer id as feature value
     *
     * Assumes sparse boolean feature representation (if a feature is active, it has a unique string
     * identifier in the Collection argument). IMPORTANT: feature ids MUST be written out in
     * ascending order.
     *
     * @param label
     * @param activeFeatures
     * @return
     */
    public String writeFeatureExample(String label, Collection<Feature> activeFeatures) {
        int lab = getLabel(label);
        int[] featIds = new int[activeFeatures.size()];

        int index = 0;
        for (Feature f : activeFeatures)
            featIds[index++] = featureLex.getFeatureId(f.getName());

        Arrays.sort(featIds);

        StringBuilder bldr = new StringBuilder();

        bldr.append(lab);

        for (int featId : featIds)
            bldr.append(" ").append(featId).append(":1");

        return bldr.toString();
    }



    private int getLabel(String label) {

        int lab = -1;

        if (isBinaryTask) {
            if (label.equals(TRUE_LAB))
                lab = 1;
            else if (label.equals(FALSE_LAB))
                lab = -1;
            else
                throw new IllegalArgumentException("ERROR: " + NAME
                        + " constructed expecting binary classification "
                        + "task. Label must be either '" + TRUE_LAB + "' or '" + FALSE_LAB + "'.");
        } else
            lab = labelLex.getFeatureId(label);

        return lab;
    }

}
