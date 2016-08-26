/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.factory.BrownClusterFeatureExtractor;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by mssammon on 5/13/16.
 */
public class TestWriteSVMLightFormat {
    private static final String EXPECTED_BINARY_NEG =
            "-1 0:1 1:1 2:1 3:1 4:1 5:1 6:1 7:1 8:1 9:1 10:1 11:1 12:1"
                    + " 13:1 14:1 15:1 16:1 17:1 18:1 19:1 20:1 21:1 22:1 23:1 24:1 25:1 26:1 27:1 28:1 29:1 30:1 31:1 32:1 33:1 34:1 35:1";
    private static final String EXPECTED_BINARY_POS =
            "1 0:1 5:1 7:1 9:1 11:1 12:1 14:1 15:1 16:1 18:1 19:1 24:1 25:1 27:1 29:1 33:1 34:1 35:1 36:1 37:1 38:1"
                    + " 39:1 40:1 41:1 42:1 43:1 44:1 45:1 46:1";

    private static final String EXPECTED_MULTI_FIRST =
            "0 0:1 1:1 2:1 3:1 4:1 5:1 6:1 7:1 8:1 9:1 10:1 11:1 12:1 13:1"
                    + " 14:1 15:1 16:1 17:1 18:1 19:1 20:1 21:1 22:1 23:1 24:1 25:1 26:1 27:1 28:1 29:1 30:1 31:1 32:1 33:1 34:1 35:1";

    private static final String EXPECTED_MULTI_SECOND =
            "1 0:1 5:1 7:1 9:1 11:1 12:1 14:1 15:1 16:1 18:1 19:1 24:1 25:1 27:1 29:1 33:1 34:1 35:1 36:1 37:1 38:1"
                    + " 39:1 40:1 41:1 42:1 43:1 44:1 45:1 46:1";


    static private BrownClusterFeatureExtractor bcfex;
    static private TokenizerTextAnnotationBuilder taBldr;
    static private TextAnnotation ta;
    static private Set<Feature> feats;
    private static TextAnnotation ta2;
    static private Set<Feature> feats2;

    @BeforeClass
    public static void runBeforeAllTests() {
        bcfex = BrownClusterFeatureExtractor.instance1000; // "brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt"
        taBldr = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
        ta =
                taBldr.createTextAnnotation(
                        "test",
                        "test",
                        "This test sentence has Joynt and Lieberknecht and Fibonnaci in it "
                                + "just to exercise possible brown cluster hits in resources used by NER.");

        ta2 =
                taBldr.createTextAnnotation("test", "test2",
                        "Why Joynt should have anything to do beyond JFK and Jimmy Carter "
                                + "is beyond your oh-so-humble British writer.");

        feats = new HashSet<>();

        for (int wordIndex = 0; wordIndex < ta.size(); ++wordIndex)
            try {
                feats.addAll(bcfex.getWordFeatures(ta, wordIndex));
            } catch (EdisonException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }

        feats2 = new HashSet<>();
        for (int wordIndex = 0; wordIndex < ta2.size(); ++wordIndex)
            try {
                feats2.addAll(bcfex.getWordFeatures(ta2, wordIndex));
            } catch (EdisonException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }

    }



    @Test
    public void testBinaryExample() {

        WriteSVMLightFormat writeSVMLightFormat = new WriteSVMLightFormat();

        String negOutput =
                writeSVMLightFormat.writeFeatureExample(WriteSVMLightFormat.FALSE_LAB, feats);

        System.out.println(negOutput);
        assertEquals(EXPECTED_BINARY_NEG, negOutput);

        String posOutput =
                writeSVMLightFormat.writeFeatureExample(WriteSVMLightFormat.TRUE_LAB, feats2);

        System.out.println(posOutput);
        assertEquals(EXPECTED_BINARY_POS, posOutput);

        // test failure for non-binary label

        boolean isExceptionThrown = false;
        try {
            String shouldFailOut = writeSVMLightFormat.writeFeatureExample("failDammit", feats);
        } catch (IllegalArgumentException e) {
            System.err.println("Expected exception is caught: ");
            e.printStackTrace();
            isExceptionThrown = true;
        }

        assertTrue(isExceptionThrown);
    }

    @Test
    public void testMulticlassExample() {

        WriteSVMLightFormat writeSVMLightFormat = new WriteSVMLightFormat(false, false, false);

        String negOutput = writeSVMLightFormat.writeFeatureExample("2", feats);

        System.out.println(negOutput);
        assertEquals(EXPECTED_MULTI_FIRST, negOutput);

        String posOutput = writeSVMLightFormat.writeFeatureExample("whatever", feats2);

        System.out.println(posOutput);
        assertEquals(EXPECTED_MULTI_SECOND, posOutput);
    }


    @Test
    public void testWriteLexicon() {
        WriteSVMLightFormat writeSVMLightFormat = new WriteSVMLightFormat(false, false, false);

    }
}
