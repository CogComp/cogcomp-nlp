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

        tagsToIgnore.add(IMG);
        tagsToIgnore.add(SNIP);
        tagsToIgnore.add(STUFF);
        tagsToIgnore.add(SARCASM);
    }


    /**
     * @param corpusName the name of the corpus, this can be anything.
     * @param sourceDirectory the name of the directory containing the file.
     * @throws Exception
     */
    public EREDocumentReader(String corpusName, String sourceDirectory, boolean throwExceptionOnXmlParseFail) throws Exception {
        super(corpusName, sourceDirectory, buildXmlProcessor(throwExceptionOnXmlParseFail));
    }

    private static XmlTextAnnotationMaker buildXmlProcessor(boolean throwExceptionOnXmlParseFail) {
        TextAnnotationBuilder textAnnotationBuilder = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());

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

        String sourceFileDir = super.getSourceDirectory() + "data/source/";
        String annotationDir = super.getSourceDirectory() + "data/ere/";
        FilenameFilter filter = (dir, name) -> name.endsWith(getRequiredFileExtension());
        /*
         * returns the FULL PATH of each file
         */
        List<String> sourceFileList= Arrays.asList(IOUtils.lsFilesRecursive(sourceFileDir, filter));
        LinkedList<String> annotationFileList = new LinkedList<>();
        for ( String f : Arrays.asList(IOUtils.lsFilesRecursive(annotationDir, filter))) {
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
            sourceAndAnnotations.add(Paths.get(fileName));
            String stem = getFileStem(fileName);

            for (String annFile : annotationFileList) {
                if (annFile.startsWith(stem)) {
                    logger.debug("Processing file '{}'", annFile);
                    sourceAndAnnotations.add(Paths.get(annotationDir + annFile));
                    sourceAndAnnotations.remove(annFile);
                }
            }
            pathList.add(sourceAndAnnotations);
        }
        return pathList;
    }



    /**
     * Exclude any files not possessing this extension.
     * 
     * @return the required file extension.
     */
    protected String getRequiredFileExtension() {
        return ".xml";
    }
}
