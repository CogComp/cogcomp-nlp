/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
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

        ParametersForLbjCode cp = null;
        try {
            boolean areWeTraining = args[0].equalsIgnoreCase("-train");
            ResourceManager rm = new ResourceManager(args[args.length - 1]);
            cp = Parameters.readConfigAndLoadExternalData(args[args.length - 1], areWeTraining);
            if (args[0].equalsIgnoreCase("-train")) {
                String dataFormat;
                // config file is always the last one.
                if(args.length < 5){
                    dataFormat = "-c";
                }else{
                    dataFormat = args[3];
                }
                LearningCurveMultiDataset.getLearningCurve(-1, dataFormat, args[1], args[2], false, cp);
            }else if (args[0].equalsIgnoreCase("-trainFixedIterations"))
                LearningCurveMultiDataset.getLearningCurve(Integer.parseInt(args[1]), args[2], args[3], false, cp);
            else {
                // load up the models
                ModelLoader.load(rm, rm.getString("modelName"), false, cp);
                if (args[0].equalsIgnoreCase("-annotate")) {

                    String dataFormat;
                    // config file is always the last one.
                    if(args.length < 5){
                        dataFormat = "-plaintext";
                    }else{
                        dataFormat = args[3];
                    }

                    NETagPlain.tagData(args[1], args[2], dataFormat, cp);
                }
                if (args[0].equalsIgnoreCase("-demo")) {
                    String input = "";
                    while (!input.equalsIgnoreCase("quit")) {
                        input = Keyboard.readLine();
                        if (input.equalsIgnoreCase("quit"))
                            System.exit(0);
                        String res = NETagPlain.tagLine(input,
                                (NETaggerLevel1) cp.taggerLevel1, (NETaggerLevel2) cp.taggerLevel2, cp);
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
                if (args[0].equalsIgnoreCase("-test")) {
                    String dataFormat;
                    // config file is always the last one.
                    if(args.length < 4){
                        dataFormat = "-c";
                    }else{
                        dataFormat = args[2];
                    }
                    NETesterMultiDataset.test(args[1], true, dataFormat, cp.labelsToIgnoreInEvaluation,
                            cp.labelsToAnonymizeInEvaluation, cp);
                }
                if (args[0].equalsIgnoreCase("-dumpFeatures"))
                    NETesterMultiDataset.dumpFeaturesLabeledData(args[1], args[2], cp);
            }
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
                "commands:\n" + "\t-demo\n" + "\t-annotate <input-dir> <output-dir> <dataformat = {-c, -json, -plaintext}, -plaintext by default>\n"
                        + "\t-train <train-dir> <test-dir> <dataformat = {-c, -r, -json}, -c by default>\n"
                        + "\t-trainFixedIterations <num-iters> <train-dir> <test-dir>\n"
                        + "\t-test <test-dir> <dataformat = {-c, -r, -json}, -c by default>\n"
                        + "\t-dumpFeatures <test-dir> <output-dir>";
        out.println(usage);
    }
}
