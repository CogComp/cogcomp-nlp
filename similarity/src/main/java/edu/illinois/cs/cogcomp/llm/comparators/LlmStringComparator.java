/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.llm.comparators;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.config.SimConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.llm.align.WordListFilter;
import edu.illinois.cs.cogcomp.mrcs.align.Aligner;
import edu.illinois.cs.cogcomp.mrcs.align.GreedyAlignmentScorer;
import edu.illinois.cs.cogcomp.mrcs.comparators.Comparator;
import edu.illinois.cs.cogcomp.mrcs.dataStructures.Alignment;
import edu.illinois.cs.cogcomp.mrcs.dataStructures.EntailmentResult;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

public class LlmStringComparator {

	public static final java.lang.String LLM_THRESHOLD = "llmThreshold";
	public static final String DEFAULT_LLM_THRESHOLD = "0.5";

	private Aligner<String, EntailmentResult> aligner;
	private WordListFilter filter;
	private Comparator<String, EntailmentResult> comparator;
	private Aligner<String, EntailmentResult> neAligner;
	private GreedyAlignmentScorer<String> scorer;
	IllinoisTokenizer tokenizer;

	public LlmStringComparator() throws IOException {
		initialize(new ResourceManager(new Properties()));
	}

	private void initialize(ResourceManager nonDefaultConfig) throws IOException {

		WordComparator wc = new WordComparator(new SimConfigurator().getConfig(nonDefaultConfig));
		initialize(nonDefaultConfig, wc);
	}

	public LlmStringComparator(ResourceManager rm_) throws IllegalArgumentException, IOException {
		initialize(rm_);
	}

	private void initialize(ResourceManager rm_, Comparator<String, EntailmentResult> comparator) throws IOException {
		ResourceManager fullRm = new SimConfigurator().getConfig(rm_);
		double threshold = fullRm.getDouble(SimConfigurator.LLM_ENTAILMENT_THRESHOLD.key);
		tokenizer = new IllinoisTokenizer();
		this.comparator = comparator;
		filter = new WordListFilter(fullRm);
		neAligner = new Aligner<String, EntailmentResult>(new NEComparator(), filter);
		aligner = new Aligner<String, EntailmentResult>(comparator, filter);
		scorer = new GreedyAlignmentScorer<String>(threshold);

	}

	public LlmStringComparator(ResourceManager rm_, Comparator<String, EntailmentResult> comparator_)
			throws IOException {
		initialize(rm_, comparator_);
	}

	/**
	 * convenience method with sentences as Strings. Tokenizes using
	 * LBJTokenizer and runs Aligner, Filter specified at instantiation.
	 * 
	 * @param text_
	 *            the Text (typically longer text span)
	 * @param hyp_
	 *            the Hypothesis (intent: is this true, given the Text?)
	 * @return EntailmentResult (contains a set of parameters describing the
	 *         entailment decision)
	 * @throws Exception
	 */

	public EntailmentResult determineEntailment(String text_, String hyp_) throws Exception {
		return determineEntailment(getTokens(text_), getTokens(hyp_));
	}

	/**
	 * determines best lexical level match of two arrays of String, each
	 * representing a tokenized sentence. some tokens may be filtered depending
	 * on the WordListFilter params specified at construction.
	 * 
	 * @param textToks_
	 * @param hypToks_
	 * @return
	 * @throws Exception
	 */

	public EntailmentResult determineEntailment(String[] textToks_, String[] hypToks_) throws Exception {
		return scoreAlignment(alignStringArrays(textToks_, hypToks_));
	}

	/**
	 * generate the result (score, label, etc.) for the given alignment using
	 * GreedyAlignmentScorer
	 * 
	 * @param alignment_
	 * @return
	 */

	public EntailmentResult scoreAlignment(Alignment<String> alignment_) {
		return scorer.scoreAlignment(alignment_);
	}

	/**
	 * convenience method to generate the alignment for two sentences specified
	 * as Strings.
	 * 
	 * @param text_
	 * @param hyp_
	 * @return
	 * @throws Exception
	 */

	public Alignment<String> alignSentences(String text_, String hyp_) throws Exception {
		return (alignStringArrays(getTokens(text_), getTokens(hyp_)));
	}

