/**
 * 
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

/**
 * Read the ERE data and produce, in CoNLL format, gold standard 
 * data includeding named and nominal named entities, but excluding
 * pronouns.
 * @author redman
 */
public class ConvertEREToGoldStandard {

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        final String erelocation = 
                        "/Volumes/xdata/CCGStuff/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/data/source/";
        EREDocumentReader dr = new EREDocumentReader("ERE Corpora",  erelocation);
        int counter = 0;
        while(dr.hasNext()) {
            TextAnnotation ta = dr.next();
            SpanLabelView nerView = new SpanLabelView("NER-ERE", ta);

            System.out.println(ta.getCorpusId());
        }
    }

}
