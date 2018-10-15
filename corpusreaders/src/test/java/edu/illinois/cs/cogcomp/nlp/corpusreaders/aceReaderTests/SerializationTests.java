/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReaderTests;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Bhargav Mangipudi
 */
public class SerializationTests {

    @Test
    public void test2004Dataset() throws Exception {
        ACEReader reader = new ACEReader(ACEReaderParseTest.ACE2004CORPUS, true);

        for (TextAnnotation ta : reader) {
            testDocumentSerialization(ta);
        }
    }

    @Test
    public void test2005Dataset() throws Exception {
        ACEReader reader = new ACEReader(ACEReaderParseTest.ACE2005CORPUS, false);

        for (TextAnnotation ta : reader) {
            testDocumentSerialization(ta);
        }
    }

    /**
     * Testing for some document serialization fidelity.
     * 
     * @param ta TextAnnotation instance.
     */
    public void testDocumentSerialization(TextAnnotation ta) {
        try {
            String jsonRepr = SerializationHelper.serializeToJson(ta);
            Assert.assertNotNull(jsonRepr);

            TextAnnotation taRepr = SerializationHelper.deserializeFromJson(jsonRepr);
            Assert.assertNotNull(taRepr);
            Assert.assertEquals(ta.getAvailableViews(), taRepr.getAvailableViews());

            for (String view : ta.getAvailableViews()) {
                Assert.assertEquals(ta.getView(view).getNumberOfConstituents(), taRepr
                        .getView(view).getNumberOfConstituents());
            }

        } catch (Exception ex) {
            Assert.fail("Should not throw an exception");
        }
    }
}
