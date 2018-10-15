/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.jlis;

import edu.illinois.cs.cogcomp.sl.core.IInstance;

import java.util.List;

public class SentenceInstance implements IInstance {

    public final List<SenseInstance> predicates;
    private int size;

    public SentenceInstance(List<SenseInstance> predicates) {
        this.predicates = predicates;
        size = predicates.size();
    }

    @Override
    public double size() {
        return size;
    }


    public int numPredicates() {
        return predicates.size();
    }
}
