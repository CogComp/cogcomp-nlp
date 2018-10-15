/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.tagger;

import edu.illinois.cs.cogcomp.ner.IO.InFile;
import edu.illinois.cs.cogcomp.ner.IO.OutFile;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import edu.illinois.cs.cogcomp.ner.NerAnnotatorManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This tagger provided a file or a directory will read the plain text data and produced labeled
 * output. If no output directory name or file output name is provided, it will produce it's output
 * to standard out.
 * <p>
 * 
 * {@code
 * Command Line Options:
 * -f <file name>: the input file to tokenize
 * -d <directory name> : the name of a directory containing input files.
 * -o <directory name>: the output directory where the output files will be produce. The files will share the same name
 *    as the input file, so obvious this directory must be different from the input directory.
 * -c <file name> : the name of the configuration file.
 * -t <number of threads> : Allows users to specify the number of threads to use, by default there will be one
 *    thread for every core on the machine.
 * }
 */
public class NamedEntityTagger {
    private static Logger logger = LoggerFactory.getLogger(NamedEntityTagger.class);

    /** input directory (or file) containing data to run. */
    protected File indirectory = null;

    /** output directory for resulting tagged data. */
    protected File outdirectory = null;

    /** this helper can create text annotations from text. */
    protected final TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(
            new StatefulTokenizer());

    /** the annotator. */
    protected NERAnnotator nerAnnotator = null;

    /** default number of threads is the number of cores, override with -t */
    int max = Runtime.getRuntime().availableProcessors();

    /**
     * Report an error and exist.
     * 
     * @param message error message.
     */
    private void parsingError(String message) {
        System.err.println(message);
        String o =
                "Command Line Options:\n"
                        + "  -i <file or directory name> : the name of a directory or file containing the input.\n"
                        + "  -o <file or directory name>: the output directory or file where the output will be stored. For \n"
                        + "     directories files will share the same name as the input file, so this directory must be different"
                        + "     from the input directory.\n"
                        + "  -c <file name> : the name of the configuration file.\n"
                        + "  -t <number of threads> : Allows users to specify the number of threads to use, by default there will be one"
                        + "     thread for every core on the machine.\n";
        System.err.println(o);
        System.exit(-1);
    }

