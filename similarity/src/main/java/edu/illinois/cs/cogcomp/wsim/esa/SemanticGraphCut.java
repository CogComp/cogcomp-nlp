/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wsim.esa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import edu.illinois.cs.cogcomp.classification.hierarchy.run.ClassifierConstant;

/**
 * yqsong@illinois.edu
 */

public class SemanticGraphCut {

	public static HashMap<Integer, Double> combineVectorsGraph(List<HashMap<Integer, Double>> vectorList,
			List<Double> scoreList, List<String> termList) {
		List<List<Integer>> clusters = computeCluster(vectorList);

		HashMap<Integer, Double> finalVector = new HashMap<Integer, Double>();

		for (int i = 0; i < clusters.size(); ++i) {
			HashMap<Integer, Double> clusterVector = new HashMap<Integer, Double>();
			List<Integer> clusterIds = clusters.get(i);
			if (clusterIds.size() > 0) {
				clusterVector = add(clusterVector, vectorList.get(clusterIds.get(0)), 1,
						scoreList.get(clusterIds.get(0)));

				if (clusterIds.size() > 1) {
					for (int j = 1; j < clusterIds.size(); ++j) {
						clusterVector = addWithBothNonZero(clusterVector, vectorList.get(clusterIds.get(j)), 1,
								scoreList.get(clusterIds.get(j)));
					}

					double decayFactor = (double) 1 / Math.log(clusterIds.size() + Math.exp(1));
					for (int j = 0; j < clusterIds.size(); ++j) {
						clusterVector = addWithDecayFactor(clusterVector, vectorList.get(clusterIds.get(j)),
								scoreList.get(clusterIds.get(j)) * decayFactor);
					}
				}

			}
			finalVector = add(finalVector, clusterVector, 1, 1);
		}

		return finalVector;
	}

	public static HashMap<Integer, Double> combineVectorsSimple(List<HashMap<Integer, Double>> vectorList,
			List<Double> scoreList) {

		HashMap<Integer, Double> finalVector = new HashMap<Integer, Double>();

		for (int i = 0; i < vectorList.size(); ++i) {
			finalVector = add(finalVector, vectorList.get(i), 1, scoreList.get(i));
		}

		return finalVector;
	}

	private static List<List<Integer>> computeCluster(List<HashMap<Integer, Double>> vectorList) {
		// List<Double> vectorNormList = new ArrayList<Double>();
		// for (int i = 0; i < vectorList.size(); ++i) {
		// vectorNormList.add(norm(vectorList.get(i)));
		// }

		int entityNum = vectorList.size();
		double[][] edgeWeight = new double[entityNum][entityNum];
		for (int i = 0; i < vectorList.size(); i++) {
			edgeWeight[i][i] = 1;
			for (int j = i + 1; j < vectorList.size(); j++) {
				// edgeWeight[i][j] = cosine(vectorList.get(i),
				// vectorList.get(j),
				// vectorNormList.get(i), vectorNormList.get(j));
				edgeWeight[i][j] = jaccard(vectorList.get(i), vectorList.get(j));
				edgeWeight[j][i] = edgeWeight[i][j];
			}
		}

		List<List<Integer>> clusters = new ArrayList<List<Integer>>();

		// flgList indicates whether an instance is alrealdy accepted by one
		// cluster
		List<Boolean> flgList = new ArrayList<Boolean>();
		for (int i = 0; i < vectorList.size(); i++) {
			flgList.add(true); // true means the i-th instance is not removed
		}

		for (int i = 0; i < vectorList.size(); i++) {
			// if the i-th instance is already accepted by one cluster, then
			// skip it
			if (flgList.get(i) == false)
				continue;

			// build a new cluster
			List<Integer> idxList = new ArrayList<Integer>();
			Stack<Integer> idxStack = new Stack<Integer>();
			idxStack.push(i);
			while (idxStack.size() != 0) {
				int k = idxStack.pop();
				// add indices to the cluster
				for (int j = 0; j < vectorList.size(); j++) {
					// skip if the j-th instance is already accepted by one
					// cluster
					if (flgList.get(j) == false)
						continue;
					// add the j-th instance to idxList
					if (edgeWeight[k][j] > ClassifierConstant.cutOff) {
						idxList.add(j);
						flgList.set(j, false);
						idxStack.push(j);
					}
				}
			}

			clusters.add(idxList);
		}
		return clusters;
	}

