/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

import junit.framework.TestCase;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Vivek Srikumar
 */
public class TestWordNetManager {

    WordNetManager wordnet;

    @Before
    public void setUp() throws Exception {
        WordNetManager.loadConfigAsClasspathResource(true);
        wordnet = WordNetManager.getInstance();
    }

    @Test
    public void testExistsEntry() throws Exception {
        assertTrue(wordnet.existsEntry("help", POS.NOUN));
        assertTrue(wordnet.existsEntry("help", POS.VERB));

        assertTrue(wordnet.existsEntry("apple", POS.NOUN));
        assertFalse(wordnet.existsEntry("apple", POS.VERB));

        assertFalse(wordnet.existsEntry("apple", POS.ADVERB));

        assertTrue(wordnet.existsEntry("quickly", POS.ADVERB));
        assertTrue(wordnet.existsEntry("beautiful", POS.ADJECTIVE));
    }

    @Test
    public void testGetGlosses() throws Exception {
        List<String> g = wordnet.getGlosses("test", POS.VERB, false);
        assertEquals(g.size(), 7);

        List<String> glosses = wordnet.getGlosses("test", POS.VERB, true);

        assertEquals(1, glosses.size());
        assertEquals(glosses, Collections.singletonList("put to the test, as "
                + "for its quality, or give experimental use to; \""
                + "This approach has been tried with good results\"; " + "\"Test this recipe\""));
    }

    @Test
    public void testGetSynonyms() throws JWNLException {

        assertEquals(Arrays.asList("trial", "trial_run", "test", "tryout", "test", "mental_test",
                "mental_testing", "psychometric_test", "examination", "exam", "test", "test",
                "trial", "test", "trial", "run", "test", "test", "prove", "try", "try_out",
                "examine", "essay", "screen", "test", "quiz", "test", "test", "test", "test",
                "test"), wordnet.getSynonyms("test", false));
        assertEquals(Arrays.asList("trial", "trial_run", "test", "tryout", "test", "prove", "try",
                "try_out", "examine", "essay"), wordnet.getSynonyms("test", true));
        assertEquals(Arrays.asList("trial", "trial_run", "test", "tryout", "test", "mental_test",
                "mental_testing", "psychometric_test", "examination", "exam", "test", "test",
                "trial", "test", "trial", "run", "test"), wordnet.getSynonyms("test", POS.NOUN,
                false));
        assertEquals(Arrays.asList("trial", "trial_run", "test", "tryout"),
                wordnet.getSynonyms("test", POS.NOUN, true));

        assertEquals(Arrays.asList("5799212", "1006675", "7197021", "794367", "791078", "1904699",
                "2531625", "2533109", "786458", "2745713", "1112584", "920778", "669970"),
                wordnet.getSynsets("test", false));

        assertEquals(Arrays.asList("5799212", "2531625"), wordnet.getSynsets("test", true));

        assertEquals(Arrays.asList("5799212", "1006675", "7197021", "794367", "791078", "1904699"),
                wordnet.getSynsets("test", POS.NOUN, false));

        assertEquals(Collections.singletonList("5799212"),
                wordnet.getSynsets("test", POS.NOUN, true));

    }

    @Test
    public void testGetHypernyms() throws Exception {
        // hyperNymsFirstOnly
        assertEquals(wordnet.getHypernyms("test", true).toString(),
                "[experiment, experimentation, evaluate, pass_judgment, judge]");

        // hyperNymsAllRelatedWords
        assertEquals(wordnet.getHypernyms("test", false).toString(),
                "[experiment, experimentation, mental_measurement, communication, communicating, attempt, effort, endeavor, endeavour, try, attempt, effort, endeavor, endeavour, try, covering, natural_covering, cover, evaluate, pass_judgment, judge, check, examine, be, score, determine, check, find_out, see, ascertain, watch, learn, take, submit]");

        // hyperNymsFirstOnlyOnlyNouns
        assertEquals(wordnet.getHypernyms("test", POS.NOUN, true).toString(), "[experiment, experimentation]");

        // hyperNymsAllRelatedWordsOnlyNouns
        assertEquals(wordnet.getHypernyms("test", POS.NOUN, false).toString(),
                "[experiment, experimentation, mental_measurement, communication, communicating, attempt, effort, endeavor, endeavour, try, attempt, effort, endeavor, endeavour, try, covering, natural_covering, cover]");
    }

