package edu.illinois.cs.cogcomp.chunker.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;
import java.util.List;

import de.bwaldvogel.liblinear.Train;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.WordSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.PlainToTokenParser;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.chunker.main.lbjava.*;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * This Chunker Tagger uses a pre-trained model. The model files will be found by checking two locations
 * in order:
 * <ul>
 * <li>First, the directory specified in the constant Constants.modelPath
 * <li>If the files are not found in this directory, the classpath will be checked (this will result
 * in loading the files from the maven repository
 * </ul>
 *
 * This class acts as a wrapper around the Chunker Tagging classes defined in the LBJava code.
 * 
 * @author nitishgupta
 */


public class TrainedChunker {

    private Chunker chunker;
    //private wordForm wordForm;

    /**
     * Initializes a tagger from either a pre-specified directory or the classpath
     */
    public TrainedChunker() {
        ResourceManager rm = new ChunkerConfigurator().getDefaultConfig();
        URL modelFile = null;
        URL modelLexFile = null;
        try {
            if ((new File(rm.getString("modelPath"))).exists()) {
                modelFile = (new File(rm.getString("modelPath"))).toURL();
            } else {
                modelFile =
                        IOUtilities.loadFromClasspath(TrainedChunker.class,
                                rm.getString("modelPath"));
            }
            if ((new File(rm.getString("modelLexPath"))).exists()) {
                modelLexFile = (new File(rm.getString("modelLexPath"))).toURL();
            } else {
                modelLexFile =
                        IOUtilities.loadFromClasspath(TrainedChunker.class,
                                rm.getString("modelLexPath"));
            }
        } catch (MalformedURLException e) {
            System.out.println("ERROR: MALRFORMED URL (THIS SHOULD NEVER HAPPEN)");
            System.exit(1);
        }
        chunker = Chunker.getInstance();
        chunker.readModel(modelFile);
	    chunker.readLexicon(modelLexFile);

        //wordForm = new wordForm();
    }

    

	/**
     * Finds the correct POS tag for the provided token
     *
     * @param w The Token whose POS tag is being sought
     * @return A string representing the POS tag for the token
     */

    public String discreteValue(Token w) {
        return chunker.discreteValue(w);
    }

    public static void main(String [] args) throws IOException {
        String testFileName = "testIn.txt";
        String testFile;
        String refFileName = "testRefOutput.txt";
        List<String> refSentences;

        URL testFileURL = TrainedChunker.class.getClassLoader().getResource(testFileName);
        assertNotNull("Test file missing", testFileURL);
        testFile = testFileURL.getFile();
        assertNotNull("Reference file missing", TrainedChunker.class.getClassLoader().getResource(refFileName));
        BufferedReader refReader = new BufferedReader(
                new InputStreamReader(TrainedChunker.class.getClassLoader().getResourceAsStream(refFileName)));

        refSentences = new ArrayList<String>();

        String line;
        while ((line = refReader.readLine()) != null) {
            refSentences.add(line);
        }

        Chunker tagger = new Chunker();
        Parser parser = new PlainToTokenParser(new WordSplitter(new SentenceSplitter(testFile)));
        String previous = "";
        String sentence = "";
        int sentenceCounter = 0;

        for (Token w = (Token) parser.next(); w != null; w = (Token) parser.next()) {
            String prediction = tagger.discreteValue(w);
            if (prediction.startsWith("B-") || prediction.startsWith("I-")
                    && !previous.endsWith(prediction.substring(2)))
                sentence += ("[" + prediction.substring(2) + " ");

            sentence += ("(" + w.partOfSpeech + " " + w.form + ") ");

            if (!prediction.equals("O")
                    && (w.next == null
                    || tagger.discreteValue((Token)w.next).equals("O")
                    || tagger.discreteValue((Token)w.next).startsWith("B-")
                    || !tagger.discreteValue((Token)w.next)
                    .endsWith(prediction.substring(2))))
                sentence += ("] ");

            if (w.next == null) {
                sentence = sentence.trim();
                String refSentence = refSentences.get(sentenceCounter).trim();
                if (!sentence.equals(refSentence))
                    fail("Produced output doesn't match reference: " +
                            "\nProduced: " + sentence +
                            "\nExpected: " + refSentence);
                sentence = "";
                sentenceCounter++;
            }
            previous = prediction;
        }
    }

}
