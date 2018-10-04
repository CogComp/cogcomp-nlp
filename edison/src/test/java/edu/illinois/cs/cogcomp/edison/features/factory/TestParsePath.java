/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * A test for {@link ParsePath}
 * @author Daniel Khashabi
 */
public class TestParsePath {
    private static TextAnnotation tas = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 1);
    private List<Constituent> cons = tas.getView(ViewNames.PARSE_GOLD).getConstituents();
    public static ParsePath parsePath = new ParsePath(ViewNames.PARSE_GOLD);
    private static Logger logger = LoggerFactory.getLogger(TestParsePath.class);

//    protected void setUp() throws Exception {
//        super.setUp();
//    }

    Set<String> correctResponses = new HashSet<>(Arrays.asList(new String[] {
            "The construction of the John Smith library finished on time .->[]",
            "The construction of the John Smith library finished on time .->[S, , l=1.0]",
            "The construction of the John Smith library->[SvNP, , l=2.0]",
            "The construction->[NPvNP, , l=2.0]",
            "The->[NPvDT, , l=2.0]",
            "The->[DT, , l=1.0]",
            "construction->[NPvNN, , l=2.0]",
            "construction->[NN, , l=1.0]",
            "of the John Smith library->[NPvPP, , l=2.0]",
            "of->[PPvIN, , l=2.0]",
            "of->[IN, , l=1.0]",
            "the John Smith library->[PPvNP, , l=2.0]",
            "the->[NPvDT, , l=2.0]",
            "the->[DT, , l=1.0]",
            "John->[NPvNNP, , l=2.0]",
            "John->[NNP, , l=1.0]",
            "Smith->[NPvNNP, , l=2.0]",
            "Smith->[NNP, , l=1.0]",
            "library->[NPvNN, , l=2.0]",
            "library->[NN, , l=1.0]",
            "finished on time->[SvVP, , l=2.0]",
            "finished->[VPvVBD, , l=2.0]",
            "finished->[VBD, , l=1.0]",
            "on time->[VPvPP, , l=2.0]",
            "on->[PPvIN, , l=2.0]",
            "on->[IN, , l=1.0]",
            "time->[PPvNP, , l=2.0]",
            "time->[NP, , l=1.0]",
            "time->[NP, , l=1.0]",
            ".->[Sv., , l=2.0]",
            ".->[., , l=1.0]"
    }));

    @Test
    public final void testParsePath() throws Exception {
        logger.info(String.valueOf(cons.size()));
        logger.info(tas.getView(ViewNames.PARSE_GOLD).toString());
        for(int i = 0; i < cons.size(); i++) {
            String prediction = cons.get(i).toString() + "->" + parsePath.getFeatures(cons.get(i)).toString();
            assertTrue(correctResponses.contains(prediction));
        }
    }
}
