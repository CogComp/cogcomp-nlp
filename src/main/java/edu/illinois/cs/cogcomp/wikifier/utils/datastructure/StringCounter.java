package main.java.edu.illinois.cs.cogcomp.wikifier.utils.datastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;

/**
 * Simple utility class for counting strings.
 * 
 * @author cheng88, upadhya3
 *
 */
public class StringCounter {

	Map<String, Integer> counter;
	int totalCount = 0;
	private int defaultCount = 0;

	public StringCounter(int defaultCount) {
		this.defaultCount = defaultCount;
		counter = new HashMap<String, Integer>();
	}

	public StringCounter() {
		this(0);
	}

	public StringCounter(Iterable<String> ss) {
		for (String s : ss) {
			count(s);
		}
	}

	/**
	 * returns the set of strings in the vocabulary
	 * 
	 * @return set of string
	 */
	public Set<String> getVocab() {
		return counter.keySet();
	}

	/**
	 * returns the hashmap string-->counts
	 * 
	 * @return
	 */
	public Map<String, Integer> getCountMap() {
		return counter;
	}

	public void count(String s) {
		if (counter.containsKey(s))
			counter.put(s, counter.get(s) + 1);
		else
			counter.put(s, 1 + defaultCount);
		totalCount++;
	}

	public void countAll(Iterable<String> ss) {
		if (ss == null)
			return;
		for (String s : ss) {
			count(s);
		}
	}

	public int getCount(String s) {
		Integer count = counter.get(s);
		return count == null ? defaultCount : count;
	}

	public void reset() {
		counter.clear();
	}

	public List<Pair<String, Double>> getTopK(int k) {
		List<Pair<String, Double>> ret = new ArrayList<Pair<String, Double>>();

		for (String key : counter.keySet()) {
			ret.add(new Pair<String, Double>(key, 1.0 * counter.get(key)));
		}
		Collections.sort(ret, highScoreFirst);
		return ret.subList(0, Math.min(k, ret.size()));
	}

	/**
	 * The second argument is the score
	 *
	 * @return
	 */
	public List<Pair<String, Double>> toScoredList() {
		List<Pair<String, Double>> ret = new ArrayList<Pair<String, Double>>();

		for (String key : counter.keySet()) {
			double matchPercentage = counter.get(key)
					/ (StringUtils.countMatches(key, "_") + 1.0);
			ret.add(new Pair<String, Double>(key, matchPercentage));
		}

		if (counter.size() <= 1)
			return ret;

		Collections.sort(ret, highScoreFirst);
		return ret;
	}

	/**
	 * Shows some statistics of the counter
	 */
	@Override
	public String toString() {
		List<Pair<String, Double>> histogram = toScoredList();
		return "unique strings:" + histogram.size() + "\n" + "total strings:"
				+ totalCount + "\n" + histogram.toString();
	}

	public List<String> toRankedList() {
		List<String> ret = new ArrayList<String>();
		for (Pair<String, Double> pair : toScoredList()) {
			ret.add(pair.getFirst());
		}
		return ret;
	}

	// public List<String> toRankedList(String surface) {
	// List<String> ret = new ArrayList<String>();
	// for (Pair<String, Double> pair : toScoredList()) {
	// ret.add(pair.getFirst());
	// }
	// return ret;
	// }

	private static final Comparator<Pair<String, Double>> highScoreFirst = new Comparator<Pair<String, Double>>() {

		@Override
		public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
			return Double.compare(o2.getSecond(), o1.getSecond());
		}

	};

	public static void main(String[] args) {
		String text = "The nursery rhyme was first published by the Boston publishing firm Marsh, Capen & Lyon, as an original poem by Sarah Josepha Hale on May 24, 1830, and was inspired by an actual incident.As a young girl, Mary Sawyer (later Mrs. Mary Tyler) kept a pet lamb, which she took to school one day at the suggestion of her brother.";
		String[] split = text.split(" ");
		StringCounter c = new StringCounter();
		for(String s:split)
		{
			c.count(s);
		}
		System.out.println(c.getTopK(10));
	}
}
