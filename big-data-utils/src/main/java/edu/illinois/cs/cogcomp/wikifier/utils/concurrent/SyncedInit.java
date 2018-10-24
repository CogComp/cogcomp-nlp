/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.concurrent;

import java.util.concurrent.locks.ReentrantLock;

public class SyncedInit {
    
    private boolean initialized = false;
    
    private ReentrantLock lock = new ReentrantLock();
    
    public void lockIfUninit(){
        if(!initialized){
            lock.lock();
        }
    }
    
    public void unlock(){
        if(!initialized && lock.isHeldByCurrentThread()){
            initialized = true;
            lock.unlock();
        }
    }

}
