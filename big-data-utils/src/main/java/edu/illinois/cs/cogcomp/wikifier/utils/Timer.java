/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple class for simple timing tasks.
 * For complete benchmarks use profilers
 *
 * @author cheng88
 *
 */
public abstract class Timer implements Runnable{
    
    /**
     * Reports task progression as {@link #reportIfNeeded()} is called
     * for each loop pass
     * @author cheng88
     *
     */
    public static class Reporter{

        private final long interval;
        private final String message;
        private long startTime;
        private long lastReport;
        private AtomicInteger counter = new AtomicInteger(0);
        private boolean disabled = false;
        private static final Logger logger = LoggerFactory.getLogger(Reporter.class);

        public Reporter(long reporteringInterval){
            this(reporteringInterval,"");
        }
        /**
         * 
         * @param reportingInterval in milliseconds
         */
        public Reporter(long reportingInterval,String message){
            this.interval = reportingInterval;
            this.message = message;
        }
        
        public Reporter disable(){
            disabled = true;
            return this;
        }
        
        /**
         * Report relevant information regarding current looping speed
         * Reports are internally synchronized, thus parallel looping
         * is also ok.
         * @param loopCount
         * @return true if reported looping speed, false otherwise
         */
        public boolean reportIfNeeded(){
            if (disabled)
                return false;
            if(counter.get() == 0){
                synchronized(this){
                    if (counter.get() == 0) {
                        startTime = now();
                        lastReport = startTime;
                    }
                }
            }
            counter.incrementAndGet();
            long curTime = now();
            if (curTime - lastReport > interval) {
                synchronized(this){
                    if (curTime - lastReport > interval) {
                        lastReport = curTime;
                        long lapsedSeconds = (curTime - startTime) / 1000;
                        System.out.printf("%s %d at %.2f/s\n",message,counter.get(),counter.doubleValue()/ lapsedSeconds);
                        return true;
                    }
                }
            }
            return false;
        }
        
        public void reportFinishTime(){
            final TimeUnit[] units = {TimeUnit.HOURS,TimeUnit.MINUTES,TimeUnit.SECONDS};
            ArrayList<Long> parts = new ArrayList<Long>();
            long remaining = now() - startTime;
            for(TimeUnit unit:units){
                long measure = unit.convert(remaining, TimeUnit.MILLISECONDS);
                remaining = remaining - TimeUnit.MILLISECONDS.convert(measure, unit);
                parts.add(measure);
            }
            StringBuilder sb = new StringBuilder("Finished in ");
            for(int i=0;i<units.length;i++){
                sb.append(parts.get(i))
                .append(' ')
                .append(units[i])
                .append(", ");
            }
            sb.deleteCharAt(sb.length()-1);
            logger.info(sb.toString());
        }

        /**
         * 
         * @return The number of iterations that this {@link Reporter} has been through
         */
        public int getCount() {
            return counter.get();
        }
        
        public void setCount(int counts) {
            counter.set(counts);
        }
        
    }

    protected String name;

    public Timer(String name) {
        this.name = name;
    }

    public void timedRun() {
        long start = now();
        try {
            run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        summarize(start);
    }

    public void timedRun(int iterations) {

        long start = now();
        for (int i = 0; i < iterations; i++) {
            try {
                run();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        System.out.printf("Took %d ms on average.\n",summarize(start)/iterations);
    }

    /**
     * Override to report time usage in other ways
     *
     * @param startTime
     */
    public long summarize(long startTime) {
        long timeSpent = now() - startTime;
        System.out.printf("%s took %d ms\n", name, timeSpent);
        return timeSpent;
    }

    /**
     * 
     * @return A runnable thread version of this class
     */
    public Thread toThread() {
        return new Thread() {
            @Override
            public void run() {
                timedRun();
            }
        };
    }

    /**
     * 
     * @param timeStamp
     * @return The time past from the given timeStamp
     */
    public static long since(long timeStamp) {
        return now() - timeStamp;
    }

    /**
     * 
     * @return Short-hand for {@link System#currentTimeMillis()}
     */
    public static long now() {
        return System.currentTimeMillis();
    }

}
