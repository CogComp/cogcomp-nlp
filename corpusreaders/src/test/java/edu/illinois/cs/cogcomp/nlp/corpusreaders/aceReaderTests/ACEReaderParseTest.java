package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReaderTests;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Bhargav Mangipudi on 4/7/16.
 */
public class ACEReaderParseTest {

    @Ignore("ACE Dataset files will not be commited to repo.")
    @Test
    public void test2004Dataset() throws Exception {
        ACEReader reader = new ACEReader("src/test/resources/ACE/ace2004/data/English", true);

        int numDocs = 0;
        assertTrue(reader.hasNext());
        while (reader.hasNext()) {
            TextAnnotation doc = reader.next();
            assertNotNull(doc);
            assertEquals(doc.getCorpusId(), "ACE2004");

            Set<String> documentViews = doc.getAvailableViews();
            assertTrue(documentViews.contains(ViewNames.TOKENS));
            assertTrue(documentViews.contains(ACEReader.ENTITYVIEW));
            assertTrue(documentViews.contains(ACEReader.RELATIONVIEW));
            assertTrue(documentViews.contains(ViewNames.COREF));

            numDocs++;
        }

        assertEquals(numDocs, 2);
    }

    @Ignore("ACE Dataset files will not be commited to repo.")
    @Test
    public void test2005Dataset() throws Exception {
        ACEReader reader = new ACEReader("src/test/resources/ACE/ace2005/data/English", false);

        int numDocs = 0;
        assertTrue(reader.hasNext());
        while (reader.hasNext()) {
            TextAnnotation doc = reader.next();
            assertNotNull(doc);
            assertEquals(doc.getCorpusId(), "ACE2005");

            Set<String> documentViews = doc.getAvailableViews();
            assertTrue(documentViews.contains(ViewNames.TOKENS));
            assertTrue(documentViews.contains(ACEReader.ENTITYVIEW));
            assertTrue(documentViews.contains(ACEReader.RELATIONVIEW));
            assertTrue(documentViews.contains(ViewNames.COREF));

            numDocs++;
        }

        assertEquals(numDocs, 6);
    }
}
