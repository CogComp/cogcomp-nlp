/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nesim.utils;

import edu.illinois.cs.cogcomp.core.algorithms.LevensteinDistance;

public class DameraoLevenstein extends LevensteinDistance {
	public int score(String s1, String s2) {
		return getLevensteinDistance(s1, s2);
	}
}
