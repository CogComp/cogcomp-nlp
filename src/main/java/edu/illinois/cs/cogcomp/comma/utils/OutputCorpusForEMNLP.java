package edu.illinois.cs.cogcomp.comma.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.comma.datastructures.Sentence;
import edu.illinois.cs.cogcomp.comma.readers.VivekAnnotationReader;

/**
 * Used for outputting the corpus with the label refinements for submission to EMPNLP
 */
public class OutputCorpusForEMNLP {
	public static void main(String[] args) throws IOException{
		String data = "";
		VivekAnnotationReader reader = new VivekAnnotationReader(CommaProperties.getInstance().getOriginalVivekAnnotationFile());
		List<Sentence> sentences = reader.getSentences();
		for(Sentence sentence : sentences){
			data += sentence.getId() + "\n" + sentence.getAnnotatedText()
					+ "\n\n";
		}
		FileUtils.writeStringToFile(new File("data/Comma_Labeled_Data.txt"), data);
	}
}
