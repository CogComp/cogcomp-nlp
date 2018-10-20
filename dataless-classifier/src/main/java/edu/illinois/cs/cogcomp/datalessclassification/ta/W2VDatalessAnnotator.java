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
import org.apache.commons.lang.NotImplementedException;
import org.json.simple.JSONObject;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.datalessclassification.config.DatalessConfigurator;
import edu.illinois.cs.cogcomp.datalessclassification.config.W2VDatalessConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.datalessclassification.representation.w2v.MemoryBasedW2V;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper of Word2Vec-based Dataless Classifier for the cogcomp pipeline.
 * 
 * @author shashank
 */

public class W2VDatalessAnnotator extends ADatalessAnnotator {
    private static String NAME = W2VDatalessAnnotator.class.getCanonicalName();
    private static Logger logger = LoggerFactory.getLogger(W2VDatalessAnnotator.class);

    public W2VDatalessAnnotator() {
        this(new W2VDatalessConfigurator().getDefaultConfig());
    }

    public W2VDatalessAnnotator(ResourceManager config) {
        super(ViewNames.DATALESS_W2V, config, true);
    }

    public W2VDatalessAnnotator(ResourceManager config, JSONObject jsonHierarchy)
            throws NotImplementedException {
        super(ViewNames.DATALESS_W2V, true);
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

    public W2VDatalessAnnotator(ResourceManager config, String hierarchyPath, String labelNameFile,
            String labelDescFile) {
        super(ViewNames.DATALESS_W2V, true);
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

    public W2VDatalessAnnotator(ResourceManager config, Set<String> topNodes,
            Map<String, Set<String>> childMap, Map<String, String> labelNameMap,
            Map<String, String> labelDescMap) {
        super(ViewNames.DATALESS_W2V, true);
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

    @Override
    protected String getClassName() {
        return NAME;
    }

    /**
     * Initializes the Word2Vec Embedding that will be used for computing the representations
     */
    protected void initializeEmbedding(ResourceManager config) {
        conceptWeights = new HashMap<>();
        embedding_dim = config.getInt(W2VDatalessConfigurator.W2V_DIM);
        embedding = new MemoryBasedW2V(config);
    }

    /**
     * @param args config: config file path testFile: Test File
     */
    public static void main(String[] args) {
        CommandLine cmd = ESADatalessAnnotator.getCMDOpts(args);

        ResourceManager rm;

        try {
            String configFile = cmd.getOptionValue("config", "config/project.properties");
            ResourceManager nonDefaultRm = new ResourceManager(configFile);

            rm = new W2VDatalessConfigurator().getConfig(nonDefaultRm);
        } catch (IOException e) {
            rm = new W2VDatalessConfigurator().getDefaultConfig();
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

            W2VDatalessAnnotator datalessAnnotator = new W2VDatalessAnnotator(rm);
            datalessAnnotator.addView(ta);

            List<Constituent> annots = ta.getView(ViewNames.DATALESS_W2V).getConstituents();

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
        } catch (AnnotatorException e) {
            e.printStackTrace();
            logger.error("Error Annotating the Test Document with the Dataless View ... exiting");
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IO Error while reading the test file ... exiting");
            System.exit(-1);
        }
    }
}
