package edu.illinois.cs.cogcomp.annotation.handler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
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
 * Wraps the Illinois Chunker (Shallow Parser) in an illinois-core-utilities Annotator
 * @author James Clarke, Mark Sammons
 *
 */
public class IllinoisChunkerHandler extends PipelineAnnotator
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

        super("Illinois Chunker", "0.3", "illinoischunker", ViewNames.SHALLOW_PARSE, new String[]{ ViewNames.POS });

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
    public void addView( TextAnnotation record ) throws AnnotatorException {
		if (!record.hasView(tokensfield) || !record.hasView(sentencesfield) || !record.hasView( posfield )) {
            String msg = getIdentifier() + ".getView(): Record must be tokenized, sentence split, and POS-tagged first.";
		    logger.error( msg );
        	throw new AnnotatorException( msg);
		}

		List<Constituent> tags = record.getView(posfield).getConstituents();
//		String rawText = record.getText();


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
			logger.debug("{} {}", lbjtoken.toString(), ( null == lbjtoken.type ) ? "NULL" : lbjtoken.type ) ;

            // what happens if we see an Inside tag -- even if it doesn't follow a Before tag
			if (null != lbjtoken.type && lbjtoken.type.charAt(0) == 'I') {
                if ( lbjtoken.type.length() < 3 )
                    throw new IllegalArgumentException("Chunker word label '" + lbjtoken.type + "' is too short!" );
				if ( null == clabel ) // we must have just seen an Outside tag and possibly completed a chunk
                {
                    // modify lbjToken.type for later ifs
					lbjtoken.type = "B" + lbjtoken.type.substring(1);
				}
                else if ( clabel.length() >= 3 && !clabel.equals(lbjtoken.type.substring(2) ) ) {
                    // trying to avoid mysterious null pointer exception...
                    lbjtoken.type = "B" + lbjtoken.type.substring(1);
                }
			}
			if ((lbjtoken.type.charAt(0) == 'B' || lbjtoken.type.charAt(0) == 'O') && clabel != null) {

				if (previous != null) {
					currentChunkEnd = previous.getEndSpan();
					Constituent label = new Constituent(clabel, ViewNames.SHALLOW_PARSE, record, currentChunkStart, currentChunkEnd );
					chunkView.addConstituent(label);
					clabel = null;
				} // else no chunk in progress (we are at the start of the doc)
			}

			if (lbjtoken.type.charAt(0) == 'B') {
				currentChunkStart = current.getStartSpan();
				clabel = lbjtoken.type.substring(2);
			}
			previous = current;
			tcounter++;
		}
		if (clabel != null && null != previous ) {
            currentChunkEnd = previous.getEndSpan();
            Constituent label = new Constituent(clabel, ViewNames.SHALLOW_PARSE, record, currentChunkStart, currentChunkEnd );
            chunkView.addConstituent(label);
		}
        record.addView( ViewNames.SHALLOW_PARSE, chunkView );

		return; // chunkView;
	}



}
