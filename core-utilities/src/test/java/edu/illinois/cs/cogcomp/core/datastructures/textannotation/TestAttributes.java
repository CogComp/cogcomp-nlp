/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Before;
import org.junit.Test;

public class TestAttributes {
    private TextAnnotation ta;

    @Before
    public void setUp() {
        this.ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);
    }

    @Test
    public void testAttributes() throws CloneNotSupportedException {
        TextAnnotation taCopy = (TextAnnotation) ta.clone();

        assert ta.equals(taCopy);
        assert ta.hashCode() == taCopy.hashCode();

        // Adding same attribute to both TextAnnotations
        ta.addAttribute("Test", "TestValue");
        taCopy.addAttribute("Test", "TestValue");

        assert ta.equals(taCopy);
        assert ta.hashCode() == taCopy.hashCode();

        ta.addAttribute("Test2", "TestValue");

        assert !ta.equals(taCopy);
        assert ta.hashCode() != taCopy.hashCode();

        assert ta.hasAttribute("Test2");
        assert ta.getAttribute("Test2").equals("TestValue");
    }

    @Test
    public void testAttributeRemove() throws CloneNotSupportedException {
        TextAnnotation taCopy = (TextAnnotation) ta.clone();

        taCopy.addAttribute("Test", "TestValue");
        taCopy.removeAllAttributes();

        assert ta.equals(taCopy);
        assert ta.hashCode() == taCopy.hashCode();
    }
}
