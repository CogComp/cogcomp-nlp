/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader;

import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.annotation.XmlTextAnnotationMaker;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CorpusReaderConfigurator;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.XmlDocumentReader;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static edu.illinois.cs.cogcomp.core.io.IOUtils.getFileName;

/**
 * Strips all useless XML markup from an ERE xml document leaving the original text and where
 * needed, appropriate attribute values. Base class for other ERE readers for Named Entities,
 * Mentions/Relations, and Events.
 *
 * There are THREE ERE English releases.
 * Regrettably, they do not follow consistent standards for organization or for annotation.
 *
 * LDC2015E29_DEFT_Rich_ERE English V2 has two sets of annotation files: one, used for the Event Argument Extraction
 *    task in TAC that year, includes a small amount of additional markup to make each xml document well-formed.
 *    This changes the annotation offsets. Taggable entities within quoted blocks are annotated.
 *
 * LDC2015E68_DEFT_Rich_ERE_English R2_V2 has as source files excerpts from multi-post discussion forum documents.
 * Taggable entities within quoted blocks are annotated.
 *
 * LDC2016E31_DEFT_Rich_ERE_English ENR3 has -- I believe -- complete threads, where annotation files may be
 *    broken into several chunks. Taggable entities within quoted blocks are NOT marked.
 *
 * @author redman
 * @author msammon
 */
public class EREDocumentReader extends XmlDocumentReader {

