/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.experiments;

import edu.illinois.cs.cogcomp.core.utilities.ExecutionTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Vivek Srikumar
 *         <p>
 *         Feb 1, 2009
 */
public class ExperimentRunner<T> {
    private static Logger log = LoggerFactory.getLogger(ExperimentRunner.class);
    private final int timeoutSeconds;

    public ExperimentRunner(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public ExperimentRunner() {
        this(10000);
    }

    public double runExperiment(final IExperiment<T> experiment, final List<T> trainingSet,
            final List<T> testSet) throws Exception {

        log.info("Running experiment: " + experiment.getDescription() + ". Start time: "
                + (new Date()));

        ExecutionTimer timer = new ExecutionTimer();
        timer.start();

        Callable<Double> experimentRunner = new Callable<Double>() {

            public Double call() throws Exception {
                return experiment.run(trainingSet, testSet);
            }
        };

        FutureTask<Double> task = new FutureTask<>(experimentRunner);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(task);

        executor.shutdown();

        executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);

        double value = task.get();

        timer.end();

        log.info("Experiment " + experiment.getDescription() + " complete. End time: "
                + (new Date()) + ". Took " + timer.getTimeSeconds() + "s.");

        return value;
    }
}
