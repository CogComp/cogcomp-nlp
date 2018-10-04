/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.md.tests;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREMentionRelationReader;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import org.cogcomp.md.BIOReader;
import org.cogcomp.md.BIOTester;
import org.cogcomp.md.MentionAnnotator;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Testing class for Mention Detection
 */
public class MentionDetectionTest {
    @Test
    public void testHeadInference(){
        EREMentionRelationReader ereMentionRelationReader = null;
        try {
            String path = "src/test/resources/ERE";
            ereMentionRelationReader = new EREMentionRelationReader(EREDocumentReader.EreCorpus.ENR3, path, false);
            POSAnnotator posAnnotator = new POSAnnotator();
            MentionAnnotator mentionAnnotator = new MentionAnnotator();
            for (XmlTextAnnotation xta : ereMentionRelationReader){
                TextAnnotation ta = xta.getTextAnnotation();
                ta.addView(posAnnotator);
                mentionAnnotator.addView(ta);
                if (ta.getView("MENTION").getNumberOfConstituents() < 60){
                    fail("Mention Head predicted performance dropped");
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testHeadTrain(){
        String path = "src/test/resources/ERE";
        BIOReader bioReader = new BIOReader(path, "ERE-TRAIN", "NAM", false);
        assertNotNull(BIOTester.train_nam_classifier(bioReader, "src/test/resources/tmp"));
    }
}
