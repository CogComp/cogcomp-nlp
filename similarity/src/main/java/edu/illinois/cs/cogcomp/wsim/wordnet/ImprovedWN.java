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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

public class ImprovedWN {

	private static final String NAME = ImprovedWN.class.getCanonicalName();
	public WNWrapper wrap;
	public double MAX = 15;
	public HashMap<String, ArrayList<String>> syns = new HashMap<String, ArrayList<String>>();
	public HashMap<String, ArrayList<String>> rels = new HashMap<String, ArrayList<String>>();

	PathFinder path;
	HashMap<String, Para> map = new HashMap<String, Para>();
	HashMap<String, Para> nopos = new HashMap<String, Para>();

	public ImprovedWN(String wnPath) throws IOException {
		this(wnPath, true);
	}

	public ImprovedWN(String wnPath, boolean useJar) throws IOException {
		wrap = WNWrapper.getWNWrapper(wnPath, useJar);
		path = new PathFinder(wnPath, useJar);
	}

	/*
	 * @param args: path to the local Wordnet resource
	 */
	public static void main(String[] args) {
		WnsimUtils.checkArgsOrDie(args, 1, NAME, "wnPath");
		ImprovedWN i = null;
		try {
			i = new ImprovedWN(args[0], false);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println(i.findPath("diehard", "person"));
	}

	// assume lematizesd
	public boolean isAntonym(String w1, String w2) {
		return wrap.isAntonym(w1, null, w2, null);
	}

	public double findPath(String small, String large) {
		ArrayList<ISynset> lis1 = getAllSynset(small);
		ArrayList<ISynset> lis2 = getAllSynset(large);

		if (lis1.size() == 0 || lis2.size() == 0)
			return -1;

		if (findConnectionHypernym(small, large, lis1, lis2) == null) {
			return 0.;
		} else {
			return 1.;
		}
	}

	public boolean isSynhyper(String w1, String w2) {
		ArrayList<ISynset> lis1 = getAllSynset(w1);
		ArrayList<ISynset> lis2 = getAllSynset(w2);
		return findConnectionHypernym(w1, w2, lis1, lis2) == null;
	}

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

	private POS getWNPOS(String pos) {
		if (pos.startsWith("V"))
			return POS.VERB;
		else if (pos.startsWith("A"))
			return POS.ADJECTIVE;
		else if (pos.startsWith("R"))
			return POS.ADVERB;
		else if (pos.startsWith("N"))
			return POS.NOUN;
		return null;
	}

	public WNPath findConnectionHypernym(String w1, String w2, ArrayList<ISynset> synsets1,
			ArrayList<ISynset> synsets2) {

		// System.out.println(w1+" "+w2+" HYPER");
		ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>> parents = new ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>>();
		for (ISynset is : synsets1) {
			parents.addAll(getParents(is, 0, new ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>>(),
					new ArrayList<IPointer>(), Pointer.HYPERNYM));
		}

		WNPath wn = null;
		for (Triple<ISynset, Integer, ArrayList<IPointer>> tt : parents) {
			if (match((ISynset) tt.getLeft(), synsets2)) {
				if (wn == null || (Integer) tt.getMiddle() < (Integer) wn.length) {
					wn = WNPath.getWN(((ArrayList<IPointer>) tt.getRight()), new ArrayList<IPointer>(),
							(ISynset) tt.getLeft(), w1, w2);
				}
			}
		}

		return wn;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>> getParents(ISynset is, int curr,
			ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>> lis, ArrayList<IPointer> pts, Pointer ptr) {

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
			if (ptr.equals(ip)) {
				ArrayList<IPointer> newlis = (ArrayList<IPointer>) pts.clone();
				newlis.add(ip);
				for (ISynsetID sid : map.get(ip)) {
					String space = "";
					for (int i = 0; i < curr; i++) {
						space += "\t";
					}
					// System.out.println(space+" "+curr+"
					// "+wrap.dict.getSynset(sid));
					getParents(wrap.dict.getSynset(sid), curr, lis, newlis, Pointer.HYPERNYM);
				}
			}
		}
		// System.exit(0);
		return lis;
	}

	public ArrayList<String> getAllParentWords(ArrayList<ISynset> syns) {
		HashSet<ISynset> allsyns = new HashSet<ISynset>();
		ArrayList<String> lemmas = new ArrayList<String>();

		for (ISynset is : syns) {
			ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>> lis = getParents(is, 0,
					new ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>>(), new ArrayList<IPointer>(),
					Pointer.HYPERNYM);
			for (int i = 0; i < lis.size(); i++) {
				allsyns.add(lis.get(i).getLeft());
			}
		}

		for (ISynset is : allsyns) {
			List<IWord> lis = is.getWords();
			for (IWord iw : lis) {
				if (!iw.getLemma().contains("_"))
					lemmas.add(iw.getLemma());
			}
		}

		return lemmas;
	}

	public ArrayList<String> getAllChildrenWords(ArrayList<ISynset> syns) {
		HashSet<ISynset> allsyns = new HashSet<ISynset>();
		ArrayList<String> lemmas = new ArrayList<String>();

		for (ISynset is : syns) {
			ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>> lis = getParents(is, 0,
					new ArrayList<Triple<ISynset, Integer, ArrayList<IPointer>>>(), new ArrayList<IPointer>(),
					Pointer.HYPONYM);
			for (int i = 0; i < lis.size(); i++) {
				allsyns.add(lis.get(i).getLeft());
			}
		}

		for (ISynset is : allsyns) {
			List<IWord> lis = is.getWords();
			for (IWord iw : lis) {
				if (!iw.getLemma().contains("_"))
					lemmas.add(iw.getLemma());
			}
		}

		return lemmas;
	}

	private boolean match(ISynset left, ArrayList<ISynset> synsets2) {
		for (ISynset is : synsets2) {
			// System.out.println(left+" "+is);
			if (is.equals(left) && is.getPOS().equals(left.getPOS()))
				return true;
		}
		return false;
	}

	public ArrayList<ISynset> getAllSynset(String word) {
		ArrayList<ISynset> lis = new ArrayList<ISynset>();
		for (POS p : POS.values()) {
			lis.addAll(getAllSynset(word, p));
			// System.out.println(p+" "+Arrays.toString(lis.toArray()));
		}

		return lis;
	}

	public ArrayList<ISynset> getAllSynset(String word, POS pos) {
		// System.out.println(word+" "+pos);
		ArrayList<ISynset> syns = new ArrayList<ISynset>();
		IIndexWord idxWord = null;
		try {
			idxWord = wrap.dict.getIndexWord(word, pos);
			// System.out.println(idxWord);
		} catch (IllegalArgumentException e) {
		}
		if (idxWord == null)
			return new ArrayList<ISynset>();
		List<IWordID> senses = idxWord.getWordIDs();
		for (IWordID iw : senses) {
			ISynset is = wrap.dict.getSynset(iw.getSynsetID());
			List<IWord> words = is.getWords();
			syns.add(is);
		}
		return syns;
	}

	private class Para {
		private String w1;
		private String w2;
		private double s;
	}
}
