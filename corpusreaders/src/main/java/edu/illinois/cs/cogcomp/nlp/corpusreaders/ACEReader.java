/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure.*;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader.AceFileProcessor;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader.ReadACEAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

/**
 * Corpus reader for the ACE data-set.
 * This reader currently only supports only the ACE-2004 and ACE-2005 data-sets.
 *
 * @author Bhargav Mangipudi
 */
public class ACEReader extends TextAnnotationReader {
    private String aceCorpusHome;
    private boolean is2004mode;
    private String[] sections;
    private String corpusId;
    private List<TextAnnotation> documents;

    private static final String RelationFirstArgumentTag = "Arg-1";
    private static final String RelationSecondArgumentTag = "Arg-2";

    private static final Logger logger = LoggerFactory.getLogger(ACEReader.class);

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

    public static final String RelationMentionArgumentIDAttribute = "RelationMentionArgumentID";
    public static final String RelationMentionArgumentRoleAttribute = "RelationMentionArgumentRole";

    /**
     * Constructor for the ACE Data-set Reader
     *
     * @param aceCorpusHome Path of the data. eg. `data/ace2004/data/English`
     * @param sections      List of sections to parse. eg. `new String[] { "nw", "bn" }`
     *                      Pass `null` to parse all available sections.
     * @param is2004mode    Boolean representing if the data-set is the ACE-2004.
     * @throws Exception Exception thrown in-case of major failure.
     */
    public ACEReader(String aceCorpusHome, String[] sections, boolean is2004mode) throws Exception {
        super(aceCorpusHome);

        this.aceCorpusHome = aceCorpusHome;
        this.corpusId = is2004mode ? "ACE2004" : "ACE2005";
        this.documents = new ArrayList<>();
        this.is2004mode = is2004mode;
        this.sections = sections;

        if (this.sections == null || this.sections.length == 0) {
            this.sections = IOUtils.lsDirectories(this.aceCorpusHome);
        }

        // TODO:Ideally constructor should'nt be reading and processing I/O.
        this.updateCurrentFiles();
    }

    /**
     * Constructor for the ACE Data-set Reader
     *
     * @param aceCorpusHome Path of the data. eg. `data/ace2004/data/English`
     * @param is2004mode    Boolean representing if the data-set is the ACE-2004.
     * @throws Exception Exception thrown in-case of major failure.
     */
    public ACEReader(String aceCorpusHome, boolean is2004mode) throws Exception {
        this(aceCorpusHome, null, is2004mode);
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
        // Useless method is useless.
    }

    // Lists out all files and creates TextAnnotation for each document
    protected void updateCurrentFiles() {
        File corpusHomeDir = new File(this.aceCorpusHome);
        assert corpusHomeDir.isDirectory();

        FilenameFilter apfFileFilter = new FilenameFilter() {
            public boolean accept(File directory, String fileName) {
                return fileName.endsWith(".apf.xml");
            }
        };

        ReadACEAnnotation.is2004mode = this.is2004mode;
        AceFileProcessor fileProcessor = new AceFileProcessor(new TokenizerTextAnnotationBuilder(new IllinoisTokenizer()));
        TextAnnotationBuilder taBuilder = new TokenizerTextAnnotationBuilder(new IllinoisTokenizer());

        for (String section : this.sections) {
            File sectionDir = new File(corpusHomeDir.getAbsolutePath() + "/" + section);

            for (File file : sectionDir.listFiles(apfFileFilter)) {
                ACEDocument doc;
                String fileName = file.getAbsolutePath();

                try {
                    doc = fileProcessor.processAceEntry(sectionDir, fileName);
                    doc.taList.get(0); // Only pick documents that have at-least one TA.
                } catch (Exception ex) {
                    logger.warn("Error while reading document - " + file.getName(), ex);
                    continue;
                }

                logger.info("Parsing file - " + file.getName());

                // Adding `section/fileName` as textId for annotation.
                String textId = fileName.substring(fileName.indexOf(section + File.separator));
                TextAnnotation ta = taBuilder.createTextAnnotation(
                        this.corpusId,
                        textId,
                        doc.contentRemovingTags);

                this.addEntityViews(ta, doc.aceAnnotation, file);
                this.addRelationView(ta, doc.aceAnnotation, file);

                // TODO: Pending Event, TimeEx and Value Views

                this.documents.add(ta);
            }
        }
    }

