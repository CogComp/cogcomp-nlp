/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.data;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;

import java.util.List;

public class PropbankFields {
    private String section, lemma, sense, identifier;

    private int predicateTerminal;

    String getSection() {
        return section;
    }

    String getIdentifier() {
        return identifier;
    }

    String getLemma() {
        return lemma;
    }

    String getSense() {
        return sense;
    }

    public PropbankFields(String line) {
        String[] fields = line.split("\\s");

        String wsjFileName = fields[0];
        int sentence = Integer.parseInt(fields[1]);
        predicateTerminal = Integer.parseInt(fields[2]);

        String roleSet = fields[4];

        section = wsjFileName.split("/")[1];

        identifier = wsjFileName + ":" + sentence;
        lemma = roleSet.substring(0, roleSet.indexOf('.'));
        sense = roleSet.substring(roleSet.indexOf(".") + 1);
    }

    public int getPredicateStart(List<Tree<Pair<String, IntPair>>> yield) {
        Tree<Pair<String, IntPair>> l = yield.get(predicateTerminal);
        return l.getLabel().getSecond().getFirst();
    }
}
