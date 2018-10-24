/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nesim.utils;

import java.util.ArrayList;

public class Permutation {

	public static ArrayList<String> permutation(String s) {
		try {
			if (s.length() > 4) {
				ArrayList<String> tmp = new ArrayList<String>();
				tmp.add(s);
				return tmp;
			}
			ArrayList<String> res = new ArrayList<String>();
			if (s.length() == 1) {
				res.add(s);
			} else if (s.length() > 1) {
				int lastIndex = s.length() - 1;
				String last = s.substring(lastIndex);
				String rest = s.substring(0, lastIndex);
				res = merge(permutation(rest), last);
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			ArrayList<String> tmp = new ArrayList<String>();
			tmp.add(s);
			return tmp;
		}
	}

	private static ArrayList<String> merge(ArrayList<String> list, String c) {
		try {
			ArrayList<String> res = new ArrayList<String>();
			for (String s : list) {
				for (int i = 0; i <= s.length(); ++i) {
					String ps = new StringBuffer(s).insert(i, c).toString();
					res.add(ps);
				}
			}
			return res;
		} catch (Exception e) {
			return list;
		}
	}

	public static void main(String[] args) {
		try {
			ArrayList<String> permutations = Permutation.permutation("aab");
			for (int i = 0; i < permutations.size(); i++)
				System.out.println(permutations.get(i));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
