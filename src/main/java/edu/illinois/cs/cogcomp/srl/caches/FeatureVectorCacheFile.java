package edu.illinois.cs.cogcomp.srl.caches;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.jlis.SRLMulticlassInstance;
import edu.illinois.cs.cogcomp.srl.jlis.SRLMulticlassLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FeatureVectorCacheFile implements Closeable,
		Iterator<Pair<SRLMulticlassInstance, SRLMulticlassLabel>> {

	private final static Logger log = LoggerFactory.getLogger(FeatureVectorCacheFile.class);

	private BufferedWriter writer;
	private BufferedReader reader;
	private String file;
	private Models model;
	private SRLManager manager;

	private String nextLine = null;

	public FeatureVectorCacheFile(String file, Models model, SRLManager manager) throws IOException {
		this.file = file;
		this.model = model;
		this.manager = manager;
	}

	private void openWriter(String file) throws IOException {
		BufferedOutputStream stream = new BufferedOutputStream(
				new GZIPOutputStream(new FileOutputStream(file)));

		writer = new BufferedWriter(new OutputStreamWriter(stream));
	}

	public synchronized void put(String lemma, int label, IFeatureVector features)
			throws Exception {

		if (writer == null) {
			openWriter(file);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(lemma).append("\t").append(label).append("\t");
		int[] idx = features.getIndices();
		float[] value = features.getValues();
		for (int i = 0; i < idx.length; i++) {
			sb.append(idx[i]).append(":").append(value[i]).append(" ");
		}

		writer.write(sb.toString().trim());
		writer.newLine();
	}

	public void close() {
		try {
			if (writer != null)
				writer.close();

			if (reader != null)
				reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized Pair<SRLMulticlassInstance, SRLMulticlassLabel> next() {
		try {
			assert reader != null;

			if (nextLine == null)
				hasNext();

			String[] parts = nextLine.split("\t");
			String lemma = parts[0].trim();
			int label = Integer.parseInt(parts[1]);

			String features = parts[2];

			SRLMulticlassInstance x = new SRLMulticlassInstance(model, lemma, features);
			SRLMulticlassLabel y = new SRLMulticlassLabel(label, model, manager);

			return new Pair<>(x, y);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void openReader() throws IOException {
		GZIPInputStream zipin = new GZIPInputStream(new FileInputStream(file));
		reader = new BufferedReader(new InputStreamReader(zipin));
	}

	@Override
	public boolean hasNext() {
		try {
			if (reader == null)
				openReader();

			nextLine = reader.readLine();

			if (nextLine == null)
				return false;

			nextLine = nextLine.trim();

			return true;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void remove() {}

	public SLProblem getStructuredProblem() {
		return getStructuredProblem(-1);
	}

	public SLProblem getStructuredProblem(int sizeLimit) {
		int count = 0;
		SLProblem problem = new SLProblem();

		log.info("Creating structured problem");
		while (hasNext()) {
			Pair<SRLMulticlassInstance, SRLMulticlassLabel> pair = next();
			problem.instanceList.add(pair.getFirst());
			problem.goldStructureList.add(pair.getSecond());

			count++;
			if (sizeLimit >= 0 && count >= sizeLimit)
				break;

			if (count % 10000 == 0) {
				log.info("{} examples loaded", count);
			}
		}

		log.info("{} examples loaded. Finished creating structured problem", count);

		return problem;
	}

}