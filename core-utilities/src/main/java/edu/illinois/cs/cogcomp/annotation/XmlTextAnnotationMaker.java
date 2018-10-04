/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.core.utilities.TextCleanerStringTransformation;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Instantiates a XmlTextAnnotation object from xml text. The xml is parsed into body text (which is further cleaned
 *    up as needed), and this cleaned text is used to create a TextAnnotation. Additional information is extracted
 *    from the xml source. The mapping between the xml source and the cleaned text (i.e. mapping between character
 *    offsets) is also derived.
 * The goal is to provide text that can be processed easily with an NLP pipeline without a lot of hacks to work around
 *    ill-formatted text. The annotations so produced can then be mapped to the offsets in the original xml text,
 *    and combined with supplementary information extracted from the xml markup.
 *
 * @author mssammon
 */
public class XmlTextAnnotationMaker {

    private static final Logger logger = LoggerFactory.getLogger(XmlTextAnnotationMaker.class);
    /**
     * tokenizes/sentence splits the cleaned text for further processing
     */
    private final TextAnnotationBuilder taBuilder;
    /**
     * parses the xml text into a cleaned text, records relevant xml markup
     */
    private final XmlDocumentProcessor xmlProcessor;


    /**
     * Specifies the behavior of the XmlTextAnnotationMaker: tokenization (via the TextAnnotationBuilder),
     *    which xml tags to use for body text and for retained attributes
     * @param taBuilder generates the sentence split and tokenized text for further processing
     * @param xmlProcessor responsible for parsing xml, extracting processable text and relevant markup info
     */
    public XmlTextAnnotationMaker(TextAnnotationBuilder taBuilder, XmlDocumentProcessor xmlProcessor) {
        this.taBuilder = taBuilder;
        this.xmlProcessor = xmlProcessor;
    }



    /**
     * A method for creating
     * {@link TextAnnotation} by
     * tokenizing the given text string.
     *
     * @param xmlText Raw xml text from corpus document
     * @param corpusId corpus identifier
     * @param docId text identifier
     * @return an XmlTextAnnotation with the cleaned text (StringTransformation), TextAnnotation for
     *          the cleaned text, and xml markup extracted from source
     */
    public XmlTextAnnotation createTextAnnotation(String xmlText, String corpusId, String docId)  {
    	logger.debug("processing text from document {}", docId);
        Pair<StringTransformation, List<XmlDocumentProcessor.SpanInfo>> cleanResults =
                xmlProcessor.processXml(xmlText);

        TextAnnotation ta = taBuilder.createTextAnnotation(corpusId, docId,
                cleanResults.getFirst().getTransformedText());

        return new XmlTextAnnotation(cleanResults.getFirst(), ta, cleanResults.getSecond());
    }

}
