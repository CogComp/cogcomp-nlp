/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.utils;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;

import java.util.*;

/**
 * This is sorted according to key.
 * Created by stephen on 9/24/15.
 */
public class TopList<TKey extends Comparable<? super TKey>, TValue> implements Iterable<Pair<TKey, TValue>>{

    private LinkedList<Pair<TKey, TValue>> ilist;
    private int topK;

    public TopList(int topK) {
        ilist = new LinkedList<>();
        this.topK = topK;
    }

    public int size(){
        return ilist.size();
    }

    @Override
    public String toString() {

        String lst = "";
        for(Pair<TKey, TValue> p : ilist){
            lst += "@@" + p.getFirst() + " :: " + p.getSecond() + "@@";
            lst += ", ";
        }

        return "TopList{" +
                "ilist=" + lst +
                '}';
    }

    /**
     * This sorts descending, according to key.
     * @param other
     */
    public void add(Pair<TKey, TValue> other){
        if(ilist.size() == 0){
            this.ilist.add(other);
            return;
        }

        int addat = -1;

        for(int i =0; i < ilist.size(); i++){
            Pair<TKey, TValue> mine = ilist.get(i);

            if(other.getFirst().compareTo(mine.getFirst()) > 0){
                addat = i;
                break;
            }

        }

        // insert it
        if(addat != -1){
            this.ilist.add(addat, other);
        }else{ // put it at the end if addat = -1
            this.ilist.addLast(other);
        }

        if(this.ilist.size() > topK) {
            this.ilist.pollLast();
        }

    }

    public void add(TKey t, TValue p){
        Pair<TKey, TValue> newp =new Pair<>(t,p);
        this.add(newp);
    }

    public int indexOf(TValue v){
        int i = 0;
        for(Pair<TKey, TValue> p : this.ilist){
            if(v.equals(p.getSecond())){
                return i;
            }
            i++;
        }

        return -1;
    }

    /**
     * Convert this TopList into a list.
     * @return
     */
    public List<Pair<TKey, TValue>> toList(){
        List<Pair<TKey, TValue>> out = new ArrayList<>(ilist.size());
        for(Pair<TKey, TValue> p : ilist){
            out.add(p);
        }
        return out;
    }

    /**
     * Get the first value in the toplist. This is the one with the highest value.
     * @return
     */
    public Pair<TKey, TValue> getFirst(){
        return ilist.getFirst();
    }


    @Override
    public Iterator<Pair<TKey, TValue>> iterator() {
        return this.ilist.iterator();
    }

    //:IList<TKey>,ICollection<TKey>,IEnumerable<TKey>,IEnumerable
    public class KeyList{
        // What goes here?

    }

    //public class ValueList:IList<TValue>,ICollection<TValue>,IEnumerable<TValue>,IEnumerable
    public class ValueList<TValue> extends AbstractList implements Iterable,Collection {

        @Override
        public Object get(int index) {
            return null;
        }

        @Override
        public Iterator<TValue> iterator() {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }
    }

}


