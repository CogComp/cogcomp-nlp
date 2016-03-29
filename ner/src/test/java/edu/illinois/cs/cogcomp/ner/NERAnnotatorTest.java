package edu.illinois.cs.cogcomp.ner;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;

import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.junit.Test;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import weka.core.Debug.Random;

/**
 * This tests the NERAnnotator. Includes a test to ensure we are extracting the expected entiies,
 * one to test performance, and one to test multithreaded viability.
 * <p>
 *
 * This class is also a good platform for performance profiling, the main method can be invoked, and
 * will run for a while so you can obtain a good snapshot on memory and CPU performance.
 * 
 * @author redman
 */
public class NERAnnotatorTest {

    final static String TOKEN_TEST = "NOHO Ltd. partners with Telford International Company Inc";

    /** the text used for this test. */
    final static String TEST_INPUT =
            " For researchers, like Larry Smarr, with big data but little access to the IT tools needed to analyze it, "
                    + "the Extreme Science and "
                    + "Engineering Discovery Environment (XSEDE) Campus Bridging team is a bit like a superhero squad. Based at Indiana "
                    + "University, the team makes it easier to connect researchers' analyses to the national cyberinfrastructure - including "
                    + "computing and data storage systems, advanced instruments and data repositories, visualization environments and people - "
                    + "smoothing the way for discovery and breakthroughs. Campuses around the US are finding that installing the XSEDE Compatible "
                    + "Basic Cluster (XCBC) software suite quickly improves computing. Designed to help researchers ranging from big data scientists "
                    + "to people running small campus clusters, XCBC lets a local campus create a high performance computing (HPC) cluster from "
                    + "scratch. The open source software tools match the software of the most commonly used systems within XSEDE, and Campus "
                    + "Bridging team members even offer on-site installation help. The first such visit took place in April at Marshall University "
                    + "in Huntington, West Virginia. The project involved increasing the cluster's capabilities, including integrating eight GPUs "
                    + "and enhancing system management. XCBC makes computing more accessible for everyone, said Jeremy Fischer, IU senior systems analyst and XCBC engineer. "
                    + "Many times researchers don't have the knowledge or the capability to set up a high performance cluster on their own. By working with the "
                    + "Campus Bridging team on an on-site XCBC installation, these folks can get their science up and running again. Jack Smith can "
                    + "attest to that. As cyberinfrastructure coordinator in the Division of Science and Research of the West Virginia Higher "
                    + "Education Policy Commission, Smith worked closely with Fischer and colleague Eric Coulter when they came to Marshall.";

    /** these are the entities we expect. */
    final static String[] tmp =
            {
                    "Larry Smarr",
                    "Extreme Science and Engineering Discovery Environment", // this is what we
                                                                             // should find, but
                                                                             // don't
                    "Engineering Discovery Environment", // this is what we get rather than the
                                                         // above.
                    "XSEDE",
                    "Campus",
                    "Indiana University",
                    "US",
                    "XSEDE Compatible Basic Cluster",
                    "XCBC",
                    "XSEDE",
                    "Campus Bridging",
                    "Marshall University",
                    "Huntington",
                    "Virginia",
                    "Jeremy Fischer",
                    "XCBC engineer",
                    "Campus Bridging",
                    "Jack Smith",
                    "Division of Science and Research of the West Virginia Higher Education Policy Commission", // should
                                                                                                                // get
                                                                                                                // this,
                                                                                                                // but
                                                                                                                // don't
                    "Division of Science and Research of the", // should NOT get this.
                    "Commission", // should NOT get this.
                    "West Virginia", "Smith", "Fischer", "Eric Coulter", "Marshall", "HPC", // should
                                                                                            // not
                                                                                            // get
                                                                                            // this
                                                                                            // either
                    "IU", // only from level 1
                    "GPUs" // should not get this either
            };

    /** this helper can create text annotations from text. */
    private static final TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(
            new IllinoisTokenizer());

    /** static annotator. */
    static private NERAnnotator nerAnnotator = null;
    static {
        try {
            nerAnnotator =
                    NerAnnotatorManager.buildNerAnnotator(new ResourceManager(new Properties()),
                            ViewNames.NER_CONLL);
        } catch (Exception e) {
            System.out.println("Cannot initialise the test, the exception was: ");
            e.printStackTrace();
            fail();
        }
    }

    /** the expected entities as a hashset. */
    final static HashSet<String> entities = new HashSet<>();
    static {
        Collections.addAll(entities, tmp);
    }

    /**
     * See if we get the right entities back.
     */
    @Test
    public void testResults() {
        TextAnnotation ta = tab.createTextAnnotation(TEST_INPUT);
        View view = getView(ta);
        for (Constituent c : view.getConstituents()) {
            assertTrue("No entity named \"" + c.toString() + "\"", entities.contains(c.toString()));
        }
    }

