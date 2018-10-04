/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wsim.wordnet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.sim.Metric;
import edu.illinois.cs.cogcomp.sim.MetricResponse;
import edu.illinois.cs.cogcomp.sim.MetricWord;
import edu.illinois.cs.cogcomp.sim.StringMetric;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to compute paraphrase based similarity
 * 
 * @author John Wieting
 * @author mssammon
 * @author ngupta18
 * @author sgupta96
 *
 */
public class Paraphrase extends StringMetric<MetricWord> {

	private static final String NAME = Paraphrase.class.getCanonicalName();
	public HashMap<String, Para> paraphrase_map_pos = new HashMap<String, Para>();
	public HashMap<String, Para> paraphrase_map_nopos = new HashMap<String, Para>();
	private Logger logger = LoggerFactory.getLogger(Paraphrase.class);

	/**
	 * Contains sample invocations
	 *
	 * @param args
	 *            optional: config file containing path to the paraphrase
	 *            resource, as in config/sampleConfig.txt
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Paraphrase p = null;
		if (args.length == 1)
			p = new Paraphrase(new ResourceManager(args[0]));
		else
			p = new Paraphrase();

		String wife = "wife";
		String woman = "woman";
		String dog = "dog";
		String animal = "animal";
		String show = "show";
		String found = "found";
		String wound = "wound";
		String injure = "injure";

		System.out.println("Similarity b/w wife and woman is " + p.compareString(wife, woman));
		System.out.println("Similarity b/w woman and wife is " + p.compareString(woman, wife));
		System.out.println("Similarity b/w dog and animal is " + p.compareString(dog, animal));
		System.out.println("Similarity b/w show and found is " + p.compareString(show, found));
		System.out.println("Similarity b/w wound and injure is " + p.compareString(wound, injure));
	}

	/**
	 * default constructor: expects all resources to be defaults, and will look
	 * for them on classpath
	 * 
	 * @throws IOException
	 */
	public Paraphrase() throws IOException {
		this(new WNSimConfigurator().getDefaultConfig());
	}

	/**
	 * Constructor
	 *
	 * @param rm
	 *            ResourceManager with non-default properties set
	 * @throws IOException
	 */
	public Paraphrase(ResourceManager rm) throws IOException {
		String paraPath = rm.getString(WnsimConstants.PARAPATH);
		initialize(paraPath);
	}

	private void initialize(String path) {
		ArrayList<String> lines = Util.readLines(path);
		for (String s : lines) {
			String[] arr = s.split("@@@");
			Para para = new Para();
			para.w1 = arr[1];
			para.w2 = arr[2];

			try {
				para.s = 1 / Double.parseDouble(arr[3]);
				paraphrase_map_pos.put(arr[0] + "@@@" + arr[1] + "@@@" + arr[2], para);
				if (paraphrase_map_nopos.containsKey(arr[1] + "@@@" + arr[2])) {
					double score = (paraphrase_map_nopos.get(arr[1] + "@@@" + arr[2])).s;
					if (score < para.s)
						paraphrase_map_nopos.put(arr[1] + "@@@" + arr[2], para);
				} else
					paraphrase_map_nopos.put(arr[1] + "@@@" + arr[2], para);
			} catch (Exception e) {
				// TODO: clean up this pattern. Check for problem explicitly,
				// don't hide exceptions.
				// this happens because some entries are malformed, with nothing
				// between some sets
				// of '@@@' delimiters.
				// e.printStackTrace();
				logger.warn("Malformed paraphrase entry from '" + path + "': " + e.getMessage());
			}
		}
	}

	/**
	 * Calculates similarity between two words when only word strings are
	 * present. may be asymmetric
	 *
	 * @param small
	 *            1st component
	 * @param big
	 *            2nd component
	 * @return similarity score
	 * @throws IllegalArgumentException
	 */
	private double score(String small, String big) {
		if (small.equals(big))
			return 3.;

		double score = parascore(small, null, big, null);
		return score;
	}

	/**
	 * 
	 * Calculates similarity between word1 and word2 TODO: determine catalog of
	 * POS values for args 2 and 4
	 *
	 * @param arg1
	 *            1st word
	 * @param pos1
	 *            part-of-speech tag for first word
	 * @param arg2
	 *            2nd word
	 * @param pos2
	 *            part-of-speech tag for second word
	 * @return similarity score between word1 and word2
	 */
	private double parascore(String arg1, String pos1, String arg2, String pos2) {
		if (pos1 == null || pos2 == null) {
			if (paraphrase_map_nopos.containsKey(arg1 + "@@@" + arg2)) {
				return (paraphrase_map_nopos.get(arg1 + "@@@" + arg2)).s;
			} else
				return 0.;
		}

		if (!pos1.equals(pos2))
			return 0.;

		String pos = getPOS(pos1);

		if (paraphrase_map_nopos.containsKey(pos + "@@@" + arg1 + "@@@" + arg2)) {
			return (paraphrase_map_nopos.get(pos + "@@@" + arg1 + "@@@" + arg2)).s;
		} else
			return 0.;
	}

	/**
	 * TODO: use enum type for valid POS
	 * 
	 * @param pos
	 *            Part of Speech Tag
	 * @return some simpler value of the pos tag
	 */
	public static String getPOS(String pos) {
		if (pos.startsWith("V"))
			return "V";
		else if (pos.startsWith("J"))
			return "J";
		else if (pos.startsWith("R"))
			return "R";
		else if (pos.startsWith("N"))
			return "N";
		return pos;
	}

	/**
	 * returns the name of this metric. Used as the reason by the default
	 * {@link #compareStringValues } method.
	 *
	 * @return the name of this metric.
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * construct a T instance from just a String to allow the
	 * {@link #compareStringValues} to interact with {@link Metric <T>.compare }
	 *
	 * @param word
	 *            the word to wrap
	 * @return the corresponding object of type T specified by the implementor
	 */
	@Override
	protected MetricWord wrapStringArgument(String word) {
		return new MetricWord(word, null);
	}

	public class Para {
		private String w1;
		private String w2;
		private double s;
	}

	/**
	 * Calculates similarity between word1 and word2 TODO: determine catalog of
	 * POS values for POS tags of word1 and word2
	 *
	 * @param arg1
	 *            1st component (word + POS tag)
	 * @param arg2
	 *            2nd component
	 * @return similarity score between words plus a "reason"
	 * @throws IllegalArgumentException
	 * @throws NotImplementedException
	 */
	public MetricResponse compare(MetricWord arg1, MetricWord arg2)
			throws IllegalArgumentException, NotImplementedException {
		String pos1 = (null == arg1.pos) ? null : arg1.pos.name();
		String pos2 = (null == arg2.pos) ? null : arg2.pos.name();
		double score = this.parascore(arg1.word, pos1, arg2.word, pos2);
		return new MetricResponse(score, NAME);
		// return null;
	}

	/**
	 * Calculates similarity between two words when only word strings are
	 * present. Metric is Asymmetric
	 * 
	 * @param arg1
	 *            1st component
	 * @param arg2
	 *            2nd component
	 * @return similarity score between word1 and word2 plus a "reason"
	 * @throws IllegalArgumentException
	 */
	public MetricResponse compareString(String arg1, String arg2) throws IllegalArgumentException {
		double score = this.score(arg1, arg2);
		return new MetricResponse(score, NAME);
	}

}
