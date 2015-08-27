package main.java.edu.illinois.cs.cogcomp.wikifier.utils;

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
