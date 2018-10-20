/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader;

import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure.*;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.SimpleXMLParser;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.XMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

public class ReadACEAnnotation {

    static boolean isDebug = false;

    /**
     * if set to 'true', will read 2004 ACE format, which may not specify entity subtype, relation
     * modality or relation tense.
     */
    public static boolean is2004mode = false;
    private static Logger logger = LoggerFactory.getLogger(ReadACEAnnotation.class);

    public static ACEDocumentAnnotation readDocument(String FileStr) throws XMLException {
        ACEDocumentAnnotation docAnnotation = new ACEDocumentAnnotation();

        try {

            Document doc = SimpleXMLParser.getDocument(FileStr);

            Element element = doc.getDocumentElement();

            element = SimpleXMLParser.getElement(element, "document");

            NamedNodeMap nnMap = element.getAttributes();

            docAnnotation.id = nnMap.getNamedItem("DOCID").getNodeValue();
            if (isDebug) {
                logger.info(docAnnotation.id);
                logger.info("\n");
            }


            NodeList entityNL = element.getElementsByTagName("entity");
            for (int i = 0; i < entityNL.getLength(); ++i) {
                ACEEntity entity = readEntity(entityNL.item(i));
                if (isDebug) {
                    logger.info("\n");
                }
                docAnnotation.entityList.add(entity);
            }


            NodeList valueNL = element.getElementsByTagName("value");
            for (int i = 0; i < valueNL.getLength(); ++i) {
                ACEValue value = readValue(valueNL.item(i));
                if (isDebug) {
                    logger.info("\n");
                }
                docAnnotation.valueList.add(value);
            }


            NodeList timeNL = element.getElementsByTagName("timex2");
            for (int i = 0; i < timeNL.getLength(); ++i) {
                ACETimeEx timeEx = readTimeEx(timeNL.item(i));
                if (isDebug) {
                    logger.info("\n");
                }
                docAnnotation.timeExList.add(timeEx);
            }

            NodeList relationNL = element.getElementsByTagName("relation");
            for (int i = 0; i < relationNL.getLength(); ++i) {
                nnMap = relationNL.item(i).getAttributes();

                if (isDebug) {
                    logger.error("relation " + i + "...");
                }

                ACERelation relation = readRelation(relationNL.item(i));

                if (isDebug) {
                    logger.info("\n");
                }

                docAnnotation.relationList.add(relation);
            }

            NodeList eventNL = element.getElementsByTagName("event");
            for (int i = 0; i < eventNL.getLength(); ++i) {
                nnMap = eventNL.item(i).getAttributes();
                ACEEvent event = readEvent(eventNL.item(i));

                if (isDebug) {
                    logger.info("\n");
                }

                docAnnotation.eventList.add(event);
            }

        } catch (XMLException e) {
            e.printStackTrace();
            throw e;
        }
        return docAnnotation;
    }

    public static ACERelation readRelation(Node node) throws XMLException {
        ACERelation relation = new ACERelation();

        NamedNodeMap nnMap = node.getAttributes();

        relation.id = nnMap.getNamedItem("ID").getNodeValue();
        relation.type = nnMap.getNamedItem("TYPE").getNodeValue();
        try {

            Node subtypeNode = nnMap.getNamedItem("SUBTYPE");
            if (null != subtypeNode)
                relation.subtype = subtypeNode.getNodeValue();

            Node modalityNode = nnMap.getNamedItem("MODALITY");
            if (null != modalityNode)
                relation.modality = modalityNode.getNodeValue();

            Node tenseNode = nnMap.getNamedItem("TENSE");
            if (null != tenseNode)
                relation.tense = tenseNode.getNodeValue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isDebug) {
            logger.info("Relation:\t" + relation.id + "\t" + relation.type + "\t"
                    + relation.subtype + "\t" + relation.modality + "\t" + relation.tense);
        }
        if (isDebug) {
            logger.info("\n");
        }

        String relationEntityArgumentKey = is2004mode ? "rel_entity_arg" : "relation_argument";
        NodeList nlArg = ((Element) node).getElementsByTagName(relationEntityArgumentKey);

        for (int i = 0; i < nlArg.getLength(); ++i) {
            ACERelationArgument relationArgument = readRelationArgument(nlArg.item(i));
            relation.relationArgumentList.add(relationArgument);
        }

        NodeList nlMention = ((Element) node).getElementsByTagName("relation_mention");

        for (int i = 0; i < nlMention.getLength(); ++i) {
            ACERelationMention relationMention = readRelationMention(nlMention.item(i));
            relation.relationMentionList.add(relationMention);
        }

        return relation;
    }

