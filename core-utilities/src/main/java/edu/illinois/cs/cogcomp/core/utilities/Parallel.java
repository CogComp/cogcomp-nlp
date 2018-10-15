/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import edu.illinois.cs.cogcomp.core.transformers.ITransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Parallel {

    /**
     * Run {@code function} on each item of {@code list} in parallel, using {@code nThreads}
     * threads. The output is a list that has the same size as {@code list}, where each element has
     * been transformed according to the input function.
     *
     * @param nThreads Number of threads to use
     * @param list The input list
     * @param function A function to apply to each element of the list
     * @param timeout Timeout
     * @param unit Units for the timeout
     * @return A list, where each element of the input list has been mapped according to the
     *         transformer
     */
    public static <T, S> List<S> map(int nThreads, List<T> list, final ITransformer<T, S> function,
            long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);

        List<FutureTask<S>> tasks = new ArrayList<>();
        for (final T item : list) {
            FutureTask<S> task = new FutureTask<>(new Callable<S>() {

                @Override
                public S call() throws Exception {
                    return function.transform(item);
                }
            });
            executor.execute(task);
            tasks.add(task);
        }
        executor.shutdown();

        executor.awaitTermination(timeout, unit);

        List<S> output = new ArrayList<>();

        for (FutureTask<S> task : tasks) {
            output.add(task.get());
        }

        return output;
    }

    public interface Method<T> {
        void run(T input);
    }

    /**
     * Run {@code function} on each item of {@code list} in parallel, using {@code nThreads}
     * threads.
     *
     * @param nThreads Number of threads to use
     * @param list The input list
     * @param function A function to apply to each element of the list
     * @param timeout Timeout
     * @param unit Units for the timeout
     */
    public static <T> void forLoop(int nThreads, List<T> list, final Method<T> function,
            long timeout, TimeUnit unit) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);

        for (final T item : list) {
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    function.run(item);
                }
            };
            executor.execute(task);

        }
        executor.shutdown();

        executor.awaitTermination(timeout, unit);
    }
}
