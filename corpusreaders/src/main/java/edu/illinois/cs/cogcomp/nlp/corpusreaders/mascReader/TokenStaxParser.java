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
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;

/**
 * Read MASC tokenization (seg) file in xml format.
 *
 *     <region xml:id="seg-r0" anchors="4 10"/>
 *     <region xml:id="seg-r2" anchors="11 17"/>

 * @author mssammon
 */
public class TokenStaxParser {

    static final String REGION = "region";
    static final String ID = "id";
    static final String ANCHORS = "anchors";


    @SuppressWarnings({ "unchecked", "null" })
    public List<Pair<String, IntPair>> parseFile(String file) throws IllegalFormatException, FileNotFoundException, XMLStreamException {
        List<Pair<String, IntPair>> items = new ArrayList<>();

        int numItems = 0;
        // First, create a new XMLInputFactory
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        // Setup a new eventReader
        InputStream in = new FileInputStream(file);
        XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
        // read the XML document
        IntPair item = null;

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();

            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                // If we have an item element, we create a new item
                if (startElement.getName().getLocalPart().equals(REGION)) {

                    IntPair offsets = null;
                    String label = null;
                    Iterator<Attribute> attributes = startElement
                            .getAttributes();
                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        String attName = attribute.getName().getLocalPart();
                        if (attName.equals(ANCHORS)) {
                            String[] offsetStrs = attribute.getValue().split(" ");
                            if (offsetStrs.length != 2)
                                throw new IllegalStateException("Offset string expected two parts. Found '" +
                                        attribute.getValue() + "'.");

                            offsets = new IntPair(Integer.parseInt(offsetStrs[0]), Integer.parseInt(offsetStrs[1]));
                        }
                        else if (attName.equals(ID)) {
                            label = attribute.getValue();
                        }
                    }

                    if (null == label || null == offsets) {
                        throw new IllegalStateException("read start of element '" + REGION + "' (item " +
                                numItems + " in file), but did not " +
                                "successfully read label or offsets (or both). File is '" + file + "'.");
                    }
                    items.add(new Pair(label, offsets));
                    numItems++;
                }

            }

        }
        return items;
    }

}