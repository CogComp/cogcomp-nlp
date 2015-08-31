package edu.illinois.cs.cogcomp.comma.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.illinois.cs.cogcomp.comma.CommaProperties;
import edu.illinois.cs.cogcomp.comma.VivekAnnotationCommaParser;
import edu.illinois.cs.cogcomp.comma.VivekAnnotationCommaParser.Ordering;
import edu.illinois.cs.cogcomp.comma.Sentence;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

/**
 * Used for outputting the corpus with the label refinements for submission to EMPNLP
 */
public class OutputCorpusForEMNLP {
	public static void main(String[] args) throws IOException{
		String data = "";
		VivekAnnotationCommaParser commaParser = new VivekAnnotationCommaParser("data/comma_resolution_data.txt", CommaProperties.getInstance().getAllCommasSerialized(), VivekAnnotationCommaParser.Ordering.ORIGINAL_SENTENCE);
		List<Sentence> sentences = commaParser.getSentences();
		for(Sentence sentence : sentences){
			data += sentence.getId() + "\n" + sentence.getAnnotatedText()
					+ "\n\n";
		}
		FileUtils.writeStringToFile(new File("data/Comma_Labeled_Data.txt"), data);
	}
}
