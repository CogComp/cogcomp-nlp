package edu.illinois.cs.cogcomp.depparse;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DepAnnotatorTest {
    private TextAnnotation ta;

    @Before
    public void setUp() {
        ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 1);
    }

    @Test
    public void testDepParserSizeFail() throws Exception {
        DepAnnotator depParser = new DepAnnotator();

        try {
            depParser.addView(ta);
            assertTrue(ta.hasView(ViewNames.DEPENDENCY));
            TreeView depTree = (TreeView) ta.getView(ViewNames.DEPENDENCY);
            assertEquals("finished", depTree.getTreeRoot(0).getSurfaceForm());
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail();
        }
    }
}