    public static ACERelationArgument readRelationArgument(Node node) {
        ACERelationArgument relationArgument = new ACERelationArgument();

        NamedNodeMap nnMap = node.getAttributes();

        String refIdKey = is2004mode ? "ENTITYID" : "REFID";
        relationArgument.id = nnMap.getNamedItem(refIdKey).getNodeValue();

        if (is2004mode) {
            // Trying to be consistent with the notation for ACE2005
            String argNum = nnMap.getNamedItem("ARGNUM").getNodeValue();
            relationArgument.role = "Arg-" + argNum;
        } else {
            relationArgument.role = nnMap.getNamedItem("ROLE").getNodeValue();
        }

        if (isDebug) {
            logger.info("  Relation argument:\t" + relationArgument.id + "\t"
                    + relationArgument.role);
            logger.info("\n");
        }

        return relationArgument;
    }

    public static ACERelationMention readRelationMention(Node node) throws XMLException {
        ACERelationMention relationMention = new ACERelationMention();

        NamedNodeMap nnMap = node.getAttributes();

        relationMention.id = nnMap.getNamedItem("ID").getNodeValue();
        // TODO: make these constants
        String lcTag = is2004mode ? "LDCLEXICALCONDITION" : "LEXICALCONDITION";
        relationMention.lexicalCondition = nnMap.getNamedItem(lcTag).getNodeValue();

        if (isDebug) {
            logger.info("  Relation mention:\t" + relationMention.id);
        }

        Element element1 = SimpleXMLParser.getElement(((Element) node), "extent");
        Element element1Char = SimpleXMLParser.getElement(element1, "charseq");

        relationMention.extent = SimpleXMLParser.getContentString(element1Char);
        relationMention.extentStart = Integer.parseInt(element1Char.getAttribute("START"));
        relationMention.extentEnd = Integer.parseInt(element1Char.getAttribute("END"));

        if (isDebug) {
            logger.info("  Relation mention extent:\t" + relationMention.extent + "\t"
                    + relationMention.extentStart + "\t" + relationMention.extentEnd);
        }

        String relationMentionArgumentTag =
                is2004mode ? "rel_mention_arg" : "relation_mention_argument";
        NodeList nlMention = ((Element) node).getElementsByTagName(relationMentionArgumentTag);

        for (int i = 0; i < nlMention.getLength(); ++i) {
            ACERelationArgumentMention relationArgumentMention =
                    readRelationArgumentMention(nlMention.item(i));
            relationMention.relationArgumentMentionList.add(relationArgumentMention);
        }

        if (isDebug) {
            logger.info("\n");
        }
        return relationMention;
    }

    public static ACERelationArgumentMention readRelationArgumentMention(Node node)
            throws XMLException {
        ACERelationArgumentMention eventArgumentMention = new ACERelationArgumentMention();

        NamedNodeMap nnMap = node.getAttributes();

        if (is2004mode) {
            // Trying to be consistent with the notation for ACE2005
            String argNum = nnMap.getNamedItem("ARGNUM").getNodeValue();
            eventArgumentMention.role = "Arg-" + argNum;
            eventArgumentMention.id = nnMap.getNamedItem("ENTITYMENTIONID").getNodeValue();
        } else {
            eventArgumentMention.id = nnMap.getNamedItem("REFID").getNodeValue();
            eventArgumentMention.role = nnMap.getNamedItem("ROLE").getNodeValue();
        }

        if (isDebug) {
            logger.info("    Relation argument mention:\t" + eventArgumentMention.id + "\t"
                    + eventArgumentMention.role);
        }

        Element element1 = SimpleXMLParser.getElement(((Element) node), "extent");
        Element element1Char = SimpleXMLParser.getElement(element1, "charseq");

        eventArgumentMention.argStr = SimpleXMLParser.getContentString(element1Char);
        eventArgumentMention.start = Integer.parseInt(element1Char.getAttribute("START"));
        eventArgumentMention.end = Integer.parseInt(element1Char.getAttribute("END"));

        if (isDebug) {
            logger.info("    Relation argument mention:\t" + eventArgumentMention.start
                    + "\t" + eventArgumentMention.end + "\t" + eventArgumentMention.argStr);
            logger.info("\n");
        }

        return eventArgumentMention;
    }