    /**
     * parse the arguments, only the directory.
     * 
     * @param args the arguments.
     * @throws IOException
     */
    private void parseArguments(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-i")) {
                i++;
                if (args.length <= i) {
                    parsingError(arg + " requires an argument that was not provided.");
                }
                indirectory = new File(args[i]);
                if (!indirectory.exists()) {
                    parsingError(args[i]
                            + " did not exist, the input directory must exist and contain plain text data files.");
                }
            } else if (arg.equals("-o")) {
                i++;
                if (args.length <= i) {
                    parsingError(arg + " requires an argument that was not provided.");
                }
                outdirectory = new File(args[i]);
            } else if (arg.equals("-c")) {
                i++;
                if (args.length <= i) {
                    parsingError(arg + " requires an argument that was not provided.");
                }
                File config = new File(args[i]);
                if (!config.exists()) {
                    parsingError(arg + "The config file specified, \"" + args[i]
                            + "\" did not exist.");
                }
                nerAnnotator =
                        NerAnnotatorManager.buildNerAnnotator(new ResourceManager(args[i]),
                                ViewNames.NER_CONLL);
            } else if (arg.equals("-t")) {
                i++;
                try {
                    max = Integer.parseInt(args[i]);
                } catch (NumberFormatException nfe) {
                    parsingError("\"-t\" must be followed by an integer number to limit the number of threads, \""
                            + args[i] + "\" is not numberic.");
                }
            } else {
                parsingError("\"" + arg + "\" is not a valid command line argument.");
            }
        }
        if (nerAnnotator == null) {
            parsingError("A configuration file must be specified with the \"-c\" option.");
        }
        if (outdirectory != null && !outdirectory.exists()) {
            if (indirectory == null)
                outdirectory.createNewFile();
            else if (indirectory.isDirectory()) {
                outdirectory.mkdirs();
            } else {

                // the input directory is a single file, the output directory will be likewise
                outdirectory.createNewFile();
            }
        }
    }

    /**
     * Render a string representing the original data with embedded labels in the text.
     */
    private String renderString(View labels, TextAnnotation ta) {
        List<Constituent> constituents = new ArrayList<>(labels.getConstituents());
        Collections.sort(constituents, TextAnnotationUtilities.constituentStartComparator);
        StringBuilder sb = new StringBuilder();
        String text = ta.getText();
        int where = 0;
        for (Constituent c : constituents) {
            int start = c.getStartCharOffset();
            String startstring = text.substring(where, start);
            sb.append(startstring).append("[").append(c.getLabel()).append(" ")
                    .append(c.getTokenizedSurfaceForm()).append(" ] ");
            where = c.getEndCharOffset();
        }
        return sb.toString();
    }

    /**
     * produce the output either in file or as on standard out.
     * 
     * @param view the view to render.
     * @param ta the text annotation.
     */
    private void generateOutput(View view, TextAnnotation ta, String filename) {
        String outputstring = renderString(view, ta);
        if (outdirectory == null) {
            logger.info(outputstring);
        } else if (outdirectory.isDirectory()) {
            File outputfile = new File(outdirectory, filename);
            OutFile of = new OutFile(outputfile.toString());
            try {
                of.print(outputstring);
            } finally {
                of.close();
            }
        } else {
            OutFile of = new OutFile(outdirectory.toString());
            try {
                of.print(outputstring);
            } finally {
                of.close();
            }
        }
    }

    /**
     * return an instance of an annotation job for the given input and output file. Subclasses can
     * override to provide their own implementation.
     */
    protected AnnotationJob getAnnotationJob(String input, String output) {
        // define a class to manage the jobs.
        return new FileIOAnnotationJob(input, output, tab, nerAnnotator);
    }

    /**
     * Run a benchmark test against each sub-directory within the benchmark directory.
     * 
     * @param args may specify the directory containing the benchmarks.
     */
    public NamedEntityTagger(String[] args) {
        try {
            parseArguments(args);
            if (indirectory == null) {

                // input is catenated directory to stand input.
                try (Scanner sc = new Scanner(System.in)) {
                    StringBuilder sb = new StringBuilder();
                    while (sc.hasNextLine()) {
                        sb.append(sc.nextLine());
                        sb.append('\n');
                    }

                    // prduce the input string.
                    String instring = sb.toString();

                    // generate the labeled output
                    TextAnnotation ta = tab.createTextAnnotation(instring);
                    View view = nerAnnotator.getView(ta);
                    generateOutput(view, ta, "datafromStdIn");
                } catch (IllegalStateException ise) {
                    // input is closed, file probabaly ended.
                }
            } else if (indirectory.isDirectory()) {
                if (outdirectory != null && outdirectory.isDirectory()) {

                    // make sure all gazetteers and models are preloaded, so not to throw off
                    // performance reporting.
                    // generate the labeled output
                    TextAnnotation ta =
                            tab.createTextAnnotation("John Sampson served on Chevrons board of directorys for three years.");
                    nerAnnotator.getView(ta);


                    // Loop over every file within the input directory. We will do this multi
                    // threaded using a custom implementation of AnnotationJob to do the work
                    // and the TaggerThreaad to measure performance times and the such.

                    // create one thread per core.
                    BlockingQueue<AnnotationJob> jobqueue = new LinkedBlockingQueue<>(max * 2);
                    TaggerThread[] tts = new TaggerThread[max];
                    for (int i = 0; i < max; i++) {
                        tts[i] = new TaggerThread(jobqueue);
                        tts[i].start();
                    }
                    logger.info("Begin processing " + indirectory);

                    // process data.
                    File[] files = indirectory.listFiles();
                    long start = System.currentTimeMillis();
                    int count = 0;
                    for (File file : files) {
                        File outputfile = new File(outdirectory, file.getName());
                        jobqueue.put(this.getAnnotationJob(file.toString(), outputfile.toString()));
                        count++;
                    }

                    int gccount = 0;
                    long heapsize = 0;
                    Runtime rt = Runtime.getRuntime();
                    while (jobqueue.size() > 0) {
                        Thread.sleep(2000);
                        gccount++;
                        rt.gc();
                        heapsize += (rt.totalMemory() - rt.freeMemory());
                    }
                    heapsize /= gccount;

                    // wait for the threads to finish.
                    for (int i = 0; i < max; i++) {
                        tts[i].interrupt();
                    }

                    // wait for the threads to finish.
                    for (int i = 0; i < max; i++) {
                        tts[i].join();
                    }
                    long totalread = 0;
                    long totalcompute = 0;
                    long totalwrite = 0;

                    // wait for the threads to finish.
                    for (int i = 0; i < max; i++) {
                        totalread += (tts[i].readtime / tts[i].count);
                        totalcompute += (tts[i].computetime / tts[i].count);
                        totalwrite += (tts[i].writetime / tts[i].count);
                    }
                    totalread /= max;
                    totalcompute /= max;
                    totalwrite /= max;
                    logger.info("Completed " + count + " files in "
                            + (System.currentTimeMillis() - start) + " ticks, " + heapsize
                            + " average heap size.");
                    logger.info("Average time per document: read = " + totalread
                            + ", compute = " + totalcompute + ", and write = " + totalwrite);
                } else {
                    // Loop over every file within the input directory. We will do this multi
                    // threaded using a custom implementation of AnnotationJob to do the work
                    // and the TaggerThreaad to measure performance times and the such.
                    File[] files = indirectory.listFiles();
                    for (File file : files) {
                        String s = InFile.readFileText(file.toString());

                        // generate the labeled output
                        TextAnnotation ta = tab.createTextAnnotation(s);
                        View view = nerAnnotator.getView(ta);
                        generateOutput(view, ta, file.getName());
                    }
                }
            } else {
                String s = InFile.readFileText(indirectory.toString());

                // generate the labeled output
                TextAnnotation ta = tab.createTextAnnotation(s);
                View view = nerAnnotator.getView(ta);
                generateOutput(view, ta, indirectory.getName());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    /**
     * Run a benchmark test against each subdirectory within the benchmark directory.
     * 
     * @param args may specify the directory containing the benchmarks.
     */
    public static void main(String[] args) {
        new NamedEntityTagger(args);
    }
}
