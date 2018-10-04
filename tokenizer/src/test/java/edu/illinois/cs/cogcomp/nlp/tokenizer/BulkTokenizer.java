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
package edu.illinois.cs.cogcomp.nlp.tokenizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tokenize all the files (assume text) in a directory, report difference in the ways
 * IllinoisTokenizer and StatefulTokenizer tokenize the text. Output is in a file called
 * "tokenizerdiffs.out".
 * 
 * @author redman
 */
public class BulkTokenizer {
    private static Logger logger = LoggerFactory.getLogger(BulkTokenizer.class);

    /** report more. */
    static private boolean verbose = false;

    /** profile new stuff only. */
    static private boolean profile = false;

    /** file or directory to process. */
    static private String file = null;

    /**
     * read an entire file into a string.
     * 
     * @param f the file
     * @return the string.
     * @throws FileNotFoundException
     */
    private static String readString(File f) throws FileNotFoundException {
        Scanner scanner = new Scanner(f, "UTF-8");
        String text = scanner.useDelimiter("\\A").next();
        scanner.close(); // Put this call in a finally block
        return text;
    }

    /**
     * read all the files in teh directory, return them as an array of strings.
     * 
     * @param files the files to read.
     * @return strings with all the data for each fiel
     * @throws FileNotFoundException
     */
    private static ArrayList<String> readAllFiles(File[] files) throws FileNotFoundException {
        ArrayList<String> strings = new ArrayList<String>();
        for (File file : files) {
            strings.add(readString(file));
        }
        return strings;
    }

    /**
     * comprare two strings ignoring differences in "'"s and their relative positions and white
     * space.
     * 
     * @param oo the old string.
     * @param no the new string.
     * @return true if they are equal, false if they are not.
     */
    private static boolean compareSentences(String oo, String no) {
        char[] ochars = oo.toCharArray();
        char[] nchars = no.toCharArray();
        int oindex = 0;
        int nindex = 0;
        while (true) {
            if (oindex == ochars.length)
                if (nindex == nchars.length) {
                    // both at end of sentence.
                    return true;
                } else
                    // end of old string but not new.
                    return false;
            else {
                if (nindex == nchars.length)
                    return false;
            }
            if (ochars[oindex] != nchars[nindex]) {
                // if start of single quote, previous char must have been "'" for both
                if (oindex > 0 && nindex > 0 && ochars[oindex - 1] == '\''
                        && nchars[nindex - 1] == '\'' && nchars[nindex] == ' ') {
                    nindex++;
                    continue;
                } else if (ochars[oindex] == '\'' && nchars[nindex] == ' ') {
                    // index beyond the quote and whitespace
                    nindex++;
                    continue;
                } else if (nchars[nindex] == '\'' && ochars[oindex] == ' ') {
                    // index beyond the quote and whitespace
                    oindex++;
                    continue;
                } else {
                    return false;
                }
            } else {
                ++oindex;
                ++nindex;
            }
        }
    }

    /**
     * parse the argumewnts.
     * 
     * @param args the arguments.
     */
    static private void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-v"))
                verbose = true;
            else if (args[i].equals("-p"))
                profile = true;
            else
                file = args[i];
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        parseArgs(args);
        if (file == null) {
            System.err.println("Must provide a file or directory name on the command line.");
            return;
        }
        File[] files;
        File nf = new File(file);
        if (nf.isDirectory())
            files = new File(args[0]).listFiles();
        else {
            files = new File[1];
            files[0] = nf;
        }
        ArrayList<String> datas = readAllFiles(files);
        BufferedWriter fw = new BufferedWriter(new FileWriter(new File("tokenizerdiffs.out")));
        final TextAnnotationBuilder stab =
                new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
        if (profile) {
            System.out.println("Starting profiling");
            while (true) {
                for (String data : datas) {
                    stab.createTextAnnotation(data);
                }
            }
        } else {
            System.out.println("Starting new annotations");
            long nt = System.currentTimeMillis();
            ArrayList<TextAnnotation> newannotations = new ArrayList<TextAnnotation>();
            final TextAnnotationBuilder ntab =
                    new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
            for (String data : datas) {
                TextAnnotation ta = ntab.createTextAnnotation(data);
                newannotations.add(ta);
            }
            nt = System.currentTimeMillis() - nt;
            System.out.println("Starting old annotations");
            long ot = System.currentTimeMillis();
            ArrayList<TextAnnotation> oldannotations = new ArrayList<TextAnnotation>();
            final TextAnnotationBuilder tab =
                    new TokenizerTextAnnotationBuilder(new IllinoisTokenizer());
            for (String data : datas) {
                TextAnnotation ta = tab.createTextAnnotation(data);
                oldannotations.add(ta);
            }
            ot = System.currentTimeMillis() - ot;
            System.out.println("new way = " + nt + ", old way = " + ot);

            int good = 0, bad = 0;
            for (int i = 0; i < oldannotations.size(); i++) {
                File file = files[i];
                TextAnnotation newone = newannotations.get(i);
                TextAnnotation oldone = oldannotations.get(i);
                if (newone.sentences().equals(oldone.sentences())) {
                    good++;
                } else {
                    bad++;
                    fw.write("-" + file + "\n");
                    if (verbose) {
                        List<Sentence> newsentences = newone.sentences();
                        List<Sentence> oldsentences = oldone.sentences();
                        int max =
                                newsentences.size() > oldsentences.size() ? newsentences.size()
                                        : oldsentences.size();
                        boolean sentencewritten = false;
                        for (int j = 0; j < max; j++) {
                            String news =
                                    newsentences.size() > j ? newsentences.get(j).toString()
                                            : "???";
                            String olds =
                                    oldsentences.size() > j ? oldsentences.get(j).toString()
                                            : "???";
                            if (!compareSentences(olds, news)) {
                                if (!sentencewritten) {
                                    sentencewritten = true;
                                    fw.write("-" + file + "\n");
                                    fw.write(newone.toString() + "\n");
                                }
                                fw.write(" new : " + news + "\n old : " + olds + "\n");
                            }
                        }
                    }
                }
            }
            fw.close();
            System.out.println(good + " correct, " + bad + " wrong.");
        }
    }
}
