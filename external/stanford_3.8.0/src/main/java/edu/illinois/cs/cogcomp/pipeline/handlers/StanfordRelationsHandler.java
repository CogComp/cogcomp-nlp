/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.handlers;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.stanford.nlp.ie.machinereading.structure.EntityMention;
import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations;
import edu.stanford.nlp.ie.machinereading.structure.RelationMention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

/**
 * Relation-mention detection with Stanford CoreNLP
 */
public class StanfordRelationsHandler extends Annotator {

    public static final String viewName = "STANFORD_RELATIONS";

    private StanfordCoreNLP pipeline = null;

    public StanfordRelationsHandler() {
        super(viewName, new String[]{}, true);
    }

    @Override
    public void initialize(ResourceManager rm) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,relation");
        this.pipeline = new StanfordCoreNLP(props);
    }

    @Override
    protected void addView(TextAnnotation ta) throws AnnotatorException {
        Annotation document = new Annotation(ta.text);
        pipeline.annotate(document);
        SpanLabelView vu = new SpanLabelView(viewName, ta);

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for(RelationMention rm : sentence.get(MachineReadingAnnotations.RelationMentionsAnnotation.class)) {
                if(rm.getType().equals("_NR")) continue;
                Map<String, Double> scores = new HashMap<>();
                for(String label : rm.getTypeProbabilities().keySet())
                    scores.put(label, rm.getTypeProbabilities().getCount(label));
                Constituent c1 = createConstituentGivenMention(rm.getEntityMentionArgs().get(0), ta);
                Constituent c2 = createConstituentGivenMention(rm.getEntityMentionArgs().get(1), ta);
                Relation r = new Relation(scores, c1, c2);
                vu.addRelation(r);
                if(!vu.containsConstituent(c1)) vu.addConstituent(c1);
                if(!vu.containsConstituent(c2)) vu.addConstituent(c2);
            }
        }

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (EntityMention rm : sentence.get(MachineReadingAnnotations.EntityMentionsAnnotation.class)) {
                Constituent c = createConstituentGivenMention(rm, ta);
                if(!vu.containsConstituent(c)) vu.addConstituent(c);
            }
        }

        ta.addView(viewName, vu);
    }

    /**
     * It gets the Stanford EntityMEntion and finds its equivalent CogComp Consittuent.
     * This mapping is done by comparing char offsets.
     */
    private static Constituent createConstituentGivenMention(EntityMention rm, TextAnnotation ta) {
        List<CoreLabel> tokens = rm.getSentence().get(CoreAnnotations.TokensAnnotation.class);
        int extentCharStart = tokens.get(rm.getExtentTokenStart()).beginPosition();
        int extentCharEnd = tokens.get(rm.getExtentTokenEnd()).endPosition();
        int headCharStart = tokens.get(rm.getHeadTokenStart()).beginPosition();
        int headCharEnd = tokens.get(rm.getHeadTokenEnd()).endPosition();

        List<Constituent> extentCons = ta.getView(ViewNames.TOKENS).getConstituentsOverlappingCharSpan(extentCharStart, extentCharEnd);
        int startIndex = extentCons.stream().min(Comparator.comparing(Constituent::getStartSpan)).get().getStartSpan();
        int endIndex = extentCons.stream().max(Comparator.comparing(Constituent::getEndSpan)).get().getEndSpan();

        List<Constituent> headCons = ta.getView(ViewNames.TOKENS).getConstituentsOverlappingCharSpan(headCharStart, headCharEnd);
        int startIndexHead = headCons.stream().min(Comparator.comparing(Constituent::getStartSpan)).get().getStartSpan();
        int endIndexHead = headCons.stream().max(Comparator.comparing(Constituent::getEndSpan)).get().getEndSpan();

        Constituent c = new Constituent(rm.getType(), viewName, ta, startIndex, endIndex);
        c.addAttribute("startIndexHead", String.valueOf(startIndexHead));
        c.addAttribute("endIndexHead", String.valueOf(endIndexHead));
        return c;
    }
}
