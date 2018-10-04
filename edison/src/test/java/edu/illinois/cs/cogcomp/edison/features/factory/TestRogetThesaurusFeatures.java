/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link RogetThesaurusFeatures}
 *
 * @author Daniel Khashabi
 */
public class TestRogetThesaurusFeatures {

    private static TextAnnotation ta;
    private static Map goldAns = new HashMap<String, String>();

    static {
        try {
            String paragraph = "This trusty servant\n" +
                    "Shall pass between us: ere long you are like to hear,\n" +
                    "If you dare venture in your own behalf,\n" +
                    "A mistress's command. Wear this; spare speech;\n" +
                    "Decline your head: this kiss, if it durst speak,\n" +
                    "Would stretch thy spirits up into the air.";
            ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(new ArrayList<>(Arrays.asList(new String[][]{paragraph.split(" ")})));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        goldAns.put("dare", "[courage, defiance]");
        goldAns.put("venture", "[undertaking, chance, courage, danger, essay]");
        goldAns.put("Wear", "[temperance, avoidance, disuse, economy, exemption, giving, inaction, insufficiency, littleness, narrowness, thinness, redundancy, relinquishment, store]");
        goldAns.put("stretch", "[length, undueness, space, elasticity, exaggeration, exertion, expansion]");
    }

    @Test
    public final void test() throws Exception {
        RogetThesaurusFeatures fe = RogetThesaurusFeatures.INSTANCE;
        for (Constituent c : ta.getView(ViewNames.TOKENS)) {
            if(goldAns.containsKey(c.getSurfaceForm().toLowerCase())) {
                assertTrue(goldAns.get(c.getSurfaceForm().toLowerCase()).toString().equals(fe.getFeatures(c).toString()));
            }
        }
    }
}
