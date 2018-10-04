/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.algorithms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ProducerConsumer<T> {

    private final static Logger log = LoggerFactory.getLogger(ProducerConsumer.class);
    protected int numConsumers;

    protected BlockingQueue<T> theQueue;
    protected Iterator<T> data;

    protected AtomicBoolean done = new AtomicBoolean(false);

    protected AtomicInteger count = new AtomicInteger(0);
    protected AtomicInteger activeConsumers = new AtomicInteger(0);

    public ProducerConsumer(Iterator<T> data, int numConsumers) {
        this.data = data;
        this.numConsumers = numConsumers;
    }

    public void run() throws InterruptedException {
        log.info("Initializing");
        initialize();

        log.info("Starting caching...");
        theQueue = new ArrayBlockingQueue<>(100);

        Thread producer = new Thread(new Producer(theQueue, data), "Producer");
        producer.start();

        List<Thread> consumers = new ArrayList<>();
        for (int i = 0; i < numConsumers; i++) {
            Thread consumer = new Thread(new Consumer(theQueue), "Consumer-" + i);
            consumer.start();
            consumers.add(consumer);
        }

        Thread status = new Thread(new Runnable() {

            @Override
            public void run() {
                log.info("Staring status reporter");
                while (!done.get()) {

                    String status = getStatus();

                    log.info(theQueue.size() + " elements in the queue " + status);

                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }, "Status");

        status.start();

        log.info("Started producer and consumer threads");

        producer.join();
        for (Thread c : consumers)
            c.join();

        status.join();
    }

    protected abstract void initialize();

    protected abstract boolean prerequisiteCheck(T input);

    protected abstract void consume(T ta);

    public class Producer implements Runnable {
        protected BlockingQueue<T> q;
        private final Iterator<T> store;

        public Producer(BlockingQueue<T> theQueue, Iterator<T> data) {
            this.q = theQueue;
            this.store = data;
        }

        @Override
        public void run() {
            int addedToQueue = 0;
            int totalCount = 0;

            log.info("Starting producer thread....");
            while (store.hasNext()) {
                try {
                    T input = store.next();

                    totalCount++;

                    if (totalCount % 10000 == 0)
                        log.info(
                                "Total number of instances added to q {},  total including skipped ones = {}",
                                addedToQueue, totalCount);

                    if (!prerequisiteCheck(input))
                        continue;

                    q.put(input);
                    addedToQueue++;

                } catch (Throwable t) {
                    log.error("Error producing", t);
                }

            } // end while

            done.set(true);
            log.info(
                    "Read all data. Found {} instances ({} instances overall including skipped ones)",
                    count.get(), totalCount);

            int count = 0;
            while (activeConsumers.get() > 0) {

                log.info("Waiting for consumers to shut down, {} active, queue size = {}, done = "
                        + done.get(), activeConsumers.get(), q.size());

                if (this.q.size() == 0) {
                    count++;
                    if (count > 100) {
                        log.info("All examples are done, but some consumers may still be alive. Quitting the producer");
                        break;
                    }
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    log.error("Interrputed...", e);
                }
            }

        }
    }

    public class Consumer implements Runnable {
        protected BlockingQueue<T> q;

        public Consumer(BlockingQueue<T> queue) {
            this.q = queue;
        }

        public void run() {
            try {
                activeConsumers.incrementAndGet();

                int myCount = 0;
                log.debug("Starting consumer thread...");

                while (!done.get() || q.size() > 0) {

                    if (q.size() == 0 && done.get())
                        break;

                    T ta = q.poll(10, TimeUnit.SECONDS);

                    // Means that the queue is still empty but we're not done
                    if (ta == null)
                        continue;

                    try {
                        for (T ta1 : process(ta)) {
                            consume(ta1);
                        }
                        int c = count.incrementAndGet();

                        if (c % 1000 == 0) {
                            log.info(c + " examples complete " + getStatus());
                        }
                        myCount++;
                    } catch (Throwable t) {
                        log.error("Unable to consume input", t);
                        t.printStackTrace();
                        Thread.sleep(3000);
                    }
                }

                activeConsumers.decrementAndGet();

                log.debug("Consumer thread complete! {} instances processed by this thread",
                        myCount);

            } catch (InterruptedException ex) {
                ex.printStackTrace();
                System.exit(-1);
                throw new RuntimeException(ex);
            }
        }
    }

    protected abstract String getStatus();

    protected abstract List<T> process(T ta);

}
