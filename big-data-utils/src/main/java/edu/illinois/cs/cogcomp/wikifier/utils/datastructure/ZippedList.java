/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.datastructure;

import java.util.Iterator;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;

public class ZippedList<K, V> implements Iterable<Pair<K,V>>{

    Iterable<K> k;
    Iterable<V> v;

    public ZippedList(Iterable<K> k,Iterable<V> v){
        this.k = k;
        this.v = v;
    }


    public static <X, Y> ZippedList<X,Y> zip(Iterable<X> k,Iterable<Y> v){
        return new ZippedList<X,Y>(k,v);
    }


    @Override
    public Iterator<Pair<K, V>> iterator() {
        final Iterator<K> ki = k.iterator();
        final Iterator<V> vi = v.iterator();

        return new Iterator<Pair<K, V>>(){

            @Override
            public boolean hasNext() {
                return ki.hasNext() && vi.hasNext();
            }

            @Override
            public Pair<K, V> next() {
                return new Pair<K,V>(ki.next(),vi.next());
            }

            @Override
            public void remove() {
                // TODO Auto-generated method stub

            }

        };
    }



}
