/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.mascReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

/**
 * Read MASC Penn (pos, lemma) file in xml format.
 * Relies on entries in the segmentation (seg) file.
 *
 * <node xml:id="penn-n1001">
 * <link targets="seg-r1893"/>
 * </node>
 * <a xml:id="penn-N65711" label="tok" ref="penn-n1001" as="anc">
 * <fs>
 * <f name="string" value="paid"/>
 * <f name="msd" value="VBN"/>
 * <f name="base" value="pay"/>
 * </fs>
 * </a>
 *
 * @author mssammon
 */
public class PennStaxParser {

    public class PosLemma {
        private final String pos;
        private final String lemma;
        private final String pennId;
        private final String as;

        public PosLemma(String pos, String lemma, String pennId, String as) {
            this.pos = pos;
            this.lemma = lemma;
            this.pennId = pennId;
            this.as = as;
        }
    }

    static final String A = "a";
    static final String ID = "id";
    static final String LINK = "link";
    static final String F = "f";
    static final String BASE = "base";
    static final String TARGETS = "targets";
    static final String MSD = "msd";
    static final String REF = "ref";
    static final String NODE = "node";
    static final String LABEL = "label";
    static final String AS = "as";
    static final String NAME = "name";
    static final String VALUE = "value";
    static final String TOK = "tok";


    @SuppressWarnings({ "unchecked", "null" })
    public Map<String, PosLemma> parseFile(String file) throws IllegalFormatException, FileNotFoundException, XMLStreamException {

        Map<String, PosLemma> segIdToSegPosAndLemmas = new HashMap<>();
        Map<String, String> nodeIdToSegId = new HashMap<>();
//        int numItems = 0;
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        InputStream in = new FileInputStream(file);
        XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

        String nodeId = null; // will see this as attribute before link event
        String pennId = null;
        String nodeRef = null;
        String as = null;
        String pos = null;
        String lemma = null;

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();

            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                // a node specifies a corresponding token in the seg file (which provides char offsets)
                /* <node xml:id="penn-n1001">
                 * <link targets="seg-r1893"/>
                 * </node>
                 */
                if (startElement.getName().getLocalPart().equals(NODE)) {

                    Iterator<Attribute> attributes = startElement.getAttributes();

                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        String attName = attribute.getName().getLocalPart();
                        if (attName.equals(ID)) {
                            nodeId = attribute.getValue();
                        }
                    }
                }
                // <link targets="seg-r1893"/>
                else if (startElement.getName().getLocalPart().equals(LINK)) {

                    Iterator<Attribute> attributes = startElement.getAttributes();

                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        String attName = attribute.getName().getLocalPart();
                        if (attName.equals(TARGETS)) {
                            List<String> targets = Arrays.asList(attribute.getValue().split(" "));
                            if (targets.size() > 1)
                                throw new IllegalStateException("ERROR: found >1 target for penn node. Node id: '" +
                                        nodeId + "'.");
                            String segId = targets.get(0);
                            nodeIdToSegId.put(nodeId, segId);
                        }
                    }
                }// <a xml:id="penn-N65711" label="tok" ref="penn-n1001" as="anc">
                else if (startElement.getName().getLocalPart().equals(A)) {

                    Iterator<Attribute> attributes = startElement.getAttributes();

                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        String attName = attribute.getName().getLocalPart();
                        if (attName.equals(ID))
                            pennId = attribute.getValue();
                        else if (attName.equals(LABEL)) {
                            String label = attribute.getValue();
                            if (!label.equals(TOK))
                                throw new IllegalStateException("penn element has label other than " + TOK + ": " + label);
                        }
                        else if (attName.equals(REF))
                            nodeRef = attribute.getValue();
                        else if (attName.equals(AS))
                            as = attribute.getValue();
                    }
                }
                //  <fs>
                //  <f name="string" value="paid"/>
                //  <f name="msd" value="VBN"/>
                //  <f name="base" value="pay"/>
                //  </fs>
                else if (startElement.getName().getLocalPart().equals(F)) {

                    Iterator<Attribute> attributes = startElement.getAttributes();
                    String name = "";

                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        String attName = attribute.getName().getLocalPart();
                        if (attName.equals(NAME))
                            name = attribute.getValue();
                        else if (attName.equals(VALUE)) {
                            String value = attribute.getValue();
                            if (name.equals(MSD))
                                pos = value;
                            else if (name.equals(BASE))
                                lemma = value;
                        }
                    }
                }
            }
            else if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();

                if (endElement.getName().getLocalPart().equals(A)) {
                    PosLemma posLemma = new PosLemma(pos, lemma, pennId, as);
                    String segId = nodeIdToSegId.get(nodeRef);
                    segIdToSegPosAndLemmas.put(segId, posLemma);
                }
            }
        }
        return segIdToSegPosAndLemmas;
    }

}