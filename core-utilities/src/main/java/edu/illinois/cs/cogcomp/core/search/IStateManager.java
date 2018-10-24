/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
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