    /**
     * Adds an Entity Extent View and a Coreference View to the TextAnnotation.
     *
     * @param ta            TextAnnotation instance to add the Entity View to.
     * @param docAnnotation Annotation for the current document.
     * @param file          Link to the .apf.xml file for the current document.
     */
    public void addEntityViews(TextAnnotation ta, ACEDocumentAnnotation docAnnotation, File file) {
        SpanLabelView entityCoarseView = new SpanLabelView(ViewNames.NER_ACE_COARSE_HEAD, ACEReader.class.getCanonicalName(), ta, 1.0f, true);
        SpanLabelView entityFineView = new SpanLabelView(ViewNames.NER_ACE_FINE_HEAD, ACEReader.class.getCanonicalName(), ta, 1.0f, true);
        SpanLabelView entityCoarseExtentView = new SpanLabelView(ViewNames.NER_ACE_COARSE_EXTENT, ACEReader.class.getCanonicalName(), ta, 1.0f, true);
        SpanLabelView entityFineExtentView = new SpanLabelView(ViewNames.NER_ACE_FINE_EXTENT, ACEReader.class.getCanonicalName(), ta, 1.0f, true);
        CoreferenceView corefHeadView = new CoreferenceView(ViewNames.COREF_HEAD, ACEReader.class.getCanonicalName(), ta, 1.0f);
        CoreferenceView corefExtentView = new CoreferenceView(ViewNames.COREF_EXTENT, ACEReader.class.getCanonicalName(), ta, 1.0f);

        for (ACEEntity entity : docAnnotation.entityList) {
            List<Constituent> corefMentions = new ArrayList<>(docAnnotation.entityList.size());
            List<Constituent> corefMentionHeads = new ArrayList<>(docAnnotation.entityList.size());

            for (ACEEntityMention entityMention : entity.entityMentionList) {
                int extentStartTokenId = ta.getTokenIdFromCharacterOffset(entityMention.extentStart);
                int extentEndTokenId = ta.getTokenIdFromCharacterOffset(entityMention.extentEnd);

                if (extentStartTokenId < 0 || extentEndTokenId < 0 || extentStartTokenId > extentEndTokenId + 1) {
                    logger.error("Incorrect Extent Token Span for mention - " + entity.id + " " + entityMention.id);
                    continue;
                }

                Constituent extentConstituent = new Constituent(entity.type, ViewNames.NER_ACE_COARSE_EXTENT, ta, extentStartTokenId, extentEndTokenId + 1);
                extentConstituent.addAttribute(EntityTypeAttribute, entity.type);
                extentConstituent.addAttribute(EntityIDAttribute, entity.id);
                extentConstituent.addAttribute(EntityMentionIDAttribute, entityMention.id);
                extentConstituent.addAttribute(EntityMentionTypeAttribute, entityMention.type);
                extentConstituent.addAttribute(EntityClassAttribute, entity.classEntity);

                if (entity.subtype != null) extentConstituent.addAttribute(EntitySubtypeAttribute, entity.subtype);
                if (entityMention.ldcType != null)
                    extentConstituent.addAttribute(EntityMentionLDCTypeAttribute, entityMention.ldcType);

                extentConstituent.addAttribute(EntityHeadStartCharOffset, entityMention.headStart + "");
                extentConstituent.addAttribute(EntityHeadEndCharOffset, entityMention.headEnd + "");

                entityCoarseExtentView.addConstituent(extentConstituent);

                // Clone constituent for the ENTITYVIEW_FINE SpanLabelView
                String subTypeLabel = (entity.subtype != null) ? entity.subtype : entity.type;
                Constituent fineExtentConstituent = extentConstituent.cloneForNewViewWithDestinationLabel(ViewNames.NER_ACE_FINE_EXTENT, subTypeLabel);
                entityFineExtentView.addConstituent(fineExtentConstituent);

                Constituent headConstituent = ACEReader.getEntityHeadForConstituent(extentConstituent, ta, ViewNames.NER_ACE_COARSE_HEAD);
                if (headConstituent != null) {
                    entityCoarseView.addConstituent(headConstituent);
                    Constituent fineHead = headConstituent.cloneForNewViewWithDestinationLabel(ViewNames.NER_ACE_FINE_HEAD, subTypeLabel);

                    entityFineView.addConstituent(fineHead);
                }

                Constituent corefExtentConstituent = extentConstituent.cloneForNewViewWithDestinationLabel(ViewNames.COREF_EXTENT, entity.id);
                corefMentions.add(corefExtentConstituent);

                Constituent corefHeadConstituent = getEntityHeadForConstituent(corefExtentConstituent, ta, ViewNames.COREF_HEAD);
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
                        || canonicalMention.getSurfaceForm().length() < cons.getSurfaceForm().length())
                    canonicalMention = cons;
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
                        || canonicalMention.getSurfaceForm().length() < cons.getSurfaceForm().length())
                    canonicalMention = cons;
            }

