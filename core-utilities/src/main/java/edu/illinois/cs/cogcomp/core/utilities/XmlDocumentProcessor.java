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

import java.nio.charset.Charset;
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
 * TODO: handle lone open/close tags: ideally, delete them, but at least transform them to avoid angle bracket
 *       parse problems in NLP components
 */

public class XmlDocumentProcessor {

    /**
     * tag to indicate that the associated information denotes a span in source xml with the associated label.
     */
    public static final String SPAN_INFO = "SPAN_INFO";
    /** list of tags marking 'body text' */
    private final Set<String> deletableSpanTags;
    /** list of tags containing attributes whose values' offsets we need */
    private final Map<String, Set<String>> tagsWithAtts;
    /** list of tags that might appear as singletons -- e.g. just an open tag, such as <img ...> */
    private final Set<String> singletonTags;
    /** what to do if there's some markup-like span outside the things we already listed */
    private final boolean throwExceptionOnUnrecognizedTag;


    /**
     * specify tags that determine processor behavior.
     * @param deletableSpanTags the names of tags containing text to be excluded from clean text (e.g. quotes)
     * @param tagsWithAtts the names of tags containing the attributes to retain, paired with sets of attribute names
     *                     MUST BE LOWERCASE.
     * @param singletonTags the names of one-off tags (i.e. no close tag) whose contents must be skipped entirely
     */
    public XmlDocumentProcessor(Set<String> deletableSpanTags, Map<String, Set<String>> tagsWithAtts,
                                Set<String> singletonTags, boolean throwExceptionOnUnrecognizedTag) {
        this.deletableSpanTags = deletableSpanTags;
        this.tagsWithAtts = tagsWithAtts;
        this.singletonTags = singletonTags;
        this.throwExceptionOnUnrecognizedTag = throwExceptionOnUnrecognizedTag;
    }

