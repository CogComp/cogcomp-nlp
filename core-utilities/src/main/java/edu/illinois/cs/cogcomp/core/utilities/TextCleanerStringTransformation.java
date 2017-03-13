/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import edu.illinois.cs.cogcomp.core.constants.CoreConfigNames;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class has methods for conversion of non-UTF-8 characters to UTF-8, and from UTF-8 to Ascii.
 * These attempt to preserve character offsets, and to make intuitive replacements (see the
 * StringTransformationCleanup class from coreUtilities).
 * <p>
 * Other methods try to remove problem character sequences from text to avoid known problems with
 * NLP components for such sequences, but don't preserve character offsets.
 *
 * This class, which replicates functionality from {@link TextCleaner}, uses StringTransformations instead
 * of Strings, and so allows recovery of character offsets in the original string after cleanup has taken
 * place, even if character offsets are not preserved.
 *
 * @author mssammon
 */

public class TextCleanerStringTransformation {
    private static final String NAME = TextCleanerStringTransformation.class.getCanonicalName();
    private static final boolean DEBUG = false;
    private static final int REGEX_TEXT_LIMIT = 10000;
    private static final String xmlQuot = "&quot;";
    private static final String xmlAmp = "&amp;";
    private static final String xmlApos = "&apos;";
    private static final String xmlLt = "&lt;";
    private static final String xmlGt = "&gt;";
    private static final Pattern xmlEscapeCharPattern = Pattern.compile(xmlQuot + "|" +xmlAmp + "|" + xmlApos + "|" +
        xmlLt + "|" + xmlGt);
    private static final Pattern underscorePattern = Pattern.compile("_");
    private static final Pattern controlSequencePattern = Pattern.compile("@\\\\^|\\\\^@|\\\\^");
    private static final Pattern atSymbolPattern = Pattern.compile("@ ");
    private static final Pattern badApostrophePattern = Pattern.compile("(\\S+)\"s(\\s+)");
    private static Logger logger = LoggerFactory.getLogger(TextCleanerStringTransformation.class);
    private static Pattern repeatPunctuationPattern = Pattern
            .compile("[\\p{P}\\*@<>=\\+#~_&\\p{P}]+");
    private static Pattern xmlTagPattern = Pattern.compile("(<[^>\\r\\n]+>\\n?)"); // NEWLINES ARE IMPORTANT!!
    /** used to extract the name of the tag, so we can match tag and attribute name. */
    private static Pattern xmlTagNamePattern = Pattern.compile("<([^\\s>]+)");
    private static Pattern whitespacePattern = Pattern.compile("\\s+");
    /** find attributes in an xml tag instance. Match whitespace then word. Group one is the
     * attribute name, group 2 is the quote mark used, three is the value. */
    private static Pattern tagAttributePatter2 = Pattern.compile("[\\s]+([^\\s>=]+)[\\s]*=[\\s\"']*([^\\s\"'>]+)");
    private static Pattern tagAttributePattern = Pattern.compile("[\\s]+([^\\s>=]+)[\\s]*=[\\s]*[\"']([^\"']+)");
    private static Pattern adhocFormatPattern = Pattern.compile("[\\*~\\^]+");
    private static Map<String, String> escXlmToChar = new HashMap<>();

    static {
        escXlmToChar.put(xmlQuot, "\"");
        escXlmToChar.put(xmlAmp, "&");
        escXlmToChar.put(xmlApos, "'");
        escXlmToChar.put(xmlLt, "<");
        escXlmToChar.put(xmlGt, ">");
    }

    private boolean removeRepeatPunctuation;
    private boolean replaceUnderscores;
    private boolean replaceControlSequence;
    private boolean replaceAdHocMarkup;
    private boolean replaceBogusApostrophe;


