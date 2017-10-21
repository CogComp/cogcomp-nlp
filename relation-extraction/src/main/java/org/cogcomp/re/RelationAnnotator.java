package org.cogcomp.re;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import org.cogcomp.md.MentionAnnotator;
import org.cogcomp.re.LbjGen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RelationAnnotator extends Annotator {

    private relation_classifier relationClassifier;
    private org.cogcomp.re.ACERelationConstrainedClassifier constrainedClassifier;

    public RelationAnnotator() {
        this(true);
    }


    public RelationAnnotator(boolean lazilyInitialize) {
        super("RELATION_EXTRACTION", new String[]{ViewNames.POS}, lazilyInitialize);
        relationClassifier = new relation_classifier();
        constrainedClassifier = new org.cogcomp.re.ACERelationConstrainedClassifier(relationClassifier);
    }

    @Override
    public void initialize(ResourceManager rm) {

    }

    @Override
    public void addView(TextAnnotation record) throws AnnotatorException {
        try {
            MentionAnnotator mentionAnnotator = new MentionAnnotator("ACE_TYPE");
            mentionAnnotator.addView(record);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        View mentionView = record.getView(ViewNames.MENTION);
        View relationView = new SpanLabelView(ViewNames.RELATION, RelationAnnotator.class.getCanonicalName(), record, 1.0f, true);
        for (int i = 0; i < record.getNumberOfSentences(); i++){
            Sentence curSentence = record.getSentence(i);
            List<Constituent> cins = mentionView.getConstituentsCoveringSpan(curSentence.getStartSpan(), curSentence.getEndSpan());
            for (int j = 0; j < cins.size(); j++){
                for (int k = j + 1; k < cins.size(); k++){
                    if (k == j) continue;
                    Constituent source = cins.get(j);
                    Constituent target = cins.get(k);
                    Relation for_test_forward = new Relation("PredictedRE", source, target, 1.0f);
                    Relation for_test_backward = new Relation("PredictedRE", target, source, 1.0f);
                    String tag_forward = constrainedClassifier.discreteValue(for_test_forward);
                    String tag_backward = constrainedClassifier.discreteValue(for_test_backward);
                    if (!tag_forward.equals("NOT_RELATED")){
                        Relation r = new Relation(tag_forward, source, target, 1.0f);
                        relationView.addRelation(r);
                    }
                    else if (!tag_backward.equals("NOT_RELATED")){
                        Relation r = new Relation(tag_backward, target, source, 1.0f);
                        relationView.addRelation(r);
                    }
                }
            }
        }
        record.addView(ViewNames.RELATION, relationView);
    }
}