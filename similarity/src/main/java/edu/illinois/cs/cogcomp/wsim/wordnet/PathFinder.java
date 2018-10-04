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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

// call constructor, and then wnsim(String,String) to get a double score.
public class PathFinder {

	private static final String NAME = PathFinder.class.getCanonicalName();
	public WNWrapper wrap;
	public int MAX = 12;
	public HashSet<Pointer> simPointers;
	public HashSet<Pointer> simPointersLex;
	public HashSet<Pointer> hypers;
	public HashSet<Pointer> ent;
	public static boolean addToPath = true;
	public static boolean root = false;
	public static boolean related = false;

	/*
	 * @param args: path to the local Wordnet resource
	 */
	public static void main(String[] args) {
		WnsimUtils.checkArgsOrDie(args, 1, NAME, "wnPath");
		PathFinder p = null;
		try {
			p = new PathFinder(args[0], false);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		// p.findConnection("full","years", false);
		// System.out.println(p.wnsimPOS("man",POS.VERB, "woman", POS.NOUN));
		System.out.println(p.wnsim("deal", "deal"));
		System.out.println(WNPath.getLast());
		// System.out.println(p.isRelated("US","U.S."));
		// System.out.println(p.wnsim_wsd("car","automobile",null,null));
	}

	public PathFinder(String wnPath) throws IOException {
		this(wnPath, true);
	}

	public PathFinder(String wnPath, boolean useJar) throws IOException {
		wrap = WNWrapper.getWNWrapper(wnPath, useJar);

		simPointers = new HashSet<Pointer>();
		simPointersLex = new HashSet<Pointer>();
		hypers = new HashSet<Pointer>();
		ent = new HashSet<Pointer>();

		simPointers.add(Pointer.HYPERNYM);
		simPointers.add(Pointer.HOLONYM_MEMBER);
		simPointers.add(Pointer.HOLONYM_PART);
		simPointers.add(Pointer.ENTAILMENT);

		hypers.add(Pointer.HYPERNYM);
		hypers.add(Pointer.HOLONYM_MEMBER);
		hypers.add(Pointer.HOLONYM_PART);

		ent.add(Pointer.ENTAILMENT);

		// simPointers.add(Pointer.DERIVATIONALLY_RELATED);
		// simPointers.add(Pointer.DERIVED_FROM_ADJ);
		// simPointers.add(Pointer.ALSO_SEE);
		// simPointers.add(Pointer.SIMILAR_TO);
		// simPointers.add(Pointer.VERB_GROUP);

		// simPointers.add(Pointer.ANTONYM);
		// simPointersLex.add(Pointer.DERIVATIONALLY_RELATED);
		// simPointersLex.add(Pointer.ALSO_SEE);
		// simPointersLex.add(Pointer.PERTAINYM);

		// System.out.println(simPointers.contains(Pointer.VERB_GROUP));
	}

	public boolean isRelated(String w1, String w2) {
		return isRelated(w1, w2, null, null);
	}

	public boolean isRelated(String w1, String w2, POS pos1, POS pos2) {

		ArrayList<IWord> iwords1 = wrap.getIWords(w1, pos1);
		ArrayList<IWord> iwords2 = wrap.getIWords(w2, pos2);

		// System.out.println(iwords1.size());
		// System.out.println(iwords2.size());

		ArrayList<IWord> dwords1 = new ArrayList<IWord>();
		ArrayList<IWord> dwords2 = new ArrayList<IWord>();

		for (int i = 0; i < iwords1.size(); i++) {
			ArrayList<IWordID> ids = new ArrayList<IWordID>(
					iwords1.get(i).getRelatedWords(Pointer.DERIVATIONALLY_RELATED));
			ids.addAll(new ArrayList<IWordID>(iwords1.get(i).getRelatedWords(Pointer.DERIVED_FROM_ADJ)));
			for (IWordID id : ids) {
				dwords1.add(wrap.dict.getWord(id));
			}
		}

		for (IWord iw : dwords1) {
			for (IWord iw2 : iwords2) {
				if (iw.equals(iw2))
					return true;
			}
		}

		for (int i = 0; i < iwords2.size(); i++) {
			ArrayList<IWordID> ids = new ArrayList<IWordID>(
					iwords2.get(i).getRelatedWords(Pointer.DERIVATIONALLY_RELATED));
			ids.addAll(new ArrayList<IWordID>(iwords2.get(i).getRelatedWords(Pointer.DERIVED_FROM_ADJ)));
			for (IWordID id : ids) {
				dwords2.add(wrap.dict.getWord(id));
			}
		}

		for (IWord iw : dwords2) {
			for (IWord iw2 : iwords1) {
				if (iw.equals(iw2))
					return true;
			}
		}

		return false;
	}

	public double score_binary(String w1, String w2) {
		if (findConnection(w1, w2))
			return 1.;

		return 0.;
	}

	// method to call to approximate cpp wnsim.
	// this method has slightly more coverage than cpp wnsim
	// it doesn't give scores for DT or PREP
	public double wnsim(String w1, String w2) {
		WNPath wp = wnsim(w1, w2, true);
		// wp = handleHas(w1,w2,wp);
		return wp.score;
	}

	public WNPath wnsim(String w1, String w2, boolean doesnothing) {
		WNPath wp1 = wnsim(w1, w2, hypers);
		WNPath wp2 = wnsim(w1, w2, hypers);
		double d1 = wp1.score;
		double d2 = wp2.score;
		double max = wp1.score;

		if (d1 < 0) {
			if (addToPath) {
				wp1.setSumString("Antonym");
				WNPath.paths.add(wp1);
			}
			return wp1;
		}

		if (d1 > 0 || d2 > 0) {
			max = d1 > d2 ? d1 : d2;

			double lcs = lcs_score(w1, w2);

			if (!root)
				lcs = 0.;

			if (lcs > max) {
				WNPath wn = new WNPath();
				wn.setSumString("LCS " + lcs + " " + w1 + " " + w2);
				wn.score = lcs;
				if (addToPath)
					WNPath.paths.add(wn);
				return wn;
			}

			if (Math.abs(d1 - max) <= .00001) {
				if (wp1 != null) {
					wp1.score = d1;
					wp1.toString();
					if (addToPath)
						WNPath.paths.add(wp1);
					return wp1;
				}
			} else {
				if (wp2 != null) {
					wp2.score = d2;
					wp2.toString();
					if (addToPath)
						WNPath.paths.add(wp2);
					return wp2;
				}
			}
		}
		if (addToPath)
			WNPath.paths.add(wp1);

		return wp1;
	}

	public WNPath wnsim(String w1, String w2, HashSet<Pointer> ptrs) {

		int k = 3;
		double theta = 0.3;
		double alpha = 1.5;

		WNPath wn = findConnection(w1, w2, true, ptrs, null, null, false, null, null);
		if (wn == null) {
			wn = new WNPath();
			wn.summaryString = "No Path";
			wn.score = 0;
			return wn;
		}

		int pl = wn.length;
		int depth = getLCS(wn.synset);
		wn.lcs = depth;
		double score = 0.;

		// System.out.println("Path length: "+pl+" LCS depth: "+depth);
		// System.out.println("Synset: "+t.getLeft());

		if (pl <= k) {
			score = Math.pow(theta, pl);
		} else {
			if (depth >= alpha * pl) {
				score = Math.pow(theta, k);
			}
		}

		if (wrap.isAntonym(w1, w2)) {
			wn.score = -0.5;
			return wn;
		}

		wn.score = score;
		if (wn.summaryString == null) {
			wn.toString();
			// wn.summaryString="Path found, but score was 0.";
		}
		return wn;
	}

	public double wnsimPOS(String word1, POS pos1, String word2, POS pos2) {
		double d1 = wnsimPOS(word1, pos1, word2, pos2, hypers);
		double d2 = wnsimPOS(word1, pos1, word2, pos2, ent);
		double max = d1;

		if (d1 < 0)
			return d1;

		if (d1 > 0 || d2 > 0)
			max = d1 > d2 ? d1 : d2;

		double lcs = lcs_score(word1, pos1, word2, pos2); // should leave in?

		if (!root)
			lcs = 0.;

		if (lcs > max)
			return lcs;

		return max;
	}

	private double wnsimPOS(String word1, POS pos1, String word2, POS pos2, HashSet<Pointer> ptrs) {

		int k = 3;
		double theta = 0.3;
		double alpha = 1.5;

		WNPath wn = findConnection(word1, word2, true, ptrs, null, null, true, pos1, pos2);
		if (wn == null)
			return 0.;

		int pl = wn.length;
		int depth = getLCS(wn.synset);
		double score = 0.;

		// System.out.println("Path length: "+pl+" LCS depth: "+depth);
		// System.out.println("Synset: "+t.getLeft());

		if (pl <= k) {
			score = Math.pow(theta, pl);
		} else {
			if (depth >= alpha * pl) {
				score = Math.pow(theta, k);
			}
		}

		if (wrap.isAntonym(word1, pos1, word2, pos2))
			return -.5;

		return score;
	}

	public boolean findConnection(String w1, String w2) {
		WNPath wn = findConnection(w1, w2, false, simPointers, null, null);
		if (wn == null)
			return false;
		return true;
	}

	public WNPath findConnection(String w1, String w2, boolean twoWay, HashSet<Pointer> ptrs, IWord iw1, IWord iw2,
			boolean pos, POS pos1, POS pos2) {

		ArrayList<ISynset> synsets2;
		ArrayList<ISynset> synsets1;

		if (iw1 == null) {
			if (pos)
				synsets1 = wrap.getAllSynset(w1, pos1);
			else
				synsets1 = wrap.getAllSynset(w1);
		} else {
			synsets1 = new ArrayList<ISynset>();
			synsets1.add(iw1.getSynset());
		}

		if (iw2 == null) {
			if (pos)
				synsets2 = wrap.getAllSynset(w2, pos2);
			else
				synsets2 = wrap.getAllSynset(w2);
		} else {
			synsets2 = new ArrayList<ISynset>();
			synsets2.add(iw2.getSynset());
		}

		return findConnection(w1, w2, twoWay, ptrs, synsets1, synsets2);
	}

	public WNPath findConnection(String w1, String w2, boolean twoWay, HashSet<Pointer> ptrs,
			ArrayList<ISynset> synsets1, ArrayList<ISynset> synsets2) {

		ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>> parents = new ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>>();
		for (ISynset is : synsets1) {
			parents.addAll(getParents(is, 0, new ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>>(),
					new ArrayList<IPointer>(), ptrs));
		}

		WNPath wn = null;
		if (twoWay) {
			// for(ISynset is: synsets2)
			// System.out.println("0 "+is);
			ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>> parents2 = new ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>>();
			for (ISynset is : synsets2) {
				parents2.addAll(getParents(is, 0, new ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>>(),
						new ArrayList<IPointer>(), ptrs));
			}
			// System.out.println(parents.size()+" "+parents2.size());
			for (Triple<ISynset, Integer, ArrayList<IPointer>> tt : parents) {
				for (Triple<ISynset, Integer, ArrayList<IPointer>> tt2 : parents2)
					if (match(tt.getLeft(), tt2.getLeft())) {
						if (wn == null || tt.getMiddle() + tt2.getMiddle() < wn.length) {
							ArrayList<IPointer> pters = tt.getRight();
							ArrayList<IPointer> pters2 = tt2.getRight();
							// System.out.println("ADDING "+w1+" "+w2);
							// WNPath.addPath(pters, pters2, (ISynset)
							// tt.getLeft(), w1, w2);
							wn = WNPath.getWN(pters, pters2, tt.getLeft(), w1, w2);
						}
					}
			}
		} else {
			for (Triple<ISynset, Integer, ArrayList<IPointer>> tt : parents) {
				if (match(tt.getLeft(), synsets2)) {
					if (wn == null || tt.getMiddle() < wn.length) {
						wn = WNPath.getWN(tt.getRight(), null, tt.getLeft(), w1, w2);
					}
				}
			}
		}
		if (wn != null) {
			// logger.debug("Found path from: "+w1+" "+w2+": "+s);
			return wn;
		}
		return null;
	}

	private boolean match(ISynset left, ISynset left2) {
		return left.equals(left2) && left.getPOS().equals(left2.getPOS());
	}

	private boolean match(ISynset left, ArrayList<ISynset> synsets2) {
		for (ISynset is : synsets2) {
			if (is.equals(left) && is.getPOS().equals(left.getPOS()))
				return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>> getParents(ISynset is, int curr,
			ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>> lis, ArrayList<IPointer> pts,
			HashSet<Pointer> ptrs) {

		if (curr == MAX) {
			return lis;
		}

		Triple<ISynset, Integer, ArrayList<IPointer>> t = new Triple<ISynset, Integer, ArrayList<IPointer>>(is, curr,
				pts);
		lis.add(t);
		// now for each matching relation recurse
		Map<IPointer, List<ISynsetID>> map = is.getRelatedMap();
		curr++;
		for (IPointer ip : map.keySet()) {
			// System.out.println(ip+" "+simPointers.contains(ip));
			if (ptrs.contains(ip)) {
				ArrayList<IPointer> newlis = (ArrayList<IPointer>) pts.clone();
				newlis.add(ip);
				for (ISynsetID sid : map.get(ip)) {
					String space = "";
					for (int i = 0; i < curr; i++) {
						space += "\t";
					}
					// System.out.println(space+" "+curr+"
					// "+wrap.dict.getSynset(sid));
					getParents(wrap.dict.getSynset(sid), curr, lis, newlis, ptrs);
				}
			}
		}
		// System.exit(0);
		return lis;
	}

	private double lcs_score(String w1, String w2) {
		return lcs_score(w1, null, w2, null);
	}

	private double lcs_score(String w1, POS p1, String w2, POS p2) {

		ArrayList<ISynset> synsets1 = wrap.getAllSynset(w1, p1);
		ArrayList<ISynset> synsets2 = wrap.getAllSynset(w2, p2);

		if (p1 != null && p2 != null && !p1.equals(p2))
			return 0.;

		double min1 = 100;
		double min2 = 100;
		double sum = 100;
		for (POS pos : POS.values()) {
			min1 = 100;
			min2 = 100;
			// System.out.println(pos);
			if (pos.equals(POS.NOUN))
				continue;
			for (ISynset is : synsets1) {
				// System.out.println(is);
				if (!pos.equals(is.getPOS()))
					continue;
				double d = getLCS(is);
				if (d < min1)
					min1 = d;
				// System.out.println("1: "+d);
			}
			// System.out.println();
			for (ISynset is : synsets2) {
				if (!pos.equals(is.getPOS()))
					continue;
				// System.out.println(is);
				double d = getLCS(is);
				if (d < min2)
					min2 = d;
				// System.out.println("2 :"+d+" "+min2);
			}
			double s = min1 + min2;
			// System.out.println(s);
			if (s < sum)
				sum = s;
		}
		// System.out.println(sum);
		if (sum <= 3)
			return Math.pow(0.3, sum);

		return 0;
	}

	public int getLCS(ISynset is) {
		return getDepth(is, 0);
	}

	private int getDepth(ISynset is, int depth) {
		Map<IPointer, List<ISynsetID>> map = is.getRelatedMap();
		int min = 1000;
		List<ISynsetID> lis = map.get(Pointer.HYPERNYM);

		if (depth > 50)
			return depth;

		if (lis == null || lis.size() == 0)
			return depth + 1;

		for (ISynsetID id : lis) {
			int m = getDepth(wrap.dict.getSynset(id), depth + 1);
			if (m < min)
				min = m;
		}

		return min;
	}
}
