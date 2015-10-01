package edu.illinois.cs.cogcomp.utils;

import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;

/**
 * Created by stephen on 9/30/15.
 */
public class InternDictionary<T> extends HashMap<T,T> {

    public InternDictionary(){
        throw new NotImplementedException("not implemented...");
    }

    public T Intern(T obj){
        throw new NotImplementedException("not implemented...");
    }

    public boolean IsInterned(T obj){
        throw new NotImplementedException("not implemented...");
    }
}
