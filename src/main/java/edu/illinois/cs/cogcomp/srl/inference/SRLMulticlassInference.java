package edu.illinois.cs.cogcomp.srl.inference;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.jlis.SRLMulticlassInstance;
import edu.illinois.cs.cogcomp.srl.jlis.SRLMulticlassLabel;

@SuppressWarnings("serial")
public class SRLMulticlassInference extends AbstractInferenceSolver {

	private final Models type;
	private final SRLManager manager;
	private boolean stepThrough;

	public SRLMulticlassInference(SRLManager manager, Models type) {
		this.manager = manager;
		this.type = type;
	}

	public void stepThrough() {
		stepThrough = true;
	}

	@Override
	public IStructure getLossAugmentedBestStructure(
			WeightVector weight, IInstance ins, IStructure goldStructure)
					throws Exception {
		SRLMulticlassInstance x = (SRLMulticlassInstance) ins;
		SRLMulticlassLabel yGold = null;
		if (goldStructure != null)
			yGold = (SRLMulticlassLabel) goldStructure;

		int numLabels = manager.getNumLabels(type);
		assert numLabels > 0;

		if (type == Models.Identifier)
			assert numLabels == 2;

		double max = Double.NEGATIVE_INFINITY;
		SRLMulticlassLabel best = null;
		double loss = 0;

		if (stepThrough) {
			System.out.println("Stepping through inference");
		}

		for (int label = 0; label < numLabels; label++) {

			if (stepThrough) {
				System.out.println("Label: " + manager.getArgument(label));
			}

			if (!manager.isValidLabel(x, type, label)) {
				if (stepThrough)
					System.out.println("Label is not valid for "
							+ x.getPredicateLemma());
				continue;
			}

			SRLMulticlassLabel y = new SRLMulticlassLabel(x, label, type,
					manager);

			double score = weight.dotProduct(x.getCachedFeatureVector(type),label * manager.getModelInfo(type).getLexicon().size());

			if (stepThrough)
				System.out.println("\t Score = " + score);

			double l = 0;
			if (goldStructure != null) {
				if (yGold.getLabel() != label)
					l++;
			}

			if (score + l > max + loss) {
				max = score;
				loss = l;
				best = y;
				if (stepThrough)
					System.out.println("\t\tBest so far");
			}

		}

		if (best == null) {
			if (type == Models.Sense) {
				System.out.println(ins);
				System.out
				.println(manager.getLegalSenses(x.getPredicateLemma()));
			}

		}

		if (stepThrough) {
			System.out.println("\nBest label: "
					+ manager.getArgument(best.getLabel()));
		}

		assert best != null : type + "\t" + ins;
//		return new Pair<IStructure, Double>(best, loss);
return best;
	}

	@Override
	public float getLoss(IInstance ins, IStructure gold, IStructure pred) {
		SRLMulticlassLabel yGold = (SRLMulticlassLabel) gold;
		SRLMulticlassLabel ypred= (SRLMulticlassLabel) pred;
		double l=0;
			if (yGold.getLabel() != ypred.getLabel())
				l++;

		return 0;
	}

	@Override
	public IStructure getBestStructure(WeightVector weight, IInstance ins)
			throws Exception {
		return getLossAugmentedBestStructure(weight, ins, null);
	}

	@Override
	public Object clone() {
		return new SRLMulticlassInference(manager, type);
	}
}