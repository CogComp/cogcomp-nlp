/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure.*;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader.AceFileProcessor;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader.ReadACEAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Corpus reader for the ACE data-set. This reader currently only supports only the ACE-2004 and
 * ACE-2005 data-sets.
 *
 * @author Bhargav Mangipudi
 */
public class ACEReader extends AnnotationReader<TextAnnotation> {
    // Entity Constants
    public static final String EntityIDAttribute = "EntityID";
    public static final String EntityTypeAttribute = "EntityType";
    public static final String EntitySubtypeAttribute = "EntitySubtype"; /* Optional */
    public static final String EntityClassAttribute = "EntityClass";
    public static final String EntityMentionIDAttribute = "EntityMentionID";
    public static final String EntityMentionTypeAttribute = "EntityMentionType";
    public static final String EntityMentionLDCTypeAttribute = "EntityMentionLDCType"; /* Optional */
    public static final String EntityHeadStartCharOffset = "EntityHeadStartCharOffset";
    public static final String EntityHeadEndCharOffset = "EntityHeadEndCharOffset";
    // Relation Constants
    public static final String RelationIDAttribute = "RelationID";
    public static final String RelationTypeAttribute = "RelationType";
    public static final String RelationSubtypeAttribute = "RelationSubtype"; /* Optional */
    public static final String RelationModalityAttribute = "RelationModality"; /* Optional */
    public static final String RelationTenseAttribute = "RelationTense"; /* Optional */
    public static final String RelationMentionIDAttribute = "RelationMentionID";
    public static final String RelationMentionLexicalConditionAttribute = "RelationMentionLexicalCondition";
    protected static final String RelationFirstArgumentTag = "Arg-1";
    protected static final String RelationSecondArgumentTag = "Arg-2";
    protected static final Logger logger = LoggerFactory.getLogger(ACEReader.class);
    protected static final AceFileProcessor fileProcessor = new AceFileProcessor();
    protected static final TextAnnotationBuilder taBuilder = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(false, false));
    protected final String aceCorpusHome;
    protected final boolean is2004mode;
    protected final String corpusId;
    protected AtomicReference<TextAnnotation> currentTextAnnotation;
    protected List<Pair<String, String>> fileList;
    protected AtomicInteger fileListPosition;

    public ACEReader(){
        this.aceCorpusHome = null;
        this.is2004mode = false;
        this.corpusId = null;
    }

    /**
     * Constructor for the ACE Data-set Reader
     *
     * @param aceCorpusHome Path of the data. eg. `data/ace2004/data/English`
     * @param sections List of sections to parse. eg. `new String[] { "nw", "bn" }` Pass `null` to
     *        parse all available sections.
     * @param is2004mode Boolean representing if the data-set is the ACE-2004.
     * @throws Exception Exception thrown in-case of major failure.
     */
    public ACEReader(String aceCorpusHome, String[] sections, boolean is2004mode) throws Exception {
        super(CorpusReaderConfigurator.buildResourceManager(aceCorpusHome));

        this.aceCorpusHome = aceCorpusHome;
        this.corpusId = is2004mode ? "ACE2004" : "ACE2005";
        this.is2004mode = is2004mode;

        if (sections == null || sections.length == 0) {
            sections = IOUtils.lsDirectories(this.aceCorpusHome);
        }

        File corpusHomeDir = new File(this.aceCorpusHome);
        assert corpusHomeDir.isDirectory();

        FilenameFilter apfFileFilter = new FilenameFilter() {
            public boolean accept(File directory, String fileName) {
                return ( new File(directory + File.separator + fileName).isDirectory() || fileName.endsWith(".apf.xml") );
            }
        };

        List<Pair<String, String>> fileNames = new ArrayList<>();
        for (String section : sections) {
            File sectionDir = new File(this.aceCorpusHome + File.separator + section);

            String[] xmlFiles = IOUtils.lsFilesRecursive(sectionDir.getAbsolutePath(), apfFileFilter);

            if (xmlFiles.length == 0) {
                logger.error("No valid xml file found. Skipping section " + section);
                continue;
            }

            for (String fileName : xmlFiles) {
                fileNames.add(new Pair<>(section, fileName));
            }
        }

        this.fileList = Collections.unmodifiableList(fileNames);
    }

    /**
     * Constructor for the ACE Data-set Reader
     *
     * @param aceCorpusHome Path of the data. eg. `data/ace2004/data/English`
     * @param is2004mode Boolean representing if the data-set is the ACE-2004.
     * @throws Exception Exception thrown in-case of major failure.
     */
    public ACEReader(String aceCorpusHome, boolean is2004mode) throws Exception {
        this(aceCorpusHome, null, is2004mode);
    }

    /**
     * Helper function to create a head constituent from an extent constituent.
     */
    public static Constituent getEntityHeadForConstituent(Constituent extentConstituent,
                                                           TextAnnotation textAnnotation,
                                                           String viewName) {
        int startCharOffset =
                Integer.parseInt(extentConstituent
                        .getAttribute(ACEReader.EntityHeadStartCharOffset));
        int endCharOffset =
                Integer.parseInt(extentConstituent.getAttribute(ACEReader.EntityHeadEndCharOffset)) - 1;
        int startToken = textAnnotation.getTokenIdFromCharacterOffset(startCharOffset);
        int endToken = textAnnotation.getTokenIdFromCharacterOffset(endCharOffset);

        if (startToken >= 0 && endToken >= 0 && !(endToken - startToken < 0)) {
            Constituent cons =
                    new Constituent(extentConstituent.getLabel(), 1.0, viewName, textAnnotation,
                            startToken, endToken + 1);

            for (String attributeKey : extentConstituent.getAttributeKeys()) {
                cons.addAttribute(attributeKey, extentConstituent.getAttribute(attributeKey));
            }

            return cons;
        }

        return null;
    }

    /**
     * @return Boolean representing if the reader is in 2004 mode.
     */
    public boolean Is2004Mode() {
        return this.is2004mode;
    }

    @Override
    protected void initializeReader() {
        // This is called even before our class's constructor initializations.
        this.fileList = new ArrayList<>();
        this.fileListPosition = new AtomicInteger(0);
        this.currentTextAnnotation = new AtomicReference<>();
    }

    /**
     * Parse a single ACE Document.
     *
     * @param section Section that the document belongs to.
     * @param fileName Name of the annotation file.
     * @return TextAnnotation instance.
     */
    protected TextAnnotation parseSingleACEFile(String section, String fileName) {
        ACEDocument doc;

        // TODO: Static field might cause issue if we try to parse both versions in parallel.
        ReadACEAnnotation.is2004mode = this.is2004mode;

        try {
            File sectionDir = new File(this.aceCorpusHome + File.separator + section);
            doc = fileProcessor.processAceEntry(sectionDir, fileName);
        } catch (Exception ex) {
            logger.warn("Error while reading document - " + fileName, ex);
            return null;
        }

        logger.info("Parsing file - " + fileName);

        // Adding `section/fileName` as textId for annotation.
        String textId = fileName.substring(fileName.indexOf(section + File.separator));
        TextAnnotation ta =
                taBuilder.createTextAnnotation(
                        this.corpusId,
                        textId,
                        doc.contentRemovingTags);

        // Add metadata attributes to the generated Text Annotation.
        if (doc.metadata != null) {
            for (String metadataKey : doc.metadata.keySet()) {
                String value = doc.metadata.get(metadataKey);
                if (!value.isEmpty()) {
                    ta.addAttribute(metadataKey, value);
                }
            }
        }

        File file = new File( fileName );
        this.addEntityViews(ta, doc.aceAnnotation, file);
        this.addEntityRelations(ta, doc.aceAnnotation, file);

        // TODO: Pending Event, TimeEx and Value Views

        return ta;
    }

    /**
     * Adds an Entity Extent View and a Coreference View to the TextAnnotation.
     *
     * @param ta TextAnnotation instance to add the Entity View to.
     * @param docAnnotation Annotation for the current document.
     * @param file Link to the .apf.xml file for the current document.
     */
    protected void addEntityViews(TextAnnotation ta, ACEDocumentAnnotation docAnnotation, File file) {
        SpanLabelView entityView =
                new SpanLabelView(ViewNames.MENTION_ACE,
                        ACEReader.class.getCanonicalName(), ta, 1.0f, true);
        CoreferenceView corefHeadView =
                new CoreferenceView(ViewNames.COREF_HEAD, ACEReader.class.getCanonicalName(), ta,
                        1.0f);
        CoreferenceView corefExtentView =
                new CoreferenceView(ViewNames.COREF_EXTENT, ACEReader.class.getCanonicalName(), ta,
                        1.0f);

        for (ACEEntity entity : docAnnotation.entityList) {
            List<Constituent> corefMentions = new ArrayList<>(docAnnotation.entityList.size());
            List<Constituent> corefMentionHeads = new ArrayList<>(docAnnotation.entityList.size());

            for (ACEEntityMention entityMention : entity.entityMentionList) {
                int extentStartTokenId =
                        ta.getTokenIdFromCharacterOffset(entityMention.extentStart);
                int extentEndTokenId = ta.getTokenIdFromCharacterOffset(entityMention.extentEnd);

                if (extentStartTokenId < 0 || extentEndTokenId < 0
                        || extentStartTokenId > extentEndTokenId + 1) {
                    logger.error("Incorrect Extent Token Span for mention - " + entity.id + " "
                            + entityMention.id);
                    continue;
                }

                Constituent extentConstituent =
                        new Constituent(entity.type, ViewNames.MENTION_ACE, ta, extentStartTokenId, extentEndTokenId + 1);
                extentConstituent.addAttribute(EntityTypeAttribute, entity.type);
                extentConstituent.addAttribute(EntityIDAttribute, entity.id);
                extentConstituent.addAttribute(EntityMentionIDAttribute, entityMention.id);
                extentConstituent.addAttribute(EntityMentionTypeAttribute, entityMention.type);
                extentConstituent.addAttribute(EntityClassAttribute, entity.classEntity);

                String entitySubType = (entity.subtype != null) ? entity.subtype : entity.type;
                extentConstituent.addAttribute(EntitySubtypeAttribute, entitySubType);

                if (entityMention.ldcType != null) {
                    extentConstituent.addAttribute(EntityMentionLDCTypeAttribute, entityMention.ldcType);
                }

                // ACE Annotation have character offsets inclusive of start/end.
                // Converting them to a one-after-then-end.
                extentConstituent.addAttribute(EntityHeadStartCharOffset, entityMention.headStart + "");
                extentConstituent.addAttribute(EntityHeadEndCharOffset, entityMention.headEnd + 1 + "");

                entityView.addConstituent(extentConstituent);

                Constituent corefExtentConstituent =
                        extentConstituent.cloneForNewViewWithDestinationLabel(
                                ViewNames.COREF_EXTENT, entity.id);
                corefMentions.add(corefExtentConstituent);

                Constituent corefHeadConstituent =
                        getEntityHeadForConstituent(corefExtentConstituent, ta,
                                ViewNames.COREF_HEAD);
                if (corefHeadConstituent != null) {
                    corefMentionHeads.add(corefHeadConstituent);
                }
            }

            // Picking the longest mention as the canonical mention
            // as we do not get this information is not present in the dataset.
            Constituent canonicalMention = null;
            double[] scores = new double[corefMentions.size()];
            for (int i = 0; i < corefMentions.size(); i++) {
                Constituent cons = corefMentions.get(i);
                scores[i] = cons.getConstituentScore();

                if (canonicalMention == null
                        || canonicalMention.getSurfaceForm().length() < cons.getSurfaceForm().length()) {
                    canonicalMention = cons;
                }
            }

            if (corefMentions.size() > 0) {
                corefExtentView.addCorefEdges(canonicalMention, corefMentions, scores);
            } else {
                logger.error("No Entity Mentions found for a given entity - " + entity.id);
            }

            // Processing Coref Head Constituents
            // Picking the longest mention as the canonical mention
            // as we do not get this information is not present in the dataset.
            canonicalMention = null;
            scores = new double[corefMentionHeads.size()];
            for (int i = 0; i < corefMentionHeads.size(); i++) {
                Constituent cons = corefMentionHeads.get(i);
                scores[i] = cons.getConstituentScore();

                if (canonicalMention == null
                        || canonicalMention.getSurfaceForm().length() < cons.getSurfaceForm().length()) {
                    canonicalMention = cons;
                }
            }

            if (corefMentionHeads.size() > 0) {
                corefHeadView.addCorefEdges(canonicalMention, corefMentionHeads, scores);
            } else {
                logger.error("No Entity Mentions found for a given entity - " + entity.id);
            }
        }

        ta.addView(ViewNames.MENTION_ACE, entityView);
        ta.addView(ViewNames.COREF_HEAD, corefHeadView);
        ta.addView(ViewNames.COREF_EXTENT, corefExtentView);
    }

    /**
     * Adds {@link Relation} edges to relations between mentions from the Entity View.
     *
     * @param ta TextAnnotation instance to add the Relation View to.
     * @param docAnnotation Annotation for the current document.
     * @param file Link to the .apf.xml file for the current document.
     */
    protected void addEntityRelations(TextAnnotation ta, ACEDocumentAnnotation docAnnotation, File file) {
        SpanLabelView entityView = (SpanLabelView) ta.getView(ViewNames.MENTION_ACE);
        Map<Pair<String, String>, Constituent> entityIdMap = new HashMap<>();

        // Prepare a mapping for entityId, entityMentionId to the corresponding constituent.
        for (Constituent entityConstituent : entityView.getConstituents()) {
            String entityId = entityConstituent.getAttribute(EntityIDAttribute);
            String entityMentionId = entityConstituent.getAttribute(EntityMentionIDAttribute);

            entityIdMap.put(new Pair<>(entityId, entityMentionId), entityConstituent);
        }

        for (ACERelation relation : docAnnotation.relationList) {
            // Check if the relation has "Arg-1" and "Arg-2"
            String firstArgumentEntityId = null;
            String secondArgumentEntityId = null;

            for (ACERelationArgument relArg : relation.relationArgumentList) {
                if (Objects.equals(relArg.role, RelationFirstArgumentTag)) {
                    firstArgumentEntityId = relArg.id;
                } else if (Objects.equals(relArg.role, RelationSecondArgumentTag)) {
                    secondArgumentEntityId = relArg.id;
                }
            }

            if (firstArgumentEntityId == null || secondArgumentEntityId == null) {
                logger.error("ACE Relation Arguments not found for relation id - " + relation.id);
                continue;
            }

            // Parse each relation mention individually
            for (ACERelationMention relationMention : relation.relationMentionList) {
                // Find both mentions in the relation
                ACERelationArgumentMention firstArgumentMention = null;
                ACERelationArgumentMention secondArgumentMention = null;

                for (ACERelationArgumentMention relArgMention : relationMention.relationArgumentMentionList) {
                    if (Objects.equals(relArgMention.role, RelationFirstArgumentTag)) {
                        firstArgumentMention = relArgMention;
                    } else if (Objects.equals(relArgMention.role, RelationSecondArgumentTag)) {
                        secondArgumentMention = relArgMention;
                    }
                }

                if (firstArgumentMention == null || secondArgumentMention == null) {
                    logger.error("Cannot find participating mention for relation id - "
                            + relation.id + " " + relationMention.id);
                    continue;
                }


                // Use the EntityId map to find the mentions that is used in the current relation.
                Constituent firstArgument = null;
                Pair<String, String> firstArgumentKey = new Pair<>(firstArgumentEntityId, firstArgumentMention.id);
                if (entityIdMap.containsKey(firstArgumentKey)) {
                    firstArgument = entityIdMap.get(firstArgumentKey);
                }

                Constituent secondArgument = null;
                Pair<String, String> secondArgumentKey = new Pair<>(secondArgumentEntityId, secondArgumentMention.id);
                if (entityIdMap.containsKey(secondArgumentKey)) {
                    secondArgument = entityIdMap.get(secondArgumentKey);
                }

                if (firstArgument == null || secondArgument == null) {
                    logger.error("Failed to find mentions for relation id - " + relation.id + " " + relationMention.id);
                    continue;
                }

                Relation entityRelation = new Relation(relation.type, firstArgument, secondArgument, 1.0f);

                // Add attributes to each of the relation.
                entityRelation.addAttribute(RelationIDAttribute, relation.id);
                entityRelation.addAttribute(RelationTypeAttribute, relation.type);

                String relationSubType = (relation.subtype != null) ? relation.subtype : relation.type;
                entityRelation.addAttribute(RelationSubtypeAttribute, relationSubType);

                if (relation.tense != null) {
                    entityRelation.addAttribute(RelationTenseAttribute, relation.tense);
                }

                if (relation.modality != null) {
                    entityRelation.addAttribute(RelationModalityAttribute, relation.modality);
                }

                entityRelation.addAttribute(RelationMentionIDAttribute, relationMention.id);
                entityRelation.addAttribute(RelationMentionLexicalConditionAttribute, relationMention.lexicalCondition);

                // Add relation to the entity view.
                entityView.addRelation(entityRelation);
            }
        }
    }

    /**
     * return the next annotation object. Don't forget to increment currentAnnotationId.
     *
     * @return an annotation object.
     */
    @Override
    public TextAnnotation next() {
        return this.currentTextAnnotation.getAndSet(null);
    }

    @Override
    public void reset() {
        this.currentTextAnnotation.set(null);
        this.fileListPosition.set(0);
    }

    /**
     * Returns {@code true} if the iteration has more elements. (In other words, returns
     * {@code true} if {@link #next} would return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        if (this.currentTextAnnotation.get() != null) {
            return true;
        }

        int currentPosition;
        TextAnnotation textAnnotation = null;

        do {
            currentPosition = this.fileListPosition.getAndIncrement();

            if (currentPosition >= this.fileList.size()) {
                break;
            }

            Pair<String, String> currentFileInfo = this.fileList.get(currentPosition);
            textAnnotation = parseSingleACEFile(currentFileInfo.getFirst(), currentFileInfo.getSecond());

            this.currentTextAnnotation.set(textAnnotation);
        } while(textAnnotation == null); // If parsing fails, continue looking for more items.

        return textAnnotation != null;
    }



    /**
     * TODO: generate a human-readable report of annotations read from the source file (plus whatever
     * other relevant statistics the user should know about).
     */

    public String generateReport() {
        throw new UnsupportedOperationException("ERROR: generateReport() Not yet implemented.");
    }


}