    public TextCleanerStringTransformation(ResourceManager rm_) throws SAXException, IOException {
        this.removeRepeatPunctuation = rm_.getBoolean(CoreConfigNames.REMOVE_REPEAT_PUNCTUATION);
        this.replaceAdHocMarkup = rm_.getBoolean(CoreConfigNames.REPLACE_ADHOC_MARKUP);
        this.replaceBogusApostrophe = rm_.getBoolean(CoreConfigNames.REPLACE_BAD_APOSTROPHE);
        this.replaceControlSequence = rm_.getBoolean(CoreConfigNames.REPLACE_CONTROL_SEQUENCE);
        this.replaceUnderscores = rm_.getBoolean(CoreConfigNames.REPLACE_UNDERSCORES);
    }

    /**
     * attempts to replace incorrect apostrophe symbol (after other substitutions for non-standard
     * quotation marks)
     */
    public static StringTransformation replaceMisusedApostropheSymbol(StringTransformation origTextSt) {
        String origStr = origTextSt.getTransformedText();
        Matcher matcher = badApostrophePattern.matcher(origStr);

        while (matcher.find()) {
            String precedingStr = matcher.group(1);
            String trailingStr = matcher.group(2);
            String replacement = precedingStr + "'s" + trailingStr;
            int start = matcher.start();
            int end = matcher.end();

            origTextSt.transformString(start, end, replacement);
        }

        return origTextSt;
    }

    public static String replaceXmlTags(String origText) {
        Matcher xmlMatcher = xmlTagPattern.matcher(origText);
        StringBuilder cleanTextBldr = new StringBuilder();
        int lastAppendedCharOffset = 0;

        while (xmlMatcher.find()) {
            int start = xmlMatcher.start();
            int end = xmlMatcher.end();
            cleanTextBldr.append(origText.substring(lastAppendedCharOffset, start));
            for (int i = start; i < end; ++i)
                cleanTextBldr.append(" ");
            lastAppendedCharOffset = end;
        }
        cleanTextBldr.append(origText.substring(lastAppendedCharOffset));

        return cleanTextBldr.toString();
    }

    /**
     * determine if we should keep the value for this tag/attr.
     * @param tagNames the names of the tags to keep.
     * @param attributeNames the corresponding attributes within the above tag we want to keep.
     * @param tagname the name of the current tag.
     * @param attrName the name of the current attrigute.
     * @return true if we keep the tag value.
     */
	private static boolean keep(List<String> tagNames, List<String> attributeNames, String tagname,
			String attrName) {
		for (int i = 0; i < tagNames.size(); i++) {
			if (tagNames.get(i).equals(tagname))
				if (attributeNames.get(i).equals(attrName))
					return true;
		}
		return false;
	}

