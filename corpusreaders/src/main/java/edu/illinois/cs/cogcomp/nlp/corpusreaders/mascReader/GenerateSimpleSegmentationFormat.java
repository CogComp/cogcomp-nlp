/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.mascReader;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;

/**
 * Given TextAnnotations serialized to json, generate a simple format for use in other (non-java) systems.
 *
 * @author mssammon
 */
public class GenerateSimpleSegmentationFormat {

    private static final String NAME = GenerateSimpleSegmentationFormat.class.getCanonicalName();

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: " + NAME + " jsonDir outDir");
            System.exit(-1);
        }
        String inDir = args[0];
        String outDir = args[1];

        IOUtils.mkdir(outDir);

        String[] inFiles = new String[0];
        try {
            inFiles = IOUtils.lsFiles(inDir);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        for (String jsonFile : inFiles) {
            TextAnnotation ta = null;
            try {
                ta = SerializationHelper.deserializeTextAnnotationFromFile(jsonFile, true);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }

            View sentences = ta.getView(ViewNames.SENTENCE);
            View tokens = ta.getView(ViewNames.TOKENS);

            String fileStem = outDir + "/" + ta.getId();

            String tokFile = fileStem + "_tok.txt";
            String sentFile = fileStem + "_sent.txt";
            String textFile = fileStem + "_text.txt";

            try {
                LineIO.write(textFile, Collections.singletonList(ta.getText()));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }

            try {
                printCharSpans(tokens, tokFile);
                printCharSpans(sentences, sentFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    private static void printCharSpans(View view, String file) throws FileNotFoundException {
        PrintStream out = new PrintStream(file);

        for (Constituent c : view.getConstituents())
            out.println(c.getStartCharOffset() + "," + c.getEndCharOffset());

        out.close();
    }
}
