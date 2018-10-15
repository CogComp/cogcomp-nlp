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
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.ArrayUtilities;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.sl.util.FeatureVector;
import edu.illinois.cs.cogcomp.verbsense.caches.FeatureVectorCacheFile;
import edu.illinois.cs.cogcomp.verbsense.core.ModelInfo;
import edu.illinois.cs.cogcomp.verbsense.core.SenseManager;
import edu.illinois.cs.cogcomp.verbsense.jlis.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PreExtractor extends ProducerConsumer<TextAnnotation> {

    protected final FeatureVectorCacheFile cacheDB;

    protected final List<PreExtractRecord> buffer = new ArrayList<>();

    protected final SenseManager manager;
    private AtomicInteger taCounter = new AtomicInteger();
    private AtomicInteger predicateCounter = new AtomicInteger();

    private boolean addNewFeatures;
    private final Lexicon lexicon;

    public PreExtractor(SenseManager manager, Iterator<TextAnnotation> data, int numConsumers,
            FeatureVectorCacheFile featureCache) {
        super(data, numConsumers);
        this.manager = manager;
        this.cacheDB = featureCache;
        this.addNewFeatures = true;
        this.lexicon = manager.getModelInfo().getLexicon();
    }

    @Override
    protected void initialize() {}

    @Override
    protected boolean prerequisiteCheck(TextAnnotation ta) {
        return ta.hasView(SenseManager.getGoldViewName());
    }

    @Override
    protected void consume(TextAnnotation ta) {
        try {
            assert manager.trainingMode;
            Pair<SentenceInstance, SentenceStructure> examples =
                    manager.exampleGenerator.getExamples(ta);
            SentenceInstance sentenceInstance = examples.getFirst();
            SentenceStructure sentenceStructure = examples.getSecond();

            for (int predicateId = 0; predicateId < sentenceInstance.numPredicates(); predicateId++) {
                SenseInstance x = sentenceInstance.predicates.get(predicateId);
                SenseStructure y = sentenceStructure.ys.get(predicateId);

                predicateCounter.incrementAndGet();
                consumeInstance(x, y);
            }
            taCounter.incrementAndGet();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    protected String getStatus() {
        return taCounter.get() + " sentences cached, " + "#predicates = " + predicateCounter.get()
                + ", " + "#features = " + manager.getModelInfo().getLexicon().size();
    }

    @Override
    protected List<TextAnnotation> process(TextAnnotation ta) {
        return Arrays.asList(ta);
    }

    protected void consumeInstance(SenseInstance x, SenseStructure y) throws Exception {
        countFeatures(x);

        synchronized (buffer) {
            FeatureVector fv = x.getCachedFeatureVector();
            assert fv != null;
            buffer.add(new PreExtractRecord(x.getPredicateLemma(), y.getLabel(), fv));
        }

        if (buffer.size() > 10000) {
            synchronized (buffer) {
                if (buffer.size() > 10000) {
                    for (PreExtractRecord r : buffer) {
                        cacheDB.put(r.lemma, r.label, r.features);
                    }
                    buffer.clear();
                }
            }
        }

    }

    public void finalize() throws Exception {
        for (PreExtractRecord r : buffer) {
            cacheDB.put(r.lemma, r.label, r.features);
        }

        this.cacheDB.close();

        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * This is where actual feature extraction is taking place. The features are defined in the
     * <b>features.fex</b> file and are read by {@link FeatureExtractor}
     * 
     * @param x The predicate to extract features from.
     * @throws EdisonException
     */
    public void countFeatures(SenseInstance x) throws EdisonException {
        ModelInfo modelInfo = manager.getModelInfo();

        Set<Feature> feats = modelInfo.fex.getFeatures(x.getConstituent());

        // This is the only place where a new feature can be added to the lexicon.
        List<Integer> ids = new ArrayList<>();
        List<Float> values = new ArrayList<>();
        synchronized (lexicon) {
            for (Feature f : feats) {
                if (addNewFeatures) {
                    if (!lexicon.contains(f.getName())) {
                        lexicon.previewFeature(f.getName());
                    }
                } else if (!lexicon.contains(f.getName())) {
                    continue;
                }
                int featureId = lexicon.lookupId(f.getName());
                lexicon.countFeature(featureId);

                ids.add(featureId);
                values.add(f.getValue());
            }
        }

        x.cacheFeatureVector(new FeatureVector(ArrayUtilities.asIntArray(ids), ArrayUtilities
                .asFloatArray(values)));
    }
}
