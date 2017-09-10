package edu.illinois.cs.cogcomp.datalessclassification.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Utilities {

    private static Set<String> stopWords;
//    private static CharArraySet charStopWords;

    public static Set<String> getStopWords() {
	if (stopWords == null) {
	    stopWords = new HashSet<String>();

	    stopWords.addAll(Arrays.asList("I", "a", "about", "an", "are",
		    "as", "at", "be", "by", "com", "de", "en", "for", "from",
		    "how", "in", "is", "it", "la", "of", "on", "or", "that",
		    "the", "this", "to", "was", "what", "when", "where", "who",
		    "will", "with", "und", "the", "www"));
	}
	return stopWords;
    }
    
    // public static Set<String> getStopWords(String configFile)
    // throws ConfigurationException {
    // if (stopWords == null) {
    //
    // stopWords = new HashSet<String>();
    // PropertiesConfiguration config = new PropertiesConfiguration(
    // configFile);
    // String s = config.getString("descartes.indexer.stopwords");
    //
    // stopWords.addAll(Arrays.asList(s.split(",+")));
    //
    // }
    // return stopWords;
    // }

}
