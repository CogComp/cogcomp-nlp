/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import junit.framework.TestCase;

public class TextAnnotationCloneTest extends TestCase {

    private TextAnnotation ta = null;

    public void setUp() throws Exception {
        super.setUp();
        ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 1);

        // adding some attributes
        ta.addAttribute("attributeToTheMainTA", "value");
        for(String viewName: ta.getAvailableViews()) {
            int iter = 0;
            for(Constituent cons : ta.getView(viewName).getConstituents()) {
                cons.addAttribute("key"+iter, "value");
                iter++;
            }
            iter = 0;
            for(Relation rel : ta.getView(viewName).getRelations()) {
                rel.addAttribute("key"+iter, "value");
                iter++;
            }
        }
    }

    public void testTextAnnotationClone() throws Exception {
        TextAnnotation taClone = (TextAnnotation) ta.clone();
        assertEquals(ta.getAvailableViews().size(), taClone.getAvailableViews().size());
        assertEquals(ta.getNumberOfSentences(), taClone.getNumberOfSentences());
        assertEquals(ta.getAttributeKeys().size(), taClone.getAttributeKeys().size());
    }

    public void testViewClone() throws Exception {
        for(String viewName : ta.getAvailableViews()) {
            View view = ta.getView(viewName);
            View viewClone = (View) view.clone();
            assertEquals(view.toString(), viewClone.toString());
            assertEquals(view.count(), viewClone.count());
            assertEquals(view.getEndSpan(), viewClone.getEndSpan());
            assertEquals(view.getStartSpan(), viewClone.getStartSpan());
            assertEquals(view.getConstituents().size(), viewClone.getConstituents().size());
            assertEquals(view.getNumberOfConstituents(), viewClone.getNumberOfConstituents());
            assertEquals(view.getRelations().size(), viewClone.getRelations().size());
            assertEquals(view.getScore(), viewClone.getScore());

            // test constituents
            for(int consIter = 0; consIter < view.getConstituents().size(); consIter++) {
                Constituent cons = view.getConstituents().get(consIter);
                Constituent consClone = viewClone.getConstituents().get(consIter);
                assert(cons.getAttributeKeys().equals(consClone.getAttributeKeys()));
            }

            // test relations
            for(int relIter = 0; relIter < view.getRelations().size(); relIter++) {
                Relation rel = view.getRelations().get(relIter);
                Relation relClone = viewClone.getRelations().get(relIter);
                assert(rel.getAttributeKeys().equals(relClone.getAttributeKeys()));
            }
        }
    }
}

