/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CoNLLNerReaderTest {

    @Test
    public void testCreateTextAnnotation() throws Exception {
        // The files have an equal number of annotations... this way we don't rely on a certain ordering.
        CoNLLNerReader cnr = new CoNLLNerReader("src/test/resources/conlldocs/");

        TextAnnotation ta = cnr.next();
        List<Constituent> cons = ta.getView(ViewNames.NER_CONLL).getConstituents();
        assertEquals(cons.size(), 13);

        List<Constituent> sentcons = ta.getView(ViewNames.SENTENCE).getConstituents();
        assert(sentcons.size() > 1);

        TextAnnotation ta2 = cnr.next();
        cons = ta2.getView(ViewNames.NER_CONLL).getConstituents();
        assertEquals(cons.size(), 13);

        List<TextAnnotation> tas = new ArrayList<>();
        tas.add(ta);
        tas.add(ta2);

        CoNLLNerReader.TaToConll(tas, "/tmp");


    }
}
