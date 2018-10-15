/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.main;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.pipeline.Stanford_3_8_0_AnnotatorService;
import edu.illinois.cs.cogcomp.pipeline.handlers.*;
import org.junit.*;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Tests for external annotators
 *
 * @author khashab2
 */
public class ExternalAnnotatorsTest {

    private AnnotatorService service = null;
    private TextAnnotation ta = null;
    private TextAnnotation ta2 = null;
    private TextAnnotation ta3 = null;

    @Ignore
    @Before
    public void init() throws IOException, AnnotatorException {
        Stanford_3_8_0_AnnotatorService.initialize();
        this.service = Stanford_3_8_0_AnnotatorService.service;
        this.ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 3);
        String sampleText = "Shelly wanted a puppy. She asked her mommy and daddy every day for one. She told them that she would help take care of the puppy, if she could have one. Her mommy and daddy talked it over and said that they would get Shelly a new puppy. Her mommy took her to the dog pound so that she could choose one that she wanted. All the puppies at the dog pound need a loving home. Shelly went to every cage and looked each puppy in the eyes and talked to each one. After each one, she told her mommy, \"No, this isn't the one for me.\" Finally, she saw a black and white spotted one that she fell in love with. She screamed, \"Mommy, this is the one!\" Her mommy asked the worker to take the puppy out so that Shelly could make sure. Shelly and the puppy fell in love with each other right away. Shelly and her mommy took the black and white spotted puppy home with them. Shelly was so excited that she talked all the way home. After thinking hard, Shelly had a name for her new puppy, Spot.";
        this.ta2 = service.createBasicTextAnnotation("", "", sampleText);
        String sampleText3 = "shelly wanted a puppy. she asked her mommy and daddy every day for one. she told them that she would help take care of the puppy, if she could have one. Her mommy and daddy talked it over and said that they would get Shelly a new puppy. Her mommy took her to the dog pound so that she could choose one that she wanted. All the puppies at the dog pound need a loving home. Shelly went to every cage and looked each puppy in the eyes and talked to each one. After each one, she told her mommy, \"No, this isn't the one for me.\" Finally, she saw a black and white spotted one that she fell in love with. She screamed, \"Mommy, this is the one!\" Her mommy asked the worker to take the puppy out so that Shelly could make sure. Shelly and the puppy fell in love with each other right away. Shelly and her mommy took the black and white spotted puppy home with them. Shelly was so excited that she talked all the way home. After thinking hard, Shelly had a name for her new puppy, spot.";
        this.ta3 = service.createBasicTextAnnotation("", "", sampleText3);
    }

    @Ignore
    @Test
    public void testExternalAnnotators() throws AnnotatorException {
        service.addView(ta, StanfordOpenIEHandler.viewName);
        assertTrue(ta.hasView(StanfordOpenIEHandler.viewName));
        assertTrue(ta.getView(StanfordOpenIEHandler.viewName).getConstituents().size() >= 5);

        service.addView(ta, StanfordRelationsHandler.viewName);
        assertTrue(ta.hasView(StanfordRelationsHandler.viewName));
        assertTrue(ta.getView(StanfordRelationsHandler.viewName).getConstituents().size() >= 5);

        service.addView(ta, StanfordCorefHandler.viewName);
        assertTrue(ta.hasView(StanfordCorefHandler.viewName));

        // sample text 2
        service.addView(ta2, StanfordCorefHandler.viewName);
        assertTrue(ta2.getView(StanfordCorefHandler.viewName).getConstituents().size() > 40);

        // sample text 3
        String trueCaseEpectedOutput = "(Shelly shelly) (wanted wanted) (a a) (puppy puppy) (. .) (She she) (asked asked) (her her) (mommy mommy) (and and) (daddy daddy) (every every) (day day) (for for) (one one) (. .) (She she) (told told) (them them) (that that) (she she) (would would) (help help) (take take) (care care) (of of) (the the) (puppy puppy) (, ,) (if if) (she she) (could could) (have have) (one one) (. .) (Her Her) (Mommy mommy) (and and) (daddy daddy) (talked talked) (it it) (over over) (and and) (said said) (that that) (they they) (would would) (get get) (shelly Shelly) (a a) (new new) (puppy puppy) (. .) (Her Her) (Mommy mommy) (took took) (her her) (to to) (the the) (dog dog) (pound pound) (so so) (that that) (she she) (could could) (choose choose) (one one) (that that) (she she) (wanted wanted) (. .) (All All) (the the) (puppies puppies) (at at) (the the) (dog dog) (pound pound) (need need) (a a) (loving loving) (home home) (. .) (Shelly Shelly) (went went) (to to) (every every) (cage cage) (and and) (looked looked) (each each) (puppy puppy) (in in) (the the) (eyes eyes) (and and) (talked talked) (to to) (each each) (one one) (. .) (After After) (each each) (one one) (, ,) (she she) (told told) (her her) (mommy mommy) (, ,) (`` \") (No No) (, ,) (this this) (is is) (n't n't) (the the) (one one) (for for) (me me) (. .) ('' \") (Finally Finally) (, ,) (she she) (saw saw) (a a) (black black) (and and) (white white) (spotted spotted) (one one) (that that) (she she) (fell fell) (in in) (love love) (with with) (. .) (She She) (screamed screamed) (, ,) (`` \") (Mommy Mommy) (, ,) (this this) (is is) (the the) (one one) (! !) ('' \") (Her Her) (Mommy mommy) (asked asked) (the the) (worker worker) (to to) (take take) (the the) (puppy puppy) (out out) (so so) (that that) (shelly Shelly) (could could) (make make) (sure sure) (. .) (Shelly Shelly) (and and) (the the) (puppy puppy) (fell fell) (in in) (love love) (with with) (each each) (other other) (right right) (away away) (. .) (Shelly Shelly) (and and) (her her) (mommy mommy) (took took) (the the) (black black) (and and) (white white) (spotted spotted) (puppy puppy) (home home) (with with) (them them) (. .) (Shelly Shelly) (was was) (so so) (excited excited) (that that) (she she) (talked talked) (all all) (the the) (way way) (home home) (. .) (After After) (thinking thinking) (hard hard) (, ,) (shelly Shelly) (had had) (a a) (name name) (for for) (her her) (new new) (puppy puppy) (, ,) (Spot spot) (. .)";
        service.addView(ta3, StanfordTrueCaseHandler.viewName);
        assertTrue(ta3.hasView(StanfordTrueCaseHandler.viewName));
        assertTrue(ta3.getView(StanfordTrueCaseHandler.viewName).toString().contains(trueCaseEpectedOutput));
        assertTrue(ta3.getView(StanfordTrueCaseHandler.viewName).getConstituents().size() > 10);
    }
}
