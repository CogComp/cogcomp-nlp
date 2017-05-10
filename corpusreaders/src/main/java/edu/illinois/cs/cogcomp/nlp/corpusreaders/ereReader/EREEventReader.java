/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.SimpleXMLParser;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.XMLException;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//TODO: make sure filler mentions are read for args
// TODO: make sure multiple mentions per entity are possible


/**
 * Read Event annotations from ERE corpus.
 *
 * @author mssammon
 */
public class EREEventReader extends EREMentionRelationReader {

    private static final String NAME = EREEventReader.class.getCanonicalName();
    private static final Logger logger = LoggerFactory.getLogger(EREEventReader.class);

    private int numEventsInSource;
    private int numEventsGenerated;
    private int numEventMentionsInSource;
    private int numEventMentionsGenerated;
    private int numEventRolesInSource;
    private int numEventRolesGenerated;

    /**
     * Read mention-relation annotations -- including coreference -- from ERE English corpus.
     *
     * @param ereCorpus                       the ERE corpus release (values from {@link EreCorpus}
     * @param corpusRoot the data root directory for the ERE corpus to be processed
     * @param throwExceptionOnXmlParseFailure if 'true', throws exception if xml parser encounters e.g. mismatched
     *                                        open/close tags  @throws Exception
     */
    public EREEventReader(EreCorpus ereCorpus, String corpusRoot, boolean throwExceptionOnXmlParseFailure) throws Exception {
        this(ereCorpus, new TokenizerTextAnnotationBuilder(new StatefulTokenizer()), corpusRoot, throwExceptionOnXmlParseFailure);
    }

    /**
     * constructor to allow arbitrary language/tokenization behavior via explicit TextAnnotationBuilder
     * @param ereCorpus the ERE corpus release (values from {@link EreCorpus} -- specifies source/markup directories,
     *                  source xml tag behavior
     * @param taBldr a {@link TextAnnotationBuilder} for the desired language/tokenization behavior.
     * @param corpusRoot the data root directory for the ERE corpus to be processed
     * @param throwExceptionOnXmlParseFailure if 'true', throws exception if xml parser encounters e.g. mismatched
     *                                        open/close tags  @throws Exception
     * @throws Exception
     */
    public EREEventReader(EreCorpus ereCorpus, TextAnnotationBuilder taBldr, String corpusRoot, boolean throwExceptionOnXmlParseFailure) throws Exception {
        super(ereCorpus, taBldr, corpusRoot, throwExceptionOnXmlParseFailure);
    }


    public static String getEventViewName() {
        return ViewNames.EVENT_ERE;
    }

    @Override
    public void reset() {
        super.reset();
        numEventsInSource = 0; // == triggers
        numEventsGenerated = 0;
        numEventMentionsInSource = 0;
        numEventMentionsGenerated = 0;
        numEventRolesInSource = 0;
        numEventRolesGenerated = 0;
    }

    @Override
    public List<XmlTextAnnotation> getAnnotationsFromFile(List<Path> corpusFileListEntry)
            throws Exception {

        XmlTextAnnotation xmlTa = super.getAnnotationsFromFile(corpusFileListEntry).get(0);
        TextAnnotation sourceTa = xmlTa.getTextAnnotation();
        // SpanLabelView tokens = (SpanLabelView)sourceTa.getView(ViewNames.TOKENS);
        View mentionView = sourceTa.getView(getMentionViewName());

        if (null == mentionView)
            throw new IllegalStateException("View '" + getMentionViewName() + "' (mention view) not found.");

        PredicateArgumentView eventView = new PredicateArgumentView(getEventViewName(), NAME, sourceTa, 1.0);

        // process one or more files containing stand-off annotations
        for (int i = 1; i < corpusFileListEntry.size(); ++i) {

            Document doc = SimpleXMLParser.getDocument(corpusFileListEntry.get(i).toFile());

            getEventsFromFile(doc, eventView, xmlTa);
        }

        sourceTa.addView(getEventViewName(), eventView);

        return Collections.singletonList(xmlTa);
    }

    private void getEventsFromFile(Document doc, PredicateArgumentView eventView, XmlTextAnnotation xmlTa) throws XMLException {
        Element element = doc.getDocumentElement();
        Element eventElement = SimpleXMLParser.getElement(element, HOPPERS);
        NodeList relNL = eventElement.getElementsByTagName(HOPPER);
        for (int j = 0; j < relNL.getLength(); ++j) {
            readEvent(relNL.item(j), eventView, xmlTa);
        }
    }

