/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wsim.wordnet;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.sim.Metric;
import edu.illinois.cs.cogcomp.sim.MetricResponse;
import edu.illinois.cs.cogcomp.sim.MetricWord;
import edu.illinois.cs.cogcomp.sim.StringMetric;

import java.io.IOException;
import java.util.Map;

/**
 *
 * Class to calculate Wordnet Pathfinder based similarity
 *
 * @author John Wieting
 * @author mssammon
 * @author ngupta18
 * @author sgupta96
 *
 */
public class WNSim extends StringMetric<String> {

	public static final String NAME = WNSim.class.getCanonicalName();

	private PathFinder pf;

	/**
	 * Contains sample invocation
	 * 
	 * @param args
	 *            optional: config file containing path to the wordnet resource,
	 *            as in config/sampleConfig.txt
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		WNSim wnsim = null;
		if (args.length == 1)
			wnsim = new WNSim(new ResourceManager(args[0]));
		else
			wnsim = new WNSim();

		String dog = "dog";
		String animal = "animal";

		System.out.println("Similarity b/w dog and animal is " + wnsim.compare(dog, animal));
		System.out.println("Similarity b/w animal and dog is " + wnsim.compare(animal, dog));
	}

	/**
	 * default constructor: expects all resources to be defaults, and will look
	 * for them on classpath
	 *
	 * @throws IOException
	 */
	public WNSim() throws IOException {
		initialize(new WNSimConfigurator().getDefaultConfig(), true);
	}

	/**
	 * Constructor
	 *
	 * @param rm
	 *            ResourceManager containing non-default config options,
	 *            including path to the local Wordnet resource
	 * @throws IOException
	 */
	public WNSim(ResourceManager rm) throws IOException {
		ResourceManager finalRm = new WNSimConfigurator().getConfig(rm);
		initialize(finalRm, false);
	}

	private void initialize(ResourceManager rm, boolean useJar) throws IOException {
		String wnPath = rm.getString(WnsimConstants.WNPATH);
		pf = new PathFinder(wnPath, useJar);
	}

	/**
	 * Calculates similarity between word1 and word2
	 *
	 * @param firstWord
	 *            1st word
	 * @param secondWord
	 *            2nd word
	 * @return similarity score between word1 and word2 plus a "reason"
	 */
	public MetricResponse compare(String firstWord, String secondWord) {
		double score = pf.wnsim(firstWord, secondWord);

		return new MetricResponse(score, NAME);
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
	 * {@link #compareStringValues(Map)} to interact with {@link Metric
	 * <T>.compare }
	 *
	 * @param word
	 *            the word to wrap
	 * @return the corresponding object of type T specified by the implementor
	 */
	@Override
	protected String wrapStringArgument(String word) {
		return word;
	}

	/**
	 * Calculates similarity between two words, using POS tags if provided.
	 * Currently ignores POS.
	 *
	 * @param arg1
	 *            1st word
	 * @param arg2
	 *            2nd word
	 * @return similarity score between arg1 and arg2 plus a "reason"
	 * @throws IllegalArgumentException
	 */
	public MetricResponse compare(MetricWord arg1, MetricWord arg2) throws IllegalArgumentException {
		return compare(arg1.word, arg2.word);
	}

}
