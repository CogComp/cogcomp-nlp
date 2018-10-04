/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.search;

import java.util.Queue;

/**
 * This is the abstract class that does graph search, based on a queuing function. Use one of its
 * descendants.
 *
 * @author Vivek Srikumar
 *         <p>
 *         May 1, 2009
 */
public abstract class GraphSearch<T> {
    Queue<T> queue;

    protected GraphSearch(Queue<T> queue) {
        this.queue = queue;
    }

    /**
     * Performs graph search, starting with the <code>initialState</code>. <code>stateManager</code>
     * defines the successor state function and the goal test function.
     * <p>
     *
     * @return The first state that satisfies the goal test function. If no such state is found,
     *         then the function returns <code>null</code>.
     */
    public T search(T initialState, IStateManager<T> stateManager) {
        queue.clear();
        queue.add(initialState);

        T currentState;

        while (true) {
            if (queue.isEmpty()) {
                currentState = null;
                break;
            }

            currentState = queue.poll();

            if (stateManager.goalTest(currentState))
                break;

            for (T nextState : stateManager.nextStates(currentState)) {
                if (!queue.contains(nextState)) {
                    queue.add(nextState);
                }
            }
        }

        return currentState;
    }
}
