package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.algorithms.Mappers;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.transformers.ITransformer;
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
    public static final String ENTITYVIEW = "ENTITYVIEW";

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
    public static final String RELATIONVIEW = "RELATIONVIEW";

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
        SpanLabelView entityView = new SpanLabelView(ENTITYVIEW, ACEReader.class.getCanonicalName(), ta, 1.0f, true);
        CoreferenceView corefView = new CoreferenceView(ViewNames.COREF, ACEReader.class.getCanonicalName(), ta, 1.0f);

        for (ACEEntity entity : docAnnotation.entityList) {
            List<Constituent> corefMentions = new ArrayList<>(docAnnotation.entityList.size());

            for (ACEEntityMention entityMention : entity.entityMentionList) {
                int extentStartTokenId = ta.getTokenIdFromCharacterOffset(entityMention.extentStart);
                int extentEndTokenId = ta.getTokenIdFromCharacterOffset(entityMention.extentEnd);

                if (extentStartTokenId < 0 || extentEndTokenId < 0 || extentStartTokenId > extentEndTokenId + 1) {
                    logger.error("Incorrect Extent Token Span for mention - " + entity.id + " " + entityMention.id);
                    continue;
                }

                Constituent extentConstituent = new Constituent(entity.type, ENTITYVIEW, ta, extentStartTokenId, extentEndTokenId + 1);
                extentConstituent.addAttribute(EntityTypeAttribute, entity.type);
                extentConstituent.addAttribute(EntityIDAttribute, entity.id);
                extentConstituent.addAttribute(EntityMentionIDAttribute, entityMention.id);
                extentConstituent.addAttribute(EntityMentionTypeAttribute, entityMention.type);
                extentConstituent.addAttribute(EntityClassAttribute, entity.classEntity);

                if (entity.subtype != null) extentConstituent.addAttribute(EntitySubtypeAttribute, entity.subtype);
                if (entityMention.ldcType != null) extentConstituent.addAttribute(EntityMentionLDCTypeAttribute, entityMention.ldcType);

                extentConstituent.addAttribute(EntityHeadStartCharOffset, entityMention.headStart + "");
                extentConstituent.addAttribute(EntityHeadEndCharOffset, entityMention.headEnd + "");

                entityView.addConstituent(extentConstituent);

                corefMentions.add(extentConstituent.cloneForNewViewWithDestinationLabel(ViewNames.COREF, entity.id));
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
                corefView.addCorefEdges(canonicalMention, corefMentions, scores);
            } else {
                logger.error("No Entity Mentions found for a given entity - " + entity.id);
            }
        }

        ta.addView(ENTITYVIEW, entityView);
        ta.addView(ViewNames.COREF, corefView);
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
        PredicateArgumentView relationView = new PredicateArgumentView(RELATIONVIEW, ACEReader.class.getCanonicalName(), ta, 1.0f);

        CoreferenceView entityCorefView = (CoreferenceView) ta.getView(ViewNames.COREF);
        Set<Constituent> corefEntitiesInDoc = entityCorefView.getCanonicalEntities();
        Set<Constituent> allCanonicalEntities = new HashSet<>();

        // Investigate this issue further.
        // getCanonicalEntities should not return mentions with incoming edges.
        for (Constituent c : corefEntitiesInDoc) {
            if (c.getIncomingRelations().size() == 0) {
                allCanonicalEntities.add(c);
            }
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

                // Use the coreference view edges to find the actual mention that is used in the current relation mention.
                // Check with the canonical mention first and then follow the edge to get other coreferent mentions.
                Constituent firstArgument = null;
                if (firstArgumentCanonicalMention.getAttribute(EntityMentionIDAttribute).equals(firstArgumentMention.id)) {
                    firstArgument = firstArgumentCanonicalMention;
                } else {
                    for (Constituent args : getCorefMentionsForSameEntityId(entityCorefView, firstArgumentCanonicalMention)) {
                        if (args.getAttribute(EntityMentionIDAttribute).equals(firstArgumentMention.id)) {
                            firstArgument = args;
                        }
                    }
                }

                Constituent secondArgument = null;
                if (secondArgumentCanonicalMention.getAttribute(EntityMentionIDAttribute).equals(secondArgumentMention.id)) {
                    secondArgument = secondArgumentCanonicalMention;
                } else {
                    for (Constituent args : getCorefMentionsForSameEntityId(entityCorefView, secondArgumentCanonicalMention)) {
                        if (args.getAttribute(EntityMentionIDAttribute).equals(secondArgumentMention.id)) {
                            secondArgument = args;
                        }
                    }
                }

                if (firstArgument == null || secondArgument == null) {
                    logger.error("Failed to find mentions for relation id - " + relation.id + " " + relationMention.id);
                    continue;
                }

                // Clone mentions for the relation view.
                firstArgument = firstArgument.cloneForNewViewWithDestinationLabel(RELATIONVIEW, firstArgumentMention.role);
                secondArgument = secondArgument.cloneForNewViewWithDestinationLabel(RELATIONVIEW, secondArgumentMention.role);

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

                // Add relation to the view.
                relationView.addPredicateArguments(
                        firstArgument,
                        Collections.singletonList(secondArgument),
                        new String[]{ relation.subtype },
                        new double[]{ 1.0f });
            }
        }

        // Add the relation view to the TextAnnotation
        ta.addView(RELATIONVIEW, relationView);
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

    /* Helper for finding correct Coreferent Mentions chain. */

    private static ITransformer<Relation, Constituent> relationsToConstituents;

    static {
        relationsToConstituents = new ITransformer<Relation, Constituent>() {

            @Override
            public Constituent transform(Relation arg0) {
                return arg0.getTarget();
            }
        };
    }

    /**
     * Find Coref Mentions for a given input mention. The method in the CoreferenceView class does not
     * work for this case due to multiple redundant mentions (same span);
     * leading to the entity id factor being totally ignored.
     *
     * @param corefView Input CoreferenceView to find coreferent mentions from.
     * @param mention   Mention to find coreferent mentions to.
     * @return List of Mentions represented as Constituents in the Coreference View.
     */
    private static List<Constituent> getCorefMentionsForSameEntityId(CoreferenceView corefView, Constituent mention) {
        Constituent canonicalEntity = corefView.getCanonicalEntity(mention);
        return Mappers.map(canonicalEntity.getOutgoingRelations(), relationsToConstituents);
    }
}