    @Test
    public void testGetLexicographerFileNames() throws JWNLException {
        assertEquals(wordnet.getLexicographerFileNames("test", true).toString(), "[noun.cognition, verb.social]");
        assertEquals(wordnet.getLexicographerFileNames("test", false).toString(),
                "[noun.cognition, noun.act, noun.communication, noun.act, noun.act, noun.animal, verb.social, verb.social, verb.communication, verb.stative, verb.competition, verb.communication, verb.cognition]");

        assertEquals(wordnet.getLexicographerFileNames("test", POS.NOUN, true).toString(), "[noun.cognition]");
        assertEquals(wordnet.getLexicographerFileNames("test", POS.NOUN, false).toString(),
                "[noun.cognition, noun.act, noun.communication, noun.act, noun.act, noun.animal]");
    }

    @Test
    public void testGetPointerTypes() throws JWNLException {
        assertEquals(wordnet.getPointers("test", false).toString(), "[hypernym, nominalization, hyponym, verb group]");
        assertEquals(wordnet.getPointers("test", true).toString(), "[hypernym, nominalization, hyponym]");

        assertEquals(wordnet.getPointers("test", POS.NOUN, false).toString(), "[hypernym, nominalization, hyponym]");
        assertEquals(wordnet.getPointers("test", POS.NOUN, true).toString(), "[hypernym, nominalization, hyponym]");
    }

    @Test
    public void testGetSentenceFrames() throws JWNLException {
        assertEquals(wordnet.getVerbFrames("test", false).toString(), "[Something ----s, Somebody ----s]");
        assertEquals(wordnet.getVerbFrames("test", true).toString(), "[Something ----s]");
    }

    @Test
    public void testGetHolonyms() throws JWNLException {
        assertEquals(wordnet.getRelatedWords("bread", PointerType.PART_HOLONYM, false).toString(), "[sandwich]");
        assertEquals(wordnet.getRelatedWords("bread", PointerType.PART_HOLONYM, true).toString(), "[sandwich]");

        assertEquals(wordnet.getRelatedWords("bread", POS.NOUN, PointerType.PART_HOLONYM, false).toString(), "[sandwich]");
        assertEquals(wordnet.getRelatedWords("bread", POS.NOUN, PointerType.PART_HOLONYM, true).toString(), "[sandwich]");

        assertEquals(wordnet.getRelatedWords("human", PointerType.MEMBER_HOLONYM, false).toString(), "[genus_Homo]");
        assertEquals(wordnet.getRelatedWords("dog", PointerType.MEMBER_HOLONYM, true).toString(),
                "[Canis, genus_Canis, pack]");

        assertEquals(wordnet.getRelatedWords("dog", POS.NOUN, PointerType.MEMBER_HOLONYM, false).toString(),
                "[Canis, genus_Canis, pack]");
        assertEquals(wordnet.getRelatedWords("dog", POS.NOUN, PointerType.MEMBER_HOLONYM, true).toString(),
                "[Canis, genus_Canis, pack]");

        assertEquals(wordnet.getRelatedWords("water", PointerType.SUBSTANCE_HOLONYM, false).toString(),
                "[tear, teardrop, perspiration, sweat, sudor, snowflake, flake, ice, water_ice, ice_crystal, snow_mist, diamond_dust, poudrin, ice_needle, frost_snow, frost_mist, body_of_water, water]");
        assertEquals(wordnet.getRelatedWords("water", PointerType.SUBSTANCE_HOLONYM, true).toString(),
                "[tear, teardrop, perspiration, sweat, sudor, snowflake, flake, ice, water_ice, ice_crystal, snow_mist, diamond_dust, poudrin, ice_needle, frost_snow, frost_mist, body_of_water, water]");

        assertEquals(wordnet.getRelatedWords("water", POS.NOUN, PointerType.SUBSTANCE_HOLONYM, false).toString(),
                "[tear, teardrop, perspiration, sweat, sudor, snowflake, flake, ice, water_ice, ice_crystal, snow_mist, diamond_dust, poudrin, ice_needle, frost_snow, frost_mist, body_of_water, water]");
        assertEquals(wordnet.getRelatedWords("water", POS.NOUN, PointerType.SUBSTANCE_HOLONYM, true).toString(),
                "[tear, teardrop, perspiration, sweat, sudor, snowflake, flake, ice, water_ice, ice_crystal, snow_mist, diamond_dust, poudrin, ice_needle, frost_snow, frost_mist, body_of_water, water]");
    }
}
