package edu.illinois.cs.cogcomp.corpusreaders.aceReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * @author Eric Bengtson
 *
 */
public class SimpleXMLParser {
    /**
     * @param filename
     *                The file to parse
     * @return The {@code Document} XML tag -- the root of the
     *         document.
     * @throws edu.illinois.cs.cogcomp.corpusreaders.aceReader.XMLException
     */
    public static Document getDocument(String filename) throws XMLException {
        return getDocument(new File(filename));
    }

    /**
     * @param file
     *                The file to parse.
     * @return The {@code Document} XML tag -- the root of the
     *         document.
     * @throws XMLException
     */
    public static Document getDocument(File file) throws XMLException {
        DocumentBuilder docBuilder = getDocumentBuilder();
        return getDocument(file, docBuilder);
    }

    /**
     * @param parent
     *                The node containing the desired element tag
     * @param tagName
     *                The name of the tag
     * @return the first target element inside the parent Node.
     * @throws XMLException
     */
    public static Element getElement(Element parent, String tagName)
            throws XMLException {
        NodeList nl = parent.getElementsByTagName(tagName);
        if (nl.getLength() <= 0)
            throw new XMLException("Element Not Found");
        return (Element) nl.item(0);
    }

    /**
     * @param tag
     *                (start) tag that encloses desired text.
     * @return The contents of the tag.
     * @throws XMLException If the tag has no children.
     */
    public static String getContentString(Element tag) throws XMLException {
        if (!tag.hasChildNodes())
            throw new XMLException("tag " + tag.getNodeName() + " empty");
        return tag.getFirstChild().getNodeValue();
    }

    /**
     * @param parent
     *                A node containing the tag enclosing desired text.
     * @param tagName
     *                Name of tag containing desired text.
     * @return The desired text.
     * @throws XMLException
     */
    public static String getTagContent(Element parent, String tagName)
            throws XMLException {
        return getContentString(getElement(parent, tagName));
    }

    /**
     * @param parent
     *                A node containing the tag enclosing desired text.
     * @param tagName
     *                Name of tag containing desired text.
     * @return The desired text, or if tag not found, defaultResult.
     */
    public static String getTagContent(Element parent, String tagName,
                                       String defaultResult) {
        NodeList nl = parent.getElementsByTagName(tagName);
        if (nl.getLength() <= 0)
            return defaultResult;
        Element tag = (Element) nl.item(0);
        if (!tag.hasChildNodes())
            return defaultResult;
        return tag.getFirstChild().getNodeValue();
    }

    /** ** UTILITY METHODS *** */

    private static DocumentBuilder getDocumentBuilder() throws XMLException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            return factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new XMLException("Misconfigured XML Parser", e);
        }
    }

    private static Document getDocument(File file,
                                        DocumentBuilder docBuilder)
            throws XMLException {
        try {
            return docBuilder.parse(file);
        } catch (SAXException e) {
            throw new XMLException("Parse Error Occurred", e);
        } catch (IOException e) {
            throw new XMLException("IO Error While Reading XML Document", e);
        }
    }

}