        /**
     * tags in document files
     */
    public static final String QUOTE = "quote";;
    public static final String AUTHOR = "author";
    public static final String ID = "id";
    public static final String DATETIME = "datetime";
    public static final String POST = "post";
    public static final String DOC = "doc";
    public static final String ORIG_AUTHOR = "orig_author";
    public static final String HEADLINE = "headline";
    public static final String IMG = "img";
    public static final String SNIP = "snip";
    public static final String SQUISH = "squish";
    public static final String STUFF = "stuff";
    public static final String SARCASM = "sarcasm";
    /**
     * tags in ERE markup files
     */
    public static final String ENTITIES = "entities";
    public static final String ENTITY = "entity";
    public static final String FILLERS = "fillers";
    public static final String FILLER = "filler";
    public static final String OFFSET = "offset";
    public static final String TYPE = "type";
    public static final String ENTITY_MENTION = "entity_mention";
    public static final String NOUN_TYPE = "noun_type";
    public static final String PRO = "PRO";
    public static final String NOM = "NOM";
    public static final String NAM = "NAM";
    public static final String FILL = "FILL";
    public static final String LENGTH = "length";
    public static final String MENTION_TEXT = "mention_text";
    public static final String MENTION_HEAD = "nom_head";
    public static final String SPECIFICITY = "specificity";
    public static final String REALIS = "realis";
    public static final String RELATIONS = "relations";
    public static final String RELATION = "relation";
    public static final String RELATION_MENTION = "relation_mention";
    public static final String SUBTYPE = "subtype";
    public static final String ARG_ONE = "rel_arg1";
    public static final String ARG_TWO = "rel_arg2";
    public static final String ENTITY_MENTION_ID = "entity_mention_id";
    public static final String ENTITY_ID = "entity_id";
    public static final String ROLE = "role";
    public static final String FILLER_ID = "filler_id";
    /** aim for consistent naming */
    public static final String EntityMentionTypeAttribute = ACEReader.EntityMentionTypeAttribute;
    public static final String EntityIdAttribute = ACEReader.EntityIDAttribute;
    public static final String EntityMentionIdAttribute = ACEReader.EntityMentionIDAttribute;
    public static final String EntityHeadStartCharOffset = ACEReader.EntityHeadStartCharOffset;
    public static final String EntityHeadEndCharOffset = ACEReader.EntityHeadEndCharOffset;
    public static final String EntitySpecificityAttribute = "EntitySpecificity";
    public static final String RelationIdAttribute = ACEReader.RelationIDAttribute;
    public static final String RelationMentionIdAttribute = ACEReader.RelationMentionIDAttribute;
    public static final String RelationSubtypeAttribute = ACEReader.RelationSubtypeAttribute;
    public static final String RelationTypeAttribute = ACEReader.RelationTypeAttribute;
    public static final String RelationRealisAttribute = "REALIS";
    public static final String RelationSourceRoleAttribute = "RelationSourceRole";
    public static final String RelationTargetRoleAttribute = "RelationTargetRole";
    /** tag sets for xml processor for ERE documents  */
    public static final Map<String, Set<String>> tagsWithAtts = new HashMap<>();
    public static final Set<String> deletableSpanTags = new HashSet<>();
    public static final Set<String> tagsToIgnore = new HashSet<>();
    private static final String DATELINE = "dateline";
    private static Logger logger = LoggerFactory.getLogger(EREDocumentReader.class);
    /** these tags contain attributes we want to keep. */
    static private ArrayList<String> retainTags = new ArrayList<>();
    /** the attributes to keep for the above tags. */
    static private ArrayList<String> retainAttributes = new ArrayList<>();
    /**
     * build an EREDocumentReader configured for the specified ERE release.
     * @param ereCorpus a value from enum EreCorpus (e.g. 'ENR1', 'ENR2', or 'ENR3')
     * @param throwExceptionOnXmlParseFailure
     * @throws Exception
     */
    public EREDocumentReader(EreCorpus ereCorpus, String corpusRoot, boolean throwExceptionOnXmlParseFailure) throws Exception {
        this(EREDocumentReader.buildEreConfig(ereCorpus.name(), corpusRoot),
                buildXmlTextAnnotationMaker(ereCorpus, throwExceptionOnXmlParseFailure));
    }

//    static {
//        Set<String> attributeNames = new HashSet<>();
//        attributeNames.add(AUTHOR);
//        attributeNames.add(ID);
//        attributeNames.add(DATETIME);
//        tagsWithAtts.put(POST, attributeNames);
//        attributeNames = new HashSet<>();
//        attributeNames.add(ID);
//        tagsWithAtts.put(DOC, attributeNames);
//        attributeNames = new HashSet<>();
//        attributeNames.add(ORIG_AUTHOR);
//        tagsWithAtts.put(QUOTE, attributeNames);
//
//        deletableSpanTags.add(QUOTE);
//        deletableSpanTags.add(DATELINE);
//
//        tagsToIgnore.add(IMG);
//        tagsToIgnore.add(SNIP);
//        tagsToIgnore.add(STUFF);
//        tagsToIgnore.add(SARCASM);
//    }


    public EREDocumentReader(ResourceManager rm, XmlTextAnnotationMaker xmlTextAnnotationMaker) throws Exception {
        super(rm, xmlTextAnnotationMaker);
    }

    /**
     * builds an XmlTextAnnotationMaker that handles the source files from the specified ERE release
     *
     * @param ereCorpusVal a value corresponding to enum EreCorpus (e.g. 'ENR1', 'ENR2', or 'ENR3')
     * @param throwExceptionOnXmlParseFailure if 'true', xml reader will throw an exception if it finds e.g.
     *                                        mismatched xml tag open/close
     * @return an XmlTextAnnotationMaker configured for the specified ERE corpus
     * @throws Exception
     */
    public static XmlTextAnnotationMaker buildEreXmlTextAnnotationMaker(String ereCorpusVal, boolean throwExceptionOnXmlParseFailure) throws Exception {
        return buildXmlTextAnnotationMaker(EreCorpus.valueOf(ereCorpusVal), throwExceptionOnXmlParseFailure);
    }

