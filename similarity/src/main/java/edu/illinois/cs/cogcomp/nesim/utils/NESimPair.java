/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nesim.utils;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;

@SuppressWarnings({ "serial", "rawtypes" })
public class NESimPair<A, B> extends Pair {

	@SuppressWarnings("unchecked")
	public NESimPair(Object obj, Object obj2) {
		super(obj, obj2);
	}
}