	private String[] getTokens(String sentence_) {
		Pair<String[], IntPair[]> lbjTokens = tokenizer.tokenizeSentence(sentence_);

		return lbjTokens.getFirst();
	}

	/**
	 * generate a lexical alignment using the comparator specified at
	 * construction, and the
	 * 
	 * 
	 * @param textTokens_
	 * @param hypTokens_
	 * @return
	 * @throws Exception
	 */

	public Alignment<String> alignStringArrays(String[] textTokens_, String[] hypTokens_) throws Exception {
		return aligner.align(textTokens_, hypTokens_);
	}

	/**
	 * determines best lexical level match of two arrays of Name Entity using
	 * NESim.
	 * 
	 * @param ne1_
	 * @param ne2_
	 * @return
	 * @throws Exception
	 */

	public Alignment<String> alignNEStringArrays(String[] ne1_, String[] ne2_) throws Exception {
		return neAligner.align(ne1_, ne2_);
	}

	/**
	 * Convenience method to generate a scalar score for two input sentences
	 * passed as TextAnnotation. The function is used only when NER is called and
	 * NER_CONLL view is generated. 
	 * 
	 * Compared with function determineEntailment(String text_, String hyp_) 
	 * which generates score for two input sentences passed as String. 
	 * 
	 * @param source_
	 * @param target_
	 * @return similarity score (double) between 0 and 1.
	 * @throws Exception
	 */

	public double compareAnnotation(TextAnnotation source, TextAnnotation target) throws Exception {

		List<Constituent> sentences = source.getView(ViewNames.SENTENCE).getConstituents();
		Constituent firstSent = sentences.get(0);
		String source_ = firstSent.getTokenizedSurfaceForm();
		sentences = target.getView(ViewNames.SENTENCE).getConstituents();
		Constituent secondSent = sentences.get(0);
		String target_ = secondSent.getTokenizedSurfaceForm();
		HashSet<String> ne1_word = new HashSet<String>();
		HashSet<String> ne2_word = new HashSet<String>();
		List<Constituent> ne1 = source.getView(ViewNames.NER_CONLL).getConstituents();
		String[] ne1_ = new String[ne1.size()];
		for (int i = 0; i < ne1.size(); i++) {
			ne1_[i] = ne1.get(i).getTokenizedSurfaceForm();
			for (String s : getTokens(ne1_[i])) {
				ne1_word.add(s);
			}
		}

		List<Constituent> ne2 = target.getView(ViewNames.NER_CONLL).getConstituents();

		String[] ne2_ = new String[ne2.size()];
		for (int i = 0; i < ne2.size(); i++) {
			ne2_[i] = ne2.get(i).getTokenizedSurfaceForm();
			for (String s : getTokens(ne2_[i])) {
				ne2_word.add(s);
			}
		}

		Alignment<String> neAlignment = alignNEStringArrays(ne1_, ne2_);

		String[] textTokens_ = new String[getTokens(source_).length - ne1_word.size()];
		int i = 0;
		for (String s : getTokens(source_)) {
			if (!ne1_word.contains(s)) {
				textTokens_[i] = s;
				i++;
			}
		}

		String[] hypTokens_ = new String[getTokens(target_).length - ne2_word.size()];
		i = 0;
		for (String s : getTokens(target_)) {
			if (!ne2_word.contains(s)) {
				hypTokens_[i] = s;
				i++;
			}
		}

		Alignment<String> sentenceAlignment = alignStringArrays(textTokens_, hypTokens_);
		double sentenceScore = scorer.scoreAlignment(sentenceAlignment).getScore();
		// System.out.println("sentencescore " + sentenceScore);
		double neScore = scorer.scoreAlignment(neAlignment).getScore();
		// System.out.println("nescore " + neScore);
		if (ne1.size() == 0 || ne2.size() == 0)
			return sentenceScore;
		return (sentenceScore * hypTokens_.length + neScore * ne2.size()) / (hypTokens_.length + ne2.size());
	}

	/**
	 * convenience method to generate a scalar score for two input sentences
	 * passed as Strings..
	 * 
	 * @param source_
	 * @param target_
	 * @return similarity score (double) between 0 and 1.
	 * @throws Exception
	 */

	public double compareStrings_(String source_, String target_) throws Exception {

		return (double) this.determineEntailment(source_, target_).getScore();
	}

}
