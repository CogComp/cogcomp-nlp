/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.helpers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Srikumar
 */
public class PredicateArgumentHelpers {
    public static Constituent getPredicate(Constituent argument) {
        return argument.getIncomingRelations().get(0).getSource();
    }

    /**
     * Assumes that a predicate is a single word.
     *
     * @param argument
     * @return
     */
    public static int getPredicatePosition(Constituent argument) {
        return getPredicate(argument).getStartSpan();
    }

    public static List<Relation> getArguments(Constituent predicate) {
        return predicate.getOutgoingRelations();
    }

    public static List<Constituent> getArgumentConstituents(Constituent predicate) {
        List<Constituent> args = new ArrayList<>();

        for (Relation r : getArguments(predicate)) {
            args.add(r.getTarget());
        }
        return args;
    }
}
