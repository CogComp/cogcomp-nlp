/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.mrcs;

import edu.illinois.cs.cogcomp.config.SimConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.llm.comparators.LlmStringComparator;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Created by sling3 on 4/12/17.
 */
public class ParaphraseTest {

	public class SentencePair {

		public String s1;
		public String s2;
		public String id;
		public double score;

		public SentencePair(String s1, String s2, String id, String score) {
			this.s1 = s1;
			this.s2 = s2;
			this.id = id;
			this.score = Double.parseDouble(score);
		}
	}

	public static void main(String[] args) throws Exception {
		ParaphraseTest p = new ParaphraseTest();
		p.testMS("wordnet");
		p.testSICK("wordnet");
		p.testMS("word2vec");
		p.testSICK("word2vec");
		p.testMS("glove");
		p.testSICK("glove");
		// p.testMS("word2vec");
		// p.testMS("esa");
		// p.testMS("gloce");
	}

	public void testMS(String metric) throws Exception {
		HashMap<SentencePair, Double> score = new HashMap();
		String CONFIG = "config/configurations.properties";
		ResourceManager rm_ = new SimConfigurator().metricsConfig(metric, CONFIG);
		int count = 0;
		LlmStringComparator llm = new LlmStringComparator(rm_);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("/shared/bronte/sling3/data/msr-para-train.tsv"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {

				line = line.trim();
				if (line.length() > 0) {
					String[] arr = line.split("\t");
					SentencePair s = new SentencePair(arr[3], arr[4], arr[1] + "-" + arr[2], arr[0]);
					// System.out.println(arr[3]+" "+arr[4]);
					double sc = llm.compareStrings_(arr[3], arr[4]);
					score.put(s, sc);

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		double max = 0;
		double th = 0;
		for (double t = 0; t < 1; t = t + 0.01) {
			if (accuracy(score, t) > max) {
				max = accuracy(score, t);
				th = t;
			}

		}
		HashMap<SentencePair, Double> score2 = new HashMap();
		try {
			reader = new BufferedReader(new FileReader("/shared/bronte/sling3/data/msr-para-test.tsv"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {

				line = line.trim();
				if (line.length() > 0) {
					String[] arr = line.split("\t");
					SentencePair s = new SentencePair(arr[3], arr[4], arr[1] + "-" + arr[2], arr[0]);
					double sc = llm.compareStrings_(arr[3], arr[4]);
					score2.put(s, sc);
					count++;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("MS" + metric + " " + accuracy(score2, th) / count + " " + th);
	}

	public double accuracy(HashMap<SentencePair, Double> score, double t) {
		int correct = 0;
		for (SentencePair s : score.keySet()) {
			if (s.score == 1 && score.get(s) >= t)
				correct++;
			if (s.score == 0 && score.get(s) < t)
				correct++;
		}
		return correct;
	}

	public void testSICK(String metric) throws Exception {
		HashMap<SentencePair, Double> score = new HashMap();
		String CONFIG = "config/configurations.properties";
		ResourceManager rm_ = new SimConfigurator().metricsConfig(metric, CONFIG);
		int count = 0;
		LlmStringComparator llm = new LlmStringComparator(rm_);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("/shared/bronte/sling3/data/SICK_test_annotated.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {

				line = line.trim();
				if (line.length() > 0) {
					String[] arr = line.split("\t");
					SentencePair s = new SentencePair(arr[1], arr[2], arr[0], arr[3]);
					double sc = llm.compareStrings_(arr[1], arr[2]);
					score.put(s, sc);
					// System.out.println(sc);
				}

				if (count % 100 == 0)
					System.out.println(count);
				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		double[] d1 = new double[score.size()];
		double[] d2 = new double[score.size()];
		int i = 0;
		for (SentencePair s : score.keySet()) {
			d1[i] = score.get(s);
			d2[i] = s.score;
			i++;
		}
		System.out.println("pscore for : " + metric + " " + new PearsonsCorrelation().correlation(d1, d2));

	}
}
