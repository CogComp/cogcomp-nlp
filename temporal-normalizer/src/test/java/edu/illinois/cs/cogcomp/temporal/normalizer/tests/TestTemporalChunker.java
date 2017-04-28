package edu.illinois.cs.cogcomp.temporal.normalizer.tests;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.pipeline.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;
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
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import static junit.framework.TestCase.assertFalse;
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
    //private String folderName = "uw_TimeBank";
    //private String folderName = "uw_AQUAINT";
    //private String folderName = "TimeBank";
    //private String folderName = "AQUAINT";
    private String folderName = "te3-platinum";
    //private String folderName = "ht2.1_res";
    //private String folderName = "AQUAINT_ht";
    //private String folderName = "TimeBank_ht";

    private static String fullFolderName;
    private List<String> docIDs;
    private List<String> te3inputText;

    @Before
    public void setUp() throws IOException, ParserConfigurationException, SAXException {
        testText = new ArrayList<>();
        DCTs = new ArrayList<>();
        docIDs = new ArrayList<>();
        te3inputText = new ArrayList<>();

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

//        te3inputText = new ArrayList<>();
//        URL te3URL = TestTemporalChunker.class.getClassLoader().getResource(te3ForderName);
//        File te3Folder = new File(te3URL.getFile());
//        File[] te3Inputs = te3Folder.listFiles();
//        for (File file : te3Inputs) {
//            String testFile = te3URL.getFile() + "/" + file.getName();
//            byte[] encoded = Files.readAllBytes(Paths.get(testFile));
//            String fileContent = new String(encoded, StandardCharsets.UTF_8);
//            te3inputText.add(fileContent);
//        }

    }

    private static final String textFile = "text/AP_20130322";

    @Test
    public void testPosPipeline() {
        String input = null;
        try {
            input = LineIO.slurp(textFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertFalse(input.length() == 0);

        AnnotatorService as = null;
        try {
            as = PipelineFactory.buildPipeline(ViewNames.POS);
        } catch (IOException | AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        try {
            as.createAnnotatedTextAnnotation("test", "test", input);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test public void testNormalizationWithTrueExtraction() throws Exception {
//        Properties props = new Properties();
//        props.setProperty( PipelineConfigurator.USE_POS.key, Configurator.TRUE );
        AnnotatorService pipeline = PipelineFactory.buildPipeline(ViewNames.POS);

        //AnnotatorService annotator = null;
//        try {
//            annotator = CuratorFactory.buildCuratorClient();
//        } catch (Exception e) {
//            fail("Exception while creating AnnotatorService " + e.getStackTrace());
//        }
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        // System.out.println(tca.normalizeSinglePhrase("Feb 28", "2013-03-22"));
        ResourceManager nerRm = new TemporalChunkerConfigurator().getDefaultConfig();
        IOUtilities.existsInClasspath(TemporalChunkerAnnotator.class, nerRm.getString("modelDirPath"));

        java.util.logging.Logger.getLogger("HeidelTimeStandalone").setLevel(Level.OFF);

        long preprocessTime = System.currentTimeMillis();
        List <TextAnnotation> taList = new ArrayList<>();
        for (int j = 0; j < te3inputText.size(); j ++) {
            String filename = "./text/"+docIDs.get(j);
            PrintStream ps = new PrintStream(filename);
            ps.print(testText.get(j));
            ps.close();
            TextAnnotation ta = pipeline.createAnnotatedTextAnnotation( "corpus", "id",  LineIO.slurp(filename));
//            try {
//                //ta = annotator.createBasicTextAnnotation("corpus", "id", testText.get(j));
//                ta = pipeline.createAnnotatedTextAnnotation( "corpus", "id", testText.get(j) );
//            } catch (AnnotatorException e) {
//                fail("Exception while creating TextAnnotation" + e.getStackTrace());
//            }
//            try {
//                annotator.addView(ta, ViewNames.POS);
//            } catch (AnnotatorException e) {
//                fail("Exception while adding POS VIEW " + e.getStackTrace());
//            }
            taList.add(ta);
        }

        long startTime = System.currentTimeMillis();

        int numTimex = 0;
        for (int j = 0; j < te3inputText.size(); j ++) {
//            if(!docIDs.get(j).contains("CNN_20130322_1003")) {
//                continue;
//            }

            TextAnnotation ta = taList.get(j);
            tca.addDocumentCreationTime(DCTs.get(j));

            System.out.println(docIDs.get(j));


            try {
                List<TimexChunk> timex = tca.extractTimexFromFile(te3inputText.get(j), testText.get(j), ta);
//                for (TimexChunk tc:timex)
//                    System.out.println(tc.toTIMEXString());
                tca.setTimex(timex);
                String outputFileName = "TE3_goldext_illininorm/" +docIDs.get(j) + ".tml";
                //String outputFileName = "AQ_goldext_htnorm/" +docIDs.get(j) + ".tml";

                //tca.write2Text(outputFileName, docIDs.get(j) ,testText.get(j));
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
        java.util.logging.Logger.getLogger("HeidelTimeStandalone").setLevel(Level.OFF);

        List <TextAnnotation>taList = new ArrayList<>();
        long preprocessTime = System.currentTimeMillis();
        for (int j = 0; j < testText.size(); j ++) {
            TextAnnotation ta = null;
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
            taList.add(ta);
        }
        System.out.println("Start");
        long startTime = System.currentTimeMillis();
        for (int j = 0; j < testText.size(); j ++) {
//            if (!docIDs.get(j).contains("XIE19981203.0008")){
//                continue;
//            }
            tca.addDocumentCreationTime(DCTs.get(j));
            TextAnnotation ta = taList.get(j);
            //System.out.println(docIDs.get(j));

            try {
                tca.addView(ta);
            } catch (AnnotatorException e) {
                fail("Exception while adding TIMEX3 VIEW " + e.getStackTrace());
            }

//            View timexView = ta.getView(ViewNames.TIMEX3);

//            String corpId = "IllinoisTimeAnnotator";
//            List<Constituent> timeCons = timexView.getConstituents();

//            for(Constituent c: timeCons) {
//                System.out.println(c);
//            }
            // Keep track of the compressed index of each constituent.
/*            Span[] compressedSpans = new Span[timeCons.size()];
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
*/
            String outputFileName = "./TE3_chunker_illininorm/"+docIDs.get(j) + ".tml";
//            for(TimexChunk tc: tca.getTimex()) {
//                System.out.println(tc.toTIMEXString());
//            }
//            System.out.println("\n");
            tca.write2Text(outputFileName, docIDs.get(j) ,testText.get(j));
            tca.deleteTimex();
        }
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Process time: " + totalTime);
        System.out.println("Preprocess + process time: " + (endTime-preprocessTime) );
    }

}
