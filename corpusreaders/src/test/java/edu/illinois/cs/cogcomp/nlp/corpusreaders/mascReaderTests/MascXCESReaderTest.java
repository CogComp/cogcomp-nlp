/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.mascReaderTests;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.mascReader.MascXCESReader;

public class MascXCESReaderTest {
    private static final String CORPUS_DIRECTORY = "src/test/resources/edu/illinois/cs/cogcomp/nlp/corpusreaders/masc/xces";

    @Test
    public void testCreateTextAnnotation() throws Exception {
        Logger.getLogger(MascXCESReader.class).setLevel(Level.INFO);

        MascXCESReader cnr = new MascXCESReader("", CORPUS_DIRECTORY, ".xml");

        Assert.assertTrue(cnr.hasNext());
        cnr.next();
        Assert.assertTrue(cnr.hasNext());
        TextAnnotation ta = cnr.next();
        Assert.assertFalse(cnr.hasNext());

        List<Constituent> tokens = ta.getView(ViewNames.TOKENS).getConstituents();
        Assert.assertEquals(tokens.size(), 291);  // tok 291
        Assert.assertEquals(tokens.get(6).toString(), "Which");

        List<Constituent> lemma = ta.getView(ViewNames.LEMMA).getConstituents();
        Assert.assertEquals(lemma.size(), 282);  // base= 282
        Assert.assertEquals(lemma.get(6).getLabel(), "which");

        List<Constituent> pos = ta.getView(ViewNames.POS).getConstituents();
        Assert.assertEquals(pos.size(), 291);  // msd= 291
        Assert.assertEquals(pos.get(6).getLabel(), "WDT");

        List<Constituent> sentences = ta.getView(ViewNames.SENTENCE).getConstituents();
        Assert.assertEquals(sentences.size(), 29);  // normalized sentences 29
        Assert.assertEquals(sentences.get(2).getStartSpan(), 14);  // a sentence is created to cover uncovered tokens
        Assert.assertEquals(sentences.get(2).getEndSpan(), 17);

        List<Constituent> sentencesGold = ta.getView(ViewNames.SENTENCE_GOLD).getConstituents();
        Assert.assertEquals(sentencesGold.size(), 21);  // s 21
        Assert.assertEquals(sentencesGold.get(2).getStartSpan(), 17);
        Assert.assertEquals(sentencesGold.get(2).getEndSpan(), 25);

        List<Constituent> shallowParse = ta.getView(ViewNames.SHALLOW_PARSE).getConstituents();
        Assert.assertEquals(shallowParse.size(), 129);  // nchunk 90, vchunk 39
        Assert.assertEquals(shallowParse.get(1).getStartSpan(), 3);
        Assert.assertEquals(shallowParse.get(1).getEndSpan(), 5);
        Assert.assertEquals(shallowParse.get(1).getLabel(), "NP");

        List<Constituent> ner = ta.getView(ViewNames.NER_CONLL).getConstituents();
        Assert.assertEquals(ner.size(), 12);  // location 1, org 5, person 6
        Assert.assertEquals(ner.get(1).getStartSpan(), 33);  // Knoxville
        Assert.assertEquals(ner.get(1).getEndSpan(), 34);
        Assert.assertEquals(ner.get(1).getLabel(), "LOC");

        List<Constituent> nerOntonotes = ta.getView(ViewNames.NER_ONTONOTES).getConstituents();
        Assert.assertEquals(nerOntonotes.size(), 13);  // date 1, location 1, org 5, person 6
        Assert.assertEquals(nerOntonotes.get(1).getStartSpan(), 33);  // Knoxville
        Assert.assertEquals(nerOntonotes.get(1).getEndSpan(), 34);
        Assert.assertEquals(nerOntonotes.get(1).getLabel(), "LOCATION");
    }
}
