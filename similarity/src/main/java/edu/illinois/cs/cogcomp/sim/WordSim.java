/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.sim;

import java.io.File;
import java.io.IOException;

import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;

import edu.illinois.cs.cogcomp.wsim.esa.MemoryBasedESA;

import edu.illinois.cs.cogcomp.wsim.wordnet.WNSim;
import edu.illinois.cs.cogcomp.config.EmbeddingConstant;
import edu.illinois.cs.cogcomp.config.SimConfigurator;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.wsim.embedding.Embedding;

/**
 * Word Similarity Metrics including word2vec, Paragram, WordNet, Glove, ESA.
 * 
 * You need specify the file path in Config file for the metrics you want to
 * use.
 * 
 * The class loads all or one of metrics above from file and compare function is
 * used to compare two words and return similarity score
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
	Datastore ds;

	@Override
	public MetricResponse compare(String arg1, String arg2) throws IllegalArgumentException {
		return compare(arg1, arg2, method);
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
		} else if (method.equals(EmbeddingConstant.memorybasedESA)) {
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
	 * @param method
	 *            is the word comparison metric
	 */
	public WordSim(ResourceManager rm_, String method) {

		this.method = method;

		if (method.equals(EmbeddingConstant.word2vec)) {
			word2vec = new Embedding(getFile(SimConfigurator.WORD2VEC.key),
					rm_.getInt(SimConfigurator.EMBEDDING_DIM.key));
		} else if (method.equals(EmbeddingConstant.paragram)) {
			File file = new File(rm_.getString(SimConfigurator.PARAGRAM.key));
			paragram = new Embedding(file, rm_.getInt(SimConfigurator.PARAGRAM_DIM.key));
		} else if (method.equals(EmbeddingConstant.glove)) {
			glove = new Embedding(getFile(SimConfigurator.GLOVE.key), rm_.getInt(SimConfigurator.EMBEDDING_DIM.key));
		} else if (method.equals(EmbeddingConstant.phrase2vec)) {
			phrase2vec = new Embedding(getFile(SimConfigurator.PHRASE2VEC.key),
					rm_.getInt(SimConfigurator.EMBEDDING_DIM.key));
		} else if (method.equals(EmbeddingConstant.memorybasedESA)) {
			esa = new MemoryBasedESA(getFile(SimConfigurator.MEMORYBASEDESA.key),
					getFile(SimConfigurator.PAGE_ID_MAPPING.key));
		} else if (method.equals(EmbeddingConstant.wordnet)) {
			try {
				wnsim = new WNSim();
			} catch (IOException e) {
			}
		} else if (method.equals(EmbeddingConstant.customized)){
			File file = new File(rm_.getString(SimConfigurator.CUSTOMIZED.key));
			paragram = new Embedding(file, rm_.getInt(SimConfigurator.CUSTOMIZED_EMBEDDING_DIM.key));
		} else
			throw new IllegalArgumentException("Requires an legal word comparison metric");

	}

	public File getFile(String method) {
		try {
			ResourceManager rm=new ResourceConfigurator().getDefaultConfig();
			ds=new Datastore(rm.getString("datastoreEndpoint"));
		} catch (DatastoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		File f = null;
		if (method.equals(EmbeddingConstant.word2vec)) {
			try {
				f = ds.getFile("org.cogcomp.wordembedding", "word2vec.txt", 1.5);
			} catch (DatastoreException e) {
				e.printStackTrace();
			}
		} else if (method.equals(EmbeddingConstant.glove)) {
			try {
				f = ds.getFile("org.cogcomp.wordembedding", "glove.txt", 1.5);
			} catch (DatastoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (method.equals(EmbeddingConstant.phrase2vec)) {
			try {
				f = ds.getFile("org.cogcomp.wordembedding", "phrase2vec.txt", 1.5);
			} catch (DatastoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (method.equals(EmbeddingConstant.memorybasedESA)) {
			try {
				f = ds.getFile("org.cogcomp.wordembedding", "memorybasedESA.txt", 1.5);
			} catch (DatastoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (method.equals(EmbeddingConstant.pageIDMapping)) {
			try {
				f = ds.getFile("org.cogcomp.wordembedding", "pageIDMapping.txt", 1.5);
			} catch (DatastoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return f;
	}

}
