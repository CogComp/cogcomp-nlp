package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import java.util.Iterator;

public interface IResetableIterator<T> extends Iterator<T> {

    public void reset();
}