    /**
     * This class removes XML markup, for the most part. For specified tags that denote spans of text other than
     *    body text (e.g. quotes, headlines), the text value and offsets are reported. For specified tags and attributes,
     *    the attribute values and their offsets are reported. Content within <code>quote</code>
     * tags is left in place (though quote tags are removed) and the offsets are reported with the
     * other specified attributes.
     * This class has some facility for handling nested tags.  Opens without closes are checked against
     *    tags to ignore (provided at construction) and if found are ignored (deleted). Otherwise, an exception
     *    is thrown.
     * @param xmlText StringTransformation whose basis is the original xml text.
     * @return String comprising text.
     */
    public Pair<StringTransformation, Map<IntPair, Map<String, String>>> processXml(String xmlText) {

        StringTransformation xmlTextSt = new StringTransformation(xmlText);

        xmlTextSt = StringTransformationCleanup.normalizeToEncoding(xmlTextSt, Charset.forName("UTF-8"));

        // there are embedded xml tags in body text. Unescape them so we can process them easily.
        xmlTextSt = replaceXmlEscapedChars(xmlTextSt);
        xmlTextSt.applyPendingEdits();

        // singletons can be nested in deletable spans, creating major headaches.
        xmlTextSt = deleteSingletons(xmlTextSt);

//        // there are some nested tags. If the nesting is simple, fix it. Otherwise, throw an exception.
//        xmlTextSt.flattenNestedTags(xmlTextSt);
//
        String xmlCurrentStr = xmlTextSt.getTransformedText();

        // don't call getTransformedText() or applyPendingEdits() in the body of the loop usinr xmlMatcher
        Matcher xmlMatcher = xmlTagPattern.matcher(xmlCurrentStr);

        Map<IntPair, Map<String, String>> attributesRetained = new HashMap<>();
        // track open/close tags, to record spans for later use (e.g. quoted blocks that aren't annotated)
        Stack<Pair<String, IntPair>> tagStack = new Stack<>();

//        // right now, this is just useful for debugging
//        Map<String, Integer> nestingLevels = new HashMap<>();

        // track whether or not a tag is nested within something marked for deletion
        int deletableNestingLevel = 0;

        // match mark-up: xml open or close tag
        while (xmlMatcher.find()) {
            String substr = xmlMatcher.group(0);
            boolean isClose = false;
            if (substr.charAt(1) == '/') {
                isClose = true;
            }
            else if (substr.endsWith("/>") || substr.startsWith("<?xml")) {
                xmlTextSt.transformString(xmlMatcher.start(0), xmlMatcher.end(0), ""); //this is an empty tag
                continue;
            }

            String lcsubstr = substr.toLowerCase();

            // get the tag name
            Matcher tagMatcher = xmlTagNamePattern.matcher(lcsubstr);

            if (tagMatcher.find()) {
                // identify the tag
                String tagName = tagMatcher.group(1);

                if (isClose) {
                    Pair<String, IntPair> openTag = tagStack.pop();
                    tagName = tagName.substring(1); // strip leading "/"

                    String openTagName = openTag.getFirst();

                    // check for lone tags (open without close or vice versa )
                    boolean isLoneClose = false;
                    while (!openTagName.equals(tagName) && !isLoneClose) {

                        if (throwExceptionOnUnrecognizedTag)
                            throw new IllegalStateException("Mismatched open and close tags. Expected '" + openTag +
                                    "', found '" + tagName + "'");
                        else {//someone used xml special chars in body text
                            logger.warn("WARNING: found close tag '{}' after open tag '{}', and (obviously) they don't match.",
                                    tagName, openTagName);
                            if (!tagStack.isEmpty()) { // if lone tag is a close tag, hope that the open stack is empty
                                openTag = tagStack.peek();
                                openTagName = openTag.getFirst();
                                if (!openTag.equals(tagName))
                                    isLoneClose = true;
                                else
                                    openTag = tagStack.pop(); //it matched, so we're good now
                            }
                            else { //unmatched lone close
                                isLoneClose = true;
                            }
                        }
                    }

                    if (isLoneClose) { //revert to previous state, and resume parsing
                        tagStack.push(openTag);
                    }
                    else {// now we have open tag and matching close tag; record span and label
                        IntPair startTagOffsets = openTag.getSecond();
                        int startTagStart = startTagOffsets.getFirst();
                        int startTagEnd = startTagOffsets.getSecond();
                        int endTagStart = xmlMatcher.start();
                        int endTagEnd = xmlMatcher.end();

                        Map<String, String> tagInfo = new HashMap<>();
                        tagInfo.put(SPAN_INFO, tagName);
                        attributesRetained.put(xmlTextSt.getOriginalOffsets(startTagEnd, endTagStart), tagInfo);

//                    int nestingLevel = nestingLevels.get(tagName) - 1;
//                    nestingLevels.put(tagName, nestingLevel);

                        boolean isDeletable = false;
                        if (deletableSpanTags.contains(tagName)) { // deletable span
                            isDeletable = true;
                            deletableNestingLevel--;
                        }
                    /*
                     * if we are within another deletable tag
                     *    DON'T DELETE or it will create problems.
                     * else
                     *    delete
                     * else we are NOT in deletable and NOT nested:
                     *    delete open and close tags.
                     */
                        if (deletableNestingLevel == 0) {
                            if (isDeletable)
                                xmlTextSt.transformString(startTagStart, endTagEnd, "");
                            else { // we should retain text between open and close, but delete the tags
                                xmlTextSt.transformString(startTagStart, startTagEnd, "");
                                xmlTextSt.transformString(endTagStart, endTagEnd, "");
                            }
                        }
                    }
                }
                else  { // tag must be open
                    tagStack.push(new Pair(tagName, new IntPair(xmlMatcher.start(), xmlMatcher.end())));
                    if (deletableSpanTags.contains(tagName))
                        deletableNestingLevel++;

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
                                IntPair attrValOffsets = xmlTextSt.getOriginalOffsets(attrValStart, attrValEnd);
                                Map<String, String> atts = attributesRetained.get(attrValOffsets);
                                if (null == atts) {
                                    atts = new HashMap<>();
                                    attributesRetained.put(attrValOffsets, atts);
                                }
                                atts.put(attrName, attrVal);
                            }
                        }
                    }
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

        xmlTextSt.applyPendingEdits();

        return new Pair(xmlTextSt, attributesRetained);
    }

    /**
     * delete all spans that correspond to singleton tags (i.e. self-contained span presented as open tag with
     *    attributes, but no corresponding close). Relies on user specifying these ahead of time.
     * @param xmlTextSt StringTransformation containing text to be searched.
     * @return StringTransformation with appropriate edits.
     */
    private StringTransformation deleteSingletons(StringTransformation xmlTextSt) {
        // don't call getTransformedText() or applyPendingEdits() in the body of the loop usinr xmlMatcher
        Matcher xmlMatcher = xmlTagPattern.matcher(xmlTextSt.getTransformedText());

        Map<IntPair, Map<String, String>> attributesRetained = new HashMap<>();

        // match mark-up: xml open or close tag
        while (xmlMatcher.find()) {

            String substr = xmlMatcher.group(0);

            if (substr.charAt(1) == '/') {
                continue; //irrelevant to singletons by definition
            }

            String lcsubstr = substr.toLowerCase();
            // get the tag name
            Matcher tagMatcher = xmlTagNamePattern.matcher(lcsubstr);

            if (tagMatcher.find()) {
                // identify the tag
                String tagName = tagMatcher.group(1);

                if (singletonTags.contains(tagName)) {
                    xmlTextSt.transformString(xmlMatcher.start(), xmlMatcher.end(), "");
                }
            }
        }
        xmlTextSt.applyPendingEdits();
        return xmlTextSt;
    }

}
