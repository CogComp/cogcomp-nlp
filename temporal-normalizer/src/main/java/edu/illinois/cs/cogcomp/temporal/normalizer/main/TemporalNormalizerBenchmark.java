/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval.TimexChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import static org.junit.Assert.fail;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
/**
 * Created by zhilifeng on 8/2/17.
 * We require users to use TempEval3 dataset (following TIMEX3 standard) for evaluation.
 * You can download the dataset from https://www.cs.york.ac.uk/semeval-2013/task1/index.php%3Fid=data.html
 * Refer to http://www.timeml.org/tempeval2/tempeval2-trial/guidelines/timex3guidelines-072009.pdf for detail
 * about TIMEX3 standard.
 *
 * This class only generates the evaluated files. Please run a separate evaluation tool provided by TempEval3
 * to get the actual performance. Refer to README.md Performance Benchmark session for details about how to run.
 */
public class TemporalNormalizerBenchmark {
    private TemporalChunkerAnnotator tca;
    private static Logger logger = LoggerFactory.getLogger(TemporalNormalizerBenchmark.class);
    private List<String> DCTs;
    private List<String> testText;

    private List<String> docIDs;
    private List<String> te3inputText;

    /**
     * Setting up TemporalChunkerAnnotator, prepare dataset
     * @param fullFolderName folder name of the dataset
     * @param useHeidelTime boolean whether use HeidelTime for normalization or not
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void setUp(String fullFolderName, boolean useHeidelTime) throws IOException, ParserConfigurationException, SAXException {
        testText = new ArrayList<>();
        DCTs = new ArrayList<>();
        docIDs = new ArrayList<>();
        te3inputText = new ArrayList<>();

        Properties rmProps = new TemporalChunkerConfigurator().getDefaultConfig().getProperties();
        rmProps.setProperty("useHeidelTime", useHeidelTime ? "True" : "False");
        tca = new TemporalChunkerAnnotator(new ResourceManager(rmProps));

        File folder = new File(fullFolderName);
        File[] listOfFiles = folder.listFiles();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                String testFile = fullFolderName + "/" + file.getName();
                byte[] encoded = Files.readAllBytes(Paths.get(testFile));
                String fileContent = new String(encoded, StandardCharsets.UTF_8);
                te3inputText.add(fileContent);
                Document document = builder.parse(new InputSource(new StringReader(fileContent)));
                Element rootElement = document.getDocumentElement();
                NodeList nodeList = rootElement.getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node currentNode = nodeList.item(i);
                    if (currentNode.getNodeName().equals("TEXT")) {
                        testText.add(currentNode.getTextContent());
                    }
                    if (currentNode.getNodeName().indexOf("DOCID")!=-1) {
                        docIDs.add(currentNode.getTextContent());
                    }
                    if (currentNode.getNodeName().indexOf("DCT")!=-1) {
                        Node dctNode = currentNode.getChildNodes().item(0);
                        NamedNodeMap dctAttrs = dctNode.getAttributes();
                        for (int j = 0; j < dctAttrs.getLength(); j++) {
                            if (dctAttrs.item(j).getNodeName().equals("value")) {
                                DCTs.add(dctAttrs.item(j).getNodeValue());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Normalize the dataset using real extraction
     * @param outputFolder
     * @param verbose
     * @throws Exception
     */
    public void testNormalizationWithTrueExtraction(String outputFolder, boolean verbose) throws Exception {
        TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(false, false));
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        ResourceManager nerRm = new TemporalChunkerConfigurator().getDefaultConfig();
        IOUtilities.existsInClasspath(TemporalChunkerAnnotator.class, nerRm.getString("modelDirPath"));

        java.util.logging.Logger.getLogger("HeidelTimeStandalone").setLevel(Level.OFF);

        long preprocessTime = System.currentTimeMillis();
        List <TextAnnotation> taList = new ArrayList<>();
        POSAnnotator annotator = new POSAnnotator();
        for (int j = 0; j < te3inputText.size(); j ++) {
            String text = testText.get(j);
            text = text.replaceAll("\\n", " ");
            TextAnnotation ta = tab.createTextAnnotation("corpus", "id", text);
            try {
                annotator.getView(ta);
            } catch (AnnotatorException e) {
                fail("AnnotatorException thrown!\n" + e.getMessage());
            }

            taList.add(ta);
        }

        long startTime = System.currentTimeMillis();

        int numTimex = 0;

        File outDir = new File(outputFolder);
        if (!outDir.exists()) {
            outDir.mkdir();
        }

