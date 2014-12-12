package edu.illinois.cs.cogcomp.srl.inference;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.stats.OneVariableStats;
import edu.illinois.cs.cogcomp.edison.data.CoNLLColumnFormatReader;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.PredicateArgumentView;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.infer.graph.ShortestPathInference;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.jlis.SRLMulticlassInstance;
import edu.illinois.cs.cogcomp.srl.jlis.SRLPredicateInstance;
import edu.illinois.cs.cogcomp.srl.jlis.SRLSentenceInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implements SRL inference as a dynamic program with only the no-overlap
 * constraints and valid labels constraints.
 * 
 * Also, allows incoming messages, to facilitate adding other constraints via
 * lagrangian relaxiation. (See SRLLangrangeInference).
 * 
 * @author Vivek Srikumar
 * 
 */
public class NoOverlapInference implements ISRLInference {

	protected boolean debug = false;

	protected SRLManager manager;

	protected boolean standardIdClPipeline;

	private boolean hasCands;
	protected TextAnnotation ta;
	protected String viewName;
	protected int numPredicates;

	private final double[][] modelSenseScores, incomingSenseScores;

	private final double[][][] modelClassifierScores, incomingClassifierScores;

	protected SRLSentenceInstance instance;

	protected double averageScoreMargin;

	public NoOverlapInference(SRLManager manager, TextAnnotation ta,
			List<Constituent> predicates, boolean standardIdClPipeline) {
		this.manager = manager;

		this.standardIdClPipeline = standardIdClPipeline;
		this.ta = ta;

		if (predicates.size() > 0) {

			List<SRLPredicateInstance> instances = new ArrayList<SRLPredicateInstance>();

			hasCands = false;
			for (Constituent predicate : predicates) {

				Constituent predicateClone = predicate
						.cloneForNewView(predicate.getViewName());
				SRLPredicateInstance x;

				assert predicateClone
						.hasAttribute(CoNLLColumnFormatReader.LemmaIdentifier);

				if (this.standardIdClPipeline)
					x = new SRLPredicateInstance(predicateClone, manager,
							manager.getArgumentIdentifier());
				else
					x = new SRLPredicateInstance(predicateClone, manager);

				x.cacheAllFeatureVectors(false);

				instances.add(x);

				if (x.getCandidateInstances().size() > 0)
					hasCands = true;

			}

			this.instance = new SRLSentenceInstance(instances);
			numPredicates = instances.size();
		}

		viewName = manager.getPredictedViewName();

		modelSenseScores = new double[numPredicates][];
		modelClassifierScores = new double[numPredicates][][];

		incomingSenseScores = new double[numPredicates][];
		incomingClassifierScores = new double[numPredicates][][];

		cacheScores(manager);

	}

	private void cacheScores(SRLManager manager) {

		OneVariableStats classifierScoreRange = new OneVariableStats();

		for (int predicateId = 0; predicateId < numPredicates; predicateId++) {

			SRLPredicateInstance x = this.instance.predicates.get(predicateId);

			SRLMulticlassInstance senseX = x.getSenseInstance();
			List<SRLMulticlassInstance> candidates = x.getCandidateInstances();

			modelSenseScores[predicateId] = manager.getScores(senseX,
					Models.Sense, true);
			incomingSenseScores[predicateId] = new double[modelSenseScores[predicateId].length];

			int numLabels = manager.getNumLabels(Models.Classifier);

			int numCandidates = candidates.size();
			modelClassifierScores[predicateId] = new double[numCandidates][numLabels];
			incomingClassifierScores[predicateId] = new double[numCandidates][numLabels];

			for (int candidateId = 0; candidateId < numCandidates; candidateId++) {
				SRLMulticlassInstance cX = candidates.get(candidateId);
				double[] scores = manager
						.getScores(cX, Models.Classifier, true);

				double idScore = 0;
				if (!this.standardIdClPipeline) {
					double[] sc = manager
							.getScores(cX, Models.Identifier, true);
					idScore = sc[1] - sc[0];
				}

				double max = Double.NEGATIVE_INFINITY;
				double secondMax = Double.NEGATIVE_INFINITY;

				for (int labelId = 0; labelId < numLabels; labelId++) {
					String label = manager.getArgument(labelId);

					double score = scores[labelId];

					if (!manager.isValidLabel(cX, Models.Classifier, labelId))
						score = -10000; // A large negative number that will
										// underwhelm everything else

					if (label.equals(SRLManager.NULL_LABEL))
						score -= idScore;

					modelClassifierScores[predicateId][candidateId][labelId] = score;

					if (score > max) {
						secondMax = max;
						max = score;
					} else if (score > secondMax) {
						secondMax = score;
					}

				}

				double range = Math.abs(max - secondMax);

				classifierScoreRange.add(range);
			}

		}

		averageScoreMargin = classifierScoreRange.min();

	}

