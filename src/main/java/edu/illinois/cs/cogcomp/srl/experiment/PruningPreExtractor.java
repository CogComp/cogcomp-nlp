package edu.illinois.cs.cogcomp.srl.experiment;

import edu.illinois.cs.cogcomp.core.algorithms.ProducerConsumer;
import edu.illinois.cs.cogcomp.core.datastructures.Lexicon;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;
import edu.illinois.cs.cogcomp.srl.caches.FeatureVectorCacheFile;
import edu.illinois.cs.cogcomp.srl.core.ModelInfo;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.jlis.SRLMulticlassInstance;
import edu.illinois.cs.cogcomp.srl.jlis.SRLMulticlassLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates a second feature cache, where the pruned features are stored.
 * 
 * @author Vivek Srikumar
 * 
 */
public class PruningPreExtractor extends
		ProducerConsumer<Pair<SRLMulticlassInstance, SRLMulticlassLabel>> {
	protected final FeatureVectorCacheFile cache;

	private SRLManager manager;
	private Models modelToExtract;

	protected final List<PreExtractRecord> buffer = new ArrayList<PreExtractRecord>();

	private AtomicInteger counter = new AtomicInteger();

	public PruningPreExtractor(SRLManager manager, Models modelToExtract,
			FeatureVectorCacheFile examples, FeatureVectorCacheFile cache,
			int nThreads) {

		super(examples, nThreads);

		this.manager = manager;
		this.modelToExtract = modelToExtract;
		this.cache = cache;

	}

	@Override
	protected void initialize() {

	}

	@Override
	protected boolean prerequisiteCheck(
			Pair<SRLMulticlassInstance, SRLMulticlassLabel> input) {
		return true;
	}

	@Override
	protected void consume(Pair<SRLMulticlassInstance, SRLMulticlassLabel> input) {

		SRLMulticlassInstance x = input.getFirst();
		SRLMulticlassLabel y = input.getSecond();

		SparseFeatureVector features = (SparseFeatureVector)x.getCachedFeatureVector(modelToExtract);

		ModelInfo modelInfo = manager.getModelInfo(modelToExtract);
		Lexicon lexicon = modelInfo.getLexicon();

		int threshold = manager.getPruneSize(modelToExtract);

		Pair<int[], float[]> pair = lexicon.pruneFeaturesByCount(
				features.getIndices(), features.getValues(), threshold);

		features = new SparseFeatureVector(pair.getFirst(), pair.getSecond());

		synchronized (buffer) {
			buffer.add(new PreExtractRecord(x.getPredicateLemma(),
					y.getLabel(), features));

		}

		if (buffer.size() > 10000) {
			synchronized (buffer) {
				if (buffer.size() > 10000) {
					for (PreExtractRecord r : buffer) {
						try {
							cache.put(r.lemma, r.label, r.features);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
					buffer.clear();
				}
			}
		}

		counter.incrementAndGet();
	}

	@Override
	protected String getStatus() {
		return counter.get() + " examples processed";
	}

	@Override
	protected List<Pair<SRLMulticlassInstance, SRLMulticlassLabel>> process(
			Pair<SRLMulticlassInstance, SRLMulticlassLabel> input) {
		List<Pair<SRLMulticlassInstance, SRLMulticlassLabel>> l = new ArrayList<Pair<SRLMulticlassInstance, SRLMulticlassLabel>>();
		l.add(input);
		return l;
	}

	public void finalize() throws Exception {
		for (PreExtractRecord r : buffer) {
			cache.put(r.lemma, r.label, r.features);

		}

		cache.close();
	}
}
