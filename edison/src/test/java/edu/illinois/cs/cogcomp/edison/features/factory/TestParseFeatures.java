/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestFeaturesResource;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Test class
 * <p>
 * <b>NB:</b> If needed, please re-create the {@code feature.collection.text} file using
 * {@link CreateTestFeaturesResource}.
 *
 * @author Vivek Srikumar
 * edited by Pavankumar Reddy M
 *
 */
public class TestParseFeatures {
    private static List<TextAnnotation> tas;

    private static String annotatedString = "TextAnnotation: " +
            "In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre " +
            "( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts ) , " +
            "the role of Celimene , played by Kim Cattrall , was mistakenly attributed to Christina Haag . ";
    // index of the annotated text in the tas list
    private static int annotatedTAIndex = 34;

    private static Logger logger = LoggerFactory.getLogger(TestParseFeatures.class);

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestChunkFeatures.class, "test.ta");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


//    protected void setUp() throws Exception {
//        super.setUp();
//    }
    

    @Test
    public final void testSampleAnnotation() throws Exception {
        TextAnnotation ta = tas.get(annotatedTAIndex);
        if (!ta.toString().equals(annotatedString)) {
            logger.info("Text Annotation string: \n" + ta.toString());
            logger.info("Reference String: \n" + annotatedString);
            throw new Exception("The text in the Text Annotation doesn't match the Reference String");
        }
        if (!ta.hasView(ViewNames.SRL_VERB))
            throw new EdisonException("SRL_VERB view is missing");
    }
    
    @Test
    public final void testParseHeadWordPOS() throws Exception {
        TextAnnotation ta = tas.get(annotatedTAIndex);

        logger.info("Testing parse head-word+POS: Charniak");
        Set<String> validCharniakResponses = new HashSet<>(Arrays.asList(
                new String[] {
                        "Revitalized----> Classics(1.0)	[hw:classics, h-pos:NNS]",
                        "Take----> in Windy City(1.0)	[hw:in, h-pos:IN]",
                        "Take----> the Stage(1.0)	[hw:stage, h-pos:NNP]",
                        "Take----> Revitalized Classics(1.0)	[hw:classics, h-pos:NNS]",
                        "played----> Celimene(1.0)	[hw:celimene, h-pos:NNP]",
                        "played----> by Kim Cattrall(1.0)	[hw:by, h-pos:IN]",
                        "played----> In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts )(1.0)	[hw:in, h-pos:IN]",
                        "was----> In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts )(1.0)	[hw:in, h-pos:IN]",
                        "was----> the role of Celimene , played by Kim Cattrall ,(1.0)	[hw:role, h-pos:NN]",
                        "attributed----> to Christina Haag(1.0)	[hw:to, h-pos:TO]",
                        "attributed----> mistakenly(1.0)	[hw:mistakenly, h-pos:RB]"
                }
        ));
        testFex(ParseHeadWordPOS.CHARNIAK, true, validCharniakResponses);

        logger.info("Testing parse head-word+POS: Stanford");

        Set<String> validStanfordResponses = new HashSet<>(Arrays.asList(
                new String[] {
                        "Revitalized----> Classics(1.0)	[hw:classics, h-pos:NNS]",
                        "Take----> in Windy City(1.0)	[hw:in, h-pos:IN]",
                        "Take----> the Stage(1.0)	[hw:stage, h-pos:NNP]",
                        "Take----> Revitalized Classics(1.0)	[hw:classics, h-pos:NNS]",
                        "played----> Celimene(1.0)	[hw:celimene, h-pos:NNP]",
                        "played----> by Kim Cattrall(1.0)	[hw:by, h-pos:IN]",
                        "played----> In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts )(1.0)	[hw:in, h-pos:IN]",
                        "was----> In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts )(1.0)	[hw:in, h-pos:IN]",
                        "was----> the role of Celimene , played by Kim Cattrall ,(1.0)	[hw:role, h-pos:NN]",
                        "attributed----> to Christina Haag(1.0)	[hw:to, h-pos:TO]",
                        "attributed----> mistakenly(1.0)	[hw:mistakenly, h-pos:RB]"
                }
        ));
        testFex(ParseHeadWordPOS.STANFORD, true, validStanfordResponses);

    }
    
    @Test
    public final void testParsePath() throws Exception {
        logger.info("Testing parse path: Charniak");
        Set<String> validCharniakResponses = new HashSet<>(Arrays.asList(
                new String[] {
                        "Revitalized----> Classics(1.0)	[VBN^NPvNNS, VBN^, l=3.0]",
                        "Take----> in Windy City(1.0)	[VBP^VPvPP, VBP^, l=3.0]",
                        "Take----> the Stage(1.0)	[VBP^VPvNP, VBP^, l=3.0]",
                        "Take----> Revitalized Classics(1.0)	[VBP^VP^SvNP, VBP^VP^, l=4.0]",
                        "played----> Celimene(1.0)	[VBN^VP^NPvNP, VBN^VP^, l=4.0]",
                        "played----> by Kim Cattrall(1.0)	[VBN^VPvPP, VBN^, l=3.0]",
                        "played----> In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts )(1.0)	[VBN^VP^NP^PP^NP^SvPP, VBN^VP^NP^PP^NP^, l=7.0]",
                        "was----> In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts )(1.0)	[AUX^VP^SvPP, AUX^VP^, l=4.0]",
                        "was----> the role of Celimene , played by Kim Cattrall ,(1.0)	[AUX^VP^SvNP, AUX^VP^, l=4.0]",
                        "attributed----> to Christina Haag(1.0)	[VBN^VPvPP, VBN^, l=3.0]",
                        "attributed----> mistakenly(1.0)	[VBN^VPvADVP, VBN^, l=3.0]"
                }
        ));
        testFex(ParsePath.CHARNIAK, true, validCharniakResponses);

        logger.info("Testing parse path: Stanford");
        Set<String> validStanfordResponses = new HashSet<>(Arrays.asList(
                new String[] {
                        "Revitalized----> Classics(1.0)	[VBN^NPvNNS, VBN^, l=3.0]",
                        "Take----> in Windy City(1.0)	[VB^VPvPP, VB^, l=3.0]",
                        "Take----> the Stage(1.0)	[VB^VPvNP, VB^, l=3.0]",
                        "Take----> Revitalized Classics(1.0)	[VB^VP^SvNP, VB^VP^, l=4.0]",
                        "played----> Celimene(1.0)	[VBN^VP^NPvPPvNP, VBN^VP^, l=5.0]",
                        "played----> by Kim Cattrall(1.0)	[VBN^VPvPP, VBN^, l=3.0]",
                        "played----> In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts )(1.0)	[VBN^VP^NP^SvPP, VBN^VP^NP^, l=5.0]",
                        "was----> In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts )(1.0)	[VBD^VP^SvPP, VBD^VP^, l=4.0]",
                        "was----> the role of Celimene , played by Kim Cattrall ,(1.0)	[VBD^VP^SvNP, VBD^VP^, l=4.0]",
                        "attributed----> to Christina Haag(1.0)	[VBN^VPvPP, VBN^, l=3.0]",
                        "attributed----> mistakenly(1.0)	[VBN^VP^VPvADVP, VBN^VP^, l=4.0]"
                }
        ));
        testFex(ParsePath.STANFORD, true, validStanfordResponses);

    }
    
    @Test
    public final void testParsePhraseType() throws Exception {
        logger.info("Testing parse phrase: Charniak");
        Set<String> validCharniakResponses = new HashSet<>(Arrays.asList(
                new String[] {
                        "Revitalized----> Classics(1.0)	[NNS, pt:h:classics, pt:h-pos:NNS, pt:NP]",
                        "Take----> in Windy City(1.0)	[PP, pt:h:take, pt:h-pos:VBP, pt:VP]",
                        "Take----> the Stage(1.0)	[NP, pt:h:take, pt:h-pos:VBP, pt:VP]",
                        "Take----> Revitalized Classics(1.0)	[NP, pt:h:take, pt:h-pos:VBP, pt:S]",
                        "played----> Celimene(1.0)	[NP, pt:h:celimene, pt:h-pos:NNP, pt:NP]",
                        "played----> by Kim Cattrall(1.0)	[PP, pt:h:played, pt:h-pos:VBN, pt:VP]",
                        "played----> In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts )(1.0)	[PP, pt:h:was, pt:h-pos:VBD, pt:S]",
                        "was----> In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts )(1.0)	[PP, pt:h:was, pt:h-pos:VBD, pt:S]",
                        "was----> the role of Celimene , played by Kim Cattrall ,(1.0)	[NP, pt:h:was, pt:h-pos:VBD, pt:S]",
                        "attributed----> to Christina Haag(1.0)	[PP, pt:h:attributed, pt:h-pos:VBN, pt:VP]",
                        "attributed----> mistakenly(1.0)	[ADVP, pt:h:attributed, pt:h-pos:VBN, pt:VP]"
                }
        ));
        testFex(ParsePhraseType.CHARNIAK, true, validCharniakResponses);

        logger.info("Testing parse phrase: Stanford");
        Set<String> validStanfordResponses = new HashSet<>(Arrays.asList(
                new String[] {
                        "Revitalized----> Classics(1.0)	[NNS, pt:h:classics, pt:h-pos:NNS, pt:NP]",
                        "Take----> in Windy City(1.0)	[PP, pt:h:take, pt:h-pos:VBP, pt:VP]",
                        "Take----> the Stage(1.0)	[NP, pt:h:take, pt:h-pos:VBP, pt:VP]",
                        "Take----> Revitalized Classics(1.0)	[NP, pt:h:take, pt:h-pos:VBP, pt:S]",
                        "played----> Celimene(1.0)	[NP, pt:h:of, pt:h-pos:IN, pt:PP]",
                        "played----> by Kim Cattrall(1.0)	[PP, pt:h:played, pt:h-pos:VBN, pt:VP]",
                        "played----> In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts )(1.0)	[PP, pt:h:was, pt:h-pos:VBD, pt:S]",
                        "was----> In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts )(1.0)	[PP, pt:h:was, pt:h-pos:VBD, pt:S]",
                        "was----> the role of Celimene , played by Kim Cattrall ,(1.0)	[NP, pt:h:was, pt:h-pos:VBD, pt:S]",
                        "attributed----> to Christina Haag(1.0)	[PP, pt:h:attributed, pt:h-pos:VBN, pt:VP]",
                        "attributed----> mistakenly(1.0)	[ADVP, pt:h:was, pt:h-pos:VBD, pt:VP]"
                }
        ));
        testFex(ParsePhraseType.STANFORD, true, validStanfordResponses);

    }

    @Test
    public final void testParseSiblings() throws Exception {
        logger.info("Testing parse siblings: Charniak");
        Set<String> validCharniakResponses = new HashSet<>(Arrays.asList(
                new String[] {
                        "Revitalized----> Classics(1.0)	[LAST_CHILD, lsis.pt:VBN, lsis.hw:revitalized, lsis.hw.pos:VBN]",
                        "Take----> in Windy City(1.0)	[LAST_CHILD, lsis.pt:NP, lsis.hw:stage, lsis.hw.pos:NNP]",
                        "Take----> the Stage(1.0)	[lsis.pt:VBP, lsis.hw:take, lsis.hw.pos:VBP, rsis.pt:PP, rsis.hw:in, rsis.hw.pos:IN]",
                        "Take----> Revitalized Classics(1.0)	[FIRST_CHILD, rsis.pt:VP, rsis.hw:take, rsis.hw.pos:VBP]",
                        "played----> Celimene(1.0)	[FIRST_CHILD, rsis.pt:,, rsis.hw:,, rsis.hw.pos:,]",
                        "played----> by Kim Cattrall(1.0)	[LAST_CHILD, lsis.pt:VBN, lsis.hw:played, lsis.hw.pos:VBN]",
                        "played----> In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts )(1.0)	[FIRST_CHILD, rsis.pt:,, rsis.hw:,, rsis.hw.pos:,]",
                        "was----> In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts )(1.0)	[FIRST_CHILD, rsis.pt:,, rsis.hw:,, rsis.hw.pos:,]",
                        "was----> the role of Celimene , played by Kim Cattrall ,(1.0)	[lsis.pt:,, lsis.hw:,, lsis.hw.pos:,, rsis.pt:VP, rsis.hw:was, rsis.hw.pos:VBD]",
                        "attributed----> to Christina Haag(1.0)	[LAST_CHILD, lsis.pt:VBN, lsis.hw:attributed, lsis.hw.pos:VBN]",
                        "attributed----> mistakenly(1.0)	[FIRST_CHILD, rsis.pt:VBN, rsis.hw:attributed, rsis.hw.pos:VBN]"
                }
        ));
        testFex(ParseSiblings.CHARNIAK, true, validCharniakResponses);

        logger.info("Testing parse siblings: Stanford");
        Set<String> validStanfordResponses = new HashSet<>(Arrays.asList(
                new String[] {
                        "Revitalized----> Classics(1.0)	[LAST_CHILD, lsis.pt:VBN, lsis.hw:revitalized, lsis.hw.pos:VBN]",
                        "Take----> in Windy City(1.0)	[LAST_CHILD, lsis.pt:NP, lsis.hw:stage, lsis.hw.pos:NNP]",
                        "Take----> the Stage(1.0)	[lsis.pt:VB, lsis.hw:take, lsis.hw.pos:VBP, rsis.pt:PP, rsis.hw:in, rsis.hw.pos:IN]",
                        "Take----> Revitalized Classics(1.0)	[FIRST_CHILD, rsis.pt:VP, rsis.hw:take, rsis.hw.pos:VBP]",
                        "played----> Celimene(1.0)	[LAST_CHILD, lsis.pt:IN, lsis.hw:of, lsis.hw.pos:IN]",
                        "played----> by Kim Cattrall(1.0)	[LAST_CHILD, lsis.pt:VBN, lsis.hw:played, lsis.hw.pos:VBN]",
                        "played----> In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts )(1.0)	[FIRST_CHILD, rsis.pt:,, rsis.hw:,, rsis.hw.pos:,]",
                        "was----> In an Oct. 19 review of `` The Misanthrope '' at Chicago 's Goodman Theatre ( `` Revitalized Classics Take the Stage in Windy City , '' Leisure & Arts )(1.0)	[FIRST_CHILD, rsis.pt:,, rsis.hw:,, rsis.hw.pos:,]",
                        "was----> the role of Celimene , played by Kim Cattrall ,(1.0)	[lsis.pt:,, lsis.hw:,, lsis.hw.pos:,, rsis.pt:VP, rsis.hw:was, rsis.hw.pos:VBD]",
                        "attributed----> to Christina Haag(1.0)	[LAST_CHILD, lsis.pt:VBN, lsis.hw:attributed, lsis.hw.pos:VBN]",
                        "attributed----> mistakenly(1.0)	[lsis.pt:VBD, lsis.hw:was, lsis.hw.pos:VBD, rsis.pt:VP, rsis.hw:attributed, rsis.hw.pos:VBN]"
                }
        ));
        testFex(ParseSiblings.STANFORD, true, validStanfordResponses);

    }

    @Test
    public final void testVerbVoiceIndicator() throws Exception {
        TextAnnotation ta = tas.get(annotatedTAIndex);

        logger.info("Testing verb voice: Charniak");

        Set<String> validCharniakResponses = new HashSet<>(Arrays.asList(
                new String[] {
                        "Revitalized	[X]",
                        "Take	[A]",
                        "played	[A]",
                        "was	[A]",
                        "attributed	[P]"
                }
        ));
        PredicateArgumentView pav = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);
        for (Constituent predicate : pav.getPredicates()) {
            String response = predicate + "\t"
                    + VerbVoiceIndicator.CHARNIAK.getFeatures(predicate);
            assertTrue(validCharniakResponses.contains(response));
        }

        logger.info("Testing verb voice: Stanford");

        Set<String> validStanfordResponses = new HashSet<>(Arrays.asList(
                new String[] {
                        "Revitalized	[X]",
                        "Take	[A]",
                        "played	[A]",
                        "was	[A]",
                        "attributed	[P]"
                }
        ));
        pav = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);
        for (Constituent predicate : pav.getPredicates()) {
            String response = predicate + "\t"
                    + VerbVoiceIndicator.STANFORD.getFeatures(predicate);
            assertTrue(validStanfordResponses.contains(response));
        }

    }


    private void testFex(FeatureExtractor fex, boolean printBoth, Set<String> validResponses)
            throws EdisonException {

        TextAnnotation ta = tas.get(annotatedTAIndex);
        if (!ta.hasView(ViewNames.SRL_VERB))
            throw new EdisonException("SRL_VERB view is missing");

        PredicateArgumentView pav = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);

        for (Constituent predicate : pav.getPredicates()) {
            Constituent p = predicate.cloneForNewView("dummy");

            for (Relation argument : pav.getArguments(predicate)) {
                Constituent c = argument.getTarget().cloneForNewView("dummy");
                Relation r = new Relation("", p, c, 1);

                String response = (printBoth ? r : c) + "\t" + fex.getFeatures(c);
                assertTrue("Response should be one of the valid responses", validResponses.contains(response));
            }
        }
    }

}
