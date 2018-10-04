/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.mascReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.AnnotationReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CorpusReaderConfigurator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;

/**
 * A reader for the entire MASC Open American National Corpus (ANC).
 * Reads all files (recursively) under a corpus directory
 * with Lemma, POS, Sentence, Shallow Parse, NER CoNLL (LOC, ORG, PER),
 * and NER Ontonotes (DATE, LOCATION, ORGANIZATION, PERSON) annotations
 *
 * This reader takes as input the XCES XML format annotation files converted by ANC Tool v3.0.2.
 * To generate the XCES format files:
 * 1. Download the MASC original files from http://www.anc.org/MASC/download/MASC-3.0.0.zip
 * 2. Download ANC Tool v3.0.2 from http://www.anc.org/tools/ANCTool-3.0.2.zip
 * 3. Run ANC Tool with
 *      GrAF resource header file: MASC-3.0.0/resource-header.xml
 *      Input directory: MASC-3.0.0/data
 *      Tab: XML
 *      Token Type: Penn POS tags
 *      Annotations: All
 *      Overlap mode: Nest
 *
 * Note:
 * 1. Some documents in the corpus can contain irregular sentence annotations,
 *    such as nested sentences (as in written/fiction/cable_spool_fort.xml),
 *    and tokens not covered by any sentence (as in written/blog/Acephalous-Cant-believe.xml).
 *    The raw sentence spans are stored into ViewNames.SENTENCE_GOLD,
 *    while the normalized spans which is guaranteed to be a partition of tokens are stored into ViewNames.SENTENCE.
 * 2. MASC annotation contains date, location, organization, and person named entities.
 *    The latter three are stored into ViewNames.NER_CONLL,
 *    while the four are stoed into ViewNames.NER_ONTONOTES.
 *
 * @author Xiaotian Le
 */
public class MascXCESReader extends AnnotationReader<TextAnnotation> {

    private static final String TOKEN_ELEMENT = "tok";  // tokens are stored in "tok" XML elements
    private static final String SENTENCE_ELEMENT = "s";  // sentences are stored in "s" XML elements
    private static final Function<Element, String> TOKEN_VALUE_PROCESSOR;
    private static final List<TokenLabelProcessor> TOKEN_LABEL_PROCESSORS;
    private static final List<SpanLabelProcessor> SPAN_LABEL_PROCESSORS;

