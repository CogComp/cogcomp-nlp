package edu.illinois.cs.cogcomp.srl.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.IResetableIterator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.srl.caches.SentenceDBHandler;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.data.Dataset;
import edu.illinois.cs.cogcomp.srl.jlis.*;

import java.util.List;

public class ArgumentCandidateUtilities {

	public static interface CandidateFunction {
		public void run(SRLMulticlassInstance x, int goldY) throws Exception;
	}

	public static interface PredicateFunction {
		public void run(SRLPredicateInstance x, SRLPredicateStructure y)
				throws Exception;
	}

	public static void foreach(SRLManager manager, Dataset dataset,
			CandidateFunction f) throws Exception {
		foreach(manager, dataset, f, null);
	}

	public static void foreach(SRLManager manager, Dataset dataset,
			CandidateFunction f, PredicateFunction p) throws Exception {
		IResetableIterator<TextAnnotation> data = SentenceDBHandler.instance
				.getDataset(dataset);

		int count = 0;
		int sentenceCount = 0;
		int predicateCount = 0;
		while (data.hasNext()) {
			TextAnnotation ta = data.next();

			if (!ta.hasView(manager.getGoldViewName()))
				continue;

			Pair<SRLSentenceInstance, SRLSentenceStructure> examples = manager.exampleGenerator
					.getExamples(ta);

			sentenceCount++;

			for (int predicateId = 0; predicateId < examples.getFirst()
					.numPredicates(); predicateId++) {

				predicateCount++;

				SRLPredicateInstance x = examples.getFirst().predicates
						.get(predicateId);
				SRLPredicateStructure y = examples.getSecond().ys
						.get(predicateId);

				List<SRLMulticlassInstance> candidateInstances = x
						.getCandidateInstances();
				for (int candidateId = 0; candidateId < candidateInstances
						.size(); candidateId++) {

					SRLMulticlassInstance candidate = candidateInstances
							.get(candidateId);

					int gold = y.getArgLabel(candidateId);

					f.run(candidate, gold);
					count++;

					if (count % 10000 == 0) {
						System.out.println(count + " examples complete");
					}
				}

				if (p != null) {
					p.run(x, y);
				}

			}

		}

		System.out.println("Number of sentences: " + sentenceCount);
		System.out.println("Number of predicates: " + predicateCount);
		System.out.println("Number of candidates: " + count);

	}

}
