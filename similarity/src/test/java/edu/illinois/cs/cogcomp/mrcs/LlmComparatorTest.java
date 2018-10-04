/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.mrcs;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import edu.illinois.cs.cogcomp.config.EmbeddingConstant;
import edu.illinois.cs.cogcomp.config.SimConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.llm.align.WordListFilter;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.cs.cogcomp.llm.comparators.LlmStringComparator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class LlmComparatorTest {
	// private final static String CONFIG = "llmTestConfig.txt";

	private static LlmStringComparator llm;

	private final String text = "The world's largest cruise ship cleared a crucial obstacle Sunday, lowering its smokestacks to "
			+ "squeeze under a bridge in Denmark. The Oasis of the Seas -- which rises about 20 stories high -- passed below the Great "
			+ "Belt Fixed Link with a slim margin as it left the Baltic Sea on its maiden voyage to Florida. Bridge operators said that "
			+ "even after lowering its telescopic smokestacks the giant ship had less than a 2-foot (half-meter) gap. Hundreds of people "
			+ "gathered on beaches at both ends of the bridge, waiting for hours to watch the brightly lit behemoth sail by shortly after "
			+ "midnight (2300GMT; 7 p.m. EDT). \"It was fantastic to see it glide under the bridge. Boy, it was big,\" said Kurt Hal, 56. "
			+ "Company officials are banking that its novelty will help guarantee its success. Five times larger than the Titanic, the $1.5 "
			+ "billion ship has seven neighborhoods, an ice rink, a small golf course and a 750-seat outdoor amphitheater. It has 2,700 cabins "
			+ "and can accommodate 6,300 passengers and 2,100 crew members. Accommodations include loft cabins, with floor-to-ceiling windows, "
			+ "and 1,600-square-foot (487-meter) luxury suites with balconies overlooking the sea or promenades. The liner also has four swimming "
			+ "pools, volleyball and basketball courts, and a youth zone with theme parks and nurseries for children. Oasis of the Sea, nearly 40 "
			+ "percent larger than the industry's next-biggest ship, was conceived years before the economic downturn caused desperate cruise "
			+ "lines to slash prices to fill vacant berths. It was built by STX Finland for Royal Caribbean International and left the shipyard "
			+ "in Finland on Friday. Officials hadn't expected any problems in passing the Great Belt bridge, but traffic was stopped for about "
			+ "15 minutes as a precaution when the ship approached, Danish navy spokesman Joergen Brand said. Aboard the Oasis of the Seas, "
			+ "project manager Toivo Ilvonen of STX Finland confirmed that the ship had passed under the bridge without any incidents. \"Nothing "
			+ "fell off,\" he said. The enormous ship features various \"neighborhoods\" -- parks, squares and arenas with special themes. One of "
			+ "them will be a tropical environment, including palm trees and vines among the total 12,000 plants on board. They will be "
			+ "planted after the ship arrives in Fort Lauderdale. In the stern, a 750-seat outdoor theater -- modeled on an ancient Greek "
			+ "amphitheater -- doubles as a swimming pool by day and an ocean front theater by night. The pool has a diving tower with "
			+ "spring boards and two 33-foot (10-meter) high-dive platforms. An indoor theater seats 1,300 guests. One of the \"neighborhoods,\" "
			+ "named Central Park, features a square with boutiques, restaurants and bars, including a bar that moves up and down three decks, "
			+ "allowing customers to get on and off at different levels. Once home, the $1.5 billion floating extravaganza will have more, if "
			+ "less visible, obstacles to duck: a sagging U.S. economy, questions about the consumer appetite for luxury cruises and criticism "
			+ "that such sailing behemoths are damaging to the environment and diminish the experience of traveling. It is due to make its U.S. "
			+ "debut on Nov. 20 at its home port, Port Everglades in Florida.";
	// private final String hyp = "Toivo Ilvonen is the president of STX Finland
	// .";
	private final String hyp = "weapons of mass destruction head towards Congo";
	private final double expectedSimpleScoreNoStopwords = 0.1; // not using
																// WNSim
	private final double expectedSimpleScoreWithStopwords = 0.2; // not using
																	// WNSim

	private static boolean isSetup = false;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		llm = new LlmStringComparator();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCompareStrings() {
		double score = 0.0;
		try {
			score = llm.compareStrings_(text, hyp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("score is: " + score);

		assertTrue((Math.abs(score - this.expectedSimpleScoreWithStopwords)) > 0.0);
	}

	@Test
	public void testwordnetLlm() {
		Properties props = new Properties();
		props.setProperty(SimConfigurator.WORD_METRIC.key, EmbeddingConstant.wordnet);
		try {
			llm = new LlmStringComparator(new ResourceManager(props));
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		double score = 0.0;
		try {
			score = llm.compareStrings_(text, hyp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("score is: " + score);

		assertTrue((Math.abs(score - this.expectedSimpleScoreWithStopwords)) > 0.0);
	}

	@Test
	public void testparagramllm() {
		Properties props = new Properties();
		props.setProperty(SimConfigurator.WORD_METRIC.key, EmbeddingConstant.paragram);
		try {
			llm = new LlmStringComparator(new ResourceManager(props));
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		double score = 0.0;
		try {
			score = llm.compareStrings_(text, hyp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("score is: " + score);

		assertTrue((Math.abs(score - this.expectedSimpleScoreWithStopwords)) > 0.0);
	}

	@Test
	public void testRemoveStopwords() {
		WordListFilter filter = null;
		try {
			filter = new WordListFilter(new SimConfigurator().getDefaultConfig());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		String sent = "This sentence is filled with unnecessary filler like their pronouns , punctuation and function "
				+ "words such as for , by , from , him , her , and to .";

		String[] tokens = sent.split("\\s+");
		String[] filteredTokens = filter.filter(tokens);

		int numSkipped = 0;
		List<String> filteredToks = new LinkedList<>();

		for (int i = 0; i < tokens.length; ++i) {
			String tok = filteredTokens[i];

			if (null == tok) {
				numSkipped++;
				filteredToks.add(tokens[i]);
			}
		}
		assert (numSkipped > 0);
		assert (filteredToks.contains("is"));

		System.out.println("Original text: " + sent);
		System.out.println("Filtered tokens: ");
		System.out.println(StringUtils.join(filteredToks, "; "));

	}

	// @Test
	// public void testAlignStringArrays()
	// {
	// assertTrue( true );
	// }
	//
	// @Test
	// public void testScoreAlignment()
	// {
	// assertTrue( true );
	// }
	//
	// @Test
	// public void testCompareStringArrays()
	// {
	// assertTrue( true );
	// }
	//
	// @Test
	// public void testCompareTokens()
	// {
	// assertTrue( true );
	// }
	//

}
