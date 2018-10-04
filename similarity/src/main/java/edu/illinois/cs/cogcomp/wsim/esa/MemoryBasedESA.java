/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wsim.esa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.ConceptData;
import edu.illinois.cs.cogcomp.classification.hierarchy.datastructure.TermData;
import edu.illinois.cs.cogcomp.config.SimConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

public class MemoryBasedESA {

	public static String tfType = "log";
	public static HashMap<String, HashMap<Integer, Double>> wordVectors = null;
	public static HashMap<String, Double> wordIDF = null;

	public static boolean isUseGraphCut = false;

	private HashMap<String, String> pageIdTitleMapping;

	public MemoryBasedESA(File memorybasedESA, File pageIDMapping) {

		wordVectors = new HashMap<String, HashMap<Integer, Double>>();
		wordIDF = new HashMap<String, Double>();
		int cc = 0;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(memorybasedESA));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {

				line = line.trim();
				if (line.length() > 0) {
					String[] arr = line.split("\t");
					String word = arr[0];
					String vec = arr[1] + "\t" + arr[2];

					String[] conceptValues = arr[2].split(";");
					wordVectors.put(word, new HashMap<Integer, Double>());
					for (int i = 0; i < conceptValues.length; ++i) {
						String[] tokens = conceptValues[i].split(",");
						wordVectors.get(word).put(Integer.parseInt(tokens[0]), Double.parseDouble(tokens[1]));
					}
					double idf = Double.parseDouble(arr[1]);
					wordIDF.put(word, idf);

				}
				cc++;
				if (cc % 100000 == 0)
					System.out.print("[Cached ESA word number]" + cc + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		pageIdTitleMapping = new HashMap<String, String>();

		try {
			//System.out.println("Read mapping file: " + mappingFile.getAbsolutePath());
			FileReader mappReader = new FileReader(pageIDMapping);
			BufferedReader bf = new BufferedReader(mappReader);
			String line = "";
			while ((line = bf.readLine()) != null) {
				if (line.equals("") == true)
					continue;
				String[] tokens = line.split("\t");
				if (tokens.length != 2)
					continue;
				if (pageIdTitleMapping.containsKey(tokens[0].trim()) == false) {
					pageIdTitleMapping.put(tokens[0], tokens[1]);
				}
			}
			System.out.println("Done.");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public List<ConceptData> retrieveConcepts(String query, int topK, String vectorField) {

		query = QueryPreProcessor.process(query);

		HashMap<Integer, Double> conceptVector = getConceptVectorBasedonSegmentation(query, topK, vectorField);

		List<ConceptData> concepts = new ArrayList<ConceptData>();
		try {
			if (conceptVector == null || conceptVector.size() == 0)
				return concepts;

			for (Integer key : conceptVector.keySet()) {
				double value = conceptVector.get(key);
				ConceptData concept = new ConceptData(key + "", value);
				concepts.add(concept);
			}

			Collections.sort(concepts);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return concepts;
	}

	public List<ConceptData> retrieveConceptNames(String query, int topK, String vectorField) {

		query = QueryPreProcessor.process(query);

		HashMap<Integer, Double> conceptVector = getConceptVectorBasedonSegmentation(query, topK, vectorField);

		List<ConceptData> concepts = new ArrayList<ConceptData>();
		try {
			if (conceptVector == null || conceptVector.size() == 0)
				return concepts;

			for (Integer key : conceptVector.keySet()) {
				double value = conceptVector.get(key);
				ConceptData concept = new ConceptData(
						pageIdTitleMapping.get(key + "").replaceAll(",", "").replaceAll(";", "").replaceAll("\t", ""),
						value);
				concepts.add(concept);
			}

			Collections.sort(concepts);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return concepts;
	}

	public List<ConceptData> translateIDToConceptString(List<ConceptData> conceptList) {
		List<ConceptData> newList = new ArrayList<ConceptData>();
		for (int i = 0; i < conceptList.size(); ++i) {
			String key = conceptList.get(i).concept;
			double value = conceptList.get(i).score;
			ConceptData concept = new ConceptData(
					pageIdTitleMapping.get(key + "").replaceAll(",", "").replaceAll(";", "").replaceAll("\t", ""),
					value);
			newList.add(concept);
		}

		return newList;
	}

	// doc: word1 word2 word1 word3 ...
	// concept1(word1)*tfidf(word1) + concept1(word2)*tfidf(word2)...
	// concept2(word1)*tfidf(word1) + concept2(word2)*tfidf(word2)...
	public HashMap<Integer, Double> getConceptVectorBasedonSegmentation(String query, int topK, String vectorField) {

		query = QueryPreProcessor.process(query);

		String[] tokens = query.split("\\s+");
		List<String> newTokens = new ArrayList<String>();
		for (int i = 0; i < tokens.length; i++) {
			String word = tokens[i].toLowerCase().trim();
			newTokens.add(word);
		}

		// for (int i = 0; i < newTokens.size(); ++i) {
		// System.out.println(newTokens.get(i) + "\t");
		// }
		// System.out.println();
		// System.out.println();

		List<HashMap<Integer, Double>> conceptMapList = new ArrayList<HashMap<Integer, Double>>();
		List<Double> weightList = new ArrayList<Double>();
		List<String> termList = new ArrayList<String>();

		HashMap<String, Double> tfidfMap = new HashMap<String, Double>();
		for (int i = 0; i < newTokens.size(); ++i) {
			String token = newTokens.get(i);
			if (tfidfMap.containsKey(token) == false) {
				tfidfMap.put(token, 1.0);
			} else {
				tfidfMap.put(token, tfidfMap.get(token) + 1);
			}
		}

		if (tfidfMap.size() == 0) {
			return null;
		}

		try {
			double vsum = 0;
			double maxValue = Collections.max(tfidfMap.values());
			for (String strTerm : tfidfMap.keySet()) {
				double tf = tfidfMap.get(strTerm);
				// double tf = 1.0 + Math.log(tfidfMap.get(strTerm));
				if (tfType.equals("boolean")) {
					tf = 1;
				} else if (tfType.equals("log")) {
					tf = 1 + Math.log(tf);
				} else if (tfType.equals("aug")) {
					tf = 0.5 + (tf * 0.5) / maxValue;
				}
				double idf = 1;
				if (wordIDF.containsKey(strTerm)) {
					idf = wordIDF.get(strTerm);
				}
				double tfidf = idf * tf;

				vsum += tfidf * tfidf;
				tfidfMap.put(strTerm, tfidf);

				vsum = Math.sqrt(vsum);
			}

			for (String strTerm : tfidfMap.keySet()) {
				double tfidf = tfidfMap.get(strTerm);
				tfidfMap.put(strTerm, tfidf / vsum);
			}

			for (String strTerm : tfidfMap.keySet()) {
				HashMap<Integer, Double> termConceptMap = getVector(strTerm);
				if (termConceptMap == null)
					continue;
				if (termConceptMap.size() > 0 && tfidfMap.containsKey(strTerm) == true && tfidfMap.get(strTerm) > 0) {
					conceptMapList.add(termConceptMap);
					weightList.add(tfidfMap.get(strTerm));
					termList.add(strTerm);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		HashMap<Integer, Double> conceptMap = new HashMap<Integer, Double>();
		if (isUseGraphCut == true) {
			conceptMap = SemanticGraphCut.combineVectorsGraph(conceptMapList, weightList, termList);
		} else {
			conceptMap = SemanticGraphCut.combineVectorsSimple(conceptMapList, weightList);
		}

		List<TermData> termDataList = new ArrayList<TermData>();
		for (Integer key : conceptMap.keySet()) {
			double value = conceptMap.get(key);
			TermData termData = new TermData();
			termData.termID = key;
			termData.tfidf = value;
			termDataList.add(termData);
		}
		Collections.sort(termDataList);

		HashMap<Integer, Double> newConceptVector = new HashMap<Integer, Double>();
		for (int i = termDataList.size() - 1; i >= Math.max(termDataList.size() - topK, 0)
				&& termDataList.get(i).tfidf > 0; i--) {
			newConceptVector.put(termDataList.get(i).termID, termDataList.get(i).tfidf / newTokens.size());
		}

		return newConceptVector;
	}

	public Hashtable<Integer, Double> getConceptVectorBasedonTFIDF(HashMap<String, Double> queryTFIDF, int topK,
			String vectorField) {
		List<HashMap<Integer, Double>> conceptMapList = new ArrayList<HashMap<Integer, Double>>();
		List<Double> weightList = new ArrayList<Double>();
		List<String> termList = new ArrayList<String>();
		try {

			for (String strTerm : queryTFIDF.keySet()) {
				HashMap<Integer, Double> termConceptMap = getVector(strTerm);
				if (termConceptMap == null)
					continue;

				if (termConceptMap.size() > 0 && queryTFIDF.containsKey(strTerm) == true
						&& queryTFIDF.get(strTerm) > 0) {
					conceptMapList.add(termConceptMap);
					weightList.add(queryTFIDF.get(strTerm));
					termList.add(strTerm);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		HashMap<Integer, Double> conceptMap = new HashMap<Integer, Double>();
		if (isUseGraphCut == true) {
			conceptMap = SemanticGraphCut.combineVectorsGraph(conceptMapList, weightList, termList);
		} else {
			conceptMap = SemanticGraphCut.combineVectorsSimple(conceptMapList, weightList);
		}

		List<TermData> termDataList = new ArrayList<TermData>();
		for (Integer key : conceptMap.keySet()) {
			double value = conceptMap.get(key);
			TermData termData = new TermData();
			termData.termID = key;
			termData.tfidf = value;
			termDataList.add(termData);
		}
		Collections.sort(termDataList);

		Hashtable<Integer, Double> newConceptVector = new Hashtable<Integer, Double>();
		for (int i = termDataList.size() - 1; i >= Math.max(termDataList.size() - topK, 0)
				&& termDataList.get(i).tfidf > 0; i--) {
			newConceptVector.put(termDataList.get(i).termID, termDataList.get(i).tfidf / queryTFIDF.size());
		}

		return newConceptVector;
	}

	public static HashMap<Integer, Double> getVector(String s) {
		s = s.toLowerCase();

		if (wordVectors.containsKey(s))
			return wordVectors.get(s);

		return null;
	}

	public double cosine(String s1, String s2) {
		List<ConceptData> conceptList1 = retrieveConcepts(s1, 500, "tfidfVector");
		HashMap<String, Double> vectorTopic = getVectorMap(conceptList1);
		double normSentence = getNorm(vectorTopic);
		List<ConceptData> conceptList2 = retrieveConcepts(s2, 500, "tfidfVector");
		HashMap<String, Double> vector = getVectorMap(conceptList2);
		double normTopic = getNorm(vector);
		return cosine(vectorTopic, vector, normSentence, normTopic);
	}

	public static double cosine(HashMap<String, Double> v1, HashMap<String, Double> v2, double norm1, double norm2) {

		double dot = 0;
		if (v1.size() < v2.size()) {
			for (String key : v1.keySet()) {
				if (v2.containsKey(key) == true) {
					double value1 = v1.get(key);
					double value2 = v2.get(key);
					dot += value1 * value2;
				}
			}
		} else {
			for (String key : v2.keySet()) {
				if (v1.containsKey(key) == true) {
					double value1 = v1.get(key);
					double value2 = v2.get(key);
					dot += value1 * value2;
				}
			}
		}

		return dot / (Double.MIN_NORMAL + norm1) / (Double.MIN_NORMAL + norm2);
	}

	public static HashMap<String, Double> getVectorMap(List<ConceptData> conceptList) {
		HashMap<String, Double> vectorMap = new HashMap<String, Double>();

		for (int i = 0; i < conceptList.size(); ++i) {
			vectorMap.put(conceptList.get(i).concept, conceptList.get(i).score);
		}

		return vectorMap;
	}

	public static double getNorm(HashMap<String, Double> vector) {
		double norm = 0;
		for (String key : vector.keySet()) {
			double value = vector.get(key);
			norm += value * value;
		}
		norm = Math.sqrt(norm);
		return norm;
	}

}
