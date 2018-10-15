/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 *
 */
package edu.illinois.cs.cogcomp.core.search;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Stack;

/**
 * @author Vivek Srikumar
 *         <p>
 *         May 1, 2009
 */
public class DepthFirstSearch<T> extends GraphSearch<T> {

    public DepthFirstSearch() {
        super(new Queue<T>() {

            Stack<T> stack = new Stack<>();

            public T element() {
                return stack.peek();
            }

            public boolean offer(T o) {
                stack.push(o);
                return true;
            }

            public T peek() {
                return stack.peek();
            }

            public T poll() {
                return stack.pop();
            }

            public T remove() {
                return stack.pop();
            }

            public boolean add(T o) {
                return offer(o);
            }

            public boolean addAll(Collection<? extends T> c) {
                return stack.addAll(c);
            }

            public void clear() {
                stack.clear();
            }

            public boolean contains(Object o) {
                return stack.contains(o);
            }

            public boolean containsAll(Collection<?> c) {
                return stack.containsAll(c);
            }

            public boolean isEmpty() {
                return stack.isEmpty();
            }

            public Iterator<T> iterator() {
                return stack.iterator();
            }

            public boolean remove(Object o) {
                return stack.remove(o);
            }

            public boolean removeAll(Collection<?> c) {
                return stack.removeAll(c);
            }

            public boolean retainAll(Collection<?> c) {
                return stack.retainAll(c);
            }

            public int size() {
                return stack.size();
            }

            public Object[] toArray() {
                return stack.toArray();
            }

            public <E> E[] toArray(E[] a) {
                return stack.toArray(a);
            }
        });
    }

    protected DepthFirstSearch(Queue<T> queue) {
        super(queue);
        // TODO Auto-generated constructor stub
    }

}
