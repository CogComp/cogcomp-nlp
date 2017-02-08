/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.cs.cogcomp.nlp.corpusreaders.ereReader;

import edu.cs.cogcomp.core.datastructures.Pair;
import edu.cs.cogcomp.core.datastructures.ViewNames;
import edu.cs.cogcomp.core.datastructures.textannotation.*;
import edu.cs.cogcomp.nlp.corpusreaders.aceReader.SimpleXMLParser;
import edu.cs.cogcomp.nlp.corpusreaders.aceReader.XMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Reads ERE data and instantiates TextAnnotations with the corresponding Mention and Relation views, as well as
 *     a Named Entity view.
 *
 * ERE annotations are provided in stand-off form: each source file (in xml, and from which character offsets
 *     are computed) has one or more corresponding annotation files (also in xml). Each annotation file corresponds
 *     to a span of the source file, and contains all information about entities, relations, and events for that
 *     span.  Entity and event identifiers presumably carry across spans from the same document.
 *
 * @author mssammon
 */
public class EREMentionRelationReader extends ERENerReader {

    private static final String NAME = ERENerReader.class.getCanonicalName();
    private static final Logger logger = LoggerFactory.getLogger(EREMentionRelationReader.class);
    private int numRelationsMissed;

    /**
     * @param corpusName      the name of the corpus, this can be anything.
     * @param sourceDirectory the name of the directory containing the file.
     * @throws Exception
     */
    public EREMentionRelationReader(String corpusName, String sourceDirectory) throws Exception {
        super(corpusName, sourceDirectory, true);
        numRelationsMissed = 0;
    }

    @Override
    public List<TextAnnotation> getTextAnnotationsFromFile(List<Path> corpusFileListEntry) throws Exception {

        TextAnnotation sourceTa = super.getTextAnnotationsFromFile(corpusFileListEntry).get(0);
//        SpanLabelView tokens = (SpanLabelView)sourceTa.getView(ViewNames.TOKENS);
        CoreferenceView mentionView = new CoreferenceView(getViewName(), NAME, sourceTa, 1.0);

        // now pull all mentions we deal with
        for (int i = 1; i < corpusFileListEntry.size(); ++i) {

            Document doc = SimpleXMLParser.getDocument(corpusFileListEntry.get(i).toFile());
            super.getEntitiesFromFile(doc, mentionView);
            super.getFillersFromFile(doc, mentionView);


            /**
             * previous call populates mentionID : Constituent map needed to build relations efficiently
             */
            getRelationsFromFile(doc, mentionView);
        }
        sourceTa.addView(getViewName(), mentionView);

        return Collections.singletonList(sourceTa);
    }

    /**
     * given an xml document containing relation markup and a view populated with mentions,
     *    generate relations in that view.
     * @param doc XML document containing relation info
     * @param mentionView View to populate with relations
     */
    private void getRelationsFromFile(Document doc, CoreferenceView mentionView) throws XMLException {
        Element element = doc.getDocumentElement();
        Element relElement = SimpleXMLParser.getElement(element, RELATIONS);
        NodeList relNL = relElement.getElementsByTagName(RELATION);
        for (int j = 0; j < relNL.getLength(); ++j) {
            readRelation(relNL.item(j), mentionView);
        }
        logger.info("number of missed relations (missing argument): {}", numRelationsMissed );
    }



    /**
     * read the relations from the gold standard xml and produce appropriate Relations linking mention constituents in
     * the view.
     *
     *   <relations>
     *     <relation id="r-1952" type="generalaffiliation" subtype="opra">
     *       <relation_mention id="relm-56bd16d7_2_837" realis="true">
     *         <rel_arg1 entity_id="ent-m.07t31" entity_mention_id="m-56bd16d7_2_159" role="org">congress</rel_arg1>
     *         <rel_arg2 entity_id="ent-m.07wbk" entity_mention_id="m-56bd16d7_2_153" role="entity">republican</rel_arg2>
     *       </relation_mention>
     *     </relation>
     *
     * @param node the entity node, contains the more specific mentions of that entity.
     * @param view the span label view we will add the labels to.
     * @throws XMLException
     */
    public void readRelation (Node node, View view) throws XMLException {
        NamedNodeMap nnMap = node.getAttributes();
        String type = nnMap.getNamedItem(TYPE).getNodeValue();
        String subtype = nnMap.getNamedItem(SUBTYPE).getNodeValue();
        String relId = nnMap.getNamedItem(ID).getNodeValue();

        // now for specifics get the mentions.
        NodeList nl = ((Element)node).getElementsByTagName(RELATION_MENTION);
        for (int i = 0; i < nl.getLength(); ++i) {
            Node relMentionNode = nl.item(i);
            Pair<String, String> arg1Info = getArgumentId(relMentionNode, ARG_ONE);
            Pair<String, String> arg2Info = getArgumentId(relMentionNode, ARG_TWO);
            Constituent arg1c = super.getMentionConstituent(arg1Info.getFirst());
            Constituent arg2c = super.getMentionConstituent(arg2Info.getFirst());

            if (null == arg1c || null == arg2c) {
                numRelationsMissed++;
                continue;
            }
            nnMap = relMentionNode.getAttributes();
            String realis = nnMap.getNamedItem(REALIS).getNodeValue();
            String mentionId = nnMap.getNamedItem(ID).getNodeValue();

            Relation entityRelation = new Relation(type, arg1c, arg2c, 1.0f);

            // Add attributes to each of the relation.
            entityRelation.addAttribute(RelationIdAttribute, relId);
            entityRelation.addAttribute(RelationMentionIdAttribute, mentionId);
            entityRelation.addAttribute(RelationTypeAttribute, type);
            entityRelation.addAttribute(RelationSubtypeAttribute, subtype);
            entityRelation.addAttribute(RelationRealisAttribute, realis);
            entityRelation.addAttribute(RelationSourceRoleAttribute, arg1Info.getSecond());
            entityRelation.addAttribute(RelationTargetRoleAttribute, arg1Info.getSecond());

            view.addRelation(entityRelation);
        }
    }

    private Pair<String, String> getArgumentId(Node mentionNode, String argTag) {

        String argId = null;
        String role = null;
        NodeList nodeList = ((Element) mentionNode).getElementsByTagName(argTag);
        if (nodeList.getLength() == 0)
            throw new IllegalStateException("relation mention node has no argument '" + argTag + "'.");

        Node argNode = nodeList.item(0);
        NamedNodeMap nnMap = argNode.getAttributes();
        if (nnMap.getLength() == 2) //filler
            argId = nnMap.getNamedItem(FILLER_ID).getNodeValue();
        else
            argId = nnMap.getNamedItem(ENTITY_MENTION_ID).getNodeValue();

        role = nnMap.getNamedItem(ROLE).getNodeValue();

        if (null == argId || null == role)
            throw new IllegalStateException("No argument/role information for argument '" + argTag + "'.");

        return new Pair(argId, role);
    }

    @Override
    public String getViewName() {
        return ViewNames.MENTION_ERE;
    }

}
