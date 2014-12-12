package edu.illinois.cs.cogcomp.annotation.handler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.common.CuratorViewNames;
import edu.illinois.cs.cogcomp.lbj.pos.POSTagger;
import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.base.Span;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.thrift.labeler.Labeler;

/**
 * Wraps the Illinois part-of-speech tagger in a Labeler.Iface.
 * @author James Clarke
 *
 */
public class IllinoisPOSHandler extends IllinoisAbstractHandler implements Labeler.Iface {
	
//	private static final String NAME = IllinoisPOSHandler.class.getCanonicalName();
    private final Logger logger = LoggerFactory.getLogger(IllinoisPOSHandler.class);
	private final POSTagger tagger = new POSTagger();
	private String tokensfield = "tokens";
	private String sentencesfield = "sentences";

	public IllinoisPOSHandler() 
	{
        super("Illinois Part-Of-Speech Tagger", "0.2", "illinoispos");

		logger.info("Loading POS model..");
		tagger.discreteValue(new Token(new Word("The"), null, ""));
		logger.info("POS Tagger ready");

        tokensfield = CuratorViewNames.tokens;
        sentencesfield = CuratorViewNames.sentences;
	}
	
	public Labeling labelRecord(Record record) throws TException {
		if (!record.getLabelViews().containsKey(tokensfield) && !record.getLabelViews().containsKey(sentencesfield)) {
			throw new TException("Record must be tokenized and sentence split first");
		}
		long startTime = System.currentTimeMillis();
		List<Token> input = recordToLBJTokens(record);
		List<Span> tokens = record.getLabelViews().get(tokensfield).getLabels();

		Labeling labeling = new Labeling();

		List<Span> labels = new ArrayList<Span>();
		
		int tcounter = 0;
		for (int i = 0; i < input.size(); i++) {
			Token lbjtoken = input.get(i);
			tagger.discreteValue(lbjtoken);
			Span token = tokens.get(tcounter);
			Span label = new Span();
			label.setLabel(lbjtoken.partOfSpeech);
			label.setStart(token.getStart());
			label.setEnding(token.getEnding());
			labels.add(label);
			tcounter++;
		}
		labeling.setLabels(labels);
		labeling.setSource( CuratorViewNames.pos );
		long endTime = System.currentTimeMillis();
		logger.debug("Tagged input in {}ms", endTime-startTime);
		return labeling;
	}


	
	/**
	 * Converts a record into LBJ Tokens for use with LBJ classifiers.
	 * 
	 * @param record
	 * @return
	 */
	private List<Token> recordToLBJTokens(Record record) {
		List<Token> lbjTokens = new LinkedList<Token>();
		List<List<String>> sentences = tokensAsStrings(record.getLabelViews().get(tokensfield)
				.getLabels(), record.getLabelViews().get(sentencesfield).getLabels(), record
				.getRawText());

		for (List<String> sentence : sentences) {
			boolean opendblquote = true;
			Word wprevious = null;
			Token tprevious = null;
			for (String token : sentence) {
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

				Word wcurrent;
				wcurrent = new Word(token, wprevious);
				Token tcurrent = new Token(wcurrent, tprevious, "");
				lbjTokens.add(tcurrent);
				if (tprevious != null) {
					tprevious.next = tcurrent;
				}
				wprevious = wcurrent;
				tprevious = tcurrent;
			}
		}
		return lbjTokens;
	}
	
	/**
	 * Converts sentences and tokens represented as spans into a list of lists
	 * of string.
	 * 
	 * @param tokens
	 * @param sentences
	 * @param rawText
	 * @return
	 */
	private static List<List<String>> tokensAsStrings(List<Span> tokens,
			List<Span> sentences, String rawText) {
		List<List<String>> strTokens = new ArrayList<List<String>>();
		int sentNum = 0;
		Span sentence = sentences.get(sentNum);
		strTokens.add(new ArrayList<String>());
		for (Span token : tokens) {
			if (token.getStart() >= sentence.getEnding()) {
				strTokens.add(new ArrayList<String>());
				sentNum++;
				sentence = sentences.get(sentNum);
			}
			strTokens.get(sentNum).add(
					rawText.substring(token.getStart(), token.getEnding()));
		}
		return strTokens;
	}

}
