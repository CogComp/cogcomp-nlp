/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl.core;


import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

/**
 * The base class for argument candidate generators.
 * 
 * @author Vivek Srikumar
 * 
 */
public abstract class ArgumentCandidateGenerator {

    protected final SRLManager manager;

    public ArgumentCandidateGenerator(SRLManager manager) {
        this.manager = manager;
    }

    public abstract String getCandidateViewName();

    public abstract List<Constituent> generateCandidates(Constituent predicate);

    protected Constituent getNewConstituent(TextAnnotation ta, Constituent predicateClone,
            int start, int end) {

        Constituent newConstituent =
                new Constituent("", 1.0, getCandidateViewName(), ta, start, end);

        new Relation("ChildOf", predicateClone, newConstituent, 1.0);
        return newConstituent;
    }

}