	public boolean hasCandidates() {
		return hasCands;
	}

	public PredicateArgumentView runInference() {

		Pair<int[][], Double> pair = getAssignment();

		return getOutput(pair.getFirst(), pair.getSecond());
	}

	protected Pair<int[][], Double> getAssignment() {
		double score = 0;

		int[][] assignments = new int[numPredicates][];

		for (int predicateId = 0; predicateId < numPredicates; predicateId++) {

			SRLPredicateInstance x = this.instance.predicates.get(predicateId);

			Pair<int[], Double> assignment = getPredicateAssignment(
					predicateId, x);
			assignments[predicateId] = assignment.getFirst();
			score += assignment.getSecond();

		}

		return new Pair<int[][], Double>(assignments, score);
	}

	// convention for assignment array:
	// assignment[0] = sense label
	// assignment[1...] = argument labels (including nulls)

	protected PredicateArgumentView getOutput(int[][] assignments, double score) {
		PredicateArgumentView pav = new PredicateArgumentView(viewName,
				manager.getSRLSystemIdentifier(), ta, score);

		int nullId = manager.getArgumentId(SRLManager.NULL_LABEL);

		for (int predicateId = 0; predicateId < numPredicates; predicateId++) {

			List<String> relations = new ArrayList<String>();
			List<Constituent> args = new ArrayList<Constituent>();

			SRLPredicateInstance x = this.instance.predicates.get(predicateId);

			int numCandidates = x.getCandidateInstances().size();
			assert assignments[predicateId].length == numCandidates + 1;

			SRLMulticlassInstance senseInstance = x.getSenseInstance();
			IntPair predicateSpan = senseInstance.getSpan();
			String lemma = senseInstance.getPredicateLemma();

			Constituent predicate = newConstituent("Predicate", predicateSpan);
			predicate.addAttribute(CoNLLColumnFormatReader.LemmaIdentifier,
					lemma);

			String senseLabel = manager.getSense(assignments[predicateId][0]);
			predicate.addAttribute(CoNLLColumnFormatReader.SenseIdentifer,
					senseLabel);

			for (int candidateId = 0; candidateId < numCandidates; candidateId++) {
				SRLMulticlassInstance candidateInstance = x
						.getCandidateInstances().get(candidateId);

				int labelId = assignments[predicateId][candidateId + 1];

				if (labelId != nullId) {
					String label = manager.getArgument(labelId);

					args.add(newConstituent(label, candidateInstance.getSpan()));
					relations.add(label);
				}
			}
			pav.addPredicateArguments(predicate, args,
					relations.toArray(new String[relations.size()]),
					new double[relations.size()]);

		}

		return pav;
	}

	private Constituent newConstituent(String label, IntPair span) {
		return new Constituent(label, viewName, ta, span.getFirst(),
				span.getSecond());
	}

