/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.constants;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

public class CoreConfigNames {
    public static final String DEBUG = "debug";
    public static final String CURATOR_HOST = "curatorHost";
    public static final String CURATOR_PORT = "curatorPort";
    public static final String CURATOR_FORCE_UPDATE = "curatorForceUpdate";

    // CuratorClient flags
    /** If set to {@code true}, the input text will be assumed to be pre-tokenized */
    public static final String RESPECT_TOKENIZATION = "respectTokenization";
    /** A comma-separated list of views to add (see {@link ViewNames} for a complete list of views. */
    public static final String VIEWS_TO_ADD = "viewsToAdd";
    /**
     * Use this option to output the annotated {@link TextAnnotation} as plain text (instead of
     * serialzed)
     */
    public static final String OUTPUT_TO_TEXT = "outputToText";
    /** Force the Curator client to overwrite the generated output files */
    public static final String IS_FORCE_OVERWRITE = "forceUpdateOutputFile";

    // CuratorTextCleaner flags
    public static final String REMOVE_REPEAT_PUNCTUATION = "removeRepeatPunctuation";
    public static final String REPLACE_ADHOC_MARKUP = "replaceAdHocMarkup";
    public static final String REPLACE_BAD_APOSTROPHE = "replaceBadApostrophe";
    public static final String REPLACE_CONTROL_SEQUENCE = "replaceControlSequence";
    public static final String REPLACE_UNDERSCORES = "replaceUnderscores";
}
