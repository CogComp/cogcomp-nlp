/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pos;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pos.lbjava.BaselineTarget;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.*;

/**
 * This parser returns only words that have been observed less than or equal to {@link #threshold}
 * times according to {@link BaselineTarget}.
 *
 * @author Nick Rizzolo
 **/
public class POSLabeledUnknownWordParser extends POSBracketToToken {
    /**
     * A reference to the classifier that knows how often words were observed during training.
     **/
    private static ResourceManager rm = new POSConfigurator().getDefaultConfig();
    private static String baselineModelFile = rm.getString("baselineModelPath");
    private static String baselineLexFile = rm.getString("baselineLexPath");
    private static final BaselineTarget baseline = new BaselineTarget(baselineModelFile,
            baselineLexFile);
    /** Only words that were observed this many times or fewer are returned. */
    public static int threshold = 3;


    /**
     * Initializes an instance with the named file.
     *
     * @param file The name of the file containing labeled data.
     **/
    POSLabeledUnknownWordParser(String file) {
        super(file);
    }


    /** Returns the next labeled word in the data. */
    public Object next() {
        Token result = (Token) super.next();
        while (result != null && baseline.observedCount(result.form) > threshold)
            result = (Token) super.next();
        return result;
    }
}
