package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class OntonotesReaderTest {
    @Test
    public void testCreateTextAnnotation() throws Exception {

        OntonotesReader or = new OntonotesReader("src/test/resources/ontonotes/");

        TextAnnotation ta = or.next();

        List<Constituent> cons = ta.getView(ViewNames.NER_CONLL).getConstituents();
        assertEquals(cons.size(), 14);

        List<Constituent> sentcons = ta.getView(ViewNames.SENTENCE).getConstituents();
        assert (sentcons.size() > 1);

    }
}
