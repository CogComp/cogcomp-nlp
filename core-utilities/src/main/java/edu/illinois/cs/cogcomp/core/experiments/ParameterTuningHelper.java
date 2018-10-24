/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.experiments;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.math.Permutations;
import edu.illinois.cs.cogcomp.core.stats.OneVariableStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * @author Vivek Srikumar
 *         <p>
 *         Sep 2, 2010
 */
public class ParameterTuningHelper<T> {

    private static Logger log = LoggerFactory.getLogger(ParameterTuningHelper.class);

    private final int numFolds;

    private final List<List<Double>> parameterCrossProduct;

    private final int timeoutSeconds;

    private final IExperimentFactory<T> experimentFactory;

    private final int numThreads;

    private final OneVariableStats[] stats;

    private Random randomSeed;

    public ParameterTuningHelper(IExperimentFactory<T> experimentFactory, int numFolds,
            List<List<Double>> parameters, int timeoutSeconds, int numThreads) {
        super();
        this.experimentFactory = experimentFactory;
        this.numFolds = numFolds;
        this.parameterCrossProduct =
                Collections.synchronizedList(Permutations.crossProduct(parameters));
        this.timeoutSeconds = timeoutSeconds;
        this.numThreads = numThreads;

        stats = new OneVariableStats[parameterCrossProduct.size()];

        for (int i = 0; i < stats.length; i++) {
            stats[i] = new OneVariableStats();
        }

        randomSeed = new Random();
    }

    /**
     * Splits the data into numFolds parts.
     * <p>
     * Note: This splits the data into K folds uniformly. If the classes are not equally
     * distributed, then this is wrong. Instead, override this to do a stratified split, so that the
     * split proportions are maintained.
     */
    protected List<List<T>> splitData(Iterable<T> data, int dataSize) {
        List<List<T>> splits = new ArrayList<>();

        int size = (int) Math.ceil((double) dataSize / numFolds);
        int current = 0;

        List<T> l = new ArrayList<>();

        int count = 0;
        for (T item : data) {

            l.add(item);
            count++;
            if (count > dataSize)
                break;
        }

        Collections.shuffle(l, randomSeed);

        for (int i = 0; i < numFolds; i++) {
            int start = current;
            int end = Math.min(current + size, dataSize);

            splits.add(l.subList(start, end));
            current = end;

        }
        return splits;
    }

    public List<Double> tune(Iterable<T> data, int dataSize) throws InterruptedException,
            ExecutionException {
        List<List<T>> splits = splitData(data, dataSize);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        List<FutureTask<Pair<Integer, Double>>> tasks = new ArrayList<>();

        for (int splitId = 0; splitId < splits.size(); splitId++) {
            final List<T> trainingSet = new ArrayList<>();

            for (int j = 0; j < numFolds; j++) {
                if (j != splitId)
                    trainingSet.addAll(splits.get(j));
            }

            final List<T> testSet = splits.get(splitId);

            for (int paramId = 0; paramId < parameterCrossProduct.size(); paramId++) {
                FutureTask<Pair<Integer, Double>> task =
                        makeExperiment(trainingSet, testSet, splitId, paramId);
                executor.execute(task);
                tasks.add(task);

            }

        }
        executor.shutdown();
        executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);

        for (FutureTask<Pair<Integer, Double>> task : tasks) {
            Pair<Integer, Double> pair = task.get();
            int paramId = pair.getFirst();
            double perf = pair.getSecond();

            stats[paramId].add(perf);
        }

        int maxId = 0;
        double max = stats[maxId].mean();
        for (int i = 1; i < stats.length; i++) {
            if (stats[i].mean() >= max) {
                max = stats[i].mean();
                maxId = i;
            }
        }

        return parameterCrossProduct.get(maxId);

    }

    private FutureTask<Pair<Integer, Double>> makeExperiment(final List<T> trainingSet,
            final List<T> testSet, final int foldId, final int paramId) {

        final List<Double> params = parameterCrossProduct.get(paramId);

        Callable<Pair<Integer, Double>> callable = new Callable<Pair<Integer, Double>>() {

            public Pair<Integer, Double> call() throws Exception {

                long start = System.currentTimeMillis();
                log.info("Starting fold " + foldId + " for parameterId: " + paramId + ": " + params);

                IExperiment<T> experiment = experimentFactory.makeExperiment();

                experiment.setParameters(params);

                double value = experiment.run(trainingSet, testSet);

                long end = System.currentTimeMillis();

                long time = (end - start) / 1000;
                log.info("End of fold " + foldId + " for parameterId: " + paramId + ". Took "
                        + time + " seconds.");

                return new Pair<>(paramId, value);
            }
        };

        return new FutureTask<>(callable);
    }
}
