/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.ner;

import edu.illinois.cs.cogcomp.ner.IO.Keyboard;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.ner.LbjTagger.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.StringTokenizer;

/**
 * The main entry point of the NER system. Supporting training, testing, plaintext annotation and
 * demo modes.
 */
public class NerTagger {
    private static Logger logger = LoggerFactory.getLogger(NerTagger.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage(System.out);
            System.exit(-1);
        }

        ParametersForLbjCode cp = ParametersForLbjCode.currentParameters;
        try {
            boolean areWeTraining = args[0].equalsIgnoreCase("-train");
            Parameters.readConfigAndLoadExternalData(args[args.length - 1], areWeTraining);

            if (args[0].equalsIgnoreCase("-annotate")) {
                NETagPlain.init();
                NETagPlain.tagData(args[1], args[2]);
            }
            if (args[0].equalsIgnoreCase("-demo")) {
                logger.info("Reading model file : " + cp.pathToModelFile + ".level1");
                NETaggerLevel1 tagger1 =
                        new NETaggerLevel1(cp.pathToModelFile + ".level1", cp.pathToModelFile
                                + ".level1.lex");
                logger.info("Reading model file : " + cp.pathToModelFile + ".level2");
                NETaggerLevel2 tagger2 =
                        new NETaggerLevel2(cp.pathToModelFile + ".level2", cp.pathToModelFile
                                + ".level2.lex");
                String input = "";
                while (!input.equalsIgnoreCase("quit")) {
                    input = Keyboard.readLine();
                    if (input.equalsIgnoreCase("quit"))
                        System.exit(0);
                    String res = NETagPlain.tagLine(input, tagger1, tagger2);
                    res = NETagPlain.insertHtmlColors(res);
                    StringTokenizer st = new StringTokenizer(res);
                    StringBuilder output = new StringBuilder();
                    while (st.hasMoreTokens()) {
                        String s = st.nextToken();
                        output.append(" ").append(s);
                    }
                    logger.info(output.toString());
                }
            }
            if (args[0].equalsIgnoreCase("-test"))
                NETesterMultiDataset.test(args[1], false, cp.labelsToIgnoreInEvaluation,
                        cp.labelsToAnonymizeInEvaluation);
            if (args[0].equalsIgnoreCase("-dumpFeatures"))
                NETesterMultiDataset.dumpFeaturesLabeledData(args[1], args[2]);
            if (args[0].equalsIgnoreCase("-train"))
                LearningCurveMultiDataset.getLearningCurve(-1, args[1], args[2]);
            if (args[0].equalsIgnoreCase("-trainFixedIterations"))
                LearningCurveMultiDataset.getLearningCurve(Integer.parseInt(args[1]), args[2],
                        args[3]);
        } catch (Exception e) {
            logger.error("Exception caught: ");
            e.printStackTrace();
            logger.error("");
            printUsage(System.err);
        }
    }

    private static void printUsage(PrintStream out) {
        String usage =
                "Usage: edu.illinois.cs.cogcomp.ner.NerTagger <command> [options] <config-file>\n";
        usage +=
                "commands:\n" + "\t-demo\n" + "\t-annotate <input-dir> <output-dir>\n"
                        + "\t-train <train-dir> <test-dir>\n"
                        + "\t-trainFixedIterations <num-iters> <train-dir> <test-dir>\n"
                        + "\t-test <test-dir>\n" + "\t-dumpFeatures <test-dir> <output-dir>";
        out.println(usage);
    }
}
