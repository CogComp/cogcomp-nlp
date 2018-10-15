/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestAttributes {
    private TextAnnotation ta;

    @Before
    public void setUp() {
        this.ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);
    }

    @Test
    public void testAttributes() throws Exception {
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

        String jsonSerialized = SerializationHelper.serializeToJson(ta);
        TextAnnotation deserializedCopy = SerializationHelper.deserializeFromJson(jsonSerialized);

        assertEquals("JSON Deserialized TA should have same attributes as original TA.", deserializedCopy, ta);

        TextAnnotation bytesDeserialized = SerializationHelper.deserializeTextAnnotationFromBytes(
                SerializationHelper.serializeTextAnnotationToBytes(ta));

        assertEquals("Bytes Deserialized TA should have same attributes as original TA.", bytesDeserialized, ta);
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
