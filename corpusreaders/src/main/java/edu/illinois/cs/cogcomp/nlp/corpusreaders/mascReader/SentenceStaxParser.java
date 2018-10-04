/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.mascReader;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

/**
 * Read MASC sentence (s) file in xml format.
 *
 *  <a xml:id="s-N65573" label="s" ref="s-n0" as="anc"/>
 *  <region xml:id="s-r1" anchors="140 161"/>
 *  <node xml:id="s-n1">
 *     <link targets="s-r1"/>
 *  </node>
 *  <a xml:id="s-N65589" label="q" ref="s-n1" as="anc"/>
 *  <region xml:id="s-r2" anchors="345 598"/>
 *  <node xml:id="s-n2">
 *     <link targets="s-r2"/>
 *  </node>
 *
 *  NOTE this annotation contains also "quotation" annotations (for direct quotations only?)
 *  and results in overlapping spans, though with different labels. The quote labels appear at the
 *  _a_ level, which refers to _node_s. For now, ignore quotes, and later add another layer of output.
 *
 * @author mssammon
 */
public class SentenceStaxParser {

    static final String REGION = "region";
    static final String ID = "id";
    static final String ANCHORS = "anchors";
    static final String TARGETS = "targets";
    static final String NODE = "node";
    static final String A = "a";
    static final String REF = "ref";
    static final String LINK = "link";
    static final String LABEL = "label";
    static final String AS = "as";

    public enum SentenceType {QUOTE, HEADER, STATEMENT};

    public class MascSentence {
        public final int start;
        public final int end;
        public final String regionId;


        public MascSentence(int start, int end, String regionId) {
            this.start = start;
            this.end = end;
            this.regionId = regionId;
        }
    }


    public class MascSentenceGroup {
        public final String id;
        public final String aId;
        public final String type;
        public final String as;
        public final Set<String> memberIds;

        public MascSentenceGroup(String id, String aId, String type, String as, Set<String> memberIds) {
            this.id = id;
            this.aId = aId;
            this.type = type;
            this.as = as;
            this.memberIds = memberIds;
        }
    }


    @SuppressWarnings({ "unchecked", "null" })
    public Pair<List<MascSentence>, List<MascSentenceGroup>> parseFile(String file) throws IllegalFormatException, FileNotFoundException, XMLStreamException {

        // map id to sentence
        Map<String, MascSentence> idToSentence = new HashMap<>();
        // map id to type (quote or stmt)
        Map<String, SentenceType> nodeIdToSentenceTypes = new HashMap<>();
        // map node id to sentence ids
        Map<String, Set<String>> nodeIdToSentenceId = new HashMap<>();

        List<MascSentenceGroup> sentenceGroups = new ArrayList<>();

        int numItems = 0;
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        InputStream in = new FileInputStream(file);
        XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

        String nodeId = null; // will see this as attribute before link event

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();

            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                // a region defines a sentence
                if (startElement.getName().getLocalPart().equals(REGION)) {

                    int start = 0;
                    int end = 0;
                    String id = null;
                    Iterator<Attribute> attributes = startElement.getAttributes();

                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        String attName = attribute.getName().getLocalPart();
                        if (attName.equals(ANCHORS)) {
                            String[] offsetStrs = attribute.getValue().split(" ");
                            if (offsetStrs.length != 2)
                                throw new IllegalStateException("Offset string expected two parts. Found '" +
                                        attribute.getValue() + "'.");

                            start = Integer.parseInt(offsetStrs[0]);
                            end = Integer.parseInt(offsetStrs[1]);
                        }
                        if (attName.equals(ID)) {
                            id = attribute.getValue();
                        }
                    }

                    if (null == id || (0 == end)) {
                        throw new IllegalStateException("read start of element '" + REGION + "' (item " +
                                numItems + " in file), but did not " +
                                "successfully read label or offsets (or both). File is '" + file + "'.");
                    }
                    idToSentence.put(id, new MascSentence(start, end, id));
                    numItems++;
                }
                //  <node xml:id="s-n1">
                else if (startElement.getName().getLocalPart().equals(NODE)) {

                    Iterator<Attribute> attributes = startElement.getAttributes();

                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        String attName = attribute.getName().getLocalPart();
                        if (attName.equals(ID))
                            nodeId = attribute.getValue();
                    }

                }
                // <link targets="s-r1"/>
                else if (startElement.getName().getLocalPart().equals(LINK)) {
                    Iterator<Attribute> attributes = startElement.getAttributes();

                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        String attName = attribute.getName().getLocalPart();
                        if (attName.equals(TARGETS)) {
                            Set<String> targets = new HashSet<>(Arrays.asList(attribute.getValue().split(" ")));
                            nodeIdToSentenceId.put(nodeId, targets);
                        }
                    }
                }
                //<a xml:id="s-N65589" label="q" ref="s-n1" as="anc"/>
                else if (startElement.getName().getLocalPart().equals(A)) {
                    Iterator<Attribute> attributes = startElement.getAttributes();

                    String aId = null;
                    String label = null;
                    String ref = null;
                    String as = null;

                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        String attName = attribute.getName().getLocalPart();
                        if (attName.equals(ID))
                            aId = attribute.getValue();
                        else if (attName.equals(LABEL))
                            label = attribute.getValue();
                        else if (attName.equals(REF))
                            ref = attribute.getValue();
                        else if (attName.equals(AS))
                            as = attribute.getValue();
                    }

                    SentenceType typeLabel = SentenceType.STATEMENT;
                    if ("q".equalsIgnoreCase(label))
                        typeLabel = SentenceType.QUOTE;
                    else if ("s".equalsIgnoreCase(label))
                        typeLabel = SentenceType.STATEMENT;
                    else if ("head".equalsIgnoreCase(label))
                        typeLabel = SentenceType.HEADER;
                    else
                        throw new IllegalArgumentException("Found unrecognized symbol '" + label + "' for sentence type.");

                    sentenceGroups.add(new MascSentenceGroup(aId, label, ref, as, nodeIdToSentenceId.get(ref)));
                    nodeIdToSentenceTypes.put(ref, typeLabel);
                }
            }
        }
        TreeSet<MascSentence> sentences = new TreeSet<>(new Comparator<MascSentence>() {
            @Override
            public int compare(MascSentence first, MascSentence second) {
                if (first.start > second.start)
                    return 1;
                else if (first.start < second.start)
                    return -1;
                else if (first.end > second.end)
                    return 1;
                else if (first.end < second.end)
                    return -1;
                return 0;
            }
        });


        for(String localNodeId : nodeIdToSentenceTypes.keySet()) {
            SentenceType t = nodeIdToSentenceTypes.get(localNodeId);

            if (!t.equals(SentenceType.QUOTE)) {
                Set<String> nodeSentIds = nodeIdToSentenceId.get(localNodeId);
                for (String id : nodeSentIds)
                    sentences.add(idToSentence.get(id));
            }
        }

        return new Pair(new ArrayList<>(sentences), sentenceGroups);
    }

}