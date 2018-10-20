/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseTreeProperties;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Creates a {@link SpanLabelView} with clause information using the parse tree. This view generator
 * follows the specification in "Tjong Kim Sang, E. F., {@literal &} D'ejean, H. (2001):
 * Introduction to the CoNLL-2001 Shared Task: Clause Identification" and picks out all parse tree
 * nodes that starts with an S to be a clause. In particular, FRAG and RRC are ignored.
 *
 * @author Vivek Srikumar
 */
public class ClauseViewGenerator extends Annotator {

    public static ClauseViewGenerator CHARNIAK = new ClauseViewGenerator(ViewNames.PARSE_CHARNIAK,
            ViewNames.CLAUSES_CHARNIAK);
    public static ClauseViewGenerator STANFORD = new ClauseViewGenerator(ViewNames.PARSE_STANFORD,
            ViewNames.CLAUSES_STANFORD);
    public static ClauseViewGenerator BERKELEY = new ClauseViewGenerator(ViewNames.PARSE_BERKELEY,
            ViewNames.CLAUSES_BERKELEY);

    private final String parseViewName;
    private final String clauseViewName;

    public ClauseViewGenerator(String parseViewName, String clauseViewName) {
        super(clauseViewName, new String[] {parseViewName});
        this.parseViewName = parseViewName;
        this.clauseViewName = clauseViewName;
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
        SpanLabelView view =
                new SpanLabelView(getViewName(), "From " + parseViewName, ta, 1.0, true);

        TreeView parse = (TreeView) ta.getView(parseViewName);

        Set<IntPair> set = new LinkedHashSet<>();
        for (Constituent c : parse) {
            if (TreeView.isLeaf(c))
                continue;

            if (ParseTreeProperties.isPreTerminal(c))
                continue;

            String label = c.getLabel();

            label = ParseUtils.stripFunctionTags(label);
            label = ParseUtils.stripIndexReferences(label);

            // This is the definition used in
            // Introduction to the CoNLL-2001 Shared Task:
            // Clause Identification

            if (label.startsWith("S") && !label.equals("S1")) {
                int start = c.getStartSpan();
                int end = c.getEndSpan();

                if (start >= 0 && end > start) {
                    set.add(new IntPair(start, end));
                }
            }
        }

        for (IntPair span : set) {
            view.addSpanLabel(span.getFirst(), span.getSecond(), "S", 1.0);

        }

        ta.addView(getViewName(), view);
    }

    @Override
    public String[] getRequiredViews() {
        return new String[] {parseViewName};
    }
}
