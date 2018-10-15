/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.AbstractSRLAnnotationReader.Fields;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Vivek Srikumar
 */
public class NombankReader extends AbstractSRLAnnotationReader {
    public NombankReader(String treebankHome, String nombankHome, String[] sections,
            String srlViewName, boolean mergeContiguousCArgs) throws Exception {

        super(treebankHome, nombankHome, sections, srlViewName, mergeContiguousCArgs);
    }

    @Override
    protected Fields readFields(String line) {
        return new NombankFields(line);
    }

    @Override
    protected String getDataFile(String dataHome) {
        return dataHome + "/nombank.1.0";
    }

}


class NombankFields extends Fields {

    final List<GoldLabel> nomLabels;

    public NombankFields(String line) {
        super(line);
        String[] fields = line.split("\\s+");

        wsjFileName = fields[0];
        sentence = Integer.parseInt(fields[1]);
        predicateTerminal = Integer.parseInt(fields[2]);

        lemma = fields[3];
        sense = fields[4];
        nomLabels = new ArrayList<>();

        Set<String> seen = new HashSet<>();

        for (int i = 5; i < fields.length; i++) {
            GoldLabel goldLabel = new GoldLabel(fields[i]);

            // An ugly hack to deal with the handful of cases where Nombank
            // repeats some fields.
            if (!seen.contains(goldLabel.toString())) {

                nomLabels.add(goldLabel);
                seen.add(goldLabel.toString());
            }
        }

        section = wsjFileName.split("/")[1];

        identifier = wsjFileName + ":" + sentence;

    }

    @Override
    public Constituent createPredicate(TextAnnotation ta, String viewName,
            List<Tree<Pair<String, IntPair>>> yield) {
        Tree<Pair<String, IntPair>> l = yield.get(predicateTerminal);
        int start = l.getLabel().getSecond().getFirst();
        Constituent predicate = new Constituent("Predicate", viewName, ta, start, start + 1);

        predicate.addAttribute(PropbankReader.LemmaIdentifier, lemma);
        predicate.addAttribute(PropbankReader.SenseIdentifier, sense);

        return predicate;
    }

    @Override
    public List<? extends GoldLabel> getGoldLabels() {
        return nomLabels;
    }
}
