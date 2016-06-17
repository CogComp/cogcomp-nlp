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