    /**
     * This method sets a range of configuration parameters based on which ERE release user specifies
     *
     * @param ereCorpusVal a value corresponding to enum EreCorpus ('ENR1', 'ENR2', or 'ENR3')
     * @param corpusRoot the root directory of the corpus on your file system
     * @return a ResourceManager with the appropriate configuration,
     * @throws Exception
     */
    public static ResourceManager buildEreConfig(String ereCorpusVal, String corpusRoot) throws Exception {

        // defaults: ENR3
        String sourceDir = "source/";
        String annotationDir = "ere/";
        String sourceExtension = ".xml";
        String annotationExtension = ".xml";
        String corpusNameVal = new StringBuilder().append("ERE_").append(ereCorpusVal).toString();

        switch(EreCorpus.valueOf(ereCorpusVal)) {
            case ENR1:
                sourceDir = "source/mpdfxml/";
                annotationDir = "ere/mpdfxml/";
                break;
            case ENR2:
                sourceExtension = ".cmp.txt";
                break;
            case ENR3:
                break;
            default:
                String errMsg = "Illegal value for ereCorpus: " + ereCorpusVal;
                logger.error(errMsg);
                throw new IllegalArgumentException(ereCorpusVal);
        }

        Properties props = new Properties();
        //set source, annotation directories relative to specified corpus root dir
        props.setProperty(CorpusReaderConfigurator.SOURCE_DIRECTORY.key, corpusRoot + "/" + sourceDir);
        props.setProperty(CorpusReaderConfigurator.ANNOTATION_DIRECTORY.key, corpusRoot + "/" + annotationDir);
        props.setProperty(CorpusReaderConfigurator.SOURCE_EXTENSION.key, sourceExtension);
        props.setProperty(CorpusReaderConfigurator.ANNOTATION_EXTENSION.key, annotationExtension);
        props.setProperty(CorpusReaderConfigurator.CORPUS_NAME.key, corpusNameVal);

        return new ResourceManager(props);
    }

    /**
     * builds an {@link XmlTextAnnotationMaker} for reading ERE format English corpus.
     *
     * @param ereCorpus which ERE release is being processed -- affects which tag blocks are marked
     * @param throwExceptionOnXmlParseFail if 'true', throw an exception if xml parser fails
     * @return an XmlTextAnnotationMaker configured for English ERE.
     */
    public static XmlTextAnnotationMaker buildXmlTextAnnotationMaker(EreCorpus ereCorpus, boolean throwExceptionOnXmlParseFail) {
        TextAnnotationBuilder textAnnotationBuilder = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());

