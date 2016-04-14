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

    @Ignore("ACE Dataset files will not be commited to repo.")
    @Test
    public void test2004Dataset() throws Exception {
        ACEReader reader = new ACEReader("src/test/resources/ACE/ace2004/data/English", true);

        for (TextAnnotation ta : reader) {
            testDocumentSerialization(ta);
        }
    }

    @Ignore("ACE Dataset files will not be commited to repo.")
    @Test
    public void test2005Dataset() throws Exception {
        ACEReader reader = new ACEReader("src/test/resources/ACE/ace2005/data/English", false);

        for (TextAnnotation ta : reader) {
            testDocumentSerialization(ta);
        }
    }

    /**
     * Testing for some document serialization fidelity.
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
                Assert.assertEquals(ta.getView(view).getNumberOfConstituents(),
                        taRepr.getView(view).getNumberOfConstituents());
            }

        } catch(Exception ex) {
            Assert.fail("Should not throw an exception");
        }
    }
}
