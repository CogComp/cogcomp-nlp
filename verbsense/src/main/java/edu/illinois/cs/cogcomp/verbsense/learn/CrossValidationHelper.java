/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.learn;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.inference.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class CrossValidationHelper<DatasetType> {

    public static interface PerformanceMeasureAverager<T extends PerformanceMeasure> {
        T average(List<? extends PerformanceMeasure> perf);
    }

    public static interface DatasetSplitter<DatasetType> {
        Pair<DatasetType, DatasetType> getFoldData(DatasetType data, int foldId);
    }

    public static interface Trainer<DatasetType> {
        WeightVector train(DatasetType dataset, LearnerParameters params,
                AbstractInferenceSolver[] inference) throws Exception;
    }

    public static interface Tester<DatasetType> {
        PerformanceMeasure evaluate(DatasetType testSet, WeightVector weight,
                AbstractInferenceSolver inference) throws Exception;
    }

    private final static Logger log = LoggerFactory.getLogger(CrossValidationHelper.class);
    private final int nFolds;
    private final AbstractInferenceSolver[] inference;
    private final PerformanceMeasureAverager<? extends PerformanceMeasure> averager;
    private final DatasetSplitter<DatasetType> foldSplitter;
    private final Tester<DatasetType> tester;
    private final Trainer<DatasetType> trainer;

    public CrossValidationHelper(int nFolds, AbstractInferenceSolver[] inference,
            PerformanceMeasureAverager<? extends PerformanceMeasure> averager,
            DatasetSplitter<DatasetType> foldSplitter, Trainer<DatasetType> trainer,
            Tester<DatasetType> tester) {
        this.nFolds = nFolds;
        this.inference = inference;
        this.averager = averager;
        this.foldSplitter = foldSplitter;
        this.trainer = trainer;
        this.tester = tester;

    }

    public LearnerParameters doCV(DatasetType train, List<LearnerParameters> params)
            throws Exception {
        return this.doCV(train, params, true);
    }

    public LearnerParameters doCV(DatasetType train, List<LearnerParameters> params,
            boolean parallel) throws Exception {

        LearnerParameters bestParams = null;
        PerformanceMeasure bestPerf = null;

        Map<String, PerformanceMeasure> perfs = new HashMap<>();

        int numDropsInARow = 0;
        PerformanceMeasure previous = null;

        for (LearnerParameters param : params) {
            log.info("Trying parameters: " + param.getLearnerParametersIdentifier());
            PerformanceMeasure perf;
            if (parallel)
                perf = tryParamParallel(train, param);
            else
                perf = tryParamSerial(train, param);

            log.info("Finished trying {}, performance = {}",
                    param.getLearnerParametersIdentifier(), perf);

            if (perf.compareTo(bestPerf) > 0) {
                bestPerf = perf;
                bestParams = param;
            }

            if (previous != null && perf.compareTo(previous) > 0) {
                numDropsInARow++;
            }
            perfs.put(param.toString(), perf);

            if (numDropsInARow >= 2) {
                log.info("Stopping CV!");
                break;
            }
        }

        for (LearnerParameters param : params) {
            System.out.println("\t" + param.toString() + "\t" + perfs.get(param.toString()));
        }

        log.info("Best param: {}, performance: {}", bestParams, bestPerf);

        return bestParams;
    }

    private PerformanceMeasure tryParamSerial(DatasetType train, LearnerParameters param)
            throws Exception {
        List<PerformanceMeasure> perf = new ArrayList<>();
        for (int foldId = 0; foldId < nFolds; foldId++) {
            Pair<DatasetType, DatasetType> foldData = foldSplitter.getFoldData(train, foldId);
            log.info("Starting fold {} for params {}", foldId, param);
            WeightVector w = trainer.train(foldData.getFirst(), param, inference);
            log.info("Finished training fold {} for params {}", foldId, param);
            PerformanceMeasure p = tester.evaluate(foldData.getSecond(), w, inference[0]);
            log.info("Performance for fold {}, params {} =" + p.summarize(), foldId, param);
            perf.add(p);
        }
        return averager.average(perf);
    }

    private PerformanceMeasure tryParamParallel(DatasetType train, LearnerParameters param)
            throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(nFolds);

        final AbstractInferenceSolver[][] foldWiseInference = splitInference(inference, nFolds);

        List<FutureTask<PerformanceMeasure>> tasks = new ArrayList<>();

        for (int foldId = 0; foldId < nFolds; foldId++) {

            Pair<DatasetType, DatasetType> foldData = foldSplitter.getFoldData(train, foldId);

            FutureTask<PerformanceMeasure> future =
                    createTask(foldData.getFirst(), foldData.getSecond(), param,
                            foldWiseInference[foldId], foldId);

            tasks.add(future);

            executor.execute(future);
        }
        executor.shutdown();

        int foldId = 0;
        List<PerformanceMeasure> perf = new ArrayList<>();
        for (FutureTask<PerformanceMeasure> task : tasks) {

            log.info("Waiting for results of fold {}", foldId);

            PerformanceMeasure measure = task.get();

            log.info("Fold {} complete.", foldId);
            perf.add(measure);
            foldId++;
        }

        return averager.average(perf);
    }

    private FutureTask<PerformanceMeasure> createTask(final DatasetType train,
            final DatasetType test, final LearnerParameters param,
            final AbstractInferenceSolver[] inference, final int foldId) {

        log.info("Creating cv task for foldId = {} ", foldId);

        assert inference != null;

        final int numInferenceThreads = inference.length;
        assert numInferenceThreads > 0;

        FutureTask<PerformanceMeasure> future =
                new FutureTask<>(new Callable<PerformanceMeasure>() {

                    @Override
                    public PerformanceMeasure call() throws Exception {
                        log.info("Starting fold {} for params {}", foldId, param);
                        WeightVector w = trainer.train(train, param, inference);
                        log.info("Finished training fold {} for params {}", foldId, param);
                        PerformanceMeasure perf = tester.evaluate(test, w, inference[0]);
                        log.info("Performance for fold {}, params {} =" + perf.summarize(), foldId,
                                param);
                        return perf;
                    }
                });
        return future;

    }

    private static AbstractInferenceSolver[][] splitInference(AbstractInferenceSolver[] inference,
            int nFolds) {

        @SuppressWarnings("unchecked")
        List<AbstractInferenceSolver>[] list = new List[nFolds];

        for (int i = 0; i < nFolds; i++) {
            list[i] = new ArrayList<>();
        }

        int foldId = 0;
        for (AbstractInferenceSolver anInference : inference) {
            list[foldId].add(anInference);
            foldId++;
            if (foldId == nFolds)
                foldId = 0;
        }

        AbstractInferenceSolver[][] foldInference = new AbstractInferenceSolver[nFolds][];
        for (int i = 0; i < nFolds; i++) {
            List<AbstractInferenceSolver> l = list[i];
            foldInference[i] = l.toArray(new AbstractInferenceSolver[l.size()]);

            assert foldInference[i] != null;

        }

        return foldInference;
    }
}
