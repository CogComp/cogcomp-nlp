package edu.illinois.cs.cogcomp.nlp.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import junit.framework.TestCase;

import java.util.Collections;

public class CollinsHeadFinderTest extends TestCase {

    private TextAnnotation ta;

    public void setUp() {
        String[] sentence = "Central Asia , North east Europe and Africa are continents .".split(" ");
        String treeString = "(S1 (S (NP (NP (NNP Central)             "
                + "  (NNP Asia))           (, ,)        "
                + "   (NP (NNP North)               (NNP east)  "
                + "             (NNP Europe))           (CC and)           "
                + "(NP (NNP Africa)))       (VP (AUX are)       "
                + "    (NP (NNS continents)))       (. .)))";

        Tree<String> tree = TreeParserFactory.getStringTreeParser().parse(treeString);

        ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(Collections.singletonList(sentence));

        TreeView view = new TreeView(ViewNames.PARSE_GOLD, "", ta, 1.0);
        view.setParseTree(0, tree);
        ta.addView(ViewNames.PARSE_GOLD, view);
    }

    public void testGetHeadword() throws Exception {
        TreeView view = (TreeView) ta.getView(ViewNames.PARSE_GOLD);

        CollinsHeadFinder headFinder = CollinsHeadFinder.getInstance();

        Constituent headWord = headFinder.getHeadWord(view.getRootConstituent(0));
        assertEquals("are", headWord.getTokenizedSurfaceForm());

        Constituent parsePhrase = view.getParsePhrase(new Constituent("", "", ta, 0, 8));

        headWord = headFinder.getHeadWord(parsePhrase);
        assertEquals("Asia", headWord.getTokenizedSurfaceForm());
    }
}