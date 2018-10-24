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

public class WindowIterator<T> implements Iterator<Window<T>>{

	final int windowSize;
	final List<T> candidates;
	int current;

	public WindowIterator(int windowSize,final List<T> candidates){
		this.windowSize = windowSize;
		this.candidates = candidates;
		current = -1;
	}

	@Override
	public boolean hasNext() {
		return current<candidates.size()-1;
	}

	@Override
	public Window<T> next() {
		current++;
		return new Window<T>(candidates,current,windowSize);
	}

	@Override
	public void remove() {

	}

}