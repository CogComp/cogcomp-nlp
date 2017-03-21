/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;

import java.util.*;
import java.util.regex.Matcher;

import static edu.illinois.cs.cogcomp.core.utilities.TextCleanerStringTransformation.*;

/**
 * Reads an xml document from a corpus, extracts some fields as the document's clean NLP-processable text,
 *    and others as metadata.
 * Provides a mapping from clean text character offsets to character offsets in the source xml.
 * Metadata includes relevant character offsets.
 * Some escaped xml elements are embedded in the document body text. These are made legible by substituting the
 *    escaped characters in an initial step.
 *
 * TODO: add constructor field to allow additional text clean/transform ops
 */

public class XmlDocumentProcessor {

    /**
     * tag to indicate that the associated information denotes a span in source xml with the associated label.
     */
    public static final String SPAN_INFO = "SPAN_INFO";
    private final Set<String> tagsWithText;
    private final Map<String, Set<String>> tagsWithAtts;
    private final Set<String> tagsToIgnore;

    /**
     * specify tags that determine processor behavior.
     * @param tagsWithText the names of tags containing text other than body text (e.g. headlines, quotes)
     * @param tagsWithAtts the names of tags containing the attributes to retain, paired with sets of attribute names
     *                     MUST BE LOWERCASE.
     * @param tagsToIgnore the names of tags whose contents must be skipped entirely
     */
    public XmlDocumentProcessor(Set<String> tagsWithText, Map<String, Set<String>> tagsWithAtts,
                                Set<String> tagsToIgnore) {
        this.tagsWithText = tagsWithText;
        this.tagsWithAtts = tagsWithAtts;
        this.tagsToIgnore = tagsToIgnore;
    }

