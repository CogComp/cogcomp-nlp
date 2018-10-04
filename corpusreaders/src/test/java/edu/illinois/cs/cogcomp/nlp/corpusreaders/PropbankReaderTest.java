/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.stats.Counter;

import org.junit.Test;

import static org.junit.Assert.*;

public class PropbankReaderTest {

    @Test
    public void testParsedViews() throws Exception {
        String treebankHome = "src/test/resources/edu/illinois/cs/cogcomp/nlp/corpusreaders/pennTreeBank_3";
        String propbankHome = "src/test/resources/edu/illinois/cs/cogcomp/nlp/corpusreaders/propBank_1";
        String[] sections = new String[] { "00" };
        PropbankReader data = new PropbankReader(treebankHome, propbankHome, sections, ViewNames.SRL_VERB, true);

        Counter<String> viewCounter = new Counter<>();
        int numDocuments = 0;

        while(data.hasNext()) {
            TextAnnotation ta = data.next();

            for (String viewName: ta.getAvailableViews()) {
                View view = ta.getView(viewName);

                for (Constituent cons: view) {
                    assertTrue("Constituents in " + viewName + " should have valid start character offset",
                            cons.getStartCharOffset() >= 0);
                    assertTrue("Constituents in " + viewName + " should have valid character offsets",
                            cons.getStartCharOffset() < cons.getEndCharOffset());
                }

                viewCounter.incrementCount(viewName);
            }

            numDocuments++;
        }

        assertEquals(3, numDocuments);

        for (String viewName: viewCounter.getSortedItems()) {
            assertEquals("ViewName_" + viewName, 3, viewCounter.getCount(viewName), 0);
        }
    }
}
