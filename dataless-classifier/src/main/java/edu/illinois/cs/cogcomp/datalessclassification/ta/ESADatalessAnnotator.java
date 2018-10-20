/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.ta;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.NotImplementedException;
import org.json.simple.JSONObject;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.datalessclassification.config.DatalessConfigurator;
import edu.illinois.cs.cogcomp.datalessclassification.config.ESADatalessConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.datalessclassification.representation.esa.MemoryBasedESA;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper of ESA-based Dataless Classifier for the cogcomp pipeline.
 * 
 * @author shashank
 */

public class ESADatalessAnnotator extends ADatalessAnnotator {
    private static final String NAME = ESADatalessAnnotator.class.getCanonicalName();
    private static Logger logger = LoggerFactory.getLogger(ESADatalessAnnotator.class);

    public ESADatalessAnnotator() {
        this(new ESADatalessConfigurator().getDefaultConfig());
    }

    public ESADatalessAnnotator(ResourceManager config) {
        super(ViewNames.DATALESS_ESA, config, true);
    }

    public ESADatalessAnnotator(ResourceManager config, JSONObject jsonHierarchy)
            throws NotImplementedException {
        super(ViewNames.DATALESS_ESA, true);
        logger.info("Initializing LabelTree...");
        initializeLabelTree(jsonHierarchy);
        logger.info("LabelTree Initialization Done.");

        logger.info("Initializing Embedding...");
        initializeEmbedding(config);
        logger.info("Embedding Initialization Done.");

        logger.info("Initializing Classifier...");
        initializeClassifier(config);
        logger.info("Classifier Initialization Done.");

        isInitialized = true;
    }

    public ESADatalessAnnotator(ResourceManager config, String hierarchyPath, String labelNameFile,
            String labelDescFile) {
        super(ViewNames.DATALESS_ESA, true);
        logger.info("Initializing LabelTree...");
        initializeLabelTree(hierarchyPath, labelNameFile, labelDescFile);
        logger.info("LabelTree Initialization Done.");

        logger.info("Initializing Embedding...");
        initializeEmbedding(config);
        logger.info("Embedding Initialization Done.");

        logger.info("Initializing Classifier...");
        initializeClassifier(config);
        logger.info("Classifier Initialization Done.");

        isInitialized = true;
    }

    public ESADatalessAnnotator(ResourceManager config, Set<String> topNodes,
            Map<String, Set<String>> childMap, Map<String, String> labelNameMap,
            Map<String, String> labelDescMap) {
        super(ViewNames.DATALESS_ESA, true);
        logger.info("Initializing LabelTree...");
        initializeLabelTree(topNodes, childMap, labelNameMap, labelDescMap);
        logger.info("LabelTree Initialization Done.");

        logger.info("Initializing Embedding...");
        initializeEmbedding(config);
        logger.info("Embedding Initialization Done.");

        logger.info("Initializing Classifier...");
        initializeClassifier(config);
        logger.info("Classifier Initialization Done.");

        isInitialized = true;
    }

    /**
     * Initializes the ESA Embedding that will be used for computing the representations
     */
    protected void initializeEmbedding(ResourceManager config) {
        conceptWeights = new HashMap<>();
        embedding_dim = config.getInt(ESADatalessConfigurator.ESA_DIM);
        embedding = new MemoryBasedESA(config);
    }

    @Override
    protected String getClassName() {
        return NAME;
    }

    public static CommandLine getCMDOpts(String[] args) {
        Options options = new Options();

        Option configOpt = new Option("c", "config", true, "config file path");
        configOpt.setRequired(false);
        options.addOption(configOpt);

        Option testFileOption =
                new Option("f", "testFile", true, "File to annotate using Dataless");
        testFileOption.setRequired(false);
        options.addOption(testFileOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return cmd;
        }

        return cmd;
    }


    /**
     * @param args config: config file path testFile: Test File
     */
    public static void main(String[] args) {
        CommandLine cmd = getCMDOpts(args);

        ResourceManager rm;

        try {
            String configFile = cmd.getOptionValue("config", "config/project.properties");
            ResourceManager nonDefaultRm = new ResourceManager(configFile);

            rm = new ESADatalessConfigurator().getConfig(nonDefaultRm);
        } catch (IOException e) {
            rm = new ESADatalessConfigurator().getDefaultConfig();
        }

        String testFile = cmd.getOptionValue("testFile", "data/graphicsTestDocument.txt");

        StringBuilder sb = new StringBuilder();

        String line;

        try(BufferedReader br = new BufferedReader(new FileReader(new File(testFile)))) {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(" ");
            }

            String text = sb.toString().trim();

            TokenizerTextAnnotationBuilder taBuilder =
                    new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
            TextAnnotation ta = taBuilder.createTextAnnotation(text);

            ESADatalessAnnotator datalessAnnotator = new ESADatalessAnnotator(rm);
            datalessAnnotator.addView(ta);

            List<Constituent> annots = ta.getView(ViewNames.DATALESS_ESA).getConstituents();

            System.out.println("Predicted LabelIDs:");
            for (Constituent annot : annots) {
                System.out.println(annot.getLabel());
            }

            Map<String, String> labelNameMap =
                    DatalessAnnotatorUtils.getLabelNameMap(rm
                            .getString(DatalessConfigurator.LabelName_Path.key));

            System.out.println("Predicted Labels:");

            for (Constituent annot : annots) {
                System.out.println(labelNameMap.get(annot.getLabel()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("Test File not found at " + testFile + " ... exiting");
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IO Error while reading the test file ... exiting");
            System.exit(-1);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            logger.error("Error Annotating the Test Document with the Dataless View ... exiting");
            System.exit(-1);
        }
    }
}
