/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */

package edu.illinois.cs.cogcomp.edison.features.lrec.ner;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.StringUtils;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.factory.BrownClusterFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test BrownClusterFeatureExtractor functionality.
 *
 * @author mssammon
 */

public class TestBrownClusterFeatureExtractor {
    private static final String expectedOutput =
            "prefix-10:0010011111,prefix-10:0011111111,prefix-10:0101010000,"
                    + "prefix-10:0101011100,prefix-10:0101101110,prefix-10:0101111011,prefix-10:0110001100,"
                    + "prefix-10:0110001101,prefix-10:0110001111,prefix-10:0110010111,prefix-10:0110011101,"
                    + "prefix-10:0110100111,prefix-10:0110101101,prefix-10:0110101110,prefix-10:0110111011,"
                    + "prefix-10:0111111010,prefix-10:0111111101,prefix-10:1010010010,prefix-10:1010011010,"
                    + "prefix-10:1010011111,prefix-10:1010110100,prefix-10:1010110101,prefix-10:1010111000,"
                    + "prefix-10:1010111010,prefix-10:1011111011,prefix-10:1101110110,prefix-10:1101111101,"
                    + "prefix-10:1101111111,prefix-10:1110001101,prefix-10:1110011100,prefix-10:1110011110,"
                    + "prefix-10:1110100111,prefix-10:1110101011,prefix-10:1110101101,prefix-10:1110101111,"
                    + "prefix-10:1110110111,prefix-10:1110111011,prefix-10:1110111111,prefix-10:1111001111,"
                    + "prefix-10:1111010100,prefix-10:1111010110,prefix-10:1111011110,prefix-10:1111111010,"
                    + "prefix-4:0000,prefix-4:0001,prefix-4:0010,prefix-4:0011,prefix-4:0101,prefix-4:0110,"
                    + "prefix-4:0111,prefix-4:1000,prefix-4:1001,prefix-4:1010,prefix-4:1011,prefix-4:1101,"
                    + "prefix-4:1110,prefix-4:1111,prefix-6:000111,prefix-6:001000,prefix-6:001001,prefix-6:001010,"
                    + "prefix-6:001111,prefix-6:010101,prefix-6:010110,prefix-6:010111,prefix-6:011000,prefix-6:011001,"
                    + "prefix-6:011010,prefix-6:011011,prefix-6:011100,prefix-6:011111,prefix-6:100010,prefix-6:100011,"
                    + "prefix-6:100101,prefix-6:100110,prefix-6:100111,prefix-6:101001,prefix-6:101010,prefix-6:101011,"
                    + "prefix-6:101100,prefix-6:101111,prefix-6:110111,prefix-6:111000,prefix-6:111001,prefix-6:111010,"
                    + "prefix-6:111011,prefix-6:111100,prefix-6:111101,prefix-6:111111,";

    @Test
    public final void test() {
        int[] prefixLengths = new int[] {4, 6, 10, 20};
        BrownClusterFeatureExtractor bcfex1 = BrownClusterFeatureExtractor.instance1000;
        BrownClusterFeatureExtractor bcfex2 = null;
        try {
            bcfex2 =
                    new BrownClusterFeatureExtractor("bllip", "brownBllipClusters",
                            prefixLengths);
        } catch (EdisonException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        BrownClusterFeatureExtractor bcfex3 = null;
        try {
            bcfex3 =
                    new BrownClusterFeatureExtractor(
                            "wiki",
                            "brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt",
                            prefixLengths);
        } catch (EdisonException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        TokenizerTextAnnotationBuilder taBldr =
                new TokenizerTextAnnotationBuilder(new StatefulTokenizer());

        TextAnnotation ta =
                taBldr.createTextAnnotation(
                        "test",
                        "test",
                        "This test sentence has Joynt and Lieberknecht and Fibonnaci in it "
                                + "just to exercise possible brown cluster hits in resources used by NER.");

        Set<Feature> feats = new HashSet<>();

        for (int wordIndex = 0; wordIndex < ta.size(); ++wordIndex)
            try {
                feats.addAll(bcfex1.getWordFeatures(ta, wordIndex));
                feats.addAll(bcfex2.getWordFeatures(ta, wordIndex));
                feats.addAll(bcfex3.getWordFeatures(ta, wordIndex));
            } catch (EdisonException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        assertTrue(ta.hasView(ViewNames.BROWN_CLUSTERS + "_wiki"));

        String[] featArray = new String[feats.size()];

        int i = 0;
        for (Feature f : feats)
            featArray[i++] = f.toString();

        Arrays.sort(featArray);
        String actualOutput = StringUtils.join(",", featArray);
        assertEquals(expectedOutput, actualOutput);
    }
}
