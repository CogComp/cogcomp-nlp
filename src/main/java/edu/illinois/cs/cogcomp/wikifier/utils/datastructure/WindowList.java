package main.java.edu.illinois.cs.cogcomp.wikifier.utils.datastructure;

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