	private Pair<int[], Double> getPredicateAssignment(int predicateId,
			SRLPredicateInstance x) {

		SRLMulticlassInstance senseX = x.getSenseInstance();
		List<SRLMulticlassInstance> candidates = x.getCandidateInstances();

		int[] assignment = new int[candidates.size() + 1];
		double score = 0;

		String lemma = senseX.getPredicateLemma();
		assert lemma != null;

		// first let's predict the sense
		double max = Double.NEGATIVE_INFINITY;
		int senseId = -1;
		for (int i = 0; i < manager.getNumLabels(Models.Sense); i++) {

			if (!manager.isValidSense(lemma, i))
				continue;

			double sc = getSenseScore(predicateId, i);

			if (sc > max) {
				max = sc;
				senseId = i;
			}
		}

		assert senseId != -1;

		assignment[0] = senseId;
		score += max;

		// then predict the arguments, if there are any candidates
		if (x.getCandidateInstances().size() > 0) {

			ShortestPathInference<IntPair> inference = createGraph(predicateId,
					x);
			List<IntPair> path = inference.runInference();

			int nullId = manager.getArgumentId(SRLManager.NULL_LABEL);

			for (int i = 1; i < assignment.length; i++) {
				int candidateId = i - 1;
				assignment[i] = nullId;

				score += getClassifierScore(predicateId, candidateId, nullId);
			}

			// if (debug) {
			// System.out.println("Lemma: " + lemma);
			// }

			for (IntPair n : path) {
				int candidateId = n.getFirst();
				int labelId = n.getSecond();

				// source
				if (candidateId == -1 && labelId == -1)
					continue;

				// sink
				if (candidateId == -2 && labelId == -2)
					continue;

				// if (candidateId >= 0)
				assignment[candidateId + 1] = labelId;

				double labelScore = getClassifierScore(predicateId,
						candidateId, labelId);
				double nullScore = getClassifierScore(predicateId, candidateId,
						nullId);

				// if (debug) {
				// String label = manager.getArgument(labelId);
				// String candidate = x.getCandidateInstances()
				// .get(candidateId).getConstituent().toString();
				// System.out.println("Candidate " + candidate + ", label ="
				// + label + ", labelScore = " + labelScore
				// + " nullScore = " + nullScore);
				// }
				score += labelScore;
				score -= nullScore;
			}
		}

		return new Pair<int[], Double>(assignment, score);

	}

