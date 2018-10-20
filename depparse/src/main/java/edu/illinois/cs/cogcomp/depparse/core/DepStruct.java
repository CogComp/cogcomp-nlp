/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.depparse.core;

import edu.illinois.cs.cogcomp.sl.core.IStructure;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * represents a dependency tree, where head[i] is the head of the ith token in the sentence(tokens
 * are indexed from 1..n). Deprels[i] contains the label for the edge from token i to its head.
 * 
 * @author Shyam
 *
 */
public class DepStruct implements IStructure {

    public int[] heads; // pos of heads of ith token is heads[i]
    public ArrayList<HashSet<Integer>> deps;
    public String[] deprels; // dependency relations

    public DepStruct(DepInst instance) {
        heads = instance.heads;
        deprels = instance.deprels;
        initializeDeps();
    }

    public DepStruct(int[] heads, String[] deprels) {
        if (heads.length != deprels.length)
            throw new IllegalArgumentException("Length of heads and deprels must be equal!");
        this.heads = heads;
        this.deprels = deprels;
        initializeDeps();
    }

    private void initializeDeps() {
        deps = new ArrayList<>(heads.length);
        for (int ignored : heads)
            deps.add(new HashSet<>());
        for (int i = 1; i < heads.length; i++) {
            deps.get(heads[i]).add(i);
        }
    }

    @Override
    public boolean equals(Object aThat) {
        if (!(aThat instanceof DepStruct))
            return false;
        DepStruct structThat = ((DepStruct) aThat);
        // check for self-comparison
        if (this == structThat)
            return true;
        if (this.heads.length != structThat.heads.length)
            return false;
        if (this.hashCode() != structThat.hashCode())
            return false;
        // check if their tags are the same
        for (int i = 0; i < this.heads.length; i++)
            if (heads[i] != structThat.heads[i] || deprels[i].equals(structThat.deprels[i]))
                return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (int head : heads)
            hashCode = hashCode * 31 + head;
        return hashCode;
    }

}