            if (corefMentionHeads.size() > 0) {
                corefHeadView.addCorefEdges(canonicalMention, corefMentionHeads, scores);
            } else {
                logger.error("No Entity Mentions found for a given entity - " + entity.id);
            }
        }

        ta.addView(ViewNames.NER_ACE_COARSE_HEAD, entityCoarseView);
        ta.addView(ViewNames.NER_ACE_FINE_HEAD, entityFineView);
        ta.addView(ViewNames.NER_ACE_COARSE_EXTENT, entityCoarseExtentView);
        ta.addView(ViewNames.NER_ACE_FINE_EXTENT, entityFineExtentView);

        ta.addView(ViewNames.COREF_HEAD, corefHeadView);
        ta.addView(ViewNames.COREF_EXTENT, corefExtentView);
    }


    /**
     * Adds a PredicateArgumentView for ACE Relations between Entities.
     * The Predicate constituent of the View presents the first Argument in the ACE Relation.
     * The Argument constituent of the View presents the second Argument in the ACE Relation.
     *
     * @param ta            TextAnnotation instance to add the Relation View to.
     * @param docAnnotation Annotation for the current document.
     * @param file          Link to the .apf.xml file for the current document.
     */
    private void addRelationView(TextAnnotation ta, ACEDocumentAnnotation docAnnotation, File file) {
        PredicateArgumentView relationCoarseView = new PredicateArgumentView(ViewNames.RELATION_ACE_COARSE_EXTENT, ACEReader.class.getCanonicalName(), ta, 1.0f);
        PredicateArgumentView relationFineView = new PredicateArgumentView(ViewNames.RELATION_ACE_FINE_EXTENT, ACEReader.class.getCanonicalName(), ta, 1.0f);

        PredicateArgumentView relationCoarseHeadView = new PredicateArgumentView(ViewNames.RELATION_ACE_COARSE_HEAD, ACEReader.class.getCanonicalName(), ta, 1.0f);
        PredicateArgumentView relationFineHeadView = new PredicateArgumentView(ViewNames.RELATION_ACE_FINE_HEAD, ACEReader.class.getCanonicalName(), ta, 1.0f);

        CoreferenceView entityCorefView = (CoreferenceView) ta.getView(ViewNames.COREF_EXTENT);
        Set<Constituent> allCanonicalEntities = entityCorefView.getCanonicalEntitiesViaRelations();

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

            // Find the canonical mentions for the participating entities.
            Constituent firstArgumentCanonicalMention = null;
            Constituent secondArgumentCanonicalMention = null;
            for (Constituent cons : allCanonicalEntities) {
                if (Objects.equals(cons.getLabel(), firstArgumentEntityId)) {
                    firstArgumentCanonicalMention = cons;
                }

                if (Objects.equals(cons.getLabel(), secondArgumentEntityId)) {
                    secondArgumentCanonicalMention = cons;
                }
            }

            if (firstArgumentCanonicalMention == null || secondArgumentCanonicalMention == null) {
                logger.error("Cannot find participating mention for relation id - " + relation.id);
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
                    logger.error("Cannot find participating mention for relation id - " + relation.id + " " + relationMention.id);
                    continue;
                }

                // Use the coreference view edges to find the coreferent mentions that is used in the current relation mention.
                Constituent firstArgument = null;
                for (Constituent args : entityCorefView.getCoreferentMentionsViaRelations(firstArgumentCanonicalMention)) {
                    if (args.getAttribute(EntityMentionIDAttribute).equals(firstArgumentMention.id)) {
                        firstArgument = args;
                        break;
                    }
                }

                Constituent secondArgument = null;
                for (Constituent args : entityCorefView.getCoreferentMentionsViaRelations(secondArgumentCanonicalMention)) {
                    if (args.getAttribute(EntityMentionIDAttribute).equals(secondArgumentMention.id)) {
                        secondArgument = args;
                        break;
                    }
                }

                if (firstArgument == null || secondArgument == null) {
                    logger.error("Failed to find mentions for relation id - " + relation.id + " " + relationMention.id);
                    continue;
                }

                // Clone mentions for the relation view.
                firstArgument = firstArgument.cloneForNewViewWithDestinationLabel(ViewNames.RELATION_ACE_COARSE_EXTENT, firstArgumentMention.role);
                secondArgument = secondArgument.cloneForNewViewWithDestinationLabel(ViewNames.RELATION_ACE_COARSE_EXTENT, secondArgumentMention.role);

                // Add attributes to each of the constituents.
                for (Constituent arg : Arrays.asList(firstArgument, secondArgument)) {
                    arg.addAttribute(RelationIDAttribute, relation.id);
                    arg.addAttribute(RelationTypeAttribute, relation.type);

                    if (relation.subtype != null) arg.addAttribute(RelationSubtypeAttribute, relation.subtype);
                    if (relation.tense != null) arg.addAttribute(RelationTenseAttribute, relation.tense);
                    if (relation.modality != null) arg.addAttribute(RelationModalityAttribute, relation.modality);

                    arg.addAttribute(RelationMentionIDAttribute, relationMention.id);
                    arg.addAttribute(RelationMentionLexicalConditionAttribute, relationMention.lexicalCondition);
                }

                firstArgument.addAttribute(RelationMentionArgumentIDAttribute, firstArgumentMention.id);
                firstArgument.addAttribute(RelationMentionArgumentRoleAttribute, firstArgumentMention.role);

                secondArgument.addAttribute(RelationMentionArgumentIDAttribute, secondArgumentMention.id);
                secondArgument.addAttribute(RelationMentionArgumentRoleAttribute, secondArgumentMention.role);

                // Add relation to the coarse view.
                relationCoarseView.addPredicateArguments(
                        firstArgument,
                        Collections.singletonList(secondArgument),
                        new String[]{relation.type},
                        new double[]{1.0f});

                Constituent firstArgumentHead = getEntityHeadForConstituent(firstArgument, ta, ViewNames.RELATION_ACE_COARSE_HEAD);
                Constituent secondArgumentHead = getEntityHeadForConstituent(secondArgument, ta, ViewNames.RELATION_ACE_COARSE_HEAD);

                if (firstArgumentHead != null && secondArgumentHead != null) {
                    relationCoarseHeadView.addPredicateArguments(
                            firstArgumentHead,
                            Collections.singletonList(secondArgumentHead),
                            new String[]{relation.type},
                            new double[]{1.0f});
                }

                // Adding relation to the fine view.
                Constituent fineFirstArg = firstArgument.cloneForNewView(ViewNames.RELATION_ACE_FINE_EXTENT);
                Constituent fineSecondArg = secondArgument.cloneForNewView(ViewNames.RELATION_ACE_FINE_EXTENT);

                String subTypeLabel = (relation.subtype != null) ? relation.subtype : relation.type;
                relationFineView.addPredicateArguments(
                        fineFirstArg,
                        Collections.singletonList(fineSecondArg),
                        new String[]{subTypeLabel},
                        new double[]{1.0f});

                Constituent fineFirstArgumentHead = getEntityHeadForConstituent(fineFirstArg, ta, ViewNames.RELATION_ACE_COARSE_HEAD);
                Constituent fineSecondArgumentHead = getEntityHeadForConstituent(fineSecondArg, ta, ViewNames.RELATION_ACE_COARSE_HEAD);

                if (fineFirstArgumentHead != null && fineSecondArgumentHead != null) {
                    relationFineHeadView.addPredicateArguments(
                            fineFirstArgumentHead,
                            Collections.singletonList(fineSecondArgumentHead),
                            new String[]{subTypeLabel},
                            new double[]{1.0f});
                }
            }
        }

        // Add the relation view to the TextAnnotation
        ta.addView(ViewNames.RELATION_ACE_COARSE_EXTENT, relationCoarseView);
        ta.addView(ViewNames.RELATION_ACE_FINE_EXTENT, relationFineView);

        ta.addView(ViewNames.RELATION_ACE_COARSE_HEAD, relationCoarseHeadView);
        ta.addView(ViewNames.RELATION_ACE_FINE_HEAD, relationFineHeadView);
    }

    @Override
    protected TextAnnotation makeTextAnnotation() throws Exception {
        return this.documents.get(this.currentAnnotationId);
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return this.documents.size() > this.currentAnnotationId;
    }

    private static Constituent getEntityHeadForConstituent(Constituent extentConstituent, TextAnnotation textAnnotation, String viewName) {
        int startCharOffset = Integer.parseInt(extentConstituent.getAttribute(ACEReader.EntityHeadStartCharOffset));
        int endCharOffset = Integer.parseInt(extentConstituent.getAttribute(ACEReader.EntityHeadEndCharOffset));
        int start_token = textAnnotation.getTokenIdFromCharacterOffset(startCharOffset);
        int end_token = textAnnotation.getTokenIdFromCharacterOffset(endCharOffset);

        if (start_token >= 0 && end_token >= 0 && !(end_token - start_token < 0)) {
            // Be careful with the +1 in end_span below. Regular TextAnnotation likes the end_token number exclusive
            Constituent cons = new Constituent(
                    extentConstituent.getLabel(),
                    1.0,
                    viewName,
                    textAnnotation,
                    start_token,
                    end_token + 1);

            for (String attributeKey : extentConstituent.getAttributeKeys()) {
                cons.addAttribute(attributeKey, extentConstituent.getAttribute(attributeKey));
            }

            return cons;
        }

        return null;
    }
}