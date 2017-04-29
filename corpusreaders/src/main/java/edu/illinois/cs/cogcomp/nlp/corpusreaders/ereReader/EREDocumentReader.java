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
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CorpusReaderConfigurator;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.XmlDocumentReader;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static edu.illinois.cs.cogcomp.core.io.IOUtils.getFileName;
import static edu.illinois.cs.cogcomp.core.io.IOUtils.getFileStem;

/**
 * Strips all useless XML markup from an ERE xml document leaving the original text and where
 * needed, appropriate attribute values. Base class for other ERE readers for Named Entities,
 * Mentions/Relations, and Events.
 *
 * @author redman
 * @author msammon
 */
public class EREDocumentReader extends XmlDocumentReader {

    /**
     * tags in document files
     */
    public static final String QUOTE = "quote";
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

    static {
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

        deletableSpanTags.add(QUOTE);
        deletableSpanTags.add(DATELINE);

        tagsToIgnore.add(IMG);
        tagsToIgnore.add(SNIP);
        tagsToIgnore.add(STUFF);
        tagsToIgnore.add(SARCASM);
    }



    /**
     * @param corpusName the name of the corpus, this can be anything.
     * @param corpusSourceDir the path to the directory containing the source documents.
     * @param corpusAnnotationDir the path to the directory containing the offset annotation documents.
     * @throws Exception
     */
    public EREDocumentReader(String corpusName, String corpusSourceDir, String corpusAnnotationDir, XmlTextAnnotationMaker xmlTextAnnotationMaker, String sourceFileExtension, String annotationFileExtension) throws Exception {
        super(corpusName, corpusSourceDir, corpusAnnotationDir, xmlTextAnnotationMaker, sourceFileExtension, annotationFileExtension);
    }


    /**
     * builds an {@link XmlTextAnnotationMaker} for reading ERE format English corpus.
     *
     * @param throwExceptionOnXmlParseFail if 'true', throw an exception if xml parser fails
     * @return an XmlTextAnnotationMaker configured for English ERE.
     */
    public static XmlTextAnnotationMaker buildXmlTextAnnotationMaker(boolean throwExceptionOnXmlParseFail) {
        TextAnnotationBuilder textAnnotationBuilder = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());

        return buildXmlTextAnnotationMaker(textAnnotationBuilder, throwExceptionOnXmlParseFail);
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
            Path fPath = Paths.get(fileName);
            sourceAndAnnotations.add(fPath);
            String stem = this.getFileStem(fPath, getRequiredAnnotationFileExtension());

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
}
