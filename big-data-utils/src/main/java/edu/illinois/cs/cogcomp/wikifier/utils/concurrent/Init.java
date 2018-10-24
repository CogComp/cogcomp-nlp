/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.concurrent;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Useful for concurrent map initialization
 * @author cheng88
 *
 * @param <V>
 */
public abstract class Init<V>{
    
    public abstract V init();
    
    /**
     * Non thread-safe version of {@link Init#getOrCreate(ConcurrentMap, Object, Init)}
     * without writing to the map
     * @param map
     * @param key
     * @param initiailzer
     * @return
     */
    public static <K,V> V fastGet(Map<K, V> map,K key, Init<V> initiailzer) {
        V ret = map.get(key);
        if (ret == null)
            return initiailzer.init();
        return ret;
    }

    /**
     * Atomic initialization and gets
     * for concurrent maps. Note that mapdb's 
     * get(key) method only returns cached values.
     * To update record, write-back is required
     * @param map
     * @param key
     * @param initiailzer
     * @return the existing value corresponding to the key
     * or a new value created by initializer
     */
    public static <K,V> V getOrCreate(ConcurrentMap<K, V> map,K key, Init<V> initiailzer) {
        V ret = map.get(key);
        if(ret == null){
            final V newVal = initiailzer.init();
            ret = map.putIfAbsent(key, newVal);
            if(ret == null){
                ret = newVal;
            }
        }
        return ret;
    }

    public static final Init<AtomicInteger> newAtmInt = new Init<AtomicInteger>() {
        @Override
        public AtomicInteger init(){
            return new AtomicInteger();
        }
    };
    public static final Init<LinkedList<Integer>> newLkdList = new Init<LinkedList<Integer>>(){
        @Override
        public LinkedList<Integer> init() {
            return new LinkedList<Integer>();
        }
    };
    public static final Init<TIntArrayList> newIntList = new Init<TIntArrayList>() {
        @Override
        public TIntArrayList init(){
            return new TIntArrayList(1);
        }
    };
    
    public static final Init<TIntIntHashMap> newTIntMap = new Init<TIntIntHashMap>(){
        @Override
        public TIntIntHashMap init() {
            return new TIntIntHashMap();
        }
    }; 
    
    public static final Init<ConcurrentHashMap<Integer,AtomicInteger>> newCountMap =
            new Init<ConcurrentHashMap<Integer,AtomicInteger>>(){
        @Override
        public ConcurrentHashMap<Integer, AtomicInteger> init() {
            return new ConcurrentHashMap<Integer, AtomicInteger>();
        }
    };
}