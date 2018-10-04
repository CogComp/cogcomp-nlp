/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;

/**
 * @author Vivek Srikumar
 *         <p>
 *         Jun 24, 2009
 */
public class XMLUtils {
    public static Document getXMLDOM(String currentFile) throws ParserConfigurationException,
            SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(currentFile);
    }

    public static Element getXMLDocumentElement(String file) throws ParserConfigurationException,
            SAXException, IOException {
        return getXMLDOM(file).getDocumentElement();
    }

    public static Document getXMLDOM(URL url) throws ParserConfigurationException, SAXException,
            IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        DocumentBuilder db = dbf.newDocumentBuilder();

        return db.parse(url.openStream());
    }

    public static Element getXMLDocumentElement(URL file) throws ParserConfigurationException,
            SAXException, IOException {
        return getXMLDOM(file).getDocumentElement();
    }
}
