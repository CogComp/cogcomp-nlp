/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.core;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.verbsense.jlis.SenseInstance;
import edu.illinois.cs.cogcomp.verbsense.jlis.SenseStructure;
import edu.illinois.cs.cogcomp.verbsense.jlis.SentenceInstance;
import edu.illinois.cs.cogcomp.verbsense.jlis.SentenceStructure;

import java.util.ArrayList;
import java.util.List;

public class SenseExampleGenerator {
    private SenseManager manager;

    public SenseExampleGenerator(SenseManager manager) {
        this.manager = manager;
    }

    public Pair<SentenceInstance, SentenceStructure> getExamples(TextAnnotation ta)
            throws Exception {
        List<SenseInstance> predicates = new ArrayList<>();
        List<SenseStructure> structures = new ArrayList<>();

        if (ta.hasView(SenseManager.getGoldViewName()))
            getTreebankExamples(ta, predicates, structures);
        else
            getExamples(ta, predicates);

        SentenceInstance sx = new SentenceInstance(predicates);
        SentenceStructure sy = new SentenceStructure(sx, structures);

        return new Pair<>(sx, sy);
    }

    /**
     * Generates SRL examples using the predicate detector to identify predicates
     */
    private void getExamples(TextAnnotation ta, List<SenseInstance> predicates) throws Exception {
        PredicateDetector predicateDetector = manager.getPredicateDetector();

        for (Constituent predicate : predicateDetector.getPredicates(ta)) {
            if (!predicate.hasAttribute(PredicateArgumentView.LemmaIdentifier)) {
                System.out.println(ta);
                System.out.println(predicate + " has no lemma!");
                assert false;
            }

            SenseInstance x = new SenseInstance(predicate, manager);
            predicates.add(x);
        }
    }

    private void getTreebankExamples(TextAnnotation ta, List<SenseInstance> predicates,
            List<SenseStructure> structures) {
        TokenLabelView view = (TokenLabelView) ta.getView(SenseManager.getGoldViewName());

        for (Constituent predicate : view.getConstituents()) {
            if (!predicate.hasAttribute(PredicateArgumentView.LemmaIdentifier)) {
                System.out.println(ta);
                System.out.println(view);
                System.out.println(predicate + " has no lemma!");
                assert false;
            }

            SenseInstance x = new SenseInstance(predicate, manager);

            int sense = manager.getSenseId(predicate.getLabel());
            SenseStructure y = new SenseStructure(x, sense, manager);

            predicates.add(x);
            structures.add(y);
        }
    }
}
