/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.sim;

import java.io.IOException;

import edu.illinois.cs.cogcomp.wsim.esa.MemoryBasedESA;

import edu.illinois.cs.cogcomp.wsim.wordnet.WNSim;
import edu.illinois.cs.cogcomp.config.EmbeddingConstant;
import edu.illinois.cs.cogcomp.config.SimConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.wsim.embedding.Embedding;

/**
 * Word Similarity Metrics including word2vec, Paragram, WordNet, Glove, ESA. 
 * 
 * You need specify the file path in Config file for the metrics you want to use.
 * 
 * The class loads all or one of metrics above from file and compare function is used
 * to compare two words and return similarity score 
 * 
 * @author shaoshi
 *
 */
public class WordSim implements Metric<String> {
	WNSim wnsim;
	Embedding paragram;
	Embedding word2vec;
	Embedding glove;
	Embedding phrase2vec;
	MemoryBasedESA esa;
	String method;

	@Override
	public MetricResponse compare(String arg1, String arg2) throws IllegalArgumentException {
		return compare(arg1, arg2, method);
	}

	/**
	 * Initialize all word similarity metrics instances
	 * 
	 * @param rm_
	 *            resource manager
	 */
	public WordSim(ResourceManager rm_) {

		paragram = new Embedding(rm_.getString(SimConfigurator.PARAGRAM.key),
				rm_.getInt(SimConfigurator.PARAGRAM_DIM.key));
		word2vec = new Embedding(rm_.getString(SimConfigurator.WORD2VEC.key),
				rm_.getInt(SimConfigurator.EMBEDDING_DIM.key));
		glove = new Embedding(rm_.getString(SimConfigurator.GLOVE.key), rm_.getInt(SimConfigurator.EMBEDDING_DIM.key));
		phrase2vec = new Embedding(rm_.getString(SimConfigurator.PHRASE2VEC.key),
				rm_.getInt(SimConfigurator.EMBEDDING_DIM.key));
		esa = new MemoryBasedESA(rm_);
		try {
			wnsim = new WNSim();
		} catch (IOException e) {
		}
	}

	/**
	 * similarity comparison method
	 * 
	 * @param small
	 *            word1
	 * @param big
	 *            word2
	 * @param method
	 *            word metrics method
	 * @return
	 */

	public MetricResponse compare(String small, String big, String method) {
		double score = 0;

		if (method.equals(EmbeddingConstant.word2vec)) {
			score = word2vec.simScore(small, big);
		} else if (method.equals(EmbeddingConstant.paragram)) {
			score = paragram.simScore(small, big);
		} else if (method.equals(EmbeddingConstant.glove)) {
			score = glove.simScore(small, big);
		} else if (method.equals(EmbeddingConstant.phrase2vec)) {
			score = phrase2vec.simScore(small, big);
		} else if (method.equals(EmbeddingConstant.esa)) {
			score = esa.cosine(small, big);
		} else if (method.equals(EmbeddingConstant.wordnet)) {

			return wnsim.compare(small, big);
		} else
			throw new IllegalArgumentException("Requires an argument");

		return new MetricResponse(score, method);
	}

	/**
	 * Initialize specific word metrics instance
	 * 
	 * @param rm_
	 * @param method is the word comparison metric
	 */
	public WordSim(ResourceManager rm_, String method) {
		this.method = method;

		if (method.equals(EmbeddingConstant.word2vec)) {
			word2vec = new Embedding(rm_.getString(SimConfigurator.WORD2VEC.key),
					rm_.getInt(SimConfigurator.EMBEDDING_DIM.key));
		} else if (method.equals(EmbeddingConstant.paragram)) {
			paragram = new Embedding(rm_.getString(SimConfigurator.PARAGRAM.key),
					rm_.getInt(SimConfigurator.PARAGRAM_DIM.key));
		} else if (method.equals(EmbeddingConstant.glove)) {
			glove = new Embedding(rm_.getString(SimConfigurator.GLOVE.key),
					rm_.getInt(SimConfigurator.EMBEDDING_DIM.key));
		} else if (method.equals(EmbeddingConstant.phrase2vec)) {
			phrase2vec = new Embedding(rm_.getString(SimConfigurator.PHRASE2VEC.key),
					rm_.getInt(SimConfigurator.EMBEDDING_DIM.key));
		} else if (method.equals(EmbeddingConstant.esa)) {
			esa = new MemoryBasedESA(rm_);
		} else if (method.equals(EmbeddingConstant.wordnet)) {
			try {
				wnsim = new WNSim();
			} catch (IOException e) {
			}
		} else
			throw new IllegalArgumentException("Requires an legal word comparison metric");

	}

}
