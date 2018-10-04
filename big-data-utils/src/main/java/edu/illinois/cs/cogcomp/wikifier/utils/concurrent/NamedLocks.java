/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Usage: 
 * <pre>
 * NamedLocks locks = new NamedLocks(); 
 * synchronized (locks.acquire(key)) { 
 *      // Synchronized for all locks named key 
 * }
 * </pre>
 * Keys must implement hashCode() and equals() correctly.
 * Lock objects are weak references thus GCed once no longer
 * reachable from code. You should not keep hard references 
 * to the acquired lock outside of the synchronized block.
 * @author cheng88
 * 
 */
public class NamedLocks {
    
    private final LoadingCache<Object,Object> locks;
    
    /**
     * Constructs the lock holder.
     */
    public NamedLocks(){
        locks = CacheBuilder.newBuilder()
                .weakValues()
                .build(new CacheLoader<Object, Object>() {
                    
            @Override
            public Object load(Object k) throws Exception {
                return new Object();
            }

        });
    }

    /**
     * Acquires a lock for the specified key.
     * @param key
     * @return the object to lock on for the key
     */
    public Object acquire(Object key){
        return locks.getUnchecked(key);
    }
    
    /**
     * 
     * @return Approximate size
     */
    public int size(){
        return (int) locks.size();
    }
    
    
    private static LoadingCache<String,InitLock> inited = CacheBuilder
            .newBuilder()
            .build(new CacheLoader<String, InitLock>(){

                @Override
                public InitLock load(String key) throws Exception {
                    return new InitLock();
                }
                
            });
    
    private static class InitLock{
        Lock lock = new ReentrantLock();
        boolean uninitialized = true;
    }
    
    public static void lockOnce(String name){
        InitLock initlock = inited.getUnchecked(name);
        if(initlock.uninitialized){
            initlock.lock.lock();
        }
    }
    
    public static void unlockOnce(String name){
        InitLock initlock = inited.getUnchecked(name);
        if(initlock.uninitialized){
            initlock.lock.unlock();
            initlock.uninitialized = false;
        }
    }

}
