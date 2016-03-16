package edu.illinois.cs.cogcomp.core.search;

import java.util.List;

/**
 * @author Vivek Srikumar
 *         <p>
 *         May 1, 2009
 */
public interface IStateManager<T> {
    List<T> nextStates(T currentState);

    boolean goalTest(T state);
}