    static {
        TOKEN_VALUE_PROCESSOR = elem -> {
            List<Node> parents = getAllParentNodes(elem);

            // SPECIAL FIX
            // some tok elements are nested, and the inner ones will be discarded
            // see spoken/telephone/sw2025-ms98-a-trans.xml
            if (parents.stream().anyMatch(isElementWithTag(TOKEN_ELEMENT))) {
                return null;
            }

            return elem.getTextContent();  // the token itself is the plain text inside the element
        };

        TOKEN_LABEL_PROCESSORS = new ArrayList<>();
        TOKEN_LABEL_PROCESSORS.add(new TokenLabelProcessor(ViewNames.LEMMA, simpleAttrProcessor("base")));  // Lemma Processor: the token label is the "base" attribute of "tok" elements
        TOKEN_LABEL_PROCESSORS.add(new TokenLabelProcessor(ViewNames.POS, simpleAttrProcessor("msd")));  // POS Processor: the token label is the "msd" attribute of "tok" elements

        SPAN_LABEL_PROCESSORS = new ArrayList<>();
        // SENTENCE contains normalized sentences annotation, which is a partition of tokens for creating TextAnnotation
        // NOTE: some tokens are not in a sentence, and they will be handled by OracleTokenizer
        // see written/blog/Acephalous-Cant-believe.xml
        // NOTE: some sentences are nested, and the inner ones will be discarded here
        // see written/fiction/cable_spool_fort.xml
        SPAN_LABEL_PROCESSORS.add(new SpanLabelProcessor(SENTENCE_ELEMENT, ViewNames.SENTENCE, elem ->
                getAllParentNodes(elem).stream().anyMatch(isElementWithTag(SENTENCE_ELEMENT)) ? null : ViewNames.SENTENCE));
        // SENTENCE_GOLD contains raw sentences annotation, which might overlap, skip tokens, etc
        SPAN_LABEL_PROCESSORS.add(new SpanLabelProcessor(SENTENCE_ELEMENT, ViewNames.SENTENCE_GOLD, elem -> ViewNames.SENTENCE));  // Sentence Processor: the span label is simply ViewNames.SENTENCE
        SPAN_LABEL_PROCESSORS.add(new SpanLabelProcessor("nchunk", ViewNames.SHALLOW_PARSE, elem -> "NP"));  // Noun Chunk Processor
        SPAN_LABEL_PROCESSORS.add(new SpanLabelProcessor("vchunk", ViewNames.SHALLOW_PARSE, elem -> "VP"));  // Verb Chunk Processor
        SPAN_LABEL_PROCESSORS.add(new SpanLabelProcessor("location", ViewNames.NER_CONLL, elem -> "LOC"));  // NER CoNLL Location Processor
        SPAN_LABEL_PROCESSORS.add(new SpanLabelProcessor("org", ViewNames.NER_CONLL, elem -> "ORG"));  // NER CoNLL Organization Processor
        SPAN_LABEL_PROCESSORS.add(new SpanLabelProcessor("person", ViewNames.NER_CONLL, elem -> "PER"));  // NER CoNLL Person Processor
        SPAN_LABEL_PROCESSORS.add(new SpanLabelProcessor("date", ViewNames.NER_ONTONOTES, elem -> "DATE"));  // NER Ontonotes Date Processor
        SPAN_LABEL_PROCESSORS.add(new SpanLabelProcessor("location", ViewNames.NER_ONTONOTES, elem -> "LOCATION"));  // NER Ontonotes Location Processor
        SPAN_LABEL_PROCESSORS.add(new SpanLabelProcessor("org", ViewNames.NER_ONTONOTES, elem -> "ORGANIZATION"));  // NER Ontonotes Organization Processor
        SPAN_LABEL_PROCESSORS.add(new SpanLabelProcessor("person", ViewNames.NER_ONTONOTES, elem -> "PERSON"));  // NER Ontonotes Person Processor
    }

    private static Logger logger = LoggerFactory.getLogger(MascXCESReader.class);
    private List<TextAnnotation> textAnnotations = new ArrayList<>();
    private List<String> failureLogs = new ArrayList<>();

    /**
     * This expects a directory that contains XCES format files.
     * @param corpusName The name of the corpus, e.g. MASC-3.0.0
     * @param corpusDirectory The folder of the source files
     * @param fileExtension Should be ".xml" for the XCES XML source files
     */
    public MascXCESReader(String corpusName, String corpusDirectory, String fileExtension)
            throws ParserConfigurationException {
        super(CorpusReaderConfigurator.buildResourceManager(corpusName, corpusDirectory, corpusDirectory, fileExtension, fileExtension));

        this.currentAnnotationId = 0;

        String[] sourceFiles;
        try {
            sourceFiles = IOUtils.lsFilesRecursive(corpusDirectory, file ->
                    file.isDirectory() || file.getAbsolutePath().endsWith(fileExtension));
        } catch (IOException e) {
            logger.error("Error listing directory.");
            logger.error(e.getMessage());
            return;
        }

        Path corpusAbsolutePath = Paths.get(corpusDirectory).toAbsolutePath();
        Arrays.sort(sourceFiles);
        for (String file : sourceFiles) {
            String error = null;

            try {
                textAnnotations.add(loadAnnotationFile(
                        corpusName,
                        file,
                        corpusAbsolutePath.relativize(Paths.get(file)).toString()
                ));
                logger.info("Created TextAnnotation from [" + file +"].");
            }
            catch (ParserConfigurationException e) {
                throw e;
            }
            catch (IOException e) {
                error = "[" + file + "] Error reading file: " + e.getMessage();
            }
            catch (SAXParseException e) {
                error = "[" + file + ":" + e.getLineNumber() + ":" + e.getColumnNumber() + "] Error parsing XML file: " + e.getMessage();
            }
            catch (SAXException e) {
                error = "[" + file + "] Error parsing XML file: " + e.getMessage();
            }
            catch (Exception e) {
                error = "[" + file + "] Error creating TextAnnotation: " + e.getMessage();
                e.printStackTrace();
            }

            if (error != null) {
                logger.error(error);
                failureLogs.add(error);
            }
        }
    }