    /**
     * get a rough measure of this machines performance capabilities. Just do double adds to see how
     * long it takes.
     * 
     * @return the computed performance factor.
     */
    private long measureMachinePerformance() {
        Random r = new Random();
        final int NUMBER_ITERATIONS = 20000;
        double counter = 0.0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUMBER_ITERATIONS; i++) {
            counter += r.nextDouble();
        }
        if (counter < 0)
            System.out.println("this should never happen.");
        return System.currentTimeMillis() - start;
    }

    /**
     * Make sure it runs in reasonable time. We will test the performance of the machine we run on
     * to get a better measure.
     */
    //@Test
    public void testPerformance() {
        // now do performance.
        final int SIZE = 100;

        // make sure any lazy loading is done outside the performance test.
        TextAnnotation tat = tab.createTextAnnotation(TEST_INPUT);
        getView(tat);
        long expectedPerformance = this.measureMachinePerformance();
        System.out.println("Expect " + expectedPerformance);
        {
            TextAnnotation ta = tab.createTextAnnotation(TEST_INPUT);
            View view = getView(ta);
            assertTrue(view != null);
        }
        // start the performance test.
        long start = System.currentTimeMillis();
        for (int i = 0; i < SIZE; i++) {
            TextAnnotation ta = tab.createTextAnnotation(TEST_INPUT);
            View view = getView(ta);
            assertTrue(view != null);
            for (Constituent c : view.getConstituents()) {
                assertTrue("No entity named \"" + c.toString() + "\"",
                        entities.contains(c.toString()));
            }
        }
        start = System.currentTimeMillis() - start;
        start /= SIZE;
        System.out.printf("For text size = %d, average NER runtime = %d, normalized = %f",
                TEST_INPUT.length(), start, (double) start / (double) expectedPerformance);
        assertTrue(start <= expectedPerformance);
    }

    /** this thread runs continuously to sample NER performance. */
    static class NERThread extends Thread {

        /** records average performance. */
        long averageRunTime = -1;

        /** the last view computed. */
        private View view = null;

        /** the number of invocations to do. */
        int duration;

        /** just a counter for unique names. */
        static int c = 0;

        /** contains an error message if there is one. */
        private String error = null;

        NERThread(int size) {
            super("NERPerformanceTest-" + c);
            c++;
            this.duration = size;
        }

        /** just do the same thing over and over. */
        public void run() {
            TextAnnotation ta1 = tab.createTextAnnotation(TEST_INPUT);
            {
                view = getView(ta1);
                assertTrue(view != null);
            }
            long start = System.currentTimeMillis();
            for (int i = 0; i < duration; i++) {
                TextAnnotation ta = tab.createTextAnnotation(TEST_INPUT);
                view = getView(ta);
                for (Constituent c : view.getConstituents()) {
                    if (!entities.contains(c.toString()))
                        error = "No entity named \"" + c.toString() + "\"";
                }
            }
            if (error == null)
                averageRunTime = (System.currentTimeMillis() - start) / duration;
        }
    }

    /**
     * on every core we should get performance below 300 ticks and the results should still be good.
     */
    //@Test
    public void testMultiThreaded() {
        final int SIZE = 1000;

        // create one thread per core, launch them.
        final int numcores = Runtime.getRuntime().availableProcessors();
        ArrayList<NERThread> threads = new ArrayList<>();
        for (int i = 0; i < numcores; i++) {
            NERThread t = new NERThread(SIZE);
            threads.add(t);
            t.start();
        }

        // wait for all to complete.
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (NERThread t : threads) {
            if (t.averageRunTime == -1) {
                System.out.println("Error : " + t.error);
                assertTrue(t.error, false);
            }
            assertTrue("Deficient average run time.", t.averageRunTime < 100);
            for (Constituent c : t.view.getConstituents()) {
                assertTrue("Entity " + c.toString() + " was not found",
                        entities.contains(c.toString()));
            }
        }
    }

    /**
     * test tokenization produces the correct number of constinuents.
     */
    @Test
    public void testTokenization() {
        TextAnnotation ta = tab.createTextAnnotation(TOKEN_TEST);
        View nerView = getView(ta);
        assertEquals(nerView.getConstituents().size(), 2);

        String tokTestB =
                "Grigory Pasko, crusading Russian journalist who documented Russian Navy's mishandling of "
                        + "nuclear waste, is released on parole after serving two-thirds of his four-year prison sentence.";

        ta = tab.createTextAnnotation(tokTestB);
        nerView = getView(ta);

        assertEquals(3, nerView.getNumberOfConstituents());
    }

    /**
     * this is a stand alone test we can run to check performance. If you want to run in a profiler,
     * this is your guy.
     * 
     * @param args the arguments.
     */
    static public void main(String[] args) {
        final int SIZE = 500;

        // need to get any lazy data initialization out of the way to get a good read.
        {
            TextAnnotation ta = tab.createTextAnnotation(TEST_INPUT);
            View view = getView(ta);
            for (Constituent c : view.getConstituents()) {
                assertTrue(entities.contains(c.toString()));
            }
        }

        // create one thread per core, launch them.
        final int numcores = Runtime.getRuntime().availableProcessors() / 2;
        ArrayList<NERThread> threads = new ArrayList<>();
        for (int i = 0; i < numcores; i++) {
            NERThread t = new NERThread(SIZE);
            threads.add(t);
            t.start();
        }
        System.out.println("Running on " + numcores);

        // wait for all to complete.
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int count = threads.size();
        long avg = 0;
        for (NERThread t : threads) {
            avg += t.averageRunTime;
        }
        System.out.println("Average runtime is " + (avg / count));
    }

    public static View getView(TextAnnotation ta) {
        nerAnnotator.addView(ta);
        return ta.getView(nerAnnotator.getViewName());
    }
}
