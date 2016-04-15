package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class PennTreebankPOSReaderTest {

    private List<String> lines;

    @Before
    public void setUp() throws Exception {
        lines = LineIO.readFromClasspath("samplePOS.br");
    }

    @Test
    public void testCreateTextAnnotation() throws Exception {
        PennTreebankPOSReader reader = new PennTreebankPOSReader("test");
        TextAnnotation ta = reader.createTextAnnotation(lines.get(0), "sent0");
        assertTrue(ta.hasView(ViewNames.POS));
        assertEquals("NNP", ta.getView(ViewNames.POS).getConstituents().get(1).getLabel());
    }
}
