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
package edu.illinois.cs.cogcomp.edison.utilities;

import junit.framework.TestCase;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Vivek Srikumar
 */
public class TestWordNetManager extends TestCase {

    WordNetManager wordnet;

    protected void setUp() throws Exception {
        WordNetManager.loadConfigAsClasspathResource(true);
        wordnet = WordNetManager.getInstance();

    }

    public void testExistsEntry() throws Exception {
        assertTrue(wordnet.existsEntry("help", POS.NOUN));
        assertTrue(wordnet.existsEntry("help", POS.VERB));

        assertTrue(wordnet.existsEntry("apple", POS.NOUN));
        assertFalse(wordnet.existsEntry("apple", POS.VERB));

        assertFalse(wordnet.existsEntry("apple", POS.ADVERB));

        assertTrue(wordnet.existsEntry("quickly", POS.ADVERB));
        assertTrue(wordnet.existsEntry("beautiful", POS.ADJECTIVE));
    }

    public void testGetGlosses() throws Exception {
        List<String> g = wordnet.getGlosses("test", POS.VERB, false);
        assertEquals(g.size(), 7);

        List<String> glosses = wordnet.getGlosses("test", POS.VERB, true);

        assertEquals(1, glosses.size());
        assertEquals(glosses, Collections.singletonList("put to the test, as "
                + "for its quality, or give experimental use to; \""
                + "This approach has been tried with good results\"; " + "\"Test this recipe\""));
    }

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

    public void testGetHypernyms() throws Exception {

        System.out.println(wordnet.getHypernyms("test", false));
        System.out.println(wordnet.getHypernyms("test", true));

        System.out.println(wordnet.getHypernyms("test", POS.NOUN, false));
        System.out.println(wordnet.getHypernyms("test", POS.NOUN, true));
    }

    public void testGetLexicographerFileNames() throws JWNLException {
        System.out.println(wordnet.getLexicographerFileNames("test", false));
        System.out.println(wordnet.getLexicographerFileNames("test", true));

        System.out.println(wordnet.getLexicographerFileNames("test", POS.NOUN, false));
        System.out.println(wordnet.getLexicographerFileNames("test", POS.NOUN, true));

    }

    public void testGetPointerTypes() throws JWNLException {
        System.out.println(wordnet.getPointers("test", false));
        System.out.println(wordnet.getPointers("test", true));

        System.out.println(wordnet.getPointers("test", POS.NOUN, false));
        System.out.println(wordnet.getPointers("test", POS.NOUN, true));
    }

    public void testGetSentenceFrames() throws JWNLException {
        System.out.println(wordnet.getVerbFrames("test", false));
        System.out.println(wordnet.getVerbFrames("test", true));

    }

    public void testGetHolonyms() throws JWNLException {

        System.out.println(wordnet.getRelatedWords("bread", PointerType.PART_HOLONYM, false));
        System.out.println(wordnet.getRelatedWords("bread", PointerType.PART_HOLONYM, true));

        System.out.println(wordnet.getRelatedWords("bread", POS.NOUN, PointerType.PART_HOLONYM,
                false));
        System.out.println(wordnet.getRelatedWords("bread", POS.NOUN, PointerType.PART_HOLONYM,
                true));

        System.out.println(wordnet.getRelatedWords("human", PointerType.MEMBER_HOLONYM, false));
        System.out.println(wordnet.getRelatedWords("dog", PointerType.MEMBER_HOLONYM, true));

        System.out.println(wordnet.getRelatedWords("dog", POS.NOUN, PointerType.MEMBER_HOLONYM,
                false));
        System.out.println(wordnet.getRelatedWords("dog", POS.NOUN, PointerType.MEMBER_HOLONYM,
                true));

        System.out.println(wordnet.getRelatedWords("water", PointerType.SUBSTANCE_HOLONYM, false));
        System.out.println(wordnet.getRelatedWords("water", PointerType.SUBSTANCE_HOLONYM, true));

        System.out.println(wordnet.getRelatedWords("water", POS.NOUN,
                PointerType.SUBSTANCE_HOLONYM, false));
        System.out.println(wordnet.getRelatedWords("water", POS.NOUN,
                PointerType.SUBSTANCE_HOLONYM, true));

    }
}
