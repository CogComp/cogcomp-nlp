/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IncrementID<T> {

    Map<T, Integer> idMap = new HashMap<T, Integer>();
    List<T> objectMap = new ArrayList<T>();

    public int id(T t) {
        Integer id = idMap.get(t);
        if (id == null) {
            int curId = idMap.size();
            idMap.put(t, curId);
            objectMap.add(t);
            return curId;
        }
        return id.intValue();
    }

    public String stringId(T t){
        return String.valueOf(id(t));
    }

    public T get(int id) {
        return objectMap.get(id);
    }

    public int size() {
        return idMap.size();
    }

    public void reset(){
        idMap.clear();
        objectMap.clear();
    }

}
