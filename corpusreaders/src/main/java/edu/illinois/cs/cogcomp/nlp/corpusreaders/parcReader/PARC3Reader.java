package edu.illinois.cs.cogcomp.nlp.corpusreaders.parcReader;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.XMLUtils;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.AbstractIncrementalCorpusReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A Reader for Penn Attribution Relations Copurs 3.0 (PARC 3.0)
 * 
 * Documents in corpus are stored as a syntactic tree in XML. Annotations are stored as child nodes
 * to "Word" nodes
 * 
 * A toy example document below:
 * 
 * <root>
 * 
 * <SENTENCE gorn="0">
 * 
 * <NP-HLN gorn="0">
 * 
 * <WORD ByteCount="9,16" gorn="0,0" lemma="two-way" pos="NNP" sentenceWord="0" text="Two-Way"
 * word="0">
 * 
 * <attribution id="wsj_2401_PDTB_annotation_level.xml_set_0">
 * 
 * <attributionRole roleValue="content"/>
 * 
 * </attribution>
 * 
 * </WORD>
 * 
 * <WORD ByteCount="17,23" gorn="0,1" lemma="street" pos="NNP" sentenceWord="1" text="Street"
 * word="1"/>
 * 
 * </NP-HLN>
 * 
 * </SENTENCE>
 * 
 * </root>
 * 
 * Given a input directory, this reader looks for files with .xml extension in all nested
 * directories.
 * 
 * Sample Directory Structure -
 * 
 * \train
 * 
 * - \00
 * 
 * - wsj-0001.xml
 * 
 * - ...
 * 
 * The current implementation only takes tokenization, sentence split, POS, Lemma and Attribution
 * Relation
 * 
 * from original annotation. The Attribution Relation annotations for each document are stored in an
 * instance of
 * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView
 * PredicateArgumentView}.
 * 
 * @author Sihao Chen
 */
public class PARC3Reader extends AbstractIncrementalCorpusReader<TextAnnotation> {

    private static Logger logger = LoggerFactory.getLogger(PARC3Reader.class);

    // Constants
    // XML tag names
    private static final String NODE_SENTENCE = "SENTENCE";
    private static final String NODE_WORD = "WORD";
    private static final String NODE_ATTRIBUTION = "attribution";
    private static final String NODE_ATTRIBUTION_ROLE = "attributionRole";

    // XML node attribute names
    private static final String ATTR_WORD_TEXT = "text";
    private static final String ATTR_POS = "pos";
    private static final String ATTR_LEM = "lemma";
    private static final String ATTR_BYTE_COUNT = "ByteCount";
    private static final String ATTR_RELATION_ID = "id";
    private static final String ATTR_ROLE_VALUE = "roleValue";

    // Name of roles of attribution spans in the original xml markup
    private static final String ROLE_SOURCE = "source";
    private static final String ROLE_CUE = "cue";
    private static final String ROLE_CONTENT = "content";

    // Labels
    public static final String LABEL_SOURCE = "SOURCE";
    public static final String LABEL_CUE = "CUE";
    public static final String LABEL_CONTENT = "CONTENT";

    // Relations
    public static final String REL_SOURCE = "SOURCE";
    public static final String REL_CONTENT = "CONTENT";

    // Names of constituent attributes, used to store additional information for each attribution
    // relation
    public static final String CON_ATTR_REL_ID = "relationID";

    // Others
    private static final String EXT_XML = ".xml";
    private static final String VIEW_NAME = ViewNames.ATTRIBUTION_RELATION;

    // Config Variables
    private boolean bPopulatePOS;
    private boolean bPopulateLemma;

    // List of AR relation ids and document name pairs, where dangling AR Constituents are found (aka AR without cues)
    private Set<Pair<String, String>> warnList;

    /**
     * Creates a PARC reader that reads all documents in a given directory with default settings.
     * (keep tokenization and sentence split, discard POS and Lemma from original annotation)
     *
     * @param parcDir Directory to PARC corpus, note that all documents with extension .xml in all
     *        subdirectories will be read
     */
    public PARC3Reader(String parcDir) throws Exception {
        super(PARC3ReaderConfigurator.getDefaultConfigWithSourceDir(parcDir));
    }

