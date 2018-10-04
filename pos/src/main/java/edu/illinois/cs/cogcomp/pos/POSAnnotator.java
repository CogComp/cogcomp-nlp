/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pos;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;

import edu.illinois.cs.cogcomp.pos.lbjava.POSTagger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Wraps the Illinois part-of-speech tagger in an illinois-core-utilites Annotator, to be a pipeline
 * component.
 * 
 * @author James Clarke, Mark Sammons
 *
 */
public class POSAnnotator extends Annotator {

    /**
     * NOTE: if you assign values here, these fields are initialized AFTER THE CONSTRUCTOR!!
     *    Therefore you CANNOT log messages in the constructor.
     */
    private static final String NAME = POSAnnotator.class.getCanonicalName();
    private final Logger logger = LoggerFactory.getLogger(POSAnnotator.class);
    private POSTagger tagger;
    private String tokensfield;// = "tokens";
    private String sentencesfield;// = "sentences";

    /**
     * lazily initialize by default.
     */
    public POSAnnotator() {
        this(true);
    }

    /**
     * Constructor allowing choice whether or not to lazily initialize.
     * 
     * @param lazilyInitialize if 'true', load models only on first call to
     *        {@link Annotator#getView(TextAnnotation)}
     */
    public POSAnnotator(boolean lazilyInitialize) {
        super(ViewNames.POS, new String[0], lazilyInitialize);
        tokensfield = ViewNames.TOKENS;
        sentencesfield = ViewNames.SENTENCE;
    }


    /**
     * called by superclass either on instantiation, or on first call to getView().
     * 
     * @param rm configuration parameters
     */
    @Override
    public void initialize(ResourceManager rm) {
        tagger = new POSTagger();
    }

    /**
     * annotates TextAnnotation with POS view and adds it to the TextAnnotation.
     *
     * @param record TextAnnotation to annotate
     */
    @Override
    public void addView(TextAnnotation record) throws AnnotatorException {
        if (!record.hasView(tokensfield) && !record.hasView(sentencesfield)) {
            throw new AnnotatorException("Record must be tokenized and sentence split first");
        }

        long startTime = System.currentTimeMillis();
        List<Token> input = LBJavaUtils.recordToLBJTokens(record);


        List<Constituent> tokens = record.getView(ViewNames.TOKENS).getConstituents();
        TokenLabelView posView = new TokenLabelView(ViewNames.POS, getAnnotatorName(), record, 1.0);
        int tcounter = 0;
        for (Token lbjtoken : input) {
            tagger.discreteValue(lbjtoken);
            Constituent token = tokens.get(tcounter);
            Constituent label =
                    new Constituent(tagger.discreteValue(lbjtoken), ViewNames.POS, record,
                            token.getStartSpan(), token.getEndSpan());
            posView.addConstituent(label);
            tcounter++;
        }
        long endTime = System.currentTimeMillis();
        logger.debug("Tagged input in {}ms", endTime - startTime);

        record.addView(ViewNames.POS, posView);

    }



    /**
     * Can be used internally by {@link edu.illinois.cs.cogcomp.annotation.AnnotatorService} to
     * check for pre-requisites before calling any single (external) {@link Annotator}.
     *
     * @return The list of {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames} required by
     *         this ViewGenerator
     */
    @Override
    public String[] getRequiredViews() {
        return new String[0];
    }

    public String getAnnotatorName() {
        return NAME;
    }

    /**
     * Return possible tag values that the POSAnnotator can produce.
     *
     * @return the set of string representing the tag values
     */
    @Override
    public Set<String> getTagValues() {
        if (!isInitialized()) {
            doInitialize();
        }
        return tagger.getTagValues();
    }
}
