package edu.illinois.cs.cogcomp.annotation.handler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbj.chunk.Chunker;


/**
 * Wraps the Illinois Chunker (Shallow Parser) into a Labeler.Iface
 * @author James Clarke
 *
 */
public class IllinoisChunkerHandler extends PipelineAnnotator implements Annotator
{
	private final Logger logger = LoggerFactory.getLogger(IllinoisChunkerHandler.class);
	private Chunker tagger = new Chunker();
	private String posfield = ViewNames.POS;
	private String tokensfield = ViewNames.TOKENS;
	private String sentencesfield = ViewNames.SENTENCE;


    public IllinoisChunkerHandler()
    {
        this( "" );
    }

	public IllinoisChunkerHandler(String configFilename) {

        super("Illinois Chunker", "0.3", "illinoischunker");

		logger.info("Loading Chunker model..");
		tagger.discreteValue(new Token(new Word("The"), null, ""));
		logger.info("Chunker ready");
		// XXX If no configuration file is give use the default values from ViewNames
		if (configFilename.trim().equals("")) {
			tokensfield = ViewNames.TOKENS;
			sentencesfield = ViewNames.SENTENCE;
			posfield = ViewNames.POS;
		}
		else {
			Properties config = new Properties();
			try {
	            FileInputStream in = new FileInputStream(configFilename);
	            config.load(new BufferedInputStream(in));
	            in.close();
	        } catch (IOException e) {
	        	logger.warn("Error reading configuration file. {}", configFilename);
	        }
			tokensfield = config.getProperty("tokens.field", ViewNames.TOKENS );
			sentencesfield = config.getProperty("sentences.field", ViewNames.SENTENCE );
			posfield = config.getProperty("pos.field", ViewNames.POS );
		}
	}


    @Override
    public String getViewName() {
        return ViewNames.SHALLOW_PARSE;
    }

    @Override
    public View getView( TextAnnotation record ) throws AnnotatorException {
		if (!record.hasView(tokensfield) || !record.hasView(sentencesfield)) {
            String msg = getIdentifier() + ".getView(): Record must be tokenized and sentence split first.";
		    logger.error( msg );
        	throw new AnnotatorException( msg);
		}

		List<Constituent> tags = record.getView(posfield).getConstituents();
		String rawText = record.getText();


		List<Token> lbjTokens = LBJavaUtils.recordToLBJTokens( record );

        View chunkView = new SpanLabelView( ViewNames.SHALLOW_PARSE, this.getIdentifier(), record, 1.0 );

        int currentChunkStart = 0;
        int currentChunkEnd = 0;

		String clabel = "";
		Constituent previous = null;
		int tcounter = 0;
		for (Token lbjtoken : lbjTokens) {
			Constituent current = tags.get(tcounter);
			tagger.discreteValue(lbjtoken);
			logger.debug("{} {}", lbjtoken.toString(), lbjtoken.type);
			if (lbjtoken.type.charAt(0) == 'I') {
				if (!clabel.equals(lbjtoken.type.substring(2))) {
					lbjtoken.type = "B" + lbjtoken.type.substring(1);
				}
			}
			if ((lbjtoken.type.charAt(0) == 'B' || lbjtoken.type.charAt(0) == 'O') && clabel != null) {

                currentChunkEnd = previous.getEndSpan();
                Constituent label = new Constituent(clabel, ViewNames.SHALLOW_PARSE, record, currentChunkStart, currentChunkEnd );
				chunkView.addConstituent(label);
                clabel = null;
			}

			if (lbjtoken.type.charAt(0) == 'B') {
				currentChunkStart = current.getStartSpan();
				clabel = lbjtoken.type.substring(2);
			}
			previous = current;
			tcounter++;
		}
		if (clabel != null) {
            currentChunkEnd = previous.getEndSpan();
            Constituent label = new Constituent(clabel, ViewNames.SHALLOW_PARSE, record, currentChunkStart, currentChunkEnd );
            chunkView.addConstituent(label);
		}
        record.addView( ViewNames.SHALLOW_PARSE, chunkView );

		return chunkView;
	}

    /**
     * Can be used internally by {@link edu.illinois.cs.cogcomp.annotation.CachingAnnotatorService} to check for pre-requisites before calling
     * any single (external) {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator}.
     *
     * @return The list of {@link edu.illinois.cs.cogcomp.core.datastructures.ViewNames} required by this ViewGenerator
     */
    @Override
    public String[] getRequiredViews() {
        return new String[0];
    }

//	public String getName() throws TException {
//		return "Illinois Chunker";
//	}
//
//	public String getVersion() throws TException {
//		return "0.3";
//	}
//
//	public boolean ping() throws TException {
//		return true;
//	}
//
//	public String getSourceIdentifier() throws TException {
//		return "illinoischunker-"+getVersion();
//	}
	

}