	private static double jaccard(HashMap<Integer, Double> v1, HashMap<Integer, Double> v2) {
		double score = 0;
		Set<Integer> set1 = new HashSet<Integer>(v1.keySet());
		Set<Integer> set2 = new HashSet<Integer>(v2.keySet());
		set1.retainAll(set2);
		int overlap = set1.size();
		return ((double) overlap) / (set1.size() + set2.size());
	}

	private static double cosine(HashMap<Integer, Double> v1, HashMap<Integer, Double> v2, double norm1, double norm2) {
		double cos = dot(v1, v2) / norm1 / norm2;
		return cos;
	}

	private static double dot(HashMap<Integer, Double> v1, HashMap<Integer, Double> v2) {
		double dot = 0;
		if (v1.size() < v2.size()) {
			for (Integer key : v1.keySet()) {
				if (v2.containsKey(key) == true) {
					double value1 = v1.get(key);
					double value2 = v2.get(key);
					dot += value1 * value2;
				}
			}
		} else {
			for (Integer key : v2.keySet()) {
				if (v1.containsKey(key) == true) {
					double value1 = v1.get(key);
					double value2 = v2.get(key);
					dot += value1 * value2;
				}
			}
		}
		return dot;
	}

	private static double norm(HashMap<Integer, Double> v) {
		double norm = 0;
		for (Integer key : v.keySet()) {
			double value = v.get(key);
			norm += value * value;
		}
		norm = Math.sqrt(norm);
		return norm;
	}

	private static HashMap<Integer, Double> add(HashMap<Integer, Double> v1, HashMap<Integer, Double> v2,
			double weight1, double weight2) {
		HashMap<Integer, Double> v3 = new HashMap<Integer, Double>();
		for (Integer key : v1.keySet()) {
			if (v2.containsKey(key) == false) {
				double value3 = v1.get(key) * weight1;
				v3.put(key, value3);
			}
		}
		for (Integer key : v2.keySet()) {
			if (v1.containsKey(key) == true) {
				double value1 = v1.get(key) * weight1;
				double value2 = v2.get(key) * weight2;
				double value3 = value1 + value2;
				v3.put(key, value3);
			} else {
				double value3 = v2.get(key) * weight2;
				v3.put(key, value3);
			}
		}
		return v3;
	}

	private static HashMap<Integer, Double> addWithDecayFactor(HashMap<Integer, Double> sumVector,
			HashMap<Integer, Double> additionalVector, double decayFactor) {
		HashMap<Integer, Double> v3 = new HashMap<Integer, Double>();
		for (Integer key : additionalVector.keySet()) {
			if (sumVector.containsKey(key) == false) {
				double value3 = additionalVector.get(key) * decayFactor;
				v3.put(key, value3);
			}
		}
		return v3;
	}

	private static HashMap<Integer, Double> addWithBothNonZero(HashMap<Integer, Double> v1, HashMap<Integer, Double> v2,
			double weight1, double weight2) {
		HashMap<Integer, Double> v3 = new HashMap<Integer, Double>();
		if (v1.size() < v2.size()) {
			for (Integer key : v1.keySet()) {
				if (v2.containsKey(key) == true) {
					double value1 = v1.get(key) * weight1;
					double value2 = v2.get(key) * weight2;
					double value3 = value1 + value2;
					v3.put(key, value3);
				}
			}
		} else {
			for (Integer key : v2.keySet()) {
				if (v1.containsKey(key) == true) {
					double value1 = v1.get(key) * weight1;
					double value2 = v2.get(key) * weight2;
					double value3 = value1 + value2;
					v3.put(key, value3);
				}
			}
		}
		return v3;
	}
}
