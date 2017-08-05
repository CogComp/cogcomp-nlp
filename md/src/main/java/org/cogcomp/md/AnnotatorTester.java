/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package org.cogcomp.md;

import org.cogcomp.md.LbjGen.*;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREMentionRelationReader;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;

/**
 * The testing class for MentionAnnotator
 * Validating if everything is working as expected
 */
public class AnnotatorTester {
    public static void test_basic_annotator(){
        EREMentionRelationReader ereMentionRelationReader = null;
        POSAnnotator posAnnotator = new POSAnnotator();
        int total_labeled = 0;
        int total_predicted = 0;
        int total_correct = 0;
        try {
            ereMentionRelationReader = new EREMentionRelationReader(EREDocumentReader.EreCorpus.ENR3, "data/ere/data", false);
            MentionAnnotator mentionAnnotator = new MentionAnnotator();
            for (XmlTextAnnotation xta : ereMentionRelationReader) {
                TextAnnotation ta = xta.getTextAnnotation();
                ta.addView(posAnnotator);
                mentionAnnotator.addView(ta);
                total_labeled += ta.getView(ViewNames.MENTION_ERE).getNumberOfConstituents();
                total_predicted += ta.getView(ViewNames.MENTION).getNumberOfConstituents();
                for (Constituent pc : ta.getView(ViewNames.MENTION).getConstituents()){
                    for (Constituent gc : ta.getView(ViewNames.MENTION_ERE).getConstituents()){
                        Constituent gch = ACEReader.getEntityHeadForConstituent(gc, ta, "B");
                        if (gch == null){
                            continue;
                        }
                        if (Integer.parseInt(pc.getAttribute("EntityHeadStartSpan")) == gch.getStartSpan() &&
                                Integer.parseInt(pc.getAttribute("EntityHeadEndSpan")) == gch.getEndSpan()){
                            total_correct ++;
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("Labeled: " + total_labeled);
        System.out.println("Predicted: " + total_predicted);
        System.out.println("Correct: " + total_correct);
    }
    public static void main(String[] args){
        test_basic_annotator();
    }
}