    private static TextAnnotation loadAnnotationFile(String corpusName, String filename, String textId) throws Exception {
        final String TOKEN_IDENTIFIER_KEY = "id";

        // Parse the XML file into DOM
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new File(filename));
        doc.getDocumentElement().normalize();  // Merge adjacent texts with no tags between

        List<String> tokens = new ArrayList<>();
        Map<String, List<Pair<Integer, String>>> tokenLabels = new HashMap<>();
        for (TokenLabelProcessor processor : TOKEN_LABEL_PROCESSORS) {
            tokenLabels.putIfAbsent(processor.getViewName(), new ArrayList<>());
        }

        int currentTokenId = 0;
        NodeList tokenNodes = doc.getElementsByTagName(TOKEN_ELEMENT);
        for (int i = 0; i < tokenNodes.getLength(); ++i) {
            Element tokenNode = (Element)tokenNodes.item(i);

            String tokenLabel = TOKEN_VALUE_PROCESSOR.apply(tokenNode);
            if (tokenLabel == null) {
                continue;
            }
            tokens.add(tokenLabel);

            for (TokenLabelProcessor processor : TOKEN_LABEL_PROCESSORS) {
                String label = processor.getProcessor().apply(tokenNode);
                if (label != null) {
                    tokenLabels.get(processor.getViewName()).add(new Pair<>(currentTokenId, label));
                }
            }

            tokenNode.setUserData(TOKEN_IDENTIFIER_KEY, currentTokenId, null);
            currentTokenId += 1;
        }

        Map<String, List<Pair<IntPair, String>>> spanLabels = new HashMap<>();

        for (SpanLabelProcessor processor : SPAN_LABEL_PROCESSORS) {
            spanLabels.putIfAbsent(processor.getViewName(), new ArrayList<>());

            NodeList spanNodes = doc.getElementsByTagName(processor.getElementName());
            for (int i = 0; i < spanNodes.getLength(); ++i) {
                Element spanNode = (Element)spanNodes.item(i);

                String label = processor.getProcessor().apply(spanNode);
                if (label != null) {
                    // A span label covers all the (direct or indirect) child "tok" elements
                    NodeList coveredTokenNodes = spanNode.getElementsByTagName(TOKEN_ELEMENT);

                    List<Integer> coveredTokenIds = new ArrayList<>();
                    for (int j = 0; j < coveredTokenNodes.getLength(); ++j) {
                        Object tokenId = coveredTokenNodes.item(j).getUserData(TOKEN_IDENTIFIER_KEY);
                        if (tokenId != null) {
                            coveredTokenIds.add((int)tokenId);
                        }
                    }

                    // and the span is from the minimum child id to the maximum child id + 1
                    if (coveredTokenIds.size() > 0) {
                        int beginToken = coveredTokenIds.stream().reduce(Integer::min).orElseThrow(NoSuchElementException::new);
                        int endToken = coveredTokenIds.stream().reduce(Integer::max).orElseThrow(NoSuchElementException::new);
                        spanLabels.get(processor.getViewName()).add(new Pair<>(new IntPair(beginToken, endToken + 1), label));
                    }
                }
            }
        }

        String rawText = doc.getDocumentElement().getTextContent();

        List<Pair<IntPair, String>> sentencesWithLabel = spanLabels.get(ViewNames.SENTENCE);
        List<IntPair> sentences = sentencesWithLabel.stream()
                .map(Pair::getFirst)
                .collect(Collectors.toList());
        spanLabels.remove(ViewNames.SENTENCE);

        OracleTokenizer tokenizer = new OracleTokenizer();
        Tokenizer.Tokenization tokenization = tokenizer.tokenize(rawText, tokens, sentences);
        TextAnnotation ta = new TextAnnotation(corpusName, textId, rawText,
                tokenization.getCharacterOffsets(), tokenization.getTokens(), tokenization.getSentenceEndTokenIndexes());