        return buildXmlTextAnnotationMaker(textAnnotationBuilder, ereCorpus, throwExceptionOnXmlParseFail);
    }

    /**
     * builds an {@link XmlTextAnnotationMaker} expecting ERE annotation.  {@link TextAnnotationBuilder} must be
     * configured for the target language.
     *
     * @param textAnnotationBuilder a TextAnnotationBuilder with tokenizer suited to target language.
     * @param throwExceptionOnXmlParseFail if 'true', the XmlTextAnnotationMaker will throw an exception if any
     *                                     errors are found in the source xml.
     * @return an XmlTextAnnotationMaker configured to parse an ERE corpus.
     */
    public static XmlTextAnnotationMaker buildXmlTextAnnotationMaker(TextAnnotationBuilder textAnnotationBuilder,
                                                                     EreCorpus ereCorpus,
                                                                     boolean throwExceptionOnXmlParseFail) {

        Map<String, Set<String>> tagsWithAtts = new HashMap<>();
        Set<String> attributeNames = new HashSet<>();
        attributeNames.add(AUTHOR);
        attributeNames.add(ID);
        attributeNames.add(DATETIME);
        tagsWithAtts.put(POST, attributeNames);
        attributeNames = new HashSet<>();
        attributeNames.add(ID);
        tagsWithAtts.put(DOC, attributeNames);
        attributeNames = new HashSet<>();
        attributeNames.add(ORIG_AUTHOR);
        tagsWithAtts.put(QUOTE, attributeNames);

        Set<String> deletableSpanTags = new HashSet<>();
        // for release 3 only, quoted blocks are NOT annotated
        if (EreCorpus.ENR3.equals(ereCorpus))
            deletableSpanTags.add(QUOTE);

        Set<String> tagsToIgnore = new HashSet<>(); // implies "delete spans enclosed by these tags"
        tagsToIgnore.add(IMG);
        tagsToIgnore.add(SNIP);
        tagsToIgnore.add(SQUISH);

        XmlDocumentProcessor xmlProcessor =
                new XmlDocumentProcessor(deletableSpanTags, tagsWithAtts, tagsToIgnore, throwExceptionOnXmlParseFail);
        return new XmlTextAnnotationMaker(textAnnotationBuilder, xmlProcessor);
    }

    /**
     * ERE corpus directory has two directories: source/ and ere/. The source/ directory contains
     * original text in an xml format. The ere/ directory contains markup files corresponding in a
     * many-to-one relationship with the source/ files: related annotation files have the same
     * prefix as the corresponding source file (up to the .xml suffix).
     * (NOTE: release 1 (LDC2015E29) has two subdirectories for both source and annotation: one version is slightly
     *    modified by adding xml markup to make the source documents well-formed, which changes the annotation offsets.
     *
     * This method generates a List of List of Paths: each component List has the source file as its
     * first element, and markup files as its remaining elements. It expects {@link
     * super.getSourceDirectory()} to return the root directory of the ERE corpus, under which
     * should be data/source/ and data/ere/ directories containing source files and annotation files
     * respectively.
     *
     * @return a list of Path objects corresponding to files containing corpus documents to process.
     */
    @Override
    public List<List<Path>> getFileListing() throws IOException {
        FilenameFilter sourceFilter = (dir, name) -> name.endsWith(getRequiredSourceFileExtension());
        /*
         * returns the FULL PATH of each file
         */
        String sourceDir = resourceManager.getString(CorpusReaderConfigurator.SOURCE_DIRECTORY.key);
        List<String> sourceFileList= Arrays.asList(IOUtils.lsFilesRecursive(sourceDir, sourceFilter));
        LinkedList<String> annotationFileList = new LinkedList<>();

        FilenameFilter annotationFilter = (dir, name) -> name.endsWith(getRequiredAnnotationFileExtension());

        String annotationDir = resourceManager.getString(CorpusReaderConfigurator.ANNOTATION_DIRECTORY.key);
        for (String f : Arrays.asList(IOUtils.lsFilesRecursive(annotationDir, annotationFilter))) {
            annotationFileList.add(getFileName(f));
        }

        List<List<Path>> pathList = new ArrayList<>();

        /*
         * fileList has multiple entries per single annotation: a source file plus one or more
         *    annotation files. These files share a prefix -- the stem of the file containing
         *    the source text.
         */
        for (String fileName : sourceFileList) {
            List<Path> sourceAndAnnotations = new ArrayList<>();
            Path fPath = Paths.get(fileName); // source file
            sourceAndAnnotations.add(fPath);
            String stem = this.getFileStem(fPath, getRequiredSourceFileExtension()); // strip *source* extension

            for (String annFile : annotationFileList) {
                if (annFile.startsWith(stem)) {
                    logger.debug("Processing file '{}'", annFile);
                    sourceAndAnnotations.add(Paths.get(resourceManager.getString(CorpusReaderConfigurator.ANNOTATION_DIRECTORY.key) + annFile));
                    sourceAndAnnotations.remove(annFile);
                }
            }
            pathList.add(sourceAndAnnotations);
        }
        return pathList;
    }

    private String getFileStem(Path filePath, String extension) {
        String fileName = filePath.getName(filePath.getNameCount() - 1).toString();
        int lastIndex = fileName.lastIndexOf(extension);
        return fileName.substring(0,lastIndex);
    }

/**
     * prefix indicates language; suffix indicates release
     */
    public static enum EreCorpus {
        ENR1, ENR2, ENR3
    }
}
