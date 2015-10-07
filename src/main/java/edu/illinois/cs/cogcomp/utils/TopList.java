package edu.illinois.cs.cogcomp.utils;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by stephen on 9/24/15.
 */
public class TopList<TKey, TValue> implements Iterable<Pair<TKey, TValue>>{
//public class TopList<TKey, TValue> : IList<KeyValuePair<TKey, TValue>>, ICollection<KeyValuePair<TKey, TValue>>, IEnumerable<KeyValuePair<TKey, TValue>>, IEnumerable

    public TopList(int topK) {

    }

    @Override
    public Iterator<Pair<TKey, TValue>> iterator() {
        return null;
    }

    public void add(Pair<TKey, TValue> p){

    }

    public void add(TKey t, TValue p){

    }



    public int indexOf(TValue v){
        return -1;
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


