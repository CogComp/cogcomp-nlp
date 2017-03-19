package edu.illinois.cs.cogcomp.temporal.normalizer.tests;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.ner.LbjTagger.Parameters;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.temporal.normalizer.main.TemporalChunkerAnnotator;
import edu.illinois.cs.cogcomp.temporal.normalizer.main.TemporalChunkerConfigurator;

import edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval.TimexChunk;
import edu.illinois.cs.cogcomp.thrift.base.Span;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by zhilifeng on 11/1/16.
 */
public class TestTemporalChunker {
//    private String testFileName = "test.txt";
//    private static String testFile;
    private TemporalChunkerAnnotator tca;
    private static Logger logger = LoggerFactory.getLogger(TestTemporalChunker.class);
    //private String testText;
    private List<String> DCTs;
    private List<String> testText;
    private String folderName = "TE3-platinum-test";
    private static String fullFolderName;
    private List<String> docIDs;
    private List<String> te3inputText;
    private String te3ForderName = "te3-platinum";

    @Before
    public void setUp() throws IOException, ParserConfigurationException, SAXException {
        testText = new ArrayList<>();
        DCTs = new ArrayList<>();
        docIDs = new ArrayList<>();

        Properties rmProps = new TemporalChunkerConfigurator().getDefaultConfig().getProperties();
        rmProps.setProperty("useHeidelTime", "False");
        tca = new TemporalChunkerAnnotator(new ResourceManager(rmProps));

        URL testFolderURL = TestTemporalChunker.class.getClassLoader().getResource(folderName);
        fullFolderName = testFolderURL.getFile();
        File folder = new File(fullFolderName);
        File[] listOfFiles = folder.listFiles();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        for (File file : listOfFiles) {
            if (file.isFile()) {
//                if (!file.getName().equals("CNN_20130322_1003.tml.TE3input"))
//                    continue;
                String testFile = fullFolderName + "/" + file.getName();
                byte[] encoded = Files.readAllBytes(Paths.get(testFile));
                String fileContent = new String(encoded, StandardCharsets.UTF_8);
                Document document = builder.parse(new InputSource(new StringReader(fileContent)));
                Element rootElement = document.getDocumentElement();
                NodeList nodeList = rootElement.getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node currentNode = nodeList.item(i);
                    if (currentNode.getNodeName().indexOf("TEXT")!=-1) {
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

        te3inputText = new ArrayList<>();
        URL te3URL = TestTemporalChunker.class.getClassLoader().getResource(te3ForderName);
        File te3Folder = new File(te3URL.getFile());
        File[] te3Inputs = te3Folder.listFiles();
        for (File file : te3Inputs) {
            String testFile = te3URL.getFile() + "/" + file.getName();
            byte[] encoded = Files.readAllBytes(Paths.get(testFile));
            String fileContent = new String(encoded, StandardCharsets.UTF_8);
            te3inputText.add(fileContent);
        }

    }

    @Test public void testNormalizationWithTrueExtraction() throws Exception {
        AnnotatorService annotator = null;
        try {
            annotator = CuratorFactory.buildCuratorClient();
        } catch (Exception e) {
            fail("Exception while creating AnnotatorService " + e.getStackTrace());
        }
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        // System.out.println(tca.normalizeSinglePhrase("Feb 28", "2013-03-22"));
        ResourceManager nerRm = new TemporalChunkerConfigurator().getDefaultConfig();
        IOUtilities.existsInClasspath(TemporalChunkerAnnotator.class, nerRm.getString("modelDirPath"));

        for (int j = 0; j < te3inputText.size(); j ++) {
            if(!docIDs.get(j).contains("WSJ_20130321_1145")) {
                continue;
            }

            tca.addDocumentCreationTime(DCTs.get(j));
            TextAnnotation ta = null;
            System.out.println(docIDs.get(j));
            try {
                ta = annotator.createBasicTextAnnotation("corpus", "id", testText.get(j));
            } catch (AnnotatorException e) {
                fail("Exception while creating TextAnnotation" + e.getStackTrace());
            }
            try {
                annotator.addView(ta, ViewNames.POS);
            } catch (AnnotatorException e) {
                fail("Exception while adding POS VIEW " + e.getStackTrace());
            }


            try {
                List<TimexChunk> timex = tca.extractTimexFromFile(te3inputText.get(j), ta);
//                for (TimexChunk tc: timex) {
//                    System.out.println(tc.toTIMEXString());
//                }
            } catch (AnnotatorException e) {
                fail("Exception while adding TIMEX3 VIEW " + e.getStackTrace());
            }
            System.out.println("\n");


        }

    }

    @Test
    public void testTemporalChunker() throws Exception {
        AnnotatorService annotator = null;
        try {
            annotator = CuratorFactory.buildCuratorClient();
        } catch (Exception e) {
            fail("Exception while creating AnnotatorService " + e.getStackTrace());
        }
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        // System.out.println(tca.normalizeSinglePhrase("Feb 28", "2013-03-22"));
        ResourceManager nerRm = new TemporalChunkerConfigurator().getDefaultConfig();
        IOUtilities.existsInClasspath(TemporalChunkerAnnotator.class, nerRm.getString("modelDirPath"));

        for (int j = 0; j < testText.size(); j ++) {
            tca.addDocumentCreationTime(DCTs.get(j));
            TextAnnotation ta = null;
            System.out.println(docIDs.get(j));
            try {
                ta = annotator.createBasicTextAnnotation("corpus", "id", testText.get(j));
            } catch (AnnotatorException e) {
                fail("Exception while creating TextAnnotation" + e.getStackTrace());
            }
            try {
                annotator.addView(ta, ViewNames.POS);
            } catch (AnnotatorException e) {
                fail("Exception while adding POS VIEW " + e.getStackTrace());
            }

            try {
                tca.addView(ta);
            } catch (AnnotatorException e) {
                fail("Exception while adding TIMEX3 VIEW " + e.getStackTrace());
            }

            View timexView = ta.getView(ViewNames.TIMEX3);

            String corpId = "IllinoisTimeAnnotator";
            List<Constituent> timeCons = timexView.getConstituents();

            // Keep track of the compressed index of each constituent.
            Span[] compressedSpans = new Span[timeCons.size()];
            int spanStart;

            // Builds a string of the concatenated constituents from a labeled view.
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < timeCons.size(); i++) {
                Constituent c = timeCons.get(i);
                spanStart = builder.length();

                builder.append(c.toString());
                builder.append("; ");

                compressedSpans[i] = new Span(spanStart, builder.length() - 2);
            }
            String compressedText = builder.toString();
            assertNotNull(compressedText);
            String outputFileName = "./ht_chunker_res/"+docIDs.get(j) + ".tml";
            tca.write2Text(outputFileName, docIDs.get(j) ,testText.get(j));
            tca.deleteTimex();
        }
    }

}