	private ShortestPathInference<IntPair> createGraph(int predicateId,
			SRLPredicateInstance x) {

		int numCandidates = x.getCandidateInstances().size();

		// Build a graph with the following nodes:
		// Source <-1,-1>
		// Sink <-2, -2>
		// all other nodes: <candidateId, labelId>

		ShortestPathInference<IntPair> inference = new ShortestPathInference<IntPair>();

		IntPair source = new IntPair(-1, -1);
		IntPair sink = new IntPair(-2, -2);

		inference.addNode(source, 0);
		inference.addNode(sink, 0);
		inference.setSource(source);
		inference.setTarget(sink);

		// For each candidate c, foreach label l except null,
		// N_{c,l}

		int numLabels = manager.getNumLabels(Models.Classifier);

		int nullId = manager.getArgumentId(SRLManager.NULL_LABEL);

		String lemma = x.getSenseInstance().getPredicateLemma();
		Set<String> legalArgsSet = manager.getLegalArguments(lemma);

		for (int candidateId = 0; candidateId < numCandidates; candidateId++) {

			for (int labelId = 0; labelId < numLabels; labelId++) {

				if (!validLabel(nullId, legalArgsSet, labelId))
					continue;

				// add a node
				IntPair node = new IntPair(candidateId, labelId);

				inference.addNode(node, 0);
			}

		}

		// Edges:
		// N_{c,l} -> Sink, 0, for all c, non-null l
		for (int candidateId = 0; candidateId < numCandidates; candidateId++) {

			// System.out.println("Adding edge from (candidate-" + candidateId
			// + ", all-labels) to sink");
			for (int l = 0; l < numLabels; l++) {
				if (!validLabel(nullId, legalArgsSet, l))
					continue;

				IntPair pair = new IntPair(candidateId, l);

				inference.addEdge(pair, sink, 0d);
			}
		}

		// Reminder: all scores are negated because we want to find the LONGEST
		// path, but the library finds the shortest one.

		//
		// Source -> N{c, l}, \sum_c score(c, null) + score(c,l) - score(c,
		// null)

		// String ns = "";
		double nullSum = 0;
		for (int candidateId = 0; candidateId < numCandidates; candidateId++) {
			double d = getClassifierScore(predicateId, candidateId, nullId);
			nullSum += d;

			// ns += (" + " + StringUtils.getFormattedString(d, 3));
		}

		// System.out.println("Null Sum =" + nullSum);

		// Set<String> argsToPrint = new HashSet<String>(Arrays.asList("A0",
		// "A1",
		// "A2", "A3", "A4", "AM-LOC", "AM-TMP"));

		for (int candidateId = 0; candidateId < numCandidates; candidateId++) {

			// System.out.println("Argument candidate "
			// + candidateId
			// + ": "
			// + x.getCandidateInstances().get(candidateId)
			// .getConstituent());
			//
			double nullScore = getClassifierScore(predicateId, candidateId,
					nullId);

			// System.out.println("<NULL>: " + nullScore);

			for (int l = 0; l < numLabels; l++) {

				if (!validLabel(nullId, legalArgsSet, l))
					continue;

				double labelScore = getClassifierScore(predicateId,
						candidateId, l);
				double score = nullSum + labelScore - nullScore;
				IntPair labelNode = new IntPair(candidateId, l);

				// String sc = ns + " - "
				// + StringUtils.getFormattedString(nullScore, 3);
				//
				// sc = sc + " + " + StringUtils.getFormattedString(labelScore,
				// 3)
				// + " = " + StringUtils.getFormattedString(score, 3);

				// String argument = manager.getArgument(l);

				// if (argsToPrint.contains(argument))
				// System.out.println(argument + ": " + source + "--->" + sc
				// + "-->" + labelNode + ", label score = "
				// + StringUtils.getFormattedString(labelScore, 3));

				inference.addEdge(source, labelNode, -score);
			}
		}

		//
		// N_{c1,l1} -> N_{c2,l2}, score(c2,l2) - score(c2, null), for
		// all c1,valid l1,l2 and c2 such that c2
		// starts after c1 ends

		// System.out.println("Valid transitions");
		for (int c2 = 0; c2 < numCandidates; c2++) {
			SRLMulticlassInstance c2Instance = x.getCandidateInstances()
					.get(c2);
			int c2Start = c2Instance.getSpan().getFirst();

			// System.out.println("Considering c2 = "
			// + c2Instance.getConstituent() + ", starting at " + c2Start);

			for (int l2 = 0; l2 < numLabels; l2++) {
				if (!validLabel(nullId, legalArgsSet, l2))
					continue;

				IntPair N_c2_l2 = new IntPair(c2, l2);

				double l2Score = getClassifierScore(predicateId, c2, l2);
				double nullScore = getClassifierScore(predicateId, c2, nullId);
				double score = l2Score - nullScore;

				// String sc = StringUtils.getFormattedString(l2Score, 3) +
				// " - "
				// + StringUtils.getFormattedString(nullScore, 3) + " = "
				// + StringUtils.getFormattedString(score, 3);

				for (int c1 = 0; c1 < numCandidates; c1++) {

					if (c1 == c2)
						continue;

					SRLMulticlassInstance c1Instance = x
							.getCandidateInstances().get(c1);
					int c1End = c1Instance.getSpan().getSecond();

					if (c1End > c2Start) {
						continue;
					}

					for (int l1 = 0; l1 < numLabels; l1++) {
						if (!validLabel(nullId, legalArgsSet, l1))
							continue;

						IntPair N_c1_l1 = new IntPair(c1, l1);

						// if (debug)
						// if (argsToPrint.contains(manager.getArgument(l1))
						// && argsToPrint.contains(manager
						// .getArgument(l2))) {
						//
						// String n1 = c1Instance.getConstituent() + ":"
						// + manager.getArgument(l1);
						// String n2 = c2Instance.getConstituent() + ":"
						// + manager.getArgument(l2);
						//
						// System.out.println(n1 + "--->" + sc + "--->"
						// + n2);
						// }

						inference.addEdge(N_c1_l1, N_c2_l2, -score);
					}

				}
			}
		}

		return inference;
	}

