package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TreeViewTest {

    private TextAnnotation ta;

    @Before
    public void setUp() throws Exception {
        String[] viewsToAdd = {ViewNames.POS, ViewNames.PARSE_STANFORD};
        ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false);
    }

    @Test
    public void testGetConstituentTree() throws Exception {
        Tree<Constituent> constituentTree = ((TreeView) ta.getView(ViewNames.PARSE_STANFORD)).getConstituentTree(0);
        // Confusingly, the constituent of each node of the tree is called a label
        Constituent root = constituentTree.getLabel();
        assertEquals("S1", root.getLabel());
        assertEquals(new IntPair(0, 9), root.getSpan());
        Constituent firstNoun = constituentTree.getChild(0).getChild(0).getChild(0).getChild(1).getLabel();
        assertEquals("construction", firstNoun.getSurfaceForm());
        Constituent prepPhrase = constituentTree.getChild(0).getChild(1).getChild(1).getLabel();
        assertEquals("on time",  prepPhrase.getSurfaceForm());
        assertEquals("IN", ta.getView(ViewNames.POS).getLabelsCovering(prepPhrase).get(0));
    }
}