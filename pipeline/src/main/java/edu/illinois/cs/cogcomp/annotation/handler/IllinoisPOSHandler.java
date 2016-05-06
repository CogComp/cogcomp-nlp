package edu.illinois.cs.cogcomp.annotation.handler;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.lbj.pos.POSTagger;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Wraps the Illinois part-of-speech tagger in an illinois-core-utilites Annotator, to be a pipeline component.
 * @author James Clarke, Mark Sammons
 *
 */
public class IllinoisPOSHandler extends PipelineAnnotator
{
	
//	private static final String NAME = IllinoisPOSHandler.class.getCanonicalName();
    private final Logger logger = LoggerFactory.getLogger(IllinoisPOSHandler.class);
	private final POSTagger tagger = new POSTagger();
	private String tokensfield = "tokens";
	private String sentencesfield = "sentences";

	public IllinoisPOSHandler() 
	{
        super("Illinois Part-Of-Speech Tagger", "0.2", "illinoispos", ViewNames.POS, new String[]{} );

		logger.info("Loading POS model..");
		tagger.discreteValue(new Token(new Word("The"), null, ""));
		logger.info("POS Tagger ready");

        tokensfield = ViewNames.TOKENS;
        sentencesfield = ViewNames.SENTENCE;
	}
	

    /**
     *  annotates TextAnnotation with POS view and returns the new POS view.
     *
     * @param record    TextAnnotation to annotate
     */
    @Override
    public void addView(TextAnnotation record) throws AnnotatorException
    {
       	if (!record.hasView( tokensfield ) && !record.hasView(sentencesfield))
        {
			throw new AnnotatorException("Record must be tokenized and sentence split first");
		}
		long startTime = System.currentTimeMillis();
		List<Token> input = LBJavaUtils.recordToLBJTokens(record);


        List< Constituent > tokens = record.getView( ViewNames.TOKENS ).getConstituents();
		TokenLabelView posView = new TokenLabelView( ViewNames.POS, getAnnotatorName(), record, 1.0 );
		int tcounter = 0;
		for (Token lbjtoken : input) {
			tagger.discreteValue(lbjtoken);
			Constituent token = tokens.get(tcounter);
			Constituent label = new Constituent(tagger.discreteValue(lbjtoken), ViewNames.POS, record, token.getStartSpan(), token.getEndSpan());
			posView.addConstituent(label);
			tcounter++;
		}
		long endTime = System.currentTimeMillis();
		logger.debug("Tagged input in {}ms", endTime-startTime);

        record.addView( ViewNames.POS, posView );

		return;
	}





}
