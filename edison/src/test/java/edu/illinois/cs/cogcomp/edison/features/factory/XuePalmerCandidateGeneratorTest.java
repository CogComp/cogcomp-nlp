package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class XuePalmerCandidateGeneratorTest {
    TextAnnotation ta;

    @Before
    public void setUp() throws Exception {
        ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(new String[]{ViewNames.PARSE_GOLD}, false);
    }

    @Test
    public void testGenerateCandidates() throws Exception {
        Constituent predicate = ta.getView(ViewNames.TOKENS).getConstituents().get(5);
        Tree<String> parseTree = ((TreeView) ta.getView(ViewNames.PARSE_GOLD)).getTree(0);
        List<Constituent> candidates = XuePalmerCandidateGenerator.generateCandidates(predicate, parseTree);
        assertEquals(4, candidates.size());
        assertEquals("on time", candidates.get(1).toString());
        assertEquals("The construction of the library", candidates.get(3).toString());
    }
}