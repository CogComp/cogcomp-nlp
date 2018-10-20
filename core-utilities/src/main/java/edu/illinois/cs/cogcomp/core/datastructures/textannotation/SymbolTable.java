/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * There is no point in storing structures with string labels. Instead, we can look them up in the
 * symtab.
 *
 * @author Vivek Srikumar
 */
class SymbolTable implements Serializable {

    /*
     * Experimented with using TIntIntHashMap to store the symbol table as a map from string
     * hashcodes to int. The problem is that string hashCode is not unique and this hurts even in
     * very simple cases. For example, "n't".hashCode() == "let".hashCode().
     * 
     * Have decided to switch to a map from string to Integer and pay the boxing and unboxing costs
     * instead of losing strings. It turns out that boxing is the more expensive operation, which is
     * done only in the add function. However, new strings are added infrequently.
     */
    private final Map<String, Integer> symtab;

    private final List<String> strings;

    public SymbolTable() {
        symtab = new HashMap<>();
        strings = new ArrayList<>();
    }

    public int getId(String label) {
        if (symtab.containsKey(label)) {
            int id = symtab.get(label);

            if (!strings.get(id).equals(label)) {
                System.out.println("Error with label " + label + ", confused with "
                        + strings.get(id));
                throw new RuntimeException();
            }

            return id;

        } else
            return -1;
    }

    public synchronized int add(String label) {
        int value = strings.size();
        strings.add(label);
        symtab.put(label, value);

        return value;
    }

    public String getLabel(int label) {
        return strings.get(label);
    }
}
