/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 * 
 */
package edu.illinois.cs.cogcomp.ner.tagger;

import java.util.concurrent.BlockingQueue;

/**
 * This is a thread safe high speed NE tagger. It reads job definitions from an input queue,
 * executions the job as defined there as fast as it can, then will read the next job until it is
 * interrupted. One per CPU core can be created to execution at a very high rate limited by the
 * capabilities of the individual machine.
 * <p>
 * The Job definitions include a consumer and producer. The input data will come from the consumer
 * in the form of a string, the producer will take a view and produce whatever is needed. The
 * implementation of consumer and producer could easily be lambdas, they are intended to be very
 * simply yet flexible.
 * 
 * @author redman
 *
 */
public class TaggerThread extends Thread {
    /** the number of times run. */
    public long count = 0;

    /** time spent reading data. */
    public long readtime = 0;

    /** time spent computing the labels. */
    public long computetime = 0;

    /** time spent writting results. */
    public long writetime = 0;

    /** the job queue used to supply work. */
    private BlockingQueue<AnnotationJob> jobqueue = null;

    /** just a counter for naming the threads. */
    static int counter;

    /** lock access to the counter so two folks can's increment same time. */
    static String counterlock = "counterlock";

    /**
     * must have a referece to the work queue
     * 
     * @param queue the job queue.
     */
    TaggerThread(BlockingQueue<AnnotationJob> queue) {
        this.jobqueue = queue;
        synchronized (counterlock) {
            this.setName("Tagger-" + (counter++));
        }
    }

    /**
     * run continuously looking for data.
     */
    public void run() {
        AnnotationJob job;
        long start;
        try {
            while ((job = jobqueue.take()) != null) {
                start = System.currentTimeMillis();
                job.getData();
                readtime += System.currentTimeMillis() - start;

                start = System.currentTimeMillis();
                job.labelData();
                computetime += System.currentTimeMillis() - start;

                start = System.currentTimeMillis();
                job.publishResults();
                writetime += System.currentTimeMillis() - start;
                count++;
            }
        } catch (InterruptedException e) {
            return;
        }
    }
}
