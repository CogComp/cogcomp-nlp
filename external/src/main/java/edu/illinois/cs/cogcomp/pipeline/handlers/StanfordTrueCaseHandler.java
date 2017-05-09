/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pipeline.handlers;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.TrueCaseAnnotator;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

/**
 * Recognizes the true case of tokens in text where this information was lost, e.g., all upper case text.
 */
public class StanfordTrueCaseHandler extends Annotator {

    public static final String viewName = "STANFORD_TRUE_CASE";

    StanfordCoreNLP pipeline = null;

    public StanfordTrueCaseHandler() {
        super(viewName, new String[]{}, true);
    }

    @Override
    public void initialize(ResourceManager rm) {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, truecase");
        props.put("truecase.model", DefaultPaths.DEFAULT_TRUECASE_MODEL);
        props.put("truecase.bias", TrueCaseAnnotator.DEFAULT_MODEL_BIAS);
        props.put("truecase.mixedcasefile", DefaultPaths.DEFAULT_TRUECASE_DISAMBIGUATION_LIST);

        this.pipeline = new StanfordCoreNLP(props);
    }

    @Override
    protected void addView(TextAnnotation ta) throws AnnotatorException {
        Annotation document = new Annotation(ta.text);
        pipeline.annotate(document);
        SpanLabelView vu = new SpanLabelView(viewName, ta);

        String outputString = "";

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String truecase = token.get(CoreAnnotations.TrueCaseTextAnnotation.class);
                outputString += " " + truecase;
            }
        }
        Constituent c = new Constituent(outputString, viewName, ta,0, ta.getTokens().length);
        vu.addConstituent(c);
        ta.addView(viewName, vu);
    }
}