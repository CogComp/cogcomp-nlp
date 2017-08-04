package org.cogcomp.md;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
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
        try {
            ereMentionRelationReader = new EREMentionRelationReader(EREDocumentReader.EreCorpus.ENR3, "data/ere/data", false);
            MentionAnnotator mentionAnnotator = new MentionAnnotator();
            for (XmlTextAnnotation xta : ereMentionRelationReader) {
                TextAnnotation ta = xta.getTextAnnotation();
                ta.addView(posAnnotator);
                mentionAnnotator.addView(ta);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        test_basic_annotator();
    }
}
