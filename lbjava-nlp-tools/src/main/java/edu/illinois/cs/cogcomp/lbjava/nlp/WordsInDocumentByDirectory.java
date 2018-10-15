/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.lbjava.nlp;

import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.*;


/**
 * This parser creates and returns labeled arrays of <code>String</code>s,
 * each representing all the words in a document.  It assumes the documents
 * can be found in the subdirectories of a user supplied directory.  The
 * names of each subdirectory becomes the label for every document it
 * contains.  That label appears as the first element of the returned array.
 * The documents are returned in a randomized order by default, but that
 * behavior is configurable.  The "words" in each document are computed
 * simply by splitting on whitespace.
 *
 * @author Nick Rizzolo
 **/
public class WordsInDocumentByDirectory implements Parser {
    private static Logger logger = LoggerFactory.getLogger(WordsInDocumentByDirectory.class);
    /**
     * The list of all files to be parsed.
     */
    protected List<File> files;
    /**
     * Points to the next element of {@link #files} to be parsed.
     */
    protected int filesIndex;


    /**
     * Creates a new parser that reads all subdirectories and randomizes the
     * order in which their contents are returned.
     *
     * @param directory This directory contains subdirectories (whose names
     *                  will be used as labels) which contain the documents to
     *                  be parsed.
     **/
    public WordsInDocumentByDirectory(String directory) {
        this(directory, null);
    }

    /**
     * Creates a new parser that reads all subdirectories except for the named
     * exceptions and randomizes the order in which their contents are
     * returned.
     *
     * @param directory  This directory contains subdirectories (whose names
     *                   will be used as labels) which contain the documents to
     *                   be parsed.
     * @param exceptions None of the subdirectories whose names appear in this
     *                   array will be parsed.  It may be null if there are no
     *                   exceptions.
     **/
    public WordsInDocumentByDirectory(String directory, String[] exceptions) {
        this(directory, exceptions, true);
    }

    /**
     * Creates a new parser that reads all subdirectories except for the named
     * exceptions.
     *
     * @param directory  This directory contains subdirectories (whose names
     *                   will be used as labels) which contain the documents to
     *                   be parsed.
     * @param exceptions None of the subdirectories whose names appear in this
     *                   array will be parsed.  It may be null if there are no
     *                   exceptions.
     * @param shuffle    Whether or not to randomly shuffle the order in which
     *                   examples are returned.
     **/
    public WordsInDocumentByDirectory(String directory, String[] exceptions, boolean shuffle) {
        this(directory, exceptions, true, -1);
    }

    /**
     * Creates a new parser that reads all subdirectories except for the named
     * exceptions.
     *
     * @param directory  This directory contains subdirectories (whose names
     *                   will be used as labels) which contain the documents to
     *                   be parsed.
     * @param exceptions None of the subdirectories whose names appear in this
     *                   array will be parsed.  It may be null if there are no
     *                   exceptions.
     * @param shuffle    Whether or not to randomly shuffle the order in which
     *                   examples are returned.
     * @param seed       For the random number generator.  If set to -1, no
     *                   seed is used.
     **/
    public WordsInDocumentByDirectory(String directory, String[] exceptions,
                                      boolean shuffle, long seed) {
        File d = new File(directory);
        if (!d.exists() || !d.isDirectory()) {
            System.err.println(
                    "Error: '" + directory + "' does not exist or is not a directory.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        final String[] e = exceptions == null ? new String[0] : exceptions;
        Arrays.sort(e);
        File[] ds =
                d.listFiles(
                        new FileFilter() {
                            public boolean accept(File f) {
                                return f.isDirectory()
                                        && Arrays.binarySearch(e, f.getName()) < 0;
                            }
                        });

        files = new ArrayList<>();
        for (File d1 : ds) {
            File[] fs =
                    d1.listFiles(
                            new FileFilter() {
                                public boolean accept(File f) {
                                    return f.isFile();
                                }
                            });

            Collections.addAll(files, fs);
        }

        if (shuffle) {
            Random random = seed < 0 ? new Random() : new Random(seed);
            Collections.shuffle(files, random);
        }
    }


    /**
     * Sets {@link #filesIndex} back to 0.
     */
    public void reset() {
        filesIndex = 0;
    }


    /**
     * Returns the next labeled array of words.
     */
    public Object next() {
        if (filesIndex == files.size()) return null;
        File current = files.get(filesIndex++);
        String label = current.getAbsolutePath();
        int lastSeparator = label.lastIndexOf(File.separatorChar);
        label =
                label.substring(
                        label.lastIndexOf(File.separatorChar, lastSeparator - 1) + 1,
                        lastSeparator);
        return fileToArray(current, label);
    }


    /**
     * Frees any resources this parser may be holding.
     */
    public void close() {
    }


    /**
     * Reads in the specified file, splits it on whitespace, and adds all
     * resulting words to an array which it returns.  The specified label
     * string appears in the returned array first, before any of the file's
     * words.
     *
     * @param file  The file to read in.
     * @param label The label associated with this file, which should appear
     *              as the first element of the returned array.
     * @return An array containing <code>label</code> followed by all the words
     * in the file in the order they appeared.
     **/
    public static String[] fileToArray(File file, String label) {
        LinkedList<String> words = new LinkedList<>();
        words.add(label);
        BufferedReader in = openReader(file);

        for (String line = readLine(in, file); line != null;
             line = readLine(in, file)) {
            String[] lineWords = line.split("\\s+");
            for (String lineWord : lineWords)
                if (!lineWord.matches("^\\s*$"))
                    words.add(lineWord);
        }

        closeReader(in, file);
        return words.toArray(new String[words.size()]);
    }


    /**
     * Opens a new input stream reading from the specified file, handling any
     * exception by reporting the error and exiting the program.
     *
     * @param inputFile The file to read from.
     * @return A reader to read from the input file.
     **/
    static BufferedReader openReader(File inputFile) {
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(inputFile));
        } catch (Exception e) {
            System.err.println("Can't open '" + inputFile + "' for input: " + e);
            System.exit(1);
        }

        return in;
    }


    /**
     * Reads a single line from the specified input stream, handling any
     * exception by reporting the error and exiting the program.
     *
     * @param in        The input stream.
     * @param inputFile The name of the file being read from (for inclusion in
     *                  any error message).
     * @return A single line of text from the input stream, including a
     * terminating newline character if any.
     **/
    static String readLine(BufferedReader in, File inputFile) {
        String line = null;

        try {
            line = in.readLine();
        } catch (Exception e) {
            System.err.println("Can't read from '" + inputFile + "': " + e);
            System.exit(1);
        }

        return line;
    }


    /**
     * Closes the specified input stream, handling any exception by reporting
     * the error and exiting the program.
     *
     * @param in        The input stream.
     * @param inputFile The name of the file being read from (for inclusion in
     *                  any error message).
     **/
    static void closeReader(BufferedReader in, File inputFile) {
        try {
            in.close();
        } catch (Exception e) {
            System.err.println("Can't close input file '" + inputFile + "': " + e);
            System.exit(1);
        }
    }
}

