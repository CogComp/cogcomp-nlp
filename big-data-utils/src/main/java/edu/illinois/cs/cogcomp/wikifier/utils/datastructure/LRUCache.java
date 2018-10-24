/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.datastructure;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class LRUCache<K, V> implements Map<K, V> {

    LoadingCache<K, V> cache;

    public LRUCache(int maxSize) {
        cache = CacheBuilder.newBuilder().maximumSize(maxSize).build(new CacheLoader<K, V>() {
            @Override
            public V load(K k) throws Exception {
                return loadValue(k);
            }

        });
    }

    protected V loadValue(K k) throws Exception {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object k) {
        try {
            return cache.get((K) k);
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public int size() {
        return cache.asMap().size();
    }

    @Override
    public boolean isEmpty() {
        return cache.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return cache.asMap().containsValue(value);
    }

    @Override
    public V put(K key, V value) {
        cache.put(key, value);
        return value;
    }

    @Override
    public V remove(Object key) {
        return cache.asMap().remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        cache.putAll(m);
    }

    @Override
    public void clear() {
        cache.asMap().clear();
    }

    @Override
    public Set<K> keySet() {
        return cache.asMap().keySet();
    }

    @Override
    public Collection<V> values() {
        return cache.asMap().values();
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return cache.asMap().entrySet();
    }

}
