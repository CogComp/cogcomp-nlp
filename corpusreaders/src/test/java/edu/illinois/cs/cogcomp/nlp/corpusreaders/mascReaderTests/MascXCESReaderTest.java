/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
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

/**
 * Please change CORPUS_DIRECTORY if your MASC XCES XML files are stored elsewhere
 */
public class MascXCESReaderTest {
    private static final String CORPUS_DIRECTORY = "/shared/corpora/corporaWeb/written/eng/MASC-3.0.0/xces";

    @Test
    public void testCreateTextAnnotation() throws Exception {
        Logger.getLogger(MascXCESReader.class).setLevel(Level.INFO);

        MascXCESReader cnr = new MascXCESReader("", Paths.get(CORPUS_DIRECTORY, "written/twitter").toString(), ".xml");

        TextAnnotation ta = cnr.next();

        List<Constituent> tokens = ta.getView(ViewNames.TOKENS).getConstituents();
        Assert.assertEquals(tokens.size(), 13389);  // tok 13390, clean tok 13389
        Assert.assertEquals(tokens.get(1).toString(), "setting");

        List<Constituent> lemma = ta.getView(ViewNames.LEMMA).getConstituents();
        Assert.assertEquals(lemma.size(), 13375);  // base= 13376, clean tok with base= 13375
        Assert.assertEquals(lemma.get(1).getLabel(), "set");

        List<Constituent> pos = ta.getView(ViewNames.POS).getConstituents();
        Assert.assertEquals(pos.size(), 13389);  // msd= 13390, clean tok with msd= 13389
        Assert.assertEquals(pos.get(1).getLabel(), "VBG");

        List<Constituent> sentences = ta.getView(ViewNames.SENTENCE_GOLD).getConstituents();
        Assert.assertEquals(sentences.size(), 1046);  // s 1046
        Assert.assertEquals(sentences.get(0).getStartSpan(), 0);
        Assert.assertEquals(sentences.get(0).getEndSpan(), 25);

        List<Constituent> shallowParse = ta.getView(ViewNames.SHALLOW_PARSE).getConstituents();
        Assert.assertEquals(shallowParse.size(), 5504);  // nchunk 3551, vchunk 1953
        Assert.assertEquals(shallowParse.get(0).getStartSpan(), 1);
        Assert.assertEquals(shallowParse.get(0).getEndSpan(), 2);
        Assert.assertEquals(shallowParse.get(0).getLabel(), "VP");

        List<Constituent> ner = ta.getView(ViewNames.NER_CONLL).getConstituents();
        Assert.assertEquals(ner.size(), 288);  // location 93, org 112, person 83
        Assert.assertEquals(ner.get(0).getStartSpan(), 379);  // Singapore
        Assert.assertEquals(ner.get(0).getEndSpan(), 380);
        Assert.assertEquals(ner.get(0).getLabel(), "LOC");

        List<Constituent> nerOntonotes = ta.getView(ViewNames.NER_ONTONOTES).getConstituents();
        Assert.assertEquals(nerOntonotes.size(), 408);  // date 120, location 93, org 112, person 83
        Assert.assertEquals(nerOntonotes.get(3).getStartSpan(), 379);  // Singapore
        Assert.assertEquals(nerOntonotes.get(3).getEndSpan(), 380);
        Assert.assertEquals(nerOntonotes.get(3).getLabel(), "LOCATION");

        Assert.assertTrue(cnr.hasNext());
        cnr.next();

        Assert.assertFalse(cnr.hasNext());
    }
}
