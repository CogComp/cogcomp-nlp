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
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.CoreferenceView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

/**
 * Do co-reference with Stanford CoreNLP
 */
public class StanfordCorefHandler extends Annotator {

    public static final String viewName = "STANFORD_COREF";

    StanfordCoreNLP pipeline = null;

    public StanfordCorefHandler() {
        super(viewName, new String[]{}, true);
    }

    @Override
    public void initialize(ResourceManager rm) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,coref");
        this.pipeline = new StanfordCoreNLP(props);
    }

    @Override
    protected void addView(TextAnnotation ta) throws AnnotatorException {
        Annotation document = new Annotation(ta.text);
        pipeline.annotate(document);
        CoreferenceView vu = new CoreferenceView(viewName, ta);

        Map corefChain = document.get(CorefCoreAnnotations.CorefChainAnnotation.class);
        for (Object key : corefChain.keySet()) {
            CorefChain chain = (CorefChain) corefChain.get(key);
            Constituent representative = createConstituentGivenMention(document,chain,chain.getRepresentativeMention(), ta);
            List<Constituent> consList = new ArrayList<>();
            for(CorefChain.CorefMention m : chain.getMentionsInTextualOrder()) {
                consList.add(createConstituentGivenMention(document, chain, m, ta));
            }
            consList.remove(representative); // remove the representative itself
            vu.addCorefEdges(representative, consList);
        }
        ta.addView(viewName, vu);
    }

    /**
     * Given the information from a CorefMention determine the byte offsets
     * of the whole mention and return as a knowitall Interval.
     */
    private static Pair<Integer, Integer> getCharIntervalFromCorefMention(Annotation document, Integer sentNum, Integer startIndex, Integer endIndex){
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        CoreMap sentence = sentences.get(sentNum-1);
        List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
        List<CoreLabel> spanningTokens = new ArrayList<>();
        for(int i = startIndex; i < endIndex; i++)
            spanningTokens.add(tokens.get(i-1));
        return new Pair<>(spanningTokens.get(0).beginPosition(),spanningTokens.get(spanningTokens.size()-1).endPosition());
    }

    private static Constituent createConstituentGivenMention(Annotation document, CorefChain chain, CorefChain.CorefMention m, TextAnnotation ta) {
        Pair<Integer, Integer> mentionCharSpan = getCharIntervalFromCorefMention(document, m.sentNum, m.startIndex, m.endIndex);
        List<Constituent> overlappingCons = ta.getView(ViewNames.TOKENS).getConstituentsOverlappingCharSpan(mentionCharSpan.getFirst(), mentionCharSpan.getSecond());
        int startIndex = overlappingCons.stream().min(Comparator.comparing(Constituent::getStartSpan)).get().getStartSpan();
        int endIndex = overlappingCons.stream().max(Comparator.comparing(Constituent::getEndSpan)).get().getEndSpan();
        Constituent c = new Constituent(String.valueOf(chain.getChainID()), viewName, ta, startIndex, endIndex);
        c.addAttribute("animacy", m.animacy.toString());
        c.addAttribute("number", m.number.toString());
        c.addAttribute("gender", m.gender.toString());
        c.addAttribute("mentionType", m.mentionType.toString());
        return c;
    }
}
