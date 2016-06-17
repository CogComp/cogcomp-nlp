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
