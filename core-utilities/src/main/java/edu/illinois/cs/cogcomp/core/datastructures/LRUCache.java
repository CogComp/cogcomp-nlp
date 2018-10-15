/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * A cache that removes the least recently used elements when it is full. This implementation is
 * <b>NOT</b> threadsafe.
 *
 * @author Vivek Srikumar
 */
public class LRUCache<K, V> {

    protected final int sizeLimit;

    protected final LinkedHashMap<K, V> cache;

    public LRUCache(int size) {
        this(size, 16);
    }

    public LRUCache(int sizeLimit, int initialCapacity) {
        this.sizeLimit = sizeLimit;
        cache = new LinkedHashMap<K, V>(initialCapacity) {
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
                return this.size() > LRUCache.this.sizeLimit;
            }
        };
    }

    public synchronized boolean contains(K key) {
        return cache.containsKey(key);
    }

    public synchronized V get(K key) {
        return cache.get(key);
    }

    public synchronized void put(K key, V value) {
        cache.put(key, value);
    }

    public synchronized Set<K> keySet() {
        return cache.keySet();
    }

    public synchronized int size() {
        return cache.size();
    }

    public synchronized int sizeLimit() {
        return this.sizeLimit;
    }
}
