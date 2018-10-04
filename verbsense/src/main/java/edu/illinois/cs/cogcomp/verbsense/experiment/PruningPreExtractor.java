/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.experiment;

import edu.illinois.cs.cogcomp.core.algorithms.ProducerConsumer;
import edu.illinois.cs.cogcomp.core.datastructures.Lexicon;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.util.FeatureVector;
import edu.illinois.cs.cogcomp.verbsense.caches.FeatureVectorCacheFile;
import edu.illinois.cs.cogcomp.verbsense.core.ModelInfo;
import edu.illinois.cs.cogcomp.verbsense.core.SenseManager;
import edu.illinois.cs.cogcomp.verbsense.jlis.SenseInstance;
import edu.illinois.cs.cogcomp.verbsense.jlis.SenseStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates a second feature cache, where the pruned features are stored.
 * 
 * @author Vivek Srikumar
 * 
 */
public class PruningPreExtractor extends ProducerConsumer<Pair<SenseInstance, SenseStructure>> {
    protected final FeatureVectorCacheFile cache;

    private SenseManager manager;

    protected final List<PreExtractRecord> buffer = new ArrayList<>();

    private AtomicInteger counter = new AtomicInteger();

    public PruningPreExtractor(SenseManager manager, FeatureVectorCacheFile examples,
            FeatureVectorCacheFile cache, int nThreads) {
        super(examples, nThreads);
        this.manager = manager;
        this.cache = cache;
    }

    @Override
    protected void initialize() {

    }

    @Override
    protected boolean prerequisiteCheck(Pair<SenseInstance, SenseStructure> input) {
        return true;
    }

    @Override
    protected void consume(Pair<SenseInstance, SenseStructure> input) {
        SenseInstance x = input.getFirst();
        SenseStructure y = input.getSecond();

        FeatureVector features = x.getCachedFeatureVector();

        ModelInfo modelInfo = manager.getModelInfo();
        Lexicon lexicon = modelInfo.getLexicon();

        int threshold = manager.getPruneSize();

        Pair<int[], float[]> pair =
                lexicon.pruneFeaturesByCount(features.getIdx(), features.getValue(), threshold);

        features = new FeatureVector(pair.getFirst(), pair.getSecond());

        synchronized (buffer) {
            buffer.add(new PreExtractRecord(x.getPredicateLemma(), y.getLabel(), features));

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
    protected List<Pair<SenseInstance, SenseStructure>> process(
            Pair<SenseInstance, SenseStructure> input) {
        List<Pair<SenseInstance, SenseStructure>> l = new ArrayList<>();
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
