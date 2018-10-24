/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.concurrent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Iterates keys while pre-fetching the next available
 * values
 * @author cheng88
 *
 * @param <K> keys
 * @param <V> values to be feteched
 */
public abstract class PrefetchIterator<K,V> implements Iterator<V>{

    private Iterator<K> keys;
    private Queue<Future<V>> results = new LinkedList<Future<V>>();
    private final ExecutorService service;
    private final int threadCount;
    
    
    /**
     * Upon each {@link #next()} call, threadCount prefetches
     * are being executed if possible.
     * @param keys
     */
    public PrefetchIterator(Iterator<K> keys) {
        this(keys, 16);
    }
    
    /**
     * Upon each {@link #next()} call, threadCount prefetches
     * are being executed if possible.
     * @param keys
     * @param threadCount
     */
    public PrefetchIterator(Iterator<K> keys,int threadCount){
        this.threadCount = threadCount;
        this.service = Executors.newFixedThreadPool(threadCount);
        this.keys = keys;
    }

    @Override
    public boolean hasNext() {
        boolean ret = !results.isEmpty() || keys.hasNext();
        if (!ret)
            close();
        return ret;
    }

    @Override
    public V next() {
        while(results.size()<threadCount && keys.hasNext()){
            results.offer(service.submit(new Callable<V>() {
                @Override
                public V call() throws Exception {
                    return fetch(keys.next());
                }
            }));
        }
        try {
            return results.poll().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Potentially expensive operation
     * @param key
     * @return value fetched
     */
    public abstract V fetch(K key);
    
    /**
     * This object closes automatically when the {@link #hasNext()}
     * returns false
     */
    public void close(){
        service.shutdown();
        try {
            service.awaitTermination(10, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