    /**
     * read event mentions collected in a single HOPPER element
     * @param node
     * @param eventView
     * @param xmlTa
     */
    private void readEvent(Node node, PredicateArgumentView eventView, XmlTextAnnotation xmlTa) throws XMLException {

    /*
      <hoppers>
    <hopper id="h-56bd16d7_2_16">
      <event_mention id="em-56bd16d7_2_1057" type="transaction" subtype="transaction" realis="other" ways="voluntary">
        <trigger source="ENG_DF_001241_20150407_F0000007T" offset="179" length="5">trade</trigger>
        <em_arg entity_id="ent-m.09c7w0" entity_mention_id="m-56bd16d7_2_135" role="giver" realis="true">US</em_arg>
        <em_arg entity_id="ent-m.0d04z6" entity_mention_id="m-56bd16d7_2_75" role="recipient" realis="true">cuba</em_arg>
      </event_mention>
    </hopper>
    <hopper id="h-56bd16d7_2_1113">
      <event_mention id="em-56bd16d7_2_1085" type="transaction" subtype="transaction" realis="other" ways="voluntary">
        <trigger source="ENG_DF_001241_20150407_F0000007T" offset="179" length="5">trade</trigger>
        <em_arg entity_id="ent-m.0d04z6" entity_mention_id="m-56bd16d7_2_75" role="giver" realis="true">cuba</em_arg>
        <em_arg entity_id="ent-m.09c7w0" entity_mention_id="m-56bd16d7_2_135" role="recipient" realis="true">US</em_arg>
      </event_mention>
    </hopper>
     */
        NamedNodeMap nnMap = node.getAttributes();
        String eventId = nnMap.getNamedItem(ID).getNodeValue();
        numEventsInSource++;
        // now for specifics get the mentions.
        NodeList nl = ((Element) node).getElementsByTagName(EVENT_MENTION);

        boolean isEventGenerated = false;
        for (int emIndex = 0; emIndex < nl.getLength(); ++emIndex) {
            Node eventMentionNode = nl.item(emIndex);
            numEventMentionsInSource++;
            NamedNodeMap mentionNodeMap = eventMentionNode.getAttributes();
            String eventMentionId = mentionNodeMap.getNamedItem(ID).getNodeValue();
            String type = mentionNodeMap.getNamedItem(TYPE).getNodeValue();
            String subtype = mentionNodeMap.getNamedItem(SUBTYPE).getNodeValue();
            String realis = mentionNodeMap.getNamedItem(REALIS).getNodeValue();
            String ways = UNSPECIFIED;

            if (mentionNodeMap.getNamedItem(WAYS) != null)
                ways = mentionNodeMap.getNamedItem(WAYS).getNodeValue();

            Constituent trigger = createTrigger(eventMentionNode, xmlTa, eventId);
            if (null == trigger) {
                logger.warn("No constituent generated for trigger '" + eventId);
                continue;
            }

            trigger.addAttribute(ID, eventId);
            trigger.addAttribute(EventMentionIdAttribute, eventMentionId);
            trigger.addAttribute(TYPE, type);
            trigger.addAttribute(SUBTYPE, subtype);
            trigger.addAttribute(REALIS, realis);
            trigger.addAttribute(WAYS, ways);

            eventView.addConstituent(trigger);
            numEventMentionsGenerated++;
            isEventGenerated = true;

            Pair<List<Pair<String, String>>, List<Constituent>> arguments = getArguments(eventMentionNode);
            List<Pair<String, String>> roleRealisList = arguments.getFirst();
            double[] scores = new double[roleRealisList.size()];
            String[] roleNames = new String[roleRealisList.size()];

            if (scores.length > 0) {
                isEventGenerated = true;
                for (int argIndex = 0; argIndex < scores.length; ++argIndex) {
                    scores[argIndex] = 1;
                    roleNames[argIndex] = roleRealisList.get(argIndex).getFirst();
                    Constituent arg = arguments.getSecond().get(argIndex);
                    Relation role = new Relation(roleNames[argIndex], trigger, arg, 1.0);
                    eventView.addConstituent(arg);
                    eventView.addRelation(role);
                }

            }
        }
        if (isEventGenerated)
            numEventsGenerated++;
    }


