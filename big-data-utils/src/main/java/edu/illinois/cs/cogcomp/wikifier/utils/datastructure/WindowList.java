/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.datastructure;

import java.util.Iterator;
import java.util.List;

/**
 * Supports iterating unique permutations of the window size
 * in the given list
 * @author cheng88
 *
 * @param <T>
 */
public class WindowList<T> implements Iterable<Window<T>>{

	final int windowSize;
	final List<T> candidates;

	public WindowList(int windowSize,final List<T> candidates){
		this.windowSize = windowSize;
		this.candidates = candidates;
	}

	@Override
	public Iterator<Window<T>> iterator() {
		return new WindowIterator<T>(windowSize,candidates);
	}

}