    /**
     * Creates a PARC reader with user specified settings
     *
     * @param rm see
     *        {@link edu.illinois.cs.cogcomp.nlp.corpusreaders.parcReader.PARC3ReaderConfigurator}
     *        for config details
     */
    public PARC3Reader(ResourceManager rm) throws Exception {
        super(rm);
    }

    @Override
    protected void initializeReader() {
        super.initializeReader();
        bPopulatePOS = resourceManager.getBoolean(PARC3ReaderConfigurator.POPULATE_POS.key);
        bPopulateLemma = resourceManager.getBoolean(PARC3ReaderConfigurator.POPULATE_LEMMA.key);
        warnList = new HashSet<>();
    }

    /**
     * Recursively reads all documents with .xml extension in the source directory
     */
    @Override
    public List<List<Path>> getFileListing() throws IOException {
        List<List<Path>> fileList = new ArrayList<>();

        String[] xmlDocs = IOUtils.lsFilesRecursive(sourceDirectory, new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(EXT_XML) || f.isDirectory();
            }
        });

        for (String doc : xmlDocs) {
            List<Path> docFile = new ArrayList<>();
            Path docPath = Paths.get(doc);
            docFile.add(docPath);
            fileList.add(docFile);
        }

        return fileList;
    }

    /**
     * Parse a document into an {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation
     * TextAnnotation}. By default TOKEN and SENTENCE view will be  populated. Other gold views will only be
     * populated if set in configurations
     *
     * @param list a list of containing one path to a xml document
     * @return a list containing one TextAnnotation, corresponding to one source text file plus
     *         annotations
     * @throws Exception if files can't be found, or if parser fails to read annotation format
     */
    @Override
    public List<TextAnnotation> getAnnotationsFromFile(List<Path> list) throws Exception {
        List<TextAnnotation> result = new ArrayList<>();

        for (Path p: list) {

            String fileStem = IOUtils.getFileStem(p.toFile().getName());
            logger.info("Processing: {}", fileStem);

            // Tokens, Sentences, POS and Lemma
            List<String> tokens = new ArrayList<>();
            List<IntPair> charOffsets = new ArrayList<>();
            List<Integer> sentTokOffset = new ArrayList<>();
            List<String> POStags = new ArrayList<>();
            List<String> lemmas = new ArrayList<>();

            // Attribution Relations - each entry in the map corresponds to one set of attribution relation
            Map<String, AttributionRelation> attrRelations = new HashMap<>();

            // Text
            StringBuilder text = new StringBuilder();
            int lastWordEndByteOffset = 0;
            int tokenIdx = 0;

            Document doc = XMLUtils.getXMLDOM(p.toString());
            doc.getDocumentElement().normalize(); // Optional, we don't actually need this, as of now.

            NodeList sentences = doc.getElementsByTagName(NODE_SENTENCE);

            for (int sid = 0; sid < sentences.getLength(); sid++) {
                Element sent = (Element) sentences.item(sid);
                NodeList words = sent.getElementsByTagName(NODE_WORD);

                for (int wid = 0; wid < words.getLength(); wid++) {
                    Element word = (Element) words.item(wid);

                    NodeList attrRels = word.getElementsByTagName(NODE_ATTRIBUTION);

                    for (int aid = 0; aid < attrRels.getLength(); aid++) {
                        Element attrRel = (Element) attrRels.item(aid);
                        String relationId = attrRel.getAttribute(ATTR_RELATION_ID);

                        // Get attribution role(s) for current token
                        NodeList attrRoles = attrRel.getElementsByTagName(NODE_ATTRIBUTION_ROLE);
                        for (int arid = 0; arid < attrRoles.getLength(); arid++) {
                            Element attrRole = (Element) attrRoles.item(arid);
                            String role = attrRole.getAttribute(ATTR_ROLE_VALUE);

                            updateAttributionRelation(attrRelations, relationId, role, tokenIdx);
                        }
                    }

                    String wordText = word.getAttribute(ATTR_WORD_TEXT);
                    String pos = word.getAttribute(ATTR_POS);
                    String lem = word.getAttribute(ATTR_LEM);
                    String[] byteOffsetStr = word.getAttribute(ATTR_BYTE_COUNT).split(",");
                    IntPair oracleByteOffset = new IntPair(
                            Integer.parseInt(byteOffsetStr[0]), Integer.parseInt(byteOffsetStr[1]));    // This is byte offset according to PARC, which is not accurate

                    // fill whitespace and update current word to text
                    int numWhiteSpace = oracleByteOffset.getFirst() - lastWordEndByteOffset;
                    text.append(String.join("", Collections.nCopies(numWhiteSpace, " ")));
                    int startCharOffset = text.length();
                    text.append(wordText);
                    int endCharOffset = text.length();
                    lastWordEndByteOffset = oracleByteOffset.getSecond();

                    // Update token and token offset
                    tokens.add(wordText);
                    charOffsets.add(new IntPair(startCharOffset, endCharOffset));
                    tokenIdx++;

                    // Update sentence token offset
                    if (wid == words.getLength() - 1)
                        sentTokOffset.add(tokenIdx);

                    // Update POS tags
                    POStags.add(pos);
                    lemmas.add(lem);
                }
            }

            TextAnnotation ta = new TextAnnotation(
                    super.corpusName,
                    fileStem,
                    text.toString(),
                    charOffsets.toArray(new IntPair[0]),
                    tokens.toArray(new String[0]),
                    sentTokOffset.stream().mapToInt(i->i).toArray());

            if (bPopulatePOS)
                populatePOS(ta, POStags);
            if (bPopulateLemma)
                populateLemma(ta, lemmas);

            populateAttribution(ta, attrRelations);
            result.add(ta);
        }

        return result;
    }

    @Override
    public String generateReport() {
        String processedNum = super.generateReport();
        StringBuilder report = new StringBuilder();
        report.append(processedNum)
                .append("Dangling constituents found in ")
                .append(warnList.size())
                .append(" ARs (aka AR without a cue).")
                .append(System.lineSeparator());

        for (Pair<String, String> reldocid: warnList) {
            report.append(reldocid.getFirst())
                    .append(" in document ")
                    .append(reldocid.getSecond())
                    .append(System.lineSeparator());
        }

        return report.toString();
    }

    private void updateAttributionRelation(Map<String, AttributionRelation> relation,
                                           String relationId, String role, int tokenId) {
        if (!relation.containsKey(relationId)) {
            relation.put(relationId, new AttributionRelation(relationId));
        }

        AttributionRelation sourceCueContent = relation.get(relationId);

        switch (role) {
            case ROLE_SOURCE:
                sourceCueContent.updateSourceSpan(tokenId);
                break;
            case ROLE_CUE:
                sourceCueContent.updateCueSpan(tokenId);
                break;
            case ROLE_CONTENT:
                sourceCueContent.updateContentSpan(tokenId);
                break;
        }
    }

    private void populatePOS(TextAnnotation ta, List<String> posTags) {
        populateTokenLabelView(ta, posTags, ViewNames.POS);
    }

    private void populateLemma(TextAnnotation ta, List<String> lemma) {
        populateTokenLabelView(ta, lemma, ViewNames.LEMMA);
    }

    private void populateTokenLabelView(TextAnnotation ta, List<String> tags, String viewName) {
        TokenLabelView v = new TokenLabelView(viewName, ta);
        for (int tkid = 0; tkid < tags.size(); tkid++)
            v.addTokenLabel(tkid, tags.get(tkid), 1.0D);
        ta.addView(viewName, v);
    }

    private void populateAttribution(TextAnnotation ta, Map<String, AttributionRelation> attrRelations) {
        PredicateArgumentView attrRelationView = new PredicateArgumentView(VIEW_NAME, "Gold-PARC3", ta , 1.0D);

        for (Map.Entry<String, AttributionRelation> ent : attrRelations.entrySet()) {
            String relationId = ent.getKey();
            AttributionRelation rel = ent.getValue();
            List<IntPair> sourceSpans = rel.getSourceSpans();
            List<IntPair> cueSpans = rel.getCueSpans();
            List<IntPair> contentSpans = rel.getContentSpans();

            List<String> relations = new ArrayList<>();

            // Process cue first, if there are no cue for this attribution relation, skip (should never happen)
            if (cueSpans.isEmpty()) {
                warnList.add(new Pair<>(rel.groupId, ta.getId()));
                continue;
            }


            IntPair cue = cueSpans.get(0); // There is one and only one cue in each AR
            Constituent cueC = new Constituent(LABEL_CUE, attrRelationView.getViewName(),
                        ta, cue.getFirst(), cue.getSecond());
            cueC.addAttribute(CON_ATTR_REL_ID, relationId);

            List<Constituent> contentCs = contentSpans.stream()
                    .map(c -> {
                        Constituent con = new Constituent(LABEL_CONTENT, attrRelationView.getViewName(), ta, c.getFirst(), c.getSecond());
                        con.addAttribute(CON_ATTR_REL_ID, relationId);
                        return con;
                    })
                    .collect(Collectors.toList());

            List<Constituent> sourceCs = sourceSpans.stream()
                    .map(c -> {
                        Constituent con = new Constituent(LABEL_SOURCE, attrRelationView.getViewName(),
                                ta, c.getFirst(), c.getSecond());
                        con.addAttribute(CON_ATTR_REL_ID, relationId);
                        return con;
                    })
                    .collect(Collectors.toList());

            relations.addAll(new ArrayList<>(Collections.nCopies(contentCs.size(), REL_CONTENT)));
            relations.addAll(new ArrayList<>(Collections.nCopies(sourceCs.size(), REL_SOURCE)));

            List<Constituent> merged = Stream.of(contentCs, sourceCs)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

//            logger.info("Num args {} Num rel {}", merged.size(), relations.size());
            double[] score = new double[relations.size()];
            Arrays.fill(score, 1.0);
            attrRelationView.addPredicateArguments(cueC, merged, relations.stream().toArray(String[]::new), score);
        }

        ta.addView(attrRelationView.getViewName(), attrRelationView);
    }

    private class AttributionRelation {

        public String groupId;

        private List<IntPair> source, cue, content;

        public AttributionRelation(String groupId) {
            this.groupId = groupId;
            source = new ArrayList<>();
            cue = new ArrayList<>();
            content = new ArrayList<>();
        }

        public void updateSourceSpan(int tkid) {
            updateSpan(source, tkid);
        }

        public void updateCueSpan(int tkid) {
            updateSpan(cue, tkid);
        }

        public void updateContentSpan(int tkid) {
            updateSpan(content, tkid);
        }

        private void updateSpan(List<IntPair> spans, int tokenId) {
            boolean updated = false;
            for (IntPair span : spans) {
                int end = span.getSecond();
                if (end == tokenId) {
                    span.setSecond(end + 1);
                    updated = true;
                    break; // Maybe this is wrong, but probably not
                }
            }
            if (!updated)
                spans.add(new IntPair(tokenId, tokenId + 1));
        }

        public List<IntPair> getSourceSpans() {
            return this.source;
        }

        public List<IntPair> getCueSpans() {
            return this.cue;
        }

        public List<IntPair> getContentSpans() {
            return this.content;
        }
    }

    /**
     * Read sections of corpus into TextAnnotations, serialize TextAnnotations into json, and save
     * the json to output directory. Specify PARC train/dev/test dir, and output directory in args.
     * 
     * @param args PARC source directory and output directory
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java" + PARC3Reader.class.getCanonicalName()
                    + " [source-parc-dir] [out-dir]");
            System.exit(1);
        }

        String inDir = args[0];
        String outDir = args[1];

        if (!IOUtils.isDirectory(inDir)) {
            System.err.println(inDir + " is not a directory.");
            System.exit(1);
        }

        IOUtils.mkdir(outDir);

        PARC3Reader reader = null;

        try {
            reader = new PARC3Reader(inDir);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        while (reader.hasNext()) {
            TextAnnotation ta = reader.next();
            String jsonTa = SerializationHelper.serializeToJson(ta, true);
            String outFile = outDir + File.separator + ta.getId() + ".json";
            try {
                logger.trace("Writing file out to '{}'...", outFile);
                LineIO.write(outFile, Collections.singletonList(jsonTa));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(reader.generateReport());
    }
}
