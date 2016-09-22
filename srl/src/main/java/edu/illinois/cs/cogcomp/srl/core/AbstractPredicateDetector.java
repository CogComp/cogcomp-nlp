/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl.core;

import edu.illinois.cs.cogcomp.core.datastructures.Option;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

import java.util.ArrayList;
import java.util.List;

/**
 * A skeleton for a predicate detector. This could be implemented either by heuristics (both verb
 * and nominalization SRL systems come with heuristics) and also a learned predicate detector.
 * 
 * @author Vivek Srikumar
 * 
 */
public abstract class AbstractPredicateDetector {

    private final SRLManager manager;

    public AbstractPredicateDetector(SRLManager manager) {
        this.manager = manager;
    }

    public boolean debug = false;

    public abstract Option<String> getLemma(TextAnnotation ta, int tokenId) throws Exception;

    public List<Constituent> getPredicates(TextAnnotation ta) throws Exception {
        List<Constituent> list = new ArrayList<>();

        for (int i = 0; i < ta.size(); i++) {
            Option<String> opt = getLemma(ta, i);

            if (opt.isPresent()) {
                Constituent c = new Constituent("", "", ta, i, i + 1);
                c.addAttribute(PredicateArgumentView.LemmaIdentifier, opt.get());
                list.add(c);
            }
        }

        return list;
    }

    public SRLManager getManager() {
        return manager;
    }

}
