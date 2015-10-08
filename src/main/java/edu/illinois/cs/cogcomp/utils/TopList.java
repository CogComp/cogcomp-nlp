package edu.illinois.cs.cogcomp.utils;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;

import java.util.*;

/**
 * Created by stephen on 9/24/15.
 */
public class TopList<TKey extends Comparable<? super TKey>, TValue> implements Iterable<Pair<TKey, TValue>>{

    private LinkedList<Pair<TKey, TValue>> ilist;
    private int topK;

    public TopList(int topK) {
        ilist = new LinkedList<>();
        this.topK = topK;
    }

//    @Override
//    public Iterator<Pair<TKey, TValue>> iterator() {
//        return null;
//    }

    /**
     * This sorts descending, according to key.
     * @param other
     */
    public void add(Pair<TKey, TValue> other){
        int addat = -1;
        for(int i =0; i < ilist.size(); i++){
            Pair<TKey, TValue> mine = ilist.get(i);

            if(other.getFirst().compareTo(mine.getFirst()) > 0){
                addat = i;
            }

        }

        if(addat != -1){
            this.ilist.add(addat, other);
            this.ilist.pollLast();
        }

    }

    public void add(TKey t, TValue p){
        this.add(new Pair<>(t,p));
    }



    public int indexOf(TValue v){
        return -1;
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


