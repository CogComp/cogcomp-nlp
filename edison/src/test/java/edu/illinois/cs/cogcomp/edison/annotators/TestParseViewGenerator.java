package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.edison.features.helpers.ParseHelper;
import edu.illinois.cs.cogcomp.edison.utilities.CollinsHeadDependencyParser;
import junit.framework.TestCase;

/**
 * @author Vivek Srikumar
 */
public class TestParseViewGenerator extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public final void testCharniakParseViewGenerator() {
		String sentence = "This is a test .";

		String treeString = "(S1 (S (NP (DT This))   (VP (AUX is)       (NP (DT a)           (NN test)))(. .)))";

		Tree<String> tree = TreeParserFactory.getStringTreeParser().parse(treeString);

		TextAnnotation ta = TextAnnotationUtilities.createFromTokenizedString(sentence);

		TreeView parseView = new TreeView(ViewNames.PARSE_CHARNIAK, "My_PARSER", ta, 1d);
		parseView.setParseTree(0, tree);

		ta.addView(ViewNames.PARSE_CHARNIAK, parseView);

		TreeView view = (TreeView) ta.getView(ViewNames.PARSE_CHARNIAK);
		System.out.println(ParseHelper.getParseTree(ViewNames.PARSE_CHARNIAK, ta, 0));

		assertEquals(tree, view.getTree(0));

		CollinsHeadDependencyParser depParser = new CollinsHeadDependencyParser(true);
		Tree<Pair<String, Integer>> depTree = depParser.getLabeledDependencyTree(view.getRootConstituent(0));

		TreeView depView = new TreeView(ViewNames.DEPENDENCY, "HeadRuleDependencyTree", ta, 1.0);

		System.out.println(depTree);

		depView.setDependencyTree(0, depTree);

		ta.addView(ViewNames.DEPENDENCY, depView);

		System.out.println(depView);

		System.out.println(depView.getTree(0));
	}
}