        for (int j = 0; j < te3inputText.size(); j ++) {
            TextAnnotation ta = taList.get(j);
            tca.addDocumentCreationTime(DCTs.get(j));

            if (verbose) {
                System.out.println(docIDs.get(j));
            }

            try {
                List<TimexChunk> timex = tca.extractTimexFromFile(te3inputText.get(j), testText.get(j), ta, verbose);

                tca.setTimex(timex);
                String outputFileName = outputFolder + "/" +docIDs.get(j) + ".tml";

                tca.write2Text(outputFileName, docIDs.get(j) ,testText.get(j));
                numTimex += timex.size();
                tca.deleteTimex();
            } catch (AnnotatorException e) {
                fail("Exception while adding TIMEX3 VIEW " + e.getStackTrace());
            }

        }
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Process time: " + totalTime);
        System.out.println("Preprocess + process time: " + (endTime-preprocessTime) );
        System.out.println("Total timex3: " + numTimex );

    }

    /**
     * Normalize the dataset using our Chunker for temporal phrases extraction
     * @param outputFolder
     * @param verbose
     * @throws Exception
     */
    public void testTemporalChunker(String outputFolder, boolean verbose) throws Exception {
        TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(false, false));

        ResourceManager nerRm = new TemporalChunkerConfigurator().getDefaultConfig();
        IOUtilities.existsInClasspath(TemporalChunkerAnnotator.class, nerRm.getString("modelDirPath"));
        java.util.logging.Logger.getLogger("HeidelTimeStandalone").setLevel(Level.OFF);

        List <TextAnnotation>taList = new ArrayList<>();
        long preprocessTime = System.currentTimeMillis();
        POSAnnotator annotator = new POSAnnotator();

        for (int j = 0; j < testText.size(); j ++) {
            TextAnnotation ta = tab.createTextAnnotation("corpus", "id", testText.get(j));
            try {
                annotator.getView(ta);
            } catch (AnnotatorException e) {
                fail("AnnotatorException thrown!\n" + e.getMessage());
            }
            taList.add(ta);
        }

        if (verbose) {
            System.out.println("Start");
        }
        long startTime = System.currentTimeMillis();
        File outDir = new File(outputFolder);
        if (!outDir.exists()) {
            outDir.mkdir();
        }

        for (int j = 0; j < testText.size(); j ++) {

            tca.addDocumentCreationTime(DCTs.get(j));
            TextAnnotation ta = taList.get(j);

            try {
                tca.addView(ta);
            } catch (AnnotatorException e) {
                fail("Exception while adding TIMEX3 VIEW " + e.getStackTrace());
            }

            String outputFileName = "./" + outputFolder + "/" + docIDs.get(j) + ".tml";
            if (verbose) {
                System.out.println(docIDs.get(j));
                for(TimexChunk tc: tca.getTimex()) {
                    System.out.println(tc.toTIMEXString());
                }
                System.out.println("\n");
            }
            tca.write2Text(outputFileName, docIDs.get(j) ,testText.get(j));
            tca.deleteTimex();
        }
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        if (verbose) {
            System.out.println("Process time: " + totalTime);
            System.out.println("Preprocess + process time: " + (endTime - preprocessTime));
        }
    }

    /**
     *
     * @param args 1. -verbose, this is optional
     *             2. -useGoldChunk, optional, if not set, temporal chunker will be used
     *             3. -inputFolder <filepath>, mandatory
     *             4. -outputFolder <filepath>, mandatory
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
        CommandLine commandLine;
        Options options = new Options();
        options.addOption("useHeidelTime", false, "If user wants to use HeidelTime to normalize");
        options.addOption("useGoldChunk", false, "If user wants to use gold timex extraction");
        options.addOption("verbose", false, "If user wants to print execution details");
        Option input_opt = Option.builder("inputFolder")
                .required(true)
                .hasArg()
                .desc("The folder of input data")
                .build();
        Option output_opt = Option.builder("outputFolder")
                .required(true)
                .hasArg()
                .desc("The folder where user wants to write data")
                .build();
        options.addOption(input_opt);
        options.addOption(output_opt);

        CommandLineParser parser = new DefaultParser();
        commandLine = parser.parse(options, args);

        boolean useHeidelTime = commandLine.hasOption("useHeidelTime");
        boolean useGoldChunk = commandLine.hasOption("useGoldChunk");
        boolean verbose = commandLine.hasOption("verbose");
        String inputFolder = commandLine.getOptionValue("inputFolder");
        String outputFolder = commandLine.getOptionValue("outputFolder");


        TemporalNormalizerBenchmark benchmark = new TemporalNormalizerBenchmark();

        benchmark.setUp(inputFolder, useHeidelTime);
        if (useGoldChunk) {
            benchmark.testNormalizationWithTrueExtraction(outputFolder, verbose);
        }
        else {
            benchmark.testTemporalChunker(outputFolder, verbose);
        }


    }

}