    public static ACEEvent readEvent(Node node) throws XMLException {
        ACEEvent event = new ACEEvent();

        NamedNodeMap nnMap = node.getAttributes();

        event.id = nnMap.getNamedItem("ID").getNodeValue();
        event.type = nnMap.getNamedItem("TYPE").getNodeValue();
        event.subtype = nnMap.getNamedItem("SUBTYPE").getNodeValue();
        event.modality = nnMap.getNamedItem("MODALITY").getNodeValue();
        event.polarity = nnMap.getNamedItem("POLARITY").getNodeValue();
        event.genericity = nnMap.getNamedItem("GENERICITY").getNodeValue();
        event.tense = nnMap.getNamedItem("TENSE").getNodeValue();

        if (isDebug) {
            logger.info("Event:\t" + event.id + "\t" + event.type + "\t" + event.subtype
                    + "\t" + event.modality + "\t" + event.polarity + "\t" + event.genericity
                    + "\t" + event.tense);
        }
        if (isDebug) {
            logger.info("\n");
        }
        NodeList nlArg = ((Element) node).getElementsByTagName("event_argument");

        for (int i = 0; i < nlArg.getLength(); ++i) {
            ACEEventArgument eventArgument = readEventArgument(nlArg.item(i));
            event.eventArgumentList.add(eventArgument);
        }

        NodeList nlMention = ((Element) node).getElementsByTagName("event_mention");

        for (int i = 0; i < nlMention.getLength(); ++i) {
            ACEEventMention eventMention = readEventMention(nlMention.item(i));
            event.eventMentionList.add(eventMention);
        }

        return event;
    }

    public static ACEEventArgument readEventArgument(Node node) {
        ACEEventArgument eventArgument = new ACEEventArgument();

        NamedNodeMap nnMap = node.getAttributes();

        eventArgument.id = nnMap.getNamedItem("REFID").getNodeValue();
        eventArgument.role = nnMap.getNamedItem("ROLE").getNodeValue();

        if (isDebug) {
            logger.info("  Event argument:\t" + eventArgument.id + "\t" + eventArgument.role);
            logger.info("\n");
        }

        return eventArgument;
    }

    public static ACEEventMention readEventMention(Node node) throws XMLException {
        ACEEventMention eventMention = new ACEEventMention();

        NamedNodeMap nnMap = node.getAttributes();

        eventMention.id = nnMap.getNamedItem("ID").getNodeValue();

        if (isDebug) {
            logger.info("  Event mention:\t" + eventMention.id);
        }

        Element element1 = SimpleXMLParser.getElement(((Element) node), "extent");
        Element element1Char = SimpleXMLParser.getElement(element1, "charseq");

        eventMention.extent = SimpleXMLParser.getContentString(element1Char);
        eventMention.extentStart = Integer.parseInt(element1Char.getAttribute("START"));
        eventMention.extentEnd = Integer.parseInt(element1Char.getAttribute("END"));

        if (isDebug) {
            logger.info("  Event mention extent:\t" + eventMention.extent + "\t"
                    + eventMention.extentStart + "\t" + eventMention.extentEnd);
        }

        if (((Element) node).getElementsByTagName("ldc_scope").getLength() > 0) {
            Element element2 = SimpleXMLParser.getElement(((Element) node), "ldc_scope");
            Element element2Char = SimpleXMLParser.getElement(element2, "charseq");

            eventMention.scope = SimpleXMLParser.getContentString(element2Char);
            eventMention.scopeStart = Integer.parseInt(element2Char.getAttribute("START"));
            eventMention.scopeEnd = Integer.parseInt(element2Char.getAttribute("END"));
        }

        if (isDebug) {
            logger.info("  Event mention scope:\t" + eventMention.scopeStart + "\t"
                    + eventMention.scopeEnd + "\t" + eventMention.scope);
        }

        Element element3 = SimpleXMLParser.getElement(((Element) node), "anchor");
        Element element3Char = SimpleXMLParser.getElement(element3, "charseq");

        eventMention.anchor = SimpleXMLParser.getContentString(element3Char);
        eventMention.anchorStart = Integer.parseInt(element3Char.getAttribute("START"));
        eventMention.anchorEnd = Integer.parseInt(element3Char.getAttribute("END"));

        if (isDebug) {
            logger.info("  Event mention anchor:\t" + eventMention.anchorStart + "\t"
                    + eventMention.anchorEnd + "\t" + eventMention.anchor);
            logger.info("\n");
        }

        NodeList nlMention = ((Element) node).getElementsByTagName("event_mention_argument");

        for (int i = 0; i < nlMention.getLength(); ++i) {
            ACEEventArgumentMention eventArgumentMention =
                    readEventArgumentMention(nlMention.item(i));
            eventMention.eventArgumentMentionList.add(eventArgumentMention);
        }

        if (isDebug) {
            logger.info("\n");
        }
        return eventMention;
    }