    /**
     * This class removes XML markup, for the most part. For specified tags that denote spans of text other than
     *    body text (e.g. quotes, headlines), the text value and offsets are reported. For specified tags and attributes,
     *    the attribute values and their offsets are reported. Content within <code>quote</code>
     * tags is left in place (though quote tags are removed) and the offsets are reported with the
     * other specified attributes.
     * Pretty sure this doesn't handle nested tags.
     * @param xmlText the original xml text.
     * @param tagsWithText the names of tags containing text other than body text (e.g. headlines, quotes)
     * @param tagsWithAtts the names of tags containing the attributes to retain, paired with sets of attribute names
     *                     MUST BE LOWERCASE.
     * @return String comprising text.
     */
    public static Pair<StringTransformation, Map<IntPair, Map<String, String>>> cleanDiscussionForumXml(
            StringTransformation xmlText, Set<String> tagsWithText, Map<String, Set<String>>tagsWithAtts) {

        StringTransformation normalizedTextSt = replaceXmlEscapedChars(xmlText);
        String normalizedText = normalizedTextSt.getTransformedText();
        Matcher xmlMatcher = xmlTagPattern.matcher(normalizedText);
        Map<IntPair, Pair<String, String>> attributesRetained = new HashMap<>();

        // match mark-up: open tag
        while (xmlMatcher.find()) {
            String substr = xmlMatcher.group(0);
            if ( substr.charAt(1) == '/')
                continue; //this is an end tag

            int tagStart = xmlMatcher.start();
            int tagEnd = xmlMatcher.end();

            substr = substr.toLowerCase();
            // get the tag name
            Matcher tagMatcher = xmlTagNamePattern.matcher(substr);
            if (tagMatcher.find()) {
                // identify the tag and its corresponding close tag
                String tagname = tagMatcher.group(1);
                int endStart = normalizedText.indexOf("</" +tagname +">", tagEnd);
                if (endStart == -1)
                    throw new IllegalArgumentException("No matching end tag for '" + tagname + "'");
                int endEnd = endStart + tagname.length() + 3;
                //strip trailing whitespace
                Matcher wsMatcher = whitespacePattern.matcher(normalizedText.substring(endEnd));
                if (wsMatcher.find()) {
                    if (wsMatcher.start() == 0)
                        endEnd += wsMatcher.end();
                }

                // within an xml tag: identify any attribute values we need to retain.
                if (tagsWithAtts.containsKey(tagname)) {
                    Set<String> attributeNames = tagsWithAtts.get(tagname);
                    // parse the substring beyond the tag name.
                    substr = substr.substring(tagMatcher.end());
                    Matcher attrMatcher = tagAttributePattern.matcher(substr);
                    while (attrMatcher.find()) {
                        String attrName = attrMatcher.group(1);
                        String attrVal = attrMatcher.group(2);
                        if (attributeNames.contains(attrName)) {
                            // substring starts at index of start of (open) xml tag + length of tag name + left angle bracket
                            int attrValOffset = tagMatcher.end() + xmlMatcher.start() + 1;
                            int attrValStart = attrMatcher.start(2) + attrValOffset;
                            int attrValEnd = attrMatcher.end(2) + attrValOffset;
                            IntPair attrValOffsets = new IntPair(attrValStart, attrValEnd);
                            attributesRetained.put(attrValOffsets, new Pair(attrName, attrVal));
                        }
                    }
                }

                // if we should retain text between open and close, do so
                if (tagsWithText.contains(tagname)) {
                    //delete the tags, leave the text
                    normalizedTextSt.transformString(tagStart, tagEnd, "");
                    normalizedTextSt.transformString(endStart, endEnd, "");
                }
                else { // just delete the whole span.
                    normalizedTextSt.transformString(tagStart, endEnd, "");
                }
                if (DEBUG) {
                    System.err.println("Current string:\n" + normalizedTextSt.getTransformedText());
                }
            }
        }
        return new Pair(normalizedTextSt, attributesRetained);
    }

    /**
     * given an xml string, replace xml-escaped characters with their ascii equivalent, padded with whitespace.
     * @param xmlTextSt StringTransformation containing text string to be modified
     * @return StringTransformation passed in as arguments, with modifications
     */
    public static StringTransformation replaceXmlEscapedChars(StringTransformation xmlTextSt) {

        String xmlText = xmlTextSt.getTransformedText();
        Matcher xmlMatcher = xmlEscapeCharPattern.matcher(xmlText);

        // match mark-up
        while (xmlMatcher.find()) {
            String substr = xmlMatcher.group(0);
            int start = xmlMatcher.start();
            int end = xmlMatcher.end();

            xmlTextSt.transformString(start, end, getSubstStr(substr));
        }

        return xmlTextSt;
    }

    private static String getSubstStr(String substr) {
        return escXlmToChar.get(substr);
    }

    /**
     * apply a matcher that finds instances of one or more sequences and replaces them with
     *    a specific string (including the empty string)
     * @param origTextSt
     */
    public static StringTransformation replaceCharacters(StringTransformation origTextSt, Pattern pattern, String replacement) {

        Matcher matcher = pattern.matcher(origTextSt.getTransformedText());
        while (matcher.find()) {
            origTextSt.transformString(matcher.start(), matcher.end(), replacement);
        }
        return origTextSt;
    }