    /**
     * expect arguments to be mention nodes already created by {@link ERENerReader}
     * @param eventMentionNode
     * @return
     */
    private Pair<List<Pair<String, String>>, List<Constituent>> getArguments(Node eventMentionNode) {

    /*
        <em_arg entity_id="ent-m.0d04z6" entity_mention_id="m-56bd16d7_2_75" role="giver" realis="true">cuba</em_arg>
        <em_arg entity_id="ent-m.09c7w0" entity_mention_id="m-56bd16d7_2_135" role="recipient" realis="true">US</em_arg>
     */
        NodeList nl = ((Element) eventMentionNode).getElementsByTagName(EVENT_ARGUMENT);

        ArrayList<Pair<String, String>> rolesAndRealis = new ArrayList<>();
        ArrayList<Constituent> arguments = new ArrayList<>();

        for (int argIndex = 0; argIndex < nl.getLength(); ++argIndex) {
            numEventRolesInSource++;
            Node argNode = nl.item(argIndex);
            NamedNodeMap nnMap = argNode.getAttributes();

            Node att = nnMap.getNamedItem(ENTITY_MENTION_ID);
            if (null == att)
                att = nnMap.getNamedItem(FILLER_ID);

            String entityMentionId = att.getNodeValue();

                    Constituent ac = getMentionConstituent(entityMentionId);
            if (null == ac)
                logger.error("Could not find mention Constituent for mentionId '{}'", entityMentionId);
            else {
                numEventRolesGenerated++;
                String role = nnMap.getNamedItem(ROLE).getNodeValue();
                String realis = nnMap.getNamedItem(REALIS).getNodeValue();
                Pair<String, String> rAndR = new Pair(role, realis);
                rolesAndRealis.add(rAndR);
                arguments.add(ac);
            }
        }

        return new Pair(rolesAndRealis, arguments);
    }


    private Constituent createTrigger(Node eventMentionNode, XmlTextAnnotation xmlTa, String eventId) throws XMLException {
        /*  <trigger source="ENG_DF_001241_20150407_F0000007T" offset="179" length="5">trade</trigger> */

        NodeList nl = ((Element) eventMentionNode).getElementsByTagName(TRIGGER);
        Constituent trigger = null;

        if (nl.getLength() == 0)
            throw new IllegalStateException("Event " + eventId + " has no trigger element.");

        if (nl.getLength() > 1)
            throw new IllegalStateException("Event " + eventId + " has multiple trigger elements.");

        for (int i = 0; i < nl.getLength(); ++i) {
            Node eventTriggerNode = nl.item(i);
            String triggerForm = SimpleXMLParser.getContentString((Element) eventTriggerNode);

            NamedNodeMap nnMap = eventTriggerNode.getAttributes();
            String source = nnMap.getNamedItem(SOURCE).getNodeValue();

            int offset = Integer.parseInt(nnMap.getNamedItem(OFFSET).getNodeValue());
            int length = Integer.parseInt(nnMap.getNamedItem(LENGTH).getNodeValue());
            IntPair offsets = getTokenOffsets(offset, offset + length, triggerForm, xmlTa);

            if (null == offsets)
                return null;
            else if (-1 == offsets.getFirst() && -1 == offsets.getSecond()) { // offsets correspond to deleted span
                return null; // handled by next layer up, which records the info separately
            }

            trigger =
                    new Constituent(TRIGGER, getEventViewName(), xmlTa.getTextAnnotation(),
                            offsets.getFirst(), offsets.getSecond() + 1);

            trigger.addAttribute(EventIdAttribute, eventId);
            trigger.addAttribute(SOURCE, source);
        }

        return trigger;
    }


    @Override
    public String generateReport() {

        StringBuilder bldr = new StringBuilder(super.generateReport());

        bldr.append("Number of events in source: ").append(numEventsInSource)
                .append(System.lineSeparator());
        bldr.append("Number of events generated: ").append(numEventsGenerated)
                .append(System.lineSeparator());
        bldr.append("Number of event mentions in source: ").append(numEventMentionsInSource)
                .append(System.lineSeparator());
        bldr.append("Number of event mentions generated: ").append(numEventMentionsGenerated)
                .append(System.lineSeparator());
        bldr.append("Number of event arguments in source: ").append(numEventRolesInSource)
                .append(System.lineSeparator());
        bldr.append("Number of event arguments generated: ").append(numEventRolesGenerated)
                .append(System.lineSeparator());

        return bldr.toString();
    }
}