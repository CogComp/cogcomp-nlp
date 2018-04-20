/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.ner.LbjTagger;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.ExpressiveFeaturesAnnotator;
import edu.illinois.cs.cogcomp.ner.IO.OutFile;
import edu.illinois.cs.cogcomp.ner.InferenceMethods.Decoder;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.ner.ParsingProcessingData.PlainTextReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

public class NETagPlain {
    private static final String NAME = NETagPlain.class.getCanonicalName();

    private static NETaggerLevel1 tagger1 = null;
    private static NETaggerLevel2 tagger2 = null;

    private static Logger logger = LoggerFactory.getLogger(NETagPlain.class);

    /**
     * assumes ParametersForLbjCode has been initialized
     */
    public static void init() {
        String modelFile = ParametersForLbjCode.currentParameters.pathToModelFile;
        tagger1 = (NETaggerLevel1) ParametersForLbjCode.currentParameters.taggerLevel1;
        tagger2 = (NETaggerLevel2) ParametersForLbjCode.currentParameters.taggerLevel2;
    }

    /**
     * Does this assume that {@link #init()} has been called already?
     *
     * @param inputPath
     * @param outputPath
     * @throws Exception
     */
    public static void tagData(String inputPath, String outputPath) throws Exception {
        File f = new File(inputPath);
        Vector<String> inFiles = new Vector<>();
        Vector<String> outFiles = new Vector<>();
        if (f.isDirectory()) {
            String[] files = f.list();
            for (String file : files)
                if (!file.startsWith(".")) {
                    inFiles.addElement(inputPath + File.separator + file);
                    outFiles.addElement(outputPath + File.separator + file);
                }
        } else {
            inFiles.addElement(inputPath);
            outFiles.addElement(outputPath);
        }
        for (int fileId = 0; fileId < inFiles.size(); fileId++) {
            logger.debug("Tagging file: " + inFiles.elementAt(fileId));
            ArrayList<LinkedVector> sentences =
                    PlainTextReader.parsePlainTextFile(inFiles.elementAt(fileId));
            NERDocument doc = new NERDocument(sentences, "consoleInput");
            Data data = new Data(doc);
            ExpressiveFeaturesAnnotator.annotate(data);
            // formerly there was code to load models here. Check that NETagPlain.init() is
            // happening.
            String tagged = tagData(data, tagger1, tagger2);
            OutFile out = new OutFile(outFiles.elementAt(fileId));
            out.println(tagged);
            out.close();
        }
    }

    /**
     * Before calling this, be sure to call {@code NETagPlain.init()}
     *
     * @param line a string to be annotated (for example:
     * 
     *        <pre>
     * John Smith works for Enron
     * </pre>
     * 
     *        )
     * @return an annotated string (looks like
     * 
     *         <pre>
     * [PER John Smith] works for [ORG Enron]
     * </pre>
     * 
     *         )
     * @throws Exception
     */
    public static String tagLine(String line) throws Exception {
        logger.debug(NAME + ".tagLine(): tagging input '" + line + "'...");
        ArrayList<LinkedVector> sentences = PlainTextReader.parseText(line);

        // NOTICE: this only checks tagger1 because tagger2 may legally be null.
        if (tagger1 == null) {
            logger.error("Tagger1 is null. You may need to call NETagPlain.init() first.");
            return "";
        }

        return tagSentenceVector(sentences, tagger1, tagger2);
    }

    public static AnnotatedDocument getAnnotatedDocument(String input) throws Exception {
        ArrayList<LinkedVector> sentences = PlainTextReader.parseText(input);
        NERDocument doc = new NERDocument(sentences, "consoleInput");
        Data data = new Data(doc);
        ExpressiveFeaturesAnnotator.annotate(data);

        // NOTICE: this only checks tagger1 because tagger2 may legally be null.
        if (tagger1 == null) {
            logger.error("Tagger1 is null. You may need to call NETagPlain.init() first.");
            return null;
        }

        Decoder.annotateDataBIO(data, tagger1, tagger2);
        return new AnnotatedDocument(data);
    }

    public static String tagLine(String line, NETaggerLevel1 tagger1, NETaggerLevel2 tagger2)
            throws Exception {
        ArrayList<LinkedVector> sentences = PlainTextReader.parseText(line);
        return tagSentenceVector(sentences, tagger1, tagger2);
    }


    public static String tagTextFromFile(String line, NETaggerLevel1 tagger1, NETaggerLevel2 tagger2)
            throws Exception {
        ArrayList<LinkedVector> sentences = PlainTextReader.parsePlainTextFile(line);
        return tagSentenceVector(sentences, tagger1, tagger2);
    }

    public static String tagSentenceVector(ArrayList<LinkedVector> sentences,
            NETaggerLevel1 tagger1, NETaggerLevel2 tagger2) throws Exception {
        NERDocument doc = new NERDocument(sentences, "consoleInput");
        Data data = new Data(doc);
        return tagData(data, tagger1, tagger2);
    }

    public static String tagData(Data data) throws Exception {
        return tagData(data, tagger1, tagger2);
    }

    public static String tagData(Data data, NETaggerLevel1 tagger1, NETaggerLevel2 tagger2)
            throws Exception {
        ExpressiveFeaturesAnnotator.annotate(data);
        Decoder.annotateDataBIO(data, tagger1, tagger2);

        StringBuffer res = new StringBuffer();
        for (int docid = 0; docid < data.documents.size(); docid++) {
            ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;
            for (LinkedVector vector : sentences) {
                boolean open = false;
                String[] predictions = new String[vector.size()];
                String[] words = new String[vector.size()];
                for (int j = 0; j < vector.size(); j++) {
                    predictions[j] = ((NEWord) vector.get(j)).neTypeLevel2;
                    words[j] = ((NEWord) vector.get(j)).form;
                }
                for (int j = 0; j < vector.size(); j++) {
                    if (predictions[j].startsWith("B-")
                            || (j > 0 && predictions[j].startsWith("I-") && (!predictions[j - 1]
                                    .endsWith(predictions[j].substring(2))))) {
                        res.append("[").append(predictions[j].substring(2)).append(" ");
                        open = true;
                    }
                    res.append(words[j]).append(" ");
                    if (open) {
                        boolean close = false;
                        if (j == vector.size() - 1) {
                            close = true;
                        } else {
                            if (predictions[j + 1].startsWith("B-"))
                                close = true;
                            if (predictions[j + 1].equals("O"))
                                close = true;
                            if (predictions[j + 1].indexOf('-') > -1
                                    && (!predictions[j].endsWith(predictions[j + 1].substring(2))))
                                close = true;
                        }
                        if (close) {
                            // SWM: makes the output a little cleaner
                            String str_res = res.toString().trim();
                            res = new StringBuffer(str_res);
                            res.append("] ");
                            open = false;
                        }
                    }
                }
            }
        }
        return res.toString();
    }

    public static String insertHtmlColors(String annotatedText) {
        String res = annotatedText.replace("[PER", "<font style=\"color:red\">[PER");
        res = res.replace("[LOC", "<font style=\"color:blue\">[LOC");
        res = res.replace("[ORG", "<font style=\"color:green\">[ORG");
        res = res.replace("[MISC", "<font style=\"color:brown\">[MISC");

        res = res.replace("]", "]</font>");
        return res;
    }
}
