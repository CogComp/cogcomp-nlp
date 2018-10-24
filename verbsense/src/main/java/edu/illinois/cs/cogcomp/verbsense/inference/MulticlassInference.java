/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.inference;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.inference.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.illinois.cs.cogcomp.verbsense.core.SenseManager;
import edu.illinois.cs.cogcomp.verbsense.jlis.SenseInstance;
import edu.illinois.cs.cogcomp.verbsense.jlis.SenseStructure;

@SuppressWarnings("serial")
public class MulticlassInference extends AbstractInferenceSolver {
    private final SenseManager manager;

    public MulticlassInference(SenseManager manager) {
        this.manager = manager;
    }

    @Override
    public Pair<IStructure, Double> getLossAugmentedBestStructure(WeightVector weight,
            IInstance ins, IStructure goldStructure) throws Exception {
        SenseInstance x = (SenseInstance) ins;
        SenseStructure yGold = null;
        if (goldStructure != null)
            yGold = (SenseStructure) goldStructure;

        int numLabels = manager.getNumLabels();
        assert numLabels > 0;

        double max = Double.NEGATIVE_INFINITY;
        SenseStructure best = null;
        double loss = 0;


        for (int label = 0; label < numLabels; label++) {
            if (!manager.isValidLabel(x, label))
                continue;

            SenseStructure y = new SenseStructure(x, label, manager);

            double score = weight.dotProduct(y.getFeatureVector());

            double l = 0;
            if (goldStructure != null) {
                if (yGold.getLabel() != label)
                    l++;
            }

            if (score + l > max + loss) {
                max = score;
                loss = l;
                best = y;
            }
        }
        if (best == null) {
            System.out.println(ins);
            System.out.println(manager.getLegalSenses(x.getPredicateLemma()));
        }
        return new Pair<IStructure, Double>(best, loss);
    }

    @Override
    public IStructure getBestStructure(WeightVector weight, IInstance ins) throws Exception {
        return getLossAugmentedBestStructure(weight, ins, null).getFirst();
    }
}
