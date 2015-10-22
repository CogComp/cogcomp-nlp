package edu.illinois.cs.cogcomp.core.datastructures.trees;

public interface INodeReader<T> {
    T parseNode(String string);
}
