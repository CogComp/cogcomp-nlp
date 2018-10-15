/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.experiments;

import edu.illinois.cs.cogcomp.core.stats.OneVariableStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * @param <T> The type of the training examples
 * @author Vivek Srikumar
 *         <p>
 *         Jan 30, 2009
 */
public class CrossValidationHelper<T> {

    private static Logger log = LoggerFactory.getLogger(CrossValidationHelper.class);

    protected final int numFolds;
    private final IExperimentFactory<T> experimentFactory;

    private OneVariableStats stats;
    private final int numThreads;
    private final long timeoutSeconds;

    /**
     * By default, do five fold cross validation
     *
     * @param experiment One run of the experiment ( test + train)
     */
    public CrossValidationHelper(IExperimentFactory<T> experiment) {
        this(5, experiment);
    }

    /**
     * @param numFolds number of folds
     * @param experiment one run of the experiment (test + train)
     */
    public CrossValidationHelper(int numFolds, IExperimentFactory<T> experiment) {
        this(10000, Math.min(Runtime.getRuntime().availableProcessors(), numFolds), numFolds,
                experiment);
    }

    public CrossValidationHelper(long timeoutSeconds, int numThreads, int numFolds,
            IExperimentFactory<T> experimentFactory) {
        this.timeoutSeconds = timeoutSeconds;
        this.numThreads = numThreads;
        this.numFolds = numFolds;
        this.experimentFactory = experimentFactory;

    }

    /**
     * NOTE: This does not take care of shuffling or randomizing the data.
     */
    public double doCrossValidation(Iterable<T> data, int dataSize) throws InterruptedException,
            ExecutionException {
        stats = new OneVariableStats();

        log.info("Starting cross validation at " + (new Date()));
        log.info("Splitting data into " + numFolds + " folds");

        List<List<T>> splits = splitData(data, dataSize);
        log.info("Splitting complete.");

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        List<FutureTask<Double>> folds = new ArrayList<>();

        for (int i = 0; i < numFolds; i++) {
            List<T> trainingSet = new ArrayList<>();

            for (int j = 0; j < numFolds; j++) {
                if (j != i)
                    trainingSet.addAll(splits.get(j));
            }

            List<T> testSet = splits.get(i);

            FutureTask<Double> fold = createFoldTask(trainingSet, testSet, i);
            executor.execute(fold);
            folds.add(fold);
        }

        executor.shutdown();
        executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);

        for (FutureTask<Double> fold : folds) {
            stats.add(fold.get());
        }

        log.info("Cross validation complete at " + (new Date()));

        return stats.mean();
    }

    private FutureTask<Double> createFoldTask(final List<T> trainingSet, final List<T> testSet,
            final int foldId) {
        return new FutureTask<>(new Callable<Double>() {
            public Double call() throws Exception {
                long start = System.currentTimeMillis();
                log.info("Starting fold " + foldId);
                IExperiment<T> experiment = experimentFactory.makeExperiment();

                double value = experiment.run(trainingSet, testSet);

                long end = System.currentTimeMillis();

                long time = (end - start) / 1000;
                log.info("End of fold " + foldId + ". Took " + time + " seconds.");

                return value;
            }
        });
    }

    public OneVariableStats getStats() {
        return stats;
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

        for (T item : data)
            l.add(item);

        for (int i = 0; i < numFolds; i++) {
            int start = current;
            int end = Math.min(current + size, dataSize);

            splits.add(l.subList(start, end));
            current = end;

        }
        return splits;
    }
}