    public static ACEEventArgumentMention readEventArgumentMention(Node node) throws XMLException {
        ACEEventArgumentMention eventArgumentMention = new ACEEventArgumentMention();

        NamedNodeMap nnMap = node.getAttributes();

        eventArgumentMention.id = nnMap.getNamedItem("REFID").getNodeValue();
        eventArgumentMention.role = nnMap.getNamedItem("ROLE").getNodeValue();

        if (isDebug) {
            logger.info("    Event argument mention:\t" + eventArgumentMention.id + "\t"
                    + eventArgumentMention.role);
        }

        Element element1 = SimpleXMLParser.getElement(((Element) node), "extent");
        Element element1Char = SimpleXMLParser.getElement(element1, "charseq");

        eventArgumentMention.argStr = SimpleXMLParser.getContentString(element1Char);
        eventArgumentMention.start = Integer.parseInt(element1Char.getAttribute("START"));
        eventArgumentMention.end = Integer.parseInt(element1Char.getAttribute("END"));

        if (isDebug) {
            logger.info("    Event argument mention:\t" + eventArgumentMention.start + "\t"
                    + eventArgumentMention.end + "\t" + eventArgumentMention.argStr);
            logger.info("\n");
        }

        return eventArgumentMention;
    }

    public static ACEEntity readEntity(Node node) throws XMLException {
        ACEEntity entity = new ACEEntity();

        NamedNodeMap nnMap = node.getAttributes();

        entity.classEntity = nnMap.getNamedItem("CLASS").getNodeValue();
        entity.id = nnMap.getNamedItem("ID").getNodeValue();
        entity.type = nnMap.getNamedItem("TYPE").getNodeValue();
        Node subtypenode = nnMap.getNamedItem("SUBTYPE");

        // Entity SUBTYPE is optional only in 2004.
        if (null != subtypenode || !is2004mode)
            entity.subtype = subtypenode.getNodeValue();

        if (isDebug) {
            logger.info("Entity:\t" + entity.id + "\t" + entity.classEntity + "\t"
                    + entity.type + "\t" + entity.subtype);
        }
        if (isDebug) {
            logger.info("\n");
        }

        NodeList nl = ((Element) node).getElementsByTagName("entity_mention");

        for (int i = 0; i < nl.getLength(); ++i) {
            ACEEntityMention entityMention = readEntityMention(nl.item(i));
            entity.entityMentionList.add(entityMention);
        }

        return entity;
    }

    public static ACEEntityMention readEntityMention(Node node) throws XMLException {
        ACEEntityMention entityMention = new ACEEntityMention();

        NamedNodeMap nnMap = node.getAttributes();

        entityMention.id = nnMap.getNamedItem("ID").getNodeValue();
        entityMention.type = nnMap.getNamedItem("TYPE").getNodeValue();

        Node entityMentionLDCTypeItem = nnMap.getNamedItem("LDCTYPE");
        if (null != entityMentionLDCTypeItem)
            entityMention.ldcType = entityMentionLDCTypeItem.getNodeValue();

        // entityMention.ldcATR = nnMap.getNamedItem("LDCATR").getNodeValue();

        if (isDebug) {
            logger.info("  Entity mention:\t" + entityMention.id + "\t" + entityMention.type
                    + "\t" + entityMention.ldcType);
        }

        Element element1 = SimpleXMLParser.getElement(((Element) node), "extent");
        Element element1Char = SimpleXMLParser.getElement(element1, "charseq");

        entityMention.extent = SimpleXMLParser.getContentString(element1Char);
        entityMention.extentStart = Integer.parseInt(element1Char.getAttribute("START"));
        entityMention.extentEnd = Integer.parseInt(element1Char.getAttribute("END"));

        if (isDebug) {
            logger.info("  Entity mention extent:\t" + entityMention.extentStart + "\t"
                    + entityMention.extentEnd + "\t" + entityMention.extent);
        }

        Element element2 = SimpleXMLParser.getElement(((Element) node), "head");
        Element element2Char = SimpleXMLParser.getElement(element2, "charseq");

        entityMention.head = SimpleXMLParser.getContentString(element2Char);
        entityMention.headStart = Integer.parseInt(element2Char.getAttribute("START"));
        entityMention.headEnd = Integer.parseInt(element2Char.getAttribute("END"));

        if (isDebug) {
            logger.info("  Entity mention head:\t" + entityMention.headStart + "\t"
                    + entityMention.headEnd + "\t" + entityMention.head);
            logger.info("\n");
        }

        return entityMention;
    }