        for (Map.Entry<String, List<Pair<Integer, String>>> entry : tokenLabels.entrySet()) {
            createTokenLabelView(entry.getValue().stream(), ta, entry.getKey());
        }

        for (Map.Entry<String, List<Pair<IntPair, String>>> entry : spanLabels.entrySet()) {
            createSpanLabelView(entry.getValue().stream(), ta, entry.getKey(), true);  // span labels in MASC dataset might overlap
        }

        return ta;
    }

    /**
     * Helper for create a processor function that take a particular attribute of the element as the label
     */
    private static Function<Element, String> simpleAttrProcessor(String attr) {
        return elem -> Optional
                .ofNullable(elem.getAttributes().getNamedItem(attr))  // the label is the `attr` attribute of the element
                .map(Node::getNodeValue)
                .orElse(null);  // or don't create the label if the attribute doesn't exist
    }

    private static List<Node> getAllParentNodes(Node node) {
        List<Node> parents = new ArrayList<>();
        for (Node parent = node.getParentNode(); parent.getNodeType() != Node.DOCUMENT_NODE; parent = parent.getParentNode()) {
            parents.add(parent);
        }
        return parents;
    }

    private static Predicate<Node> isElementWithTag(String tag) {
        return parent -> parent.getNodeType() == Node.ELEMENT_NODE && ((Element)parent).getTagName().equals(tag);
    }

    /**
     * Helper for create a TokenLabelView from a stream of token labels
     */
    private static int createTokenLabelView(
            Stream<Pair<Integer, String>> tokenLabels,
            TextAnnotation ta,
            String viewName) {
        TokenLabelView view = new TokenLabelView(viewName, "GoldStandard", ta, 1.0);
        tokenLabels.forEach(label -> view.addTokenLabel(label.getFirst(), label.getSecond(), 1.0));
        ta.addView(viewName, view);
        return view.count();
    }

    /**
     * Helper for create a SpanLabelView from a stream of span labels
     */
    private static int createSpanLabelView(
            Stream<Pair<IntPair, String>> spans,
            TextAnnotation ta,
            String viewName,
            boolean allowOverlapping) {
        SpanLabelView view = new SpanLabelView(viewName, "GoldStandard", ta, 1.0, allowOverlapping);
        spans.forEach(span -> view.addSpanLabel(
                span.getFirst().getFirst(), span.getFirst().getSecond(), span.getSecond(), 1.0));
        ta.addView(viewName, view);
        return view.count();
    }

    @Override
    protected void initializeReader() {}

    @Override
    public boolean hasNext() {
        return textAnnotations.size() > currentAnnotationId;
    }

    @Override
    public TextAnnotation next() {
        return textAnnotations.get(currentAnnotationId++);
    }

    @Override
    public String generateReport() {
        StringBuilder builder = new StringBuilder();
        builder
                .append("Number of TextAnnotations generated:\t")
                .append(textAnnotations.size())
                .append(System.lineSeparator());
        builder
                .append("Number of files unable to be processed:\t")
                .append(failureLogs.size())
                .append(System.lineSeparator());
        for (String log : failureLogs) {
            builder
                    .append(log)
                    .append(System.lineSeparator());
        }
        return builder.toString();
    }

    public static void main(String[] args) throws Exception {
        String corpusDirectory = "/shared/corpora/corporaWeb/written/eng/MASC-3.0.0/xces";
        String outputDirectory = "/shared/corpora/corporaWeb/written/eng/MASC-3.0.0/json";
        if (args.length >= 2) {
            corpusDirectory = args[0];
            outputDirectory = args[1];
        }

        MascXCESReader reader = new MascXCESReader("MASC-3.0.0", corpusDirectory, ".xml");
        for (TextAnnotation ta : reader) {
            String outputFile = Paths.get(outputDirectory, ta.getId() + ".json").toAbsolutePath().toString();
            new File(outputFile).getParentFile().mkdirs();
            SerializationHelper.serializeTextAnnotationToFile(ta, outputFile, true, true);
            logger.info("Serialized TextAnnotation to [" + outputFile + "]");
        }

        System.out.print(reader.generateReport());
    }
}
