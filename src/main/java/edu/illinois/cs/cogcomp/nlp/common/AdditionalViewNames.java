package edu.illinois.cs.cogcomp.nlp.common;

//import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;

/**
 * Use Edison ViewNames for TextAnnotations, Edison CuratorViewNames for Records
 * copied from Edison, augmented where necessary
 * values correspond to the field names in the Curator annotators.xml configuration file
 * @author mssammon
 *
 */

public class AdditionalViewNames {

//	public static String stanfordDep = ViewNames.DEPENDENCY_STANFORD;
//	public static String dependencies = ViewNames.DEPENDENCY;
//	public static String chunk = ViewNames.SHALLOW_PARSE;
//	public static String stanfordParse = ViewNames.PARSE_STANFORD;
//	public static String tokens = "tokens";
//	public static String sentences = ViewNames.SENTENCE;
//	public static String pos = ViewNames.POS;
	public static final String lemmaWn = "lemma_wn";
	public static final String lemmaWnPlus = "lemma_wnplus";
	public static final String lemmaPorter = "lemma_porter";
	public static final String lemmaKp = "lemma_kp";
	public static final String ccgLemma = "illinois_lemma";
    
}
