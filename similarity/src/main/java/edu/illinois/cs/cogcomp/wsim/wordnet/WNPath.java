/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wsim.wordnet;

import java.util.ArrayList;

import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.POS;

public class WNPath {

	public static ArrayList<WNPath> paths = new ArrayList<WNPath>();

	public ArrayList<IPointer> ptrs1;
	public ArrayList<IPointer> ptrs2;
	public ISynset synset;
	public POS pos;
	public String w1;
	public String w2;
	public double lcs;
	public double score;
	public double parascore;
	public int length;
	public String summaryString;

	public void setSumString(String s) {
		summaryString = s;
	}

	public static WNPath getLast() {
		if (paths.size() > 0)
			return paths.get(paths.size() - 1);
		else
			return null;
	}

	public static WNPath getMax() {
		double max = 0;
		int i = 0;
		for (int j = 0; j < paths.size(); j++) {
			if (Math.abs(paths.get(j).score) > Math.abs(max)) {
				max = paths.get(j).score;
				i = j;
			}
		}

		if (i < paths.size())
			return paths.get(i);
		return null;
	}

	public static void clear() {
		paths.clear();
	}

	public static WNPath getWN(ArrayList<IPointer> ptrs1, ArrayList<IPointer> ptrs2, ISynset synset, String w1,
			String w2) {
		WNPath wn = new WNPath();
		wn.ptrs1 = ptrs1;
		wn.ptrs2 = ptrs2;
		wn.synset = synset;
		wn.w1 = w1;
		wn.w2 = w2;
		wn.length = ptrs1.size() + ptrs2.size();
		wn.pos = synset.getPOS();
		return wn;
	}

	public static void addPath(ArrayList<IPointer> pters1, ArrayList<IPointer> pters2, ISynset synset, String w1,
			String w2) {

		WNPath wn = new WNPath();
		wn.ptrs1 = pters1;
		wn.ptrs2 = pters2;
		wn.synset = synset;
		wn.w1 = w1;
		wn.w2 = w2;
		wn.pos = synset.getPOS();
		paths.add(wn);
	}

	public static void addLCS(double d) {
		getLast().lcs = d;
	}

	public String toString() {
		String s = "first: ";
		for (int i = 0; i < ptrs1.size(); i++) {
			s += (ptrs1.get(i) + " ");
		}
		s = s.trim();

		s += " second: ";
		for (int i = 0; i < ptrs2.size(); i++) {
			s += (ptrs2.get(i) + " ");
		}
		s = s.trim();

		s += " " + pos + " " + synset + " " + w1 + " " + w2 + " " + lcs + " " + score;

		summaryString = s;
		return s;

	}
}