	private boolean validLabel(int nullId, Set<String> legalArgsSet, int l) {
		boolean ll = true;
		if (l == nullId)
			ll = false;
		String label = manager.getArgument(l);
		if (!legalArgsSet.contains(label))
			ll = false;
		return ll;
	}

	private double getClassifierScore(int predicateId, int candidateId,
			int labelId) {
		assert predicateId >= 0;
		assert candidateId >= 0;
		assert labelId >= 0;

		double modelScore = modelClassifierScores[predicateId][candidateId][labelId];

		double incoming = incomingClassifierScores[predicateId][candidateId][labelId];
		if (debug)
			if (incoming != 0) {

				System.out.println(getVariableDescriptor(predicateId,
						candidateId, labelId)
						+ "\tModel score = "
						+ modelScore
						+ "\tIncoming score = "
						+ incoming
						+ "\tEffective score = " + (modelScore + incoming));
			}

		return modelScore + incoming;
	}

	private double getSenseScore(int predicateId, int senseId) {
		double modelScore = modelSenseScores[predicateId][senseId];
		double incoming = incomingSenseScores[predicateId][senseId];

		if (debug)
			if (incoming != 0) {
				System.out.println(getVariableDescriptor(predicateId, senseId)
						+ "\tModel score = " + modelScore
						+ "\tIncoming score = " + incoming
						+ "\tEffective score = " + (modelScore + incoming));
			}

		return modelScore + incoming;
	}

	public void addToIncomingSenseScore(int predicateId, int senseId,
			double score) {
		incomingSenseScores[predicateId][senseId] += score;
	}

	public void addToIncomingArgumentScore(int predicateId, int candidateId,
			int label, double score) {
		incomingClassifierScores[predicateId][candidateId][label] += score;
	}

	public void resetIncomingSenseScores() {
		for (int i = 0; i < incomingSenseScores.length; i++) {
			for (int j = 0; j < incomingSenseScores[i].length; j++)
				incomingSenseScores[i][j] = 0;
		}
	}

	public void resetIncomingArgumentScore() {
		for (int i = 0; i < incomingClassifierScores.length; i++) {
			for (int j = 0; j < incomingClassifierScores[i].length; j++)
				for (int k = 0; k < incomingClassifierScores[i][j].length; k++)
					incomingClassifierScores[i][j][k] = 0;
		}

	}

	@Override
	public PredicateArgumentView getOutputView() throws Exception {
		return runInference();
	}

	protected String getVariableDescriptor(int predicateId, int candidateId,
			int labelId) {
		SRLPredicateInstance pred = this.instance.predicates.get(predicateId);

		return pred.getSenseInstance().getPredicateLemma()
				+ ": "
				+ pred.getCandidateInstances().get(candidateId)
						.getConstituent() + ": " + manager.getArgument(labelId);
	}

	protected String getVariableDescriptor(int predicateId, int senseId) {
		SRLPredicateInstance pred = this.instance.predicates.get(predicateId);

		return pred.getSenseInstance().getPredicateLemma() + ": sense = "
				+ manager.getSense(senseId);

	}

	public void setDebug() {
		this.debug = true;
	}

}
