/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.tests;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.nlp.utilities.StringCleanup;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by zhilifeng on 11/1/16.
 * This is the test class for TemporalChunkerAnnotator
 * In addition, users can follow the test to set up annotators.
 * Also we provide two evaluations: 1) normalizing with gold chunks, and
 * 2) normalizing with our extraction (using chunker)
 */
public class TestTemporalChunker {
    private TemporalChunkerAnnotator tca;
    private static Logger logger = LoggerFactory.getLogger(TestTemporalChunker.class);
    private List<String> DCTs;
    private List<String> testText;
    private String folderName = "te3-platinum";

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

    @Test
    public void testTemporalChunkerWithPlainText() throws Exception{
        Properties props = new Properties();
        props.setProperty( PipelineConfigurator.USE_POS.key, Configurator.TRUE );
        // TODO: Configurtaor.TRUE/FALSE is reversed, in the future when this is fixed,
        // TODO: change it back
        props.setProperty( PipelineConfigurator.USE_SENTENCE_PIPELINE.key, Configurator.TRUE );
        AnnotatorService pipeline = null;
        pipeline = PipelineFactory.buildPipeline(new ResourceManager(props));
        String text = "The flu season is winding down, and it has killed 105 children so far - about the average toll.\n" +
                "\n" +
                "The season started about a month earlier than usual, sparking concerns it might turn into the worst in " +
                "a decade. It ended up being very hard on the elderly, but was moderately severe overall, according to " +
                "the Centers for Disease Control and Prevention.\n" +
                "\n" +
                "Six of the pediatric deaths were reported in the last week, and it's possible there will be more, said " +
                "the CDC's Dr. Michael Jhung said Friday.\n" +
                "\n" +
                "Roughly 100 children die in an average flu season. One exception was the swine flu pandemic of " +
                "2009-2010, when 348 children died.\n" +
                "\n" +
                "The CDC recommends that all children ages 6 months and older be vaccinated against flu each season, " +
                "though only about half get a flu shot or nasal spray.\n" +
                "\n" +
                "All but four of the children who died were old enough to be vaccinated, but 90 percent of them did " +
                "not get vaccinated, CDC officials said.\n" +
                "\n" +
                "This year's vaccine was considered effective in children, though it didn't work very well in older " +
                "people. And the dominant flu strain early in the season was one that tends to " +
                "cause more severe illness.\n" +
                "\n" +
                "The government only does a national flu death count for children. But it does track hospitalization " +
                "rates for people 65 and older, and those statistics have been grim.\n" +
                "\n" +
                "In that group, 177 out of every 100,000 were hospitalized with flu-related illness in the past " +
                "several months. That's more than 2 1/2 times higher than any other recent season.\n" +
                "\n" +
                "This flu season started in early December, a month earlier than usual, and peaked by the end " +
                "of year. Since then, flu reports have been dropping off throughout the country.\n" +
                "\n" +
                "\"We appear to be getting close to the end of flu season,\" Jhung said.";
        TextAnnotation ta = pipeline.createAnnotatedTextAnnotation("corpus", "id", text);
        tca.addView(ta);
        View temporalViews = ta.getView(ViewNames.TIMEX3);
        List<Constituent> constituents = temporalViews.getConstituents();
        assertEquals("<TIMEX3 type=\"DURATION\" value=\"P1M\">", constituents.get(0).getLabel());
    }
}
