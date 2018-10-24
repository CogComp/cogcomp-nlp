/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class CollectUtil {

    public static <T> ArrayList<T> newList(){
        return new ArrayList<T>();
    }

    public static <T> ArrayList<T> newList(int size){
        return new ArrayList<T>(size);
    }

    public static <T> ArrayList<T> newList(Collection<T> collect){
        return new ArrayList<T>(collect);
    }

    public static <T> HashSet<T> newSet(){
        return new HashSet<T>();
    }

    public static <T> HashSet<T> newSet(int size){
        return new HashSet<T>(size);
    }

    public static <T> HashSet<T> newSet(Collection<T> collect){
        return new HashSet<T>(collect);
    }

}
