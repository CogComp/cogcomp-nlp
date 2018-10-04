/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.utils;

import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;

/**
 * Created by stephen on 9/30/15.
 */
public class InternDictionary<T> extends HashMap<T,T> {

    public InternDictionary(){
        // don't think I need anything here...
    }

    /**
     * The reason this works is that containsKey uses the .equals method, which actually compares contents. This way,
     * we can save memory because fewer strings are actually stored, but also avoid some nasty bugs. That is, we may
     * have two strings with different locations in memory, but the same contents (dynamically created strings). If
     * the code uses a == operator, it will return false. This way, all strings with the same contents will be the same
     * location in memory.
     * @param obj
     * @return
     */
    public T Intern(T obj){

        if(!this.containsKey(obj)) {
            this.put(obj, obj);
        }

        return this.get(obj);

    }
}
