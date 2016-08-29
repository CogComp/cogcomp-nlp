/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.core.datastructures;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

/**
 * This class contains the canonical strings used for naming standard views. They are used both in
 * Curator's views as well as {@link TextAnnotation} views
 *
 * @author Vivek Srikumar
 * @author Christos Christodoulopoulos
 */
public class ViewNames {
    public static final String TOKENS = "TOKENS";

    public static final String SENTENCE = "SENTENCE";
    public static final String PARAGRAPH = "PARAGRAPH";

    public static final String LEMMA = "LEMMA";
    public static final String POS = "POS";

    /** the tree gazetteer produced by the SimpleGazetteerAnnotator. */
    public static final String TREE_GAZETTEER = "TREE_GAZETTEER";

    /** View for Mentions and Relations for the ACE Dataset */
    public static final String MENTION_ACE = "MENTION_ACE";

    /**
     * @deprecated Replaced by ViewNames#NER_CONLL, ViewNames#NER_ONTONOTES
     */
    @Deprecated
    public static final String NER = "NER";

    public static final String NER_CONLL = "NER_CONLL";
    public static final String NER_ONTONOTES = "NER_ONTONOTES";

    public static final String SHALLOW_PARSE = "SHALLOW_PARSE";

    /**
     * @deprecated Replaced by {@link #SHALLOW_PARSE}
     */
    @Deprecated
    public static final String CHUNK = "CHUNK";

    /**
     * @deprecated Replaced by {@link #SRL_VERB}
     */
    @Deprecated
    public static final String SRL = "SRL";

    /**
     * @deprecated Replaced by {@link #SRL_NOM}
     */
    @Deprecated
    public static final String NOM = "NOM";

    public static final String SRL_VERB = "SRL_VERB";
    public static final String SRL_NOM = "SRL_NOM";
    public static final String SRL_PREP = "SRL_PREP";

    public static final String COREF = "COREF";
    // Constituents in this view contains heads of mentions only
    public static final String COREF_HEAD = "COREF_HEAD";
    // Constituents in this view contains extents of mentions only
    public static final String COREF_EXTENT = "COREF_EXTENT";

    public static final String DEPENDENCY_STANFORD = "DEPENDENCY_STANFORD";
    public static final String DEPENDENCY = "DEPENDENCY";

    public static final String QUANTITIES = "QUANTITIES";

    public static final String WIKIFIER = "WIKIFIER";

    /**
     * @deprecated Replaced by {@link #CLAUSES_CHARNIAK}, {@link #CLAUSES_BERKELEY},
     *             {@link #CLAUSES_STANFORD}
     */
    @Deprecated
    public static final String CLAUSES = "CLAUSES";

    /**
     * @deprecated Replaced by {@link #PSEUDO_PARSE_CHARNIAK}, {@link #PSEUDO_PARSE_BERKELEY},
     *             {@link #PSEUDO_PARSE_STANFORD}
     */
    @Deprecated
    public static final String PSEUDO_PARSE = "PSEUDO_PARSE";

    public static final String PARSE_GOLD = "PARSE_GOLD";

    public static final String PARSE_BERKELEY = "PARSE_BERKELEY";
    public static final String PARSE_STANFORD = "PARSE_STANFORD";
    public static final String PARSE_CHARNIAK = "PARSE_CHARNIAK";

    public static final String PARSE_STANFORD_KBEST = "PARSE_STANFORD_KBEST";
    public static final String PARSE_CHARNIAK_KBEST = "PARSE_CHARNIAK_KBEST";

    public static final String CLAUSES_CHARNIAK = "CLAUSES_CHARNIAK";
    public static final String CLAUSES_STANFORD = "CLAUSES_STANFORD";
    public static final String CLAUSES_BERKELEY = "CLAUSES_BERKELEY";

    public static final String PSEUDO_PARSE_CHARNIAK = "PSEUDO_PARSE_CHARNIAK";
    public static final String PSEUDO_PARSE_BERKELEY = "PSEUDO_PARSE_BERKELEY";
    public static final String PSEUDO_PARSE_STANFORD = "PSEUDO_PARSE_STANFORD";
    public static final String GAZETTEER = "GAZETTEER";
    public static final String BROWN_CLUSTERS = "BROWN_CLUSTERS";
    public static final String DEPENDENCY_HEADFINDER = "DEPENDENCY_HEADFINDER";
    public static final String GAZETTEER_NE = "GAZETTEER_NE";

    public static ViewTypes getViewType(String viewName) {
        switch (viewName) {
            case TOKENS:
            case LEMMA:
            case POS:
                return ViewTypes.TOKEN_LABEL_VIEW;
            case SENTENCE:
            case PARAGRAPH:
            case MENTION_ACE:
            case NER_CONLL:
            case NER_ONTONOTES:
            case SHALLOW_PARSE:
            case QUANTITIES:
            case WIKIFIER:
            case CLAUSES_CHARNIAK:
            case CLAUSES_STANFORD:
            case CLAUSES_BERKELEY:
            case BROWN_CLUSTERS:
            case GAZETTEER:
            case TREE_GAZETTEER:
            case GAZETTEER_NE:
                return ViewTypes.SPAN_LABEL_VIEW;
            case DEPENDENCY:
            case DEPENDENCY_STANFORD:
            case DEPENDENCY_HEADFINDER:
                return ViewTypes.DEPENDENCY_VIEW;
            case PARSE_GOLD:
            case PARSE_CHARNIAK:
            case PARSE_CHARNIAK_KBEST:
            case PSEUDO_PARSE_CHARNIAK:
            case PARSE_STANFORD:
            case PARSE_STANFORD_KBEST:
            case PSEUDO_PARSE_STANFORD:
            case PARSE_BERKELEY:
            case PSEUDO_PARSE_BERKELEY:
                return ViewTypes.PARSE_VIEW;
            case SRL_VERB:
            case SRL_NOM:
            case SRL_PREP:
                return ViewTypes.PREDICATE_ARGUMENT_VIEW;
            case COREF:
            case COREF_HEAD:
            case COREF_EXTENT:
                return ViewTypes.COREF_VIEW;
        }
        return null;
    }

    /* whether a given input is a parse view or not */
    public static boolean isItParseView(String viewName) {
        return viewName.equals(ViewNames.PARSE_BERKELEY) || viewName.equals(ViewNames.PARSE_CHARNIAK) ||
                viewName.equals(ViewNames.PARSE_CHARNIAK_KBEST) || viewName.equals(ViewNames.PARSE_GOLD) ||
                viewName.equals(ViewNames.PARSE_STANFORD);
    }
}
