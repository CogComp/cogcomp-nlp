package edu.illinois.cs.cogcomp.annotation.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;

import edu.illinois.cs.cogcomp.lbjava.nlp.Sentence;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.thrift.labeler.MultiLabeler;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.base.Span;

/**
 * Thrift Service for Illinois Tokenizer (built into Learning Based Java).
 * Implements the MultiLabeler service where first Labeling are the sentence
 * boundaries and the second Labeling the token boundaries.
 * 
 * @author James Clarke
 * 
 */
@Deprecated
public class IllinoisTokenizerHandler extends IllinoisAbstractHandler implements MultiLabeler.Iface {

    public IllinoisTokenizerHandler(){
        super("Illinois Tokenizer", "0.4", "illinoistokenizer");
    }
    
	public List<Labeling> labelRecord(Record record)
			throws AnnotationFailedException, TException {
		SentenceSplitter splitter = new SentenceSplitter(record.getRawText()
				.split("\n"));

		List<Span> tokens = new ArrayList<Span>();
		List<Span> sentences = new ArrayList<Span>();
		for (Sentence s : splitter.splitAll()) {
			LinkedVector words = s.wordSplit();
			if (s.end >= record.getRawText().length()) {
				throw new AnnotationFailedException("Error in tokenizer, sentence end greater than rawtext length");
			}
			for (int i = 0; i < words.size(); i++) {
				Word word = (Word) words.get(i);
				Span token = new Span();
				token.setStart(word.start);
				token.setEnding(word.end + 1);
				tokens.add(token);
			}
			Span span = new Span();
			span.setStart(s.start);
			span.setEnding(s.end + 1);
			sentences.add(span);
		}

		List<Labeling> result = new ArrayList<Labeling>();
		Labeling tokenLabeling = new Labeling();
		tokenLabeling.setLabels(tokens);
		tokenLabeling.setSource(getSourceIdentifier());
		Labeling sentenceLabeling = new Labeling();
		sentenceLabeling.setLabels(sentences);
		sentenceLabeling.setSource(getSourceIdentifier());
		result.add(sentenceLabeling);
		result.add(tokenLabeling);
		return result;
	}


}
