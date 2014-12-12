package edu.illinois.cs.cogcomp.annotation.handler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.edison.data.curator.CuratorViewNames;
import edu.illinois.cs.cogcomp.lbj.chunk.Chunker;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.base.Span;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.thrift.labeler.Labeler;


/**
 * Wraps the Illinois Chunker (Shallow Parser) into a Labeler.Iface
 * @author James Clarke
 *
 */
public class IllinoisChunkerHandler extends IllinoisAbstractHandler implements Labeler.Iface 
{
	private final Logger logger = LoggerFactory.getLogger(IllinoisChunkerHandler.class);
	private Chunker tagger = new Chunker();
	private String posfield = CuratorViewNames.pos;
	private String tokensfield = CuratorViewNames.tokens;
	private String sentencesfield = CuratorViewNames.sentences;

	public IllinoisChunkerHandler() {
		this("");
	}
	
	public IllinoisChunkerHandler(String configFilename) {

        super("Illinois Chunker", "0.3", "illinoischunker");

		logger.info("Loading Chunker model..");
		tagger.discreteValue(new Token(new Word("The"), null, ""));
		logger.info("Chunker ready");
		// XXX If no configuration file is give use the default values from CuratorViewNames
		if (configFilename.trim().equals("")) {
			tokensfield = CuratorViewNames.tokens;
			sentencesfield = CuratorViewNames.sentences;
			posfield = CuratorViewNames.pos;
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
			tokensfield = config.getProperty("tokens.field", CuratorViewNames.tokens );
			sentencesfield = config.getProperty("sentences.field", CuratorViewNames.sentences );
			posfield = config.getProperty("pos.field", CuratorViewNames.pos );
		}
	}
	
	public Labeling labelRecord(Record record) throws AnnotationFailedException,
			TException {
		if (!record.getLabelViews().containsKey(tokensfield) && !record.getLabelViews().containsKey(sentencesfield)) {
			throw new TException("Record must be tokenized and sentence split first");
		}
		if (!record.getLabelViews().containsKey(posfield)) {
			throw new TException("Record must be POS tagged.");
		}
		long startTime = System.currentTimeMillis();
		
		List<Span> tags = record.getLabelViews().get(posfield).getLabels();
		String rawText = record.getRawText();

		logger.debug( "IllinoisChunkerHandler.labelRecord(): rawText is '" + rawText + "'" );

		List<Token> lbjTokens = recordToLBJTokensPos(record);
		Labeling labeling = new Labeling();

		List<Span> labels = new ArrayList<Span>();
		
		Span label = null;
		String clabel = "";
		Span previous = null;
		int tcounter = 0;
		for (int i = 0; i < lbjTokens.size(); i++) {
			Token lbjtoken = lbjTokens.get(i);
			Span current = tags.get(tcounter);
			tagger.discreteValue(lbjtoken);
			logger.debug("{} {}", lbjtoken.toString(), lbjtoken.type);
			if (lbjtoken.type.charAt(0) == 'I') {
				if (!clabel.equals(lbjtoken.type.substring(2))) {
					lbjtoken.type = "B" + lbjtoken.type.substring(1);
				}
			} 
			if ((lbjtoken.type.charAt(0) == 'B' || lbjtoken.type.charAt(0) == 'O') && label !=null) {
				label.setEnding(previous.getEnding());
				labels.add(label);
				label = null;
			}
			if (lbjtoken.type.charAt(0) == 'B') {
				label = new Span();
				label.setStart(current.getStart());
				clabel = lbjtoken.type.substring(2);
				label.setLabel(clabel);
			}
			previous = current;
			tcounter++;
		}
		if (label != null) {
			label.setEnding(previous.getEnding());
			labels.add(label);
		}
		labeling.setLabels(labels);
		labeling.setSource(getSourceIdentifier());

		long endTime = System.currentTimeMillis();
		logger.info("Tagged input in {}ms", endTime-startTime);
		return labeling;
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
	
	/**
	 * Converts a Record to LBJ Tokens with POS information.
	 * 
	 * @param record
	 * @return
	 */
	private List<Token> recordToLBJTokensPos(Record record) {
		List<Token> lbjTokens = new LinkedList<Token>();
		int j = 0;
		List<Span> tags = record.getLabelViews().get(posfield).getLabels();
		String rawText = record.getRawText();
		for (Span sentence : record.getLabelViews().get(sentencesfield).getLabels()) {
			Word wprevious = null;
			Token tprevious = null;
			boolean opendblquote = true;
			Span tag = null;
			do {
				tag = tags.get(j);
				Word wcurrent;
				String token = rawText.substring(tag.getStart(), tag.getEnding());
				if (token.equals("\"")) {
					token = opendblquote ? "``" : "''";
					opendblquote = !opendblquote;
				} else if (token.equals("(")) {
					token = "-LRB-";
				} else if (token.equals(")")) {
					token = "-RRB-";
				} else if (token.equals("{")) {
					token = "-LCB-";
				} else if (token.equals("}")) {
					token = "-RCB-";
				} else if (token.equals("[")) {
					token = "-LSB-";
				} else if (token.equals("]")) {
					token = "-RSB-";
				}
				wcurrent = new Word(token, wprevious);
				wcurrent.partOfSpeech = tag.getLabel();
				Token tcurrent = new Token(wcurrent, tprevious, "");
				lbjTokens.add(tcurrent);
				if (tprevious != null) {
					tprevious.next = tcurrent;
				}
				wprevious = wcurrent;
				tprevious = tcurrent;
				j++;
			} while (tag.getEnding() < sentence.getEnding());

		}
		return lbjTokens;
	}

}
