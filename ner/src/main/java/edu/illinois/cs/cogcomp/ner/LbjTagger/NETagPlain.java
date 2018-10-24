/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.LbjTagger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.ner.ParsingProcessingData.TextAnnotationConverter;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.ExpressiveFeaturesAnnotator;
import edu.illinois.cs.cogcomp.ner.InferenceMethods.Decoder;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.ner.ParsingProcessingData.PlainTextReader;

public class NETagPlain {
    private static final String NAME = NETagPlain.class.getCanonicalName();

    private static Logger logger = LoggerFactory.getLogger(NETagPlain.class);

    /**
     * Does this assume that init() has been called already?
     *
     * @param inputPath
     * @param outputPath
     * @throws Exception
     */
    public static void tagData(String inputPath, String outputPath, String dataFormat, ParametersForLbjCode params) throws Exception {

        Data data;

        if(!dataFormat.equals("-plaintext")) {
            data = new Data(inputPath, inputPath, dataFormat, new String[]{}, new String[]{}, params);
        }else{
            // plaintext reading/writing.
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

            data = new Data();

            for (int fileId = 0; fileId < inFiles.size(); fileId++) {
                logger.debug("Tagging file: " + inFiles.elementAt(fileId));
                ArrayList<LinkedVector> sentences =
                        PlainTextReader.parsePlainTextFile(inFiles.elementAt(fileId), params);
                NERDocument doc = new NERDocument(sentences, "consoleInput");
                data.documents.add(doc);
            }
        }

        ExpressiveFeaturesAnnotator.annotate(data, params);
        Decoder.annotateDataBIO(data, params);



        if(dataFormat.equals("-c")) {
            for (int docid = 0; docid < data.documents.size(); docid++) {
                List<String> res = new ArrayList<>();
                ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;
                for (LinkedVector vector : sentences) {

                    for (int j = 0; j < vector.size(); j++) {
                        NEWord w = (NEWord) vector.get(j);
                        res.add(w.form + " " + w.neLabel + " " + w.neTypeLevel1);

                    }
                    res.add("");
                }
                LineIO.write(outputPath + "/" + docid + ".txt", res);
            }
        }else if(dataFormat.equals("-json")){
            File inputfiles = new File(inputPath);
            List<TextAnnotation> tas = new ArrayList<>();
            for(String f : inputfiles.list()) {
                TextAnnotation ta = SerializationHelper.deserializeTextAnnotationFromFile(f, true);
                tas.add(ta);
            }
            TextAnnotationConverter.Data2TextAnnotation(data, tas);

            for(TextAnnotation ta : tas){
                SerializationHelper.serializeTextAnnotationToFile(ta, outputPath + "/" + ta.getId(), true);
            }
        }else{
            throw new NotImplementedException("We do not yet support dataFormat of " + dataFormat + " yet.");
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
    public static String tagLine(String line, ParametersForLbjCode params) throws Exception {
        logger.debug(NAME + ".tagLine(): tagging input '" + line + "'...");
        ArrayList<LinkedVector> sentences = PlainTextReader.parseText(line, params);

        // NOTICE: this only checks tagger1 because tagger2 may legally be null.
        if (params.taggerLevel1 == null) {
            logger.error("Tagger1 is null. You may need to call NETagPlain.init() first.");
            return "";
        }

        return tagSentenceVector(sentences, params);
    }

    public static AnnotatedDocument getAnnotatedDocument(String input, ParametersForLbjCode params) throws Exception {
        ArrayList<LinkedVector> sentences = PlainTextReader.parseText(input, params);
        NERDocument doc = new NERDocument(sentences, "consoleInput");
        Data data = new Data(doc);
        ExpressiveFeaturesAnnotator.annotate(data, params);

        // NOTICE: this only checks tagger1 because tagger2 may legally be null.
        if (params.taggerLevel1 == null) {
            logger.error("Tagger1 is null. You may need to call NETagPlain.init() first.");
            return null;
        }

        Decoder.annotateDataBIO(data, params);
        return new AnnotatedDocument(data);
    }

    public static String tagLine(String line, NETaggerLevel1 tagger1, NETaggerLevel2 tagger2, ParametersForLbjCode params)
            throws Exception {
        ArrayList<LinkedVector> sentences = PlainTextReader.parseText(line, params);
        return tagSentenceVector(sentences, params);
    }


    public static String tagTextFromFile(String line, ParametersForLbjCode params)
            throws Exception {
        ArrayList<LinkedVector> sentences = PlainTextReader.parsePlainTextFile(line, params);
        return tagSentenceVector(sentences, params);
    }

    public static String tagSentenceVector(ArrayList<LinkedVector> sentences, ParametersForLbjCode params) throws Exception {
        NERDocument doc = new NERDocument(sentences, "consoleInput");
        Data data = new Data(doc);
        return tagData(data, params);
    }

    public static String tagData(Data data, ParametersForLbjCode params) throws Exception {
        ExpressiveFeaturesAnnotator.annotate(data, params);
        Decoder.annotateDataBIO(data, params);

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