    /**
     * replaces duplicate punctuation with single punctuation
     * <p>
     * This sometimes happens due to a typo, or may be due to ad-hoc web formating -- in the latter
     * case, this may not have the ideal effect.
     * <p>
     * In addition, use of double dashes and ellipses may cause problems to NLP components; this
     * should help, though it may introduce extra sentence breaks in the case of ellipses.
     */
    public static StringTransformation replaceDuplicatePunctuation(StringTransformation origTextSt) {
        String startText = origTextSt.getTransformedText();
        Matcher matcher = TextCleanerStringTransformation.repeatPunctuationPattern.matcher(startText);

        int pos = 0;
        while (matcher.find(pos)) {
            pos = matcher.start();
            int end = matcher.end();
            origTextSt.transformString(pos, end, startText.substring(pos, pos + 1));
            pos = matcher.end();
        }
        return origTextSt;
    }

    /**
     * Test here.
     * @param args not used.
     * @throws Exception
     */
    static public void main(String[] args) throws Exception {
        String origText = "<headline>No way. Really?</headline>\n" +
                "<distraction>don't print me. Don't save me.</distraction>\n<post author='John Marston' toop='1'>\n";
        origText += "Hi, how do you do?</post>\n";

        Map<String, Set<String>> tagsWithAtts = new HashMap<>();
        Set<String> attributeNames = new HashSet<>();
        attributeNames.add("author");
        tagsWithAtts.put("post", attributeNames);
        System.out.println(origText);
        Set<String> tagsWithText = new HashSet<>();
        tagsWithText.add("headline");
//        tagsWithText.add("post");
        StringTransformation origTextSt = new StringTransformation(origText);
        Pair<StringTransformation, Map<IntPair, Map<String, String>>> nt =
                TextCleanerStringTransformation.cleanDiscussionForumXml(origTextSt, tagsWithText, tagsWithAtts);

  // check that we retained the right attributes, cleaned up the text, generated a sensible cleaned text, and can
        // recover the offsets of strings in the original text.
        StringTransformation st = nt.getFirst();
        Map<IntPair, Map<String, String>> retainedTagInfo = nt.getSecond();

        System.out.println("Original String:\n'" + origText + "'");
        System.out.println("Original String in StringTransformation:\n'" + origTextSt.getOrigText() + "'");
        System.out.println("Cleaned String: '" + origTextSt.getTransformedText() + "'");

        System.out.println("Retained tag info: ");
        for (IntPair offsets : retainedTagInfo.keySet()) {
            System.out.print("Offsets (" + offsets.getFirst() + "," + offsets.getSecond() + "): ");
            for (String att : retainedTagInfo.get(offsets).keySet())
                System.out.println(att + ": " + retainedTagInfo.get(offsets).get(att));
        }
    }

    /**
     * attempts to remove/replace characters likely to cause problems to NLP tools -- output should
     * be ascii only
     */
    public StringTransformation cleanText(StringTransformation origTextSt) {

        StringTransformation st = StringTransformationCleanup.normalizeToAscii(origTextSt);

        String text = st.getTransformedText();
        int start = 0;
        int end = 0;
        int textLen = text.length();

        // regexp can die due to heap exhaustion (recursion problem) for long inputs,
        // so the text is chunked

//        while (end < textLen) {
//            end = Math.min(start + REGEX_TEXT_LIMIT, textLen);
//
//            String chunk = text.substring(start, end);


        if (this.removeRepeatPunctuation)
            st = replaceDuplicatePunctuation(st);

        if (this.replaceUnderscores)
            st = replaceCharacters(st, underscorePattern, "-");

        if (this.replaceControlSequence) {
            st = replaceCharacters(st, controlSequencePattern, "");
            st = replaceCharacters(st, atSymbolPattern, " ");
        }

        if (this.replaceAdHocMarkup)
            st = replaceCharacters(st, adhocFormatPattern, "");

        if (this.replaceBogusApostrophe)
            st = replaceMisusedApostropheSymbol(st);

        logger.debug(NAME + ".cleanText(): original text: \n'" + text + "';\n new text: \n'"
                + st.getTransformedText() + "'.");
        return st;
    }

}
