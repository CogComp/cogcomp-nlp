/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.StringStatisticsUtils;

import java.util.Vector;


public class MemoryEfficientHashtable<T1, T2> {
    public Vector<T1> keys = new Vector<>();
    public Vector<T2> values = new Vector<>();

    public MemoryEfficientHashtable(int size) {
        keys = new Vector<>();
        values = new Vector<>();
    }

    public T2 get(T1 key) {
        for (int i = 0; i < keys.size(); i++)
            if (keys.elementAt(i).equals(key))
                return values.elementAt(i);
        return null;
    }

    public boolean containsKey(T1 key) {
        for (int i = 0; i < keys.size(); i++)
            if (keys.elementAt(i).equals(key))
                return true;
        return false;
    }

    public void put(T1 key, T2 value) {
        keys.addElement(key);
        values.addElement(value);
    }

    public void remove(T1 key) {
        int idx = -1;
        for (int i = 0; i < keys.size(); i++)
            if (keys.elementAt(i).equals(key))
                idx = i;
        keys.removeElementAt(idx);
        values.removeElementAt(idx);
    }
}
