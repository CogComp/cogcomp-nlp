/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.annotation.XmlTextAnnotationMaker;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represent a document that contains xml markup. Another class -- {@link XmlTextAnnotationMaker}, extracts a subset
 *    of the xml source text that will be processed by NLP components and creates a TextAnnotation from it.
 *    It also extracts xml markup that contains relevant information for use by applications. Examples of possible
 *    text fields are "<body>" and "<headline>"; possible supplementary info could be the "author" attribute
 *    in a tag such as "<post author=\"marfa\">". Finally, it provides a StringTransformation that maps between
 *    the xml source and the cleaned NLP-processable text. These elements comprise the information for a
 *    {@link XmlTextAnnotation} object.
 *
 * @author mssammon
 */
public class XmlTextAnnotation {

    private final StringTransformation xmlSt;
    private final TextAnnotation textAnnotation;
    private final List<XmlDocumentProcessor.SpanInfo> xmlMarkup;

    public XmlTextAnnotation(StringTransformation xmlSt, TextAnnotation ta, List<XmlDocumentProcessor.SpanInfo> xmlMarkup) {
        this.xmlSt = xmlSt;
        this.textAnnotation = ta;
        this.xmlMarkup = xmlMarkup;
    }

    public StringTransformation getXmlSt() {
        return xmlSt;
    }

    public TextAnnotation getTextAnnotation() {
        return textAnnotation;
    }

    public List<XmlDocumentProcessor.SpanInfo> getXmlMarkup() {
        return xmlMarkup;
    }

}
