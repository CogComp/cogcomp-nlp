/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl.experiment;

import edu.illinois.cs.cogcomp.core.algorithms.ProducerConsumer;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Lexicon;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.ArrayUtilities;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.srl.caches.FeatureVectorCacheFile;
import edu.illinois.cs.cogcomp.srl.core.*;
import edu.illinois.cs.cogcomp.srl.learn.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PreExtractor extends ProducerConsumer<TextAnnotation> {

    protected final FeatureVectorCacheFile cacheDB;

    protected final List<PreExtractRecord> buffer = new ArrayList<>();

    protected final SRLManager manager;
    private AtomicInteger taCounter = new AtomicInteger();
    private AtomicInteger argCounter = new AtomicInteger();
    private AtomicInteger predicateCounter = new AtomicInteger();
    protected final Models modelToExtract;

    private boolean addNewFeatures;
    private final Lexicon lexicon;

    public PreExtractor(SRLManager manager, Iterator<TextAnnotation> data, int numConsumers,
            Models modelToExtract, FeatureVectorCacheFile featureCache) {
        super(data, numConsumers);
        this.modelToExtract = modelToExtract;
        this.manager = manager;
        this.cacheDB = featureCache;
        this.addNewFeatures = true;

        if (modelToExtract == Models.Classifier)
            manager.getModelInfo(Models.Identifier).loadWeightVector();

        lexicon = manager.getModelInfo(modelToExtract).getLexicon();
    }

    @Override
    protected void initialize() {}

    @Override
    protected boolean prerequisiteCheck(TextAnnotation ta) {
        return ta.hasView(manager.getGoldViewName());
    }

    @Override
    protected void consume(TextAnnotation ta) {
        try {
            assert manager.trainingMode;

            if (modelToExtract == Models.Predicate) {
                AbstractPredicateDetector detector = manager.getHeuristicPredicateDetector();
                List<Constituent> predicates = detector.getPredicates(ta);

                Set<IntPair> gold = new HashSet<>();
                if (ta.hasView(manager.getGoldViewName())) {
                    PredicateArgumentView pav =
                            (PredicateArgumentView) ta.getView(manager.getGoldViewName());

                    for (Constituent pred : pav.getPredicates())
                        gold.add(pred.getSpan());
                }

                for (Constituent c : predicates) {
                    SRLMulticlassInstance predicateInstance =
                            new SRLMulticlassInstance(c, c, manager);

                    int label = gold.contains(c.getSpan()) ? 1 : 0;
                    SRLMulticlassLabel y = new SRLMulticlassLabel(label, Models.Predicate, manager);

                    consumeInstance(predicateInstance, y);

                    predicateCounter.incrementAndGet();
                }
            } else {
                Pair<SRLSentenceInstance, SRLSentenceStructure> examples =
                        manager.exampleGenerator.getExamples(ta);
                SRLSentenceInstance sentenceInstance = examples.getFirst();
                SRLSentenceStructure sentenceStructure = examples.getSecond();

                for (int predicateId = 0; predicateId < sentenceInstance.numPredicates(); predicateId++) {
                    SRLPredicateInstance x = sentenceInstance.predicates.get(predicateId);
                    SRLPredicateStructure y = sentenceStructure.ys.get(predicateId);

                    predicateCounter.incrementAndGet();
                    if (modelToExtract == Models.Sense) {
                        consumeInstance(x.getSenseInstance(), y.getSenseMulticlassLabel());
                    } else {
                        List<SRLMulticlassInstance> candidateInstances;

                        candidateInstances = x.getCandidateInstances();

                        for (int i = 0; i < candidateInstances.size(); i++) {
                            SRLMulticlassInstance cand = candidateInstances.get(i);
                            SRLMulticlassLabel candLabel;
                            if (modelToExtract == Models.Identifier)
                                candLabel = y.getIdentifierMulticlassLabel(i);
                            else
                                candLabel = y.getClassifierMulticlassLabel(i);

                            if (modelToExtract == Models.Classifier) {
                                ArgumentIdentifier identifier = manager.getArgumentIdentifier();

                                Set<Feature> idFeats =
                                        manager.getModelInfo(Models.Identifier).fex
                                                .getFeatures(cand.getConstituent());
                                cand.cacheFeatureVector(Models.Identifier, idFeats);

                                if (!identifier.getIdentifierScaledDecision(cand))
                                    continue;
                            }
                            consumeInstance(cand, candLabel);
                            argCounter.incrementAndGet();
                        }
                    }
                }
            }
            taCounter.incrementAndGet();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    protected String getStatus() {
        String s =
                taCounter.get() + " sentences cached, #predicates =" + predicateCounter.get()
                        + ", #args = " + argCounter.get() + ", ";

        s += "#features = " + manager.getModelInfo(modelToExtract).getLexicon().size();

        return s;
    }

    @Override
    protected List<TextAnnotation> process(TextAnnotation ta) {
        return Collections.singletonList(ta);
    }

    protected void consumeInstance(SRLMulticlassInstance x, SRLMulticlassLabel y) throws Exception {
        countFeatures(x);

        synchronized (buffer) {
            IFeatureVector fv = x.getCachedFeatureVector(modelToExtract);
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
     * <b>*.fex</b> files (per SRL type and model) and are read by {@link FeatureExtractor} <br/>
     * <b>NB:</b>For prepSRL we are considering the features of the Governor and Object instead of
     * the predicate itself.
     * 
     * @param x The predicate to extract features from.
     * @throws EdisonException
     */
    public void countFeatures(SRLMulticlassInstance x) throws EdisonException {
        ModelInfo modelInfo = manager.getModelInfo(modelToExtract);

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
        FeatureVectorBuffer tmp =
                new FeatureVectorBuffer(ArrayUtilities.asIntArray(ids),
                        ArrayUtilities.asFloatArray(values));
        x.cacheFeatureVector(modelToExtract, tmp.toFeatureVector());
    }

    public void lockLexicon() {
        this.addNewFeatures = false;
    }
}
