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


/*
 * Class RelationAnnotator
 * Generates View "RELATION_EXTRACTION"
 * This is a annotator that currently relies on gold mentions from MENTION_ACE view
 * It reads all the pairs of mentions in all the sentences in the TextAnnotation
 * For each pair, the annotator predicts its relation label
 * If the label is not NULL, the annotator puts the relation to the generating new view
 * For the new relations, attribute "RelationSubtype" is set
 */

public class RelationAnnotator extends Annotator {

    private static final String NAME = RelationAnnotator.class.getCanonicalName();
    private final Logger logger = LoggerFactory.getLogger(RelationAnnotator.class);
    private relation_classifier relationClassifier;
    private org.cogcomp.re.ACERelationConstrainedClassifier constrainedClassifier;

    private static Constituent getEntityHeadForConstituent(Constituent extentConstituent,
                                                           TextAnnotation textAnnotation,
                                                           String viewName) {
        int startCharOffset =
                Integer.parseInt(extentConstituent
                        .getAttribute(ACEReader.EntityHeadStartCharOffset));
        int endCharOffset =
                Integer.parseInt(extentConstituent.getAttribute(ACEReader.EntityHeadEndCharOffset)) - 1;
        int startToken = textAnnotation.getTokenIdFromCharacterOffset(startCharOffset);
        int endToken = textAnnotation.getTokenIdFromCharacterOffset(endCharOffset);
        if (startToken >= 0 && endToken >= 0 && !(endToken - startToken < 0)) {
            Constituent cons =
                    new Constituent(extentConstituent.getLabel(), 1.0, viewName, textAnnotation,
                            startToken, endToken + 1);
            for (String attributeKey : extentConstituent.getAttributeKeys()) {
                cons.addAttribute(attributeKey, extentConstituent.getAttribute(attributeKey));
            }
            return cons;
        }
        return null;
    }

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
        View relationView = new SpanLabelView("RELATION_EXTRACTION_RELATIONS", RelationAnnotator.class.getCanonicalName(), record, 1.0f, true);
        for (int i = 0; i < record.getNumberOfSentences(); i++){
            Sentence curSentence = record.getSentence(i);
            List<Constituent> cins = mentionView.getConstituentsCoveringSpan(curSentence.getStartSpan(), curSentence.getEndSpan());
            for (int j = 0; j < cins.size(); j++){
                for (int k = 0; k < cins.size(); k++){
                    if (k == j) continue;
                    Constituent source = cins.get(j);
                    Constituent target = cins.get(k);
                    Relation for_test = new Relation("PredictedRE", source, target, 1.0f);
                    String tag = constrainedClassifier.discreteValue(for_test);
                    if (tag.equals("NOT_RELATED") == false){
                        Relation newRelation = new Relation(tag, source, target, 1.0f);
                        newRelation.addAttribute("RelationSubtype", tag);
                        relationView.addRelation(newRelation);
                    }
                }
            }
        }
        record.addView("RELATION_EXTRACTION_MENTIONS", mentionView);
        record.addView("RELATION_EXTRACTION_RELATIONS", relationView);
    }
}