/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pos;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import edu.illinois.cs.cogcomp.lbjava.classify.Feature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;

import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.lbjava.parse.FoldSeparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This parser returns <code>FeatureVector</code>s deserialized out of the first file passed to the
 * constructor. In fact, this first file contains only <code>int</code>s that index the features.
 * The second file passed to the constructor is the lexicon used to translate from <code>int</code>s
 * to <code>Feature</code> objects.
 *
 * <p>
 * When run as a stand-alone program, this class takes the names of the example and lexicon files as
 * input on the command line and simply writes a string representation of every example to
 * <code>STDOUT</code>.
 *
 * @author Nick Rizzolo
 **/
public class FeatureVectorParser implements Parser {
    private static Logger logger = LoggerFactory.getLogger(FeatureVectorParser.class);

    /** Reader for file currently being parsed. */
    protected DataInputStream in;
    /** The name of the file to parse. */
    protected String exampleFileName;
    /**
     * The feature objects corresponding to the <code>int</code>s in the example file.
     **/
    protected Feature[] lexicon;


    /**
     * For internal use only. This constructor sets up the stream to read in the examples, but does
     * not initialize the lexicon.
     *
     * @param exampleFile The name of the file to parse.
     **/
    protected FeatureVectorParser(String exampleFile) {
        exampleFileName = exampleFile;
        try {
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(exampleFileName)));
        } catch (Exception e) {
            System.err.println("Can't open '" + exampleFileName + "' for input:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates the parser.
     *
     * @param exampleFile The name of the file to parse.
     * @param lexicon The lexicon.
     **/
    public FeatureVectorParser(String exampleFile, Feature[] lexicon) {
        this(exampleFile);
        this.lexicon = lexicon;
    }

    /**
     * Creates the parser.
     *
     * @param exampleFile The name of the file to parse.
     * @param lexiconFile The name of the lexicon file.
     **/
    public FeatureVectorParser(String exampleFile, String lexiconFile) {
        this(exampleFile);

        ObjectInputStream lexIn = null;
        try {
            lexIn = new ObjectInputStream(new FileInputStream(lexiconFile));
        } catch (Exception e) {
            System.err.println("Can't open '" + lexiconFile + "' for input:");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            lexicon = (Feature[]) lexIn.readObject();
        } catch (Exception e) {
            System.err.println("Can't read from '" + lexiconFile + "':");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            lexIn.close();
        } catch (Exception e) {
            System.err.println("Can't close '" + lexiconFile + "':");
            e.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Returns a <code>FeatureVector</code> deserialized out of the given file.
     **/
    public Object next() {
        FeatureVector result = new FeatureVector();

        try {
            int features = in.readInt();

            // A -1 means that there was a fold separator here
            if (features == -1)
                return FoldSeparator.separator;
            else {
                for (int i = 0; i < features; ++i)
                    result.addLabel(lexicon[in.readInt()]);

                features = in.readInt();
                for (int i = 0; i < features; ++i)
                    result.addFeature(lexicon[in.readInt()]);
            }
        } catch (EOFException eof) {
            result = null;
            close();
        } catch (Exception e) {
            System.err.println("Can't read from '" + exampleFileName + "':");
            e.printStackTrace();
            System.exit(1);
        }

        return result;
    }


    /**
     * Resets the example file stream to the beginning. Alternatively, one could simply create a new
     * <code>FeatureVectorParser</code> with the same constructor arguments, but this method avoids
     * re-reading the lexicon.
     **/
    public void reset() {
        try {
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(exampleFileName)));
        } catch (Exception e) {
            System.err.println("Can't open '" + exampleFileName + "' for input:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Takes the names of the example and lexicon files as input on the command line and prints all
     * the examples to <code>STDOUT</code>.
     **/
    public static void main(String[] args) {
        String exampleFile = null;
        String lexiconFile = null;

        try {
            exampleFile = args[0];
            lexiconFile = args[1];
            if (args.length > 2)
                throw new Exception();
        } catch (Exception e) {
            System.err
                    .println("usage: java edu.illinois.cs.cogcomp.lbjava.parse.FeatureVectorParser <example file> <lexicon file>");
            System.exit(1);
        }

        Parser parser = new FeatureVectorParser(exampleFile, lexiconFile);

        for (FeatureVector v = (FeatureVector) parser.next(); v != null; v =
                (FeatureVector) parser.next()) {
            v.sort();
            logger.info(v.toString());
        }
    }


    /** Frees any resources this parser may be holding. */
    public void close() {
        if (in == null)
            return;
        try {
            in.close();
        } catch (Exception e) {
            System.err.println("Can't close '" + exampleFileName + "':");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
