/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.utilities.SentenceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Create a pseudo parse view from the clauses, shallow parse and the part of speech views. The
 * implementation assumes that the input text annotation contains those views.
 *
 * @author Gourab Kundu, Vivek Srikumar
 */
public class PseudoParse extends Annotator {

    public static final PseudoParse STANFORD = new PseudoParse(ViewNames.CLAUSES_STANFORD,
            ViewNames.PSEUDO_PARSE_STANFORD);
    public static final PseudoParse BERKELEY = new PseudoParse(ViewNames.CLAUSES_BERKELEY,
            ViewNames.PSEUDO_PARSE_BERKELEY);
    public static final PseudoParse CHARNIAK = new PseudoParse(ViewNames.CLAUSES_CHARNIAK,
            ViewNames.PSEUDO_PARSE_CHARNIAK);

    private final String pseudoParseViewName;
    private final String clauseViewName;

    public PseudoParse(String clauseViewName, String pseudoParseViewName) {
        super(pseudoParseViewName, new String[] {clauseViewName, ViewNames.POS,
                ViewNames.SHALLOW_PARSE});
        this.clauseViewName = clauseViewName;
        this.pseudoParseViewName = pseudoParseViewName;

    }

    /**
     * noop.
     * 
     * @param rm configuration parameters
     */
    @Override
    public void initialize(ResourceManager rm) {}

    @Override
    public void addView(TextAnnotation ta) {
        SpanLabelView cv = (SpanLabelView) ta.getView(clauseViewName);

        List<Constituent> clauses = cv.getConstituents();

        TokenLabelView posView = (TokenLabelView) ta.getView(ViewNames.POS);

        List<Constituent> poss = posView.getConstituents();

        List<Constituent> shallowParse = ta.getView(ViewNames.SHALLOW_PARSE).getConstituents();

        List<Pair<Double, String>> output = new ArrayList<>();
        List<Integer> endOfChunks = new ArrayList<>();

        for (int i = 0; i < ta.size(); i++) {
            output.add(new Pair<>((double) i, SentenceUtils.convertBracketsToPTB(ta.getToken(i))));
        }

        for (Constituent c : poss) {
            output.add(new Pair<>(c.getStartSpan() - 0.1, "(" + c.getLabel()));
            output.add(new Pair<>(c.getEndSpan() - 1 + 0.4, ")"));
        }

        for (Constituent c : shallowParse) {
            output.add(new Pair<>(c.getStartSpan() - 0.2, "(" + c.getLabel()));
            output.add(new Pair<>(c.getEndSpan() - 1 + 0.5, ")"));
            endOfChunks.add(c.getEndSpan());
        }

        for (Constituent c : clauses) {
            if (endOfChunks.contains(c.getStartSpan())) {
                int index = endOfChunks.indexOf(c.getStartSpan());
                output.add(new Pair<>(c.getStartSpan() - 0.3, "(" + c.getLabel() + "-"
                        + shallowParse.get(index).getLabel()));
            } else
                output.add(new Pair<>(c.getStartSpan() - 0.3, "(" + c.getLabel()));
            output.add(new Pair<>(c.getEndSpan() - 1 + 0.6, ")"));
        }

        Collections.sort(output, new Comparator<Pair<Double, String>>() {

            @Override
            public int compare(Pair<Double, String> arg0, Pair<Double, String> arg1) {
                return arg0.getFirst().compareTo(arg1.getFirst());
            }
        });

        StringBuilder sb = new StringBuilder();

        for (Pair<Double, String> o : output) {
            sb.append(o.getSecond()).append(" ");
        }
        String parse = sb.toString();

        TreeView parseView = new TreeView(getViewName(), "From:" + clauseViewName, ta, 1.0);
        Tree<String> parseTree = TreeParserFactory.getStringTreeParser().parse(parse);

        parseView.setParseTree(0, parseTree);

        ta.addView(getViewName(), parseView);
    }


}
