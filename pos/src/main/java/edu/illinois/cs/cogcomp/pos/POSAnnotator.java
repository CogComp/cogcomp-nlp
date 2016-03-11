package edu.illinois.cs.cogcomp.pos;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Wraps the Illinois part-of-speech tagger in an illinois-core-utilites Annotator, to be a pipeline
 * component.
 * 
 * @author James Clarke, Mark Sammons
 *
 */
public class POSAnnotator extends Annotator {

    private static final String NAME = "illinoispos";
    private final Logger logger = LoggerFactory.getLogger(POSAnnotator.class);
    private final TrainedPOSTagger tagger = new TrainedPOSTagger();
    private String tokensfield = "tokens";
    private String sentencesfield = "sentences";

    public POSAnnotator() {
        super(ViewNames.POS, new String[0]);
        tokensfield = ViewNames.TOKENS;
        sentencesfield = ViewNames.SENTENCE;
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


    @Override
    public String getViewName() {
        return ViewNames.POS;
    }



    /**
     * Can be used internally by {@link edu.illinois.cs.cogcomp.annotation.AnnotatorService} to
     * check for pre-requisites before calling any single (external)
     * {@link Annotator}.
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
}
