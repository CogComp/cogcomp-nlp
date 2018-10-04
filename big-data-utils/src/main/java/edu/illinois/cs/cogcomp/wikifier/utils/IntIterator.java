/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils;

import gnu.trove.iterator.TIntIterator;

import java.util.Iterator;

public class IntIterator implements Iterator<Integer> {

    private TIntIterator iter;

    public IntIterator(TIntIterator iter) {
        this.iter = iter;
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public Integer next() {
        return iter.next();
    }

    @Override
    public void remove() {
        iter.remove();

    }
}