    /**
     * This class removes XML markup, for the most part. For specified tags that denote spans of text other than
     *    body text (e.g. quotes, headlines), the text value and offsets are reported. For specified tags and attributes,
     *    the attribute values and their offsets are reported. Content within <code>quote</code>
     * tags is left in place (though quote tags are removed) and the offsets are reported with the
     * other specified attributes.
     * Pretty sure this doesn't handle nested tags.
     * @param xmlText StringTransformation whose basis is the original xml text.
     * @return String comprising text.
     */
    public Pair<StringTransformation, Map<IntPair, Map<String, String>>> processXml(String xmlText) {


        Matcher xmlMatcher = xmlTagPattern.matcher(xmlText);
        StringTransformation xmlTextSt = new StringTransformation(xmlText);
        Map<IntPair, Map<String, String>> attributesRetained = new HashMap<>();
        // track open/close tags, to record spans for later use (e.g. quoted blocks that aren't annotated)
        Stack<Pair<String, Integer>> tagStack = new Stack<>();
        // match mark-up: xml open or close tag
        while (xmlMatcher.find()) {
            String substr = xmlMatcher.group(0);
            boolean isClose = false;
            if (substr.charAt(1) == '/') {
                xmlTextSt.transformString(xmlMatcher.start(0), xmlMatcher.end(0), ""); //this is an end tag
                isClose = true;
            }
            else if (substr.endsWith("/>"))
                continue; // empty tag

            int tagStart = xmlMatcher.start();
            int tagEnd = xmlMatcher.end();
            String lcsubstr = substr.toLowerCase();

            // get the tag name
            Matcher tagMatcher = xmlTagNamePattern.matcher(lcsubstr);
            if (tagMatcher.find()) {
                // identify the tag and its corresponding close tag
                String tagName = tagMatcher.group(1);

                if (isClose) {
                    Pair<String, Integer> openTag = tagStack.pop();
                    tagName = tagName.substring(1); // strip leading "/"
                    if (!openTag.getFirst().equals(tagName))
                        throw new IllegalStateException("Mismatched open and close tags. Expected '" + openTag +
                                "', found '" + tagName + "'");
                    // now we have open tag and matching close tag; record span and label
                    int start = openTag.getSecond();
                    int end = xmlMatcher.start();
                    Map<String, String> tagInfo = new HashMap<>();
                    tagInfo.put(SPAN_INFO, tagName);
                    attributesRetained.put(new IntPair(start, end), tagInfo);
                    continue;
                }
                // tag must be open
                int end = xmlMatcher.end();
                tagStack.push(new Pair(tagName, end));
                // within an xml open tag: identify any attribute values we need to retain.
                if (tagsWithAtts.containsKey(tagName)) {
                    Set<String> attributeNames = tagsWithAtts.get(tagName);
                    // parse the substring beyond the tag name.
                    lcsubstr = lcsubstr.substring(tagMatcher.end());
                    substr = substr.substring(tagMatcher.end());

                    Matcher attrMatcher = tagAttributePattern.matcher(lcsubstr);
                    while (attrMatcher.find()) {
                        String attrName = attrMatcher.group(1);
                        // avoid lowercasing attribute values
                        String attrVal = substr.substring(attrMatcher.start(2), attrMatcher.end(2)); //attrMatcher.group(2);
                        if (attributeNames.contains(attrName)) {
                            // substring starts at index of start of (open) xml tag + length of tag name + left angle bracket
                            // note that we are using a transformed text, so need original offsets
                            int attrValOffset = tagMatcher.end() + xmlMatcher.start();
                            int attrValStart = attrMatcher.start(2) + attrValOffset;
                            int attrValEnd = attrMatcher.end(2) + attrValOffset;
                            IntPair attrValOffsets = new IntPair(attrValStart, attrValEnd);
                            Map<String, String> atts = attributesRetained.get(attrValOffsets);
                            if (null == atts) {
                                atts = new HashMap<>();
                                attributesRetained.put(attrValOffsets, atts);
                            }
                            atts.put(attrName, attrVal);
                        }
                    }
                }

                // if we should retain text between open and close, do so
                if (tagsWithText.contains(tagName)) {

                    // FIXME: this uses original string. If this method is called on a transformed string, it may introduce errors.
                    // FIXME: use getOriginalOffsets() from stringtransformation.
                    int endStart = xmlText.indexOf("</" +tagName +">", tagEnd);
                    if (endStart == -1)
                        throw new IllegalArgumentException("No matching end tag for '" + tagName + "'");
                    //delete the open tag, leave the text (main loop will catch and delete close tag
                    xmlTextSt.transformString(tagStart, tagEnd, "");
                }
                else if (tagsToIgnore.contains(tagName)) { // need to delete content, though span was recorded
                    int endStart = xmlText.indexOf("</" +tagName +">", tagEnd);
                    // an earlier check deletes close tags, so only delete up to close
                    xmlTextSt.transformString(tagStart, endStart, "");
                }
                else { // just delete the tag.
                    xmlTextSt.transformString(xmlMatcher.start(0), xmlMatcher.end(0), ""); //this is an end tag
                }
            }
        }
        // cleanup double newlines/spaces
        String postProcessedText = xmlTextSt.getTransformedText(); // updates StringTransformation current str
        Matcher wsMatcher = TextCleanerStringTransformation.whitespacePattern.matcher(postProcessedText);

        while (wsMatcher.find()) {
            int start = wsMatcher.start();
            int end = wsMatcher.end();

            if (start == 0) // leading whitespace
                xmlTextSt.transformString(start, end, "");
            else if (end - start > 1 ) { // multiple whitespace
                String replacement = wsMatcher.group().substring(0, 1);
                if (wsMatcher.group().indexOf('\n') > 0) //contains newline somewhere -- retain one newline
                    replacement = System.lineSeparator();

                xmlTextSt.transformString(start, end, replacement);
            }
        }

        // there are embedded xml tags in body text. Unescape them so we can process them easily.
        xmlTextSt = replaceXmlEscapedChars(xmlTextSt);
        xmlTextSt.applyPendingEdits();

        return new Pair(xmlTextSt, attributesRetained);
    }

}
