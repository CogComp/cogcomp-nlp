package edu.illinois.cs.cogcomp.comma;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

public class CommaParserTest  extends TestCase{
	private String serializedDataFileName = CommaProperties.getInstance().getAllCommasSerialized();

	@Test
	public void testOrderOfCommasInOrderedSentenceOrderCommaParser() {
		Parser orderedSentenceOrderCommaParser = new VivekAnnotationCommaParser("data/comma_resolution_data.txt", serializedDataFileName, VivekAnnotationCommaParser.Ordering.RANDOM_SENTENCE);
		Set<Sentence> seenSentences = new HashSet<>();
		Comma comma = (Comma) orderedSentenceOrderCommaParser.next();
		Sentence previousSentence = comma.getSentence();
		seenSentences.add(comma.getSentence());
		for(comma = (Comma) orderedSentenceOrderCommaParser.next(); comma!=null; comma = (Comma) orderedSentenceOrderCommaParser.next()){
			if(comma.getSentence() != previousSentence){
				assertEquals(false, seenSentences.contains(comma.getSentence()));
				seenSentences.add(comma.getSentence());
				previousSentence = comma.getSentence();
			}
		}
	}
	
	@Test
	public void testOrderOfCommasInRandomSentenceOrderCommaParser(){
		Parser randomSentenceOrderCommaParser = new VivekAnnotationCommaParser("data/comma_resolution_data.txt", serializedDataFileName, VivekAnnotationCommaParser.Ordering.ORDERED_SENTENCE);
		Set<Sentence> seenSentences = new HashSet<>();
		Comma comma = (Comma) randomSentenceOrderCommaParser.next();
		Sentence previousSentence = comma.getSentence();
		seenSentences.add(comma.getSentence());
		for(comma = (Comma) randomSentenceOrderCommaParser.next(); comma!=null; comma = (Comma) randomSentenceOrderCommaParser.next()){
			if(comma.getSentence() != previousSentence){
				assertEquals(false, seenSentences.contains(comma.getSentence()));
				seenSentences.add(comma.getSentence());
				previousSentence = comma.getSentence();
			}
		}
	}

}