    public static ACEValue readValue(Node node) throws XMLException {
        ACEValue value = new ACEValue();

        NamedNodeMap nnMap = node.getAttributes();

        value.id = nnMap.getNamedItem("ID").getNodeValue();
        value.type = nnMap.getNamedItem("TYPE").getNodeValue();

        if (isDebug) {
            logger.info("Value:\t" + value.id + "\t" + value.type);
        }
        if (isDebug) {
            logger.info("\n");
        }

        NodeList nl = ((Element) node).getElementsByTagName("value_mention");

        for (int i = 0; i < nl.getLength(); ++i) {
            ACEValueMention valueMention = readValueMention(nl.item(i));
            value.valueMentionList.add(valueMention);
        }


        return value;
    }


    public static ACEValueMention readValueMention(Node node) throws XMLException {
        ACEValueMention valueMention = new ACEValueMention();

        NamedNodeMap nnMap = node.getAttributes();

        valueMention.id = nnMap.getNamedItem("ID").getNodeValue();

        if (isDebug) {
            logger.info("  Value mention:\t" + valueMention.id);
        }

        Element element1 = SimpleXMLParser.getElement(((Element) node), "extent");
        Element element1Char = SimpleXMLParser.getElement(element1, "charseq");

        valueMention.extent = SimpleXMLParser.getContentString(element1Char);
        valueMention.extentStart = Integer.parseInt(element1Char.getAttribute("START"));
        valueMention.extentEnd = Integer.parseInt(element1Char.getAttribute("END"));

        if (isDebug) {
            logger.info("  Value mention extent:\t" + valueMention.extentStart + "\t"
                    + valueMention.extentEnd + "\t" + valueMention.extent);
        }

        return valueMention;
    }

    public static ACETimeEx readTimeEx(Node node) throws XMLException {
        ACETimeEx time = new ACETimeEx();

        NamedNodeMap nnMap = node.getAttributes();

        time.id = nnMap.getNamedItem("ID").getNodeValue();

        if (isDebug) {
            logger.info("TimeEx:\t" + time.id);
        }
        if (isDebug) {
            logger.info("\n");
        }

        NodeList nl = ((Element) node).getElementsByTagName("timex2_mention");

        for (int i = 0; i < nl.getLength(); ++i) {
            ACETimeExMention valueMention = readTimeExMention(nl.item(i));
            time.timeExMentionList.add(valueMention);
        }


        return time;
    }

    public static ACETimeExMention readTimeExMention(Node node) throws XMLException {
        ACETimeExMention timeExMention = new ACETimeExMention();

        NamedNodeMap nnMap = node.getAttributes();

        timeExMention.id = nnMap.getNamedItem("ID").getNodeValue();

        if (isDebug) {
            logger.info("  TimeEx mention:\t" + timeExMention.id);
        }

        Element element1 = SimpleXMLParser.getElement(((Element) node), "extent");
        Element element1Char = SimpleXMLParser.getElement(element1, "charseq");

        timeExMention.extent = SimpleXMLParser.getContentString(element1Char);
        timeExMention.extentStart = Integer.parseInt(element1Char.getAttribute("START"));
        timeExMention.extentEnd = Integer.parseInt(element1Char.getAttribute("END"));

        if (isDebug) {
            logger.info("  TimeEx mention extent:\t" + timeExMention.extentStart + "\t"
                    + timeExMention.extentEnd + "\t" + timeExMention.extent);
        }

        return timeExMention;
    }



}
