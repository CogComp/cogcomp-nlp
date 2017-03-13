package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;

import java.util.Map;
import java.util.Set;

/**
 * Represent a document that contains xml markup. This creates challenges representing annotations for information
 *    in xml attributes, and it may be useful to identify the source tag for different elements' text -- such
 *    as article headlines, bylines, date of publication, or sub-headers.
 *
 * @author mssammon
 */
public class XmlTextAnnotation {

    private final StringTransformation xmlSt;
    private final TextAnnotation textAnnotation;
    private final Map<IntPair, Map<String, String>> xmlMarkup;

    public XmlTextAnnotation(StringTransformation xmlSt, TextAnnotation ta, Map<IntPair, Map<String, String>> xmlMarkup) {
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

    public Map<IntPair, Map<String, String>> getXmlMarkup() {
        return xmlMarkup;
    }

}
