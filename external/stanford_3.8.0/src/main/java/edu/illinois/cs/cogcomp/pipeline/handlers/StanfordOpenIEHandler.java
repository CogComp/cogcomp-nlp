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
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

import java.util.*;

/**
 * Do openIE with Stanford CoreNLP
 * @author khashab2
 */
public class StanfordOpenIEHandler extends Annotator {

    public static final String viewName = "STANFORD_OPENIE";

    StanfordCoreNLP pipeline = null;

    public StanfordOpenIEHandler() {
        super(viewName, new String[]{}, true);
    }

    @Override
    public void initialize(ResourceManager rm) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,natlog,openie");
        this.pipeline = new StanfordCoreNLP(props);
    }

    @Override
    protected void addView(TextAnnotation ta) throws AnnotatorException {
        Annotation document = new Annotation(ta.text);
        pipeline.annotate(document);
        SpanLabelView vu = new SpanLabelView(viewName, ta);
        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
            for (RelationTriple triple : triples) {
                Constituent subject = getConstituent(triple.subjectGloss(), triple.subjectTokenSpan(), sentence, ta);
                subject.addAttribute("subjectGloss", triple.subjectGloss());
                subject.addAttribute("subjectLemmaGloss", triple.subjectLemmaGloss());
                subject.addAttribute("subjectLink", triple.subjectLink());
                Constituent object = getConstituent(triple.objectGloss(), triple.objectTokenSpan(), sentence, ta);
                object.addAttribute("objectGloss", triple.objectGloss());
                object.addAttribute("objectLemmaGloss", triple.objectLemmaGloss());
                object.addAttribute("objectLink", triple.objectLink());
                Constituent relation = getConstituent(triple.relationGloss(), triple.relationTokenSpan(), sentence, ta);
                relation.addAttribute("relationGloss", triple.relationGloss());
                relation.addAttribute("relationLemmaGloss", triple.relationLemmaGloss());
                Relation subj = new Relation("subj", relation, subject, triple.confidence);
                Relation obj = new Relation("obj", relation, object, triple.confidence);
                vu.addRelation(subj);
                vu.addRelation(obj);
                vu.addConstituent(subject);
                vu.addConstituent(object);
                vu.addConstituent(relation);
            }
        }
        ta.addView(viewName, vu);
    }

    private static Constituent getConstituent(String label, Pair<Integer, Integer> tokenSpan, CoreMap sentence, TextAnnotation ta) {
        List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
        int extentCharStart = tokens.get(tokenSpan.first()).beginPosition();
        int extentCharEnd = tokens.get(tokenSpan.second()).endPosition() - 1;

        List<Constituent> extentCons = ta.getView(ViewNames.TOKENS).getConstituentsOverlappingCharSpan(extentCharStart, extentCharEnd);
        int startIndex = extentCons.stream().min(Comparator.comparing(Constituent::getStartSpan)).get().getStartSpan();
        int endIndex = extentCons.stream().max(Comparator.comparing(Constituent::getEndSpan)).get().getEndSpan();

        return new Constituent(label, viewName, ta, startIndex, endIndex);
    }
}
