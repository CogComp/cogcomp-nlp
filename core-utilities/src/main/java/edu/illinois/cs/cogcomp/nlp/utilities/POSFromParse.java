package edu.illinois.cs.cogcomp.nlp.utilities;

import edu.illinois.cs.cogcomp.annotation.BasicAnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;

/**
 * Generates a part-of-speech view using the pre-terminals from a parse tree.
 *
 * @author Vivek Srikumar
 */
public class POSFromParse implements Annotator {

    private final String parseViewName;

    /**
     * Creates a new POSFromParse that uses the specified parse tree to create a
     * POS tag view.
     *
     * @param parseViewName The name of the parse view to use in order to get the POS
     *                      labels.
     */
    public POSFromParse(String parseViewName) {
        this.parseViewName = parseViewName;
    }

    @Override
    public View getView(TextAnnotation ta) {
        TokenLabelView posView = new TokenLabelView(ViewNames.POS, "ParsePOS",
                ta, 1.0);

        int tokenId = 0;
        for (int sentenceId = 0; sentenceId < ta.getNumberOfSentences(); sentenceId++) {

            Tree<String> parseTree = ((TreeView) (ta.getView(parseViewName)))
                    .getTree(sentenceId);

            parseTree = ParseUtils.snipNullNodes(parseTree);
            parseTree = ParseUtils.stripFunctionTags(parseTree);

            if (parseTree.getYield().size() != ta.getSentence(sentenceId)
                    .size())
                throw new IllegalStateException("Parse tree size != ta.size()");

            for (Tree<String> y : parseTree.getYield()) {
                posView.addTokenLabel(tokenId++, y.getParent().getLabel(), 1.0);
            }

        }
        return posView;
    }

    /**
     * Can be used internally by {@link BasicAnnotatorService} to check for pre-requisites before calling
     * any single (external) {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator}.
     *
     * @return The list of {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames} required by this ViewGenerator
     */
    @Override
    public String[] getRequiredViews() {
        return new String[0];
    }

    @Override
    public String getViewName() {

        return ViewNames.POS;
    }

}
