/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import edu.illinois.cs.cogcomp.core.constants.CoreConfigNames;
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
 * These attempt to make intuitive replacements, and preserve a mapping to the original character offsets (see
 * {@link StringTransformationCleanup}).
 * <p>
 * This class, which replicates functionality from {@link TextCleaner}, uses StringTransformations instead
 * of Strings, and so allows recovery of character offsets in the original string after cleanup has taken
 * place, even if character offsets are not preserved.
 *
 * @author mssammon
 */
public class TextCleanerStringTransformation {
    protected static final String xmlQuot = "&quot;";
    protected static final String xmlAmp = "&amp;";
    protected static final String xmlApos = "&apos;";
    protected static final String xmlLt = "&lt;";
    protected static final String xmlGt = "&gt;";
    protected static final Pattern xmlEscapeCharPattern = Pattern.compile(xmlQuot + "|" +xmlAmp + "|" + xmlApos + "|" +
        xmlLt + "|" + xmlGt);
    protected static final Pattern underscorePattern = Pattern.compile("_");
    protected static final Pattern controlSequencePattern = Pattern.compile("@\\\\^|\\\\^@|\\\\^");
    protected static final Pattern atSymbolPattern = Pattern.compile("@ ");
    protected static final Pattern badApostrophePattern = Pattern.compile("(\\S+)\"s(\\s+)");
    private static final String NAME = TextCleanerStringTransformation.class.getCanonicalName();
//    private static final boolean DEBUG = false;
//    private static final int REGEX_TEXT_LIMIT = 10000;
    protected static Logger logger = LoggerFactory.getLogger(TextCleanerStringTransformation.class);
    protected static Pattern repeatPunctuationPattern = Pattern
            .compile("[\\p{P}\\*@<>=\\+#~_&\\p{P}]+");
    protected static Pattern xmlTagPattern = Pattern.compile("(<[^>\\r\\n]+>)"); // handle newlines separately
    /** used to extract the name of the tag, so we can match tag and attribute name. */
    protected static Pattern xmlTagNamePattern = Pattern.compile("<([^\\s>]+)");
    protected static Pattern whitespacePattern = Pattern.compile("\\s+");
    /** find attributes in an xml tag instance. Match whitespace then word. Group one is the
     * attribute name, group 2 is the quote mark used, three is the value. */
    protected static Pattern tagAttributePatter2 = Pattern.compile("[\\s]+([^\\s>=]+)[\\s]*=[\\s\"']*([^\\s\"'>]+)");
    protected static Pattern tagAttributePattern = Pattern.compile("[\\s]+([^\\s>=]+)[\\s]*=[\\s]*[\"']([^\"']+)");
    protected static Pattern adhocFormatPattern = Pattern.compile("[\\*~\\^]+");
    protected static Map<String, String> escXlmToChar = new HashMap<>();

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


    public TextCleanerStringTransformation(ResourceManager rm) throws SAXException, IOException {
        this.removeRepeatPunctuation = rm.getBoolean(CoreConfigNames.REMOVE_REPEAT_PUNCTUATION);
        this.replaceAdHocMarkup = rm.getBoolean(CoreConfigNames.REPLACE_ADHOC_MARKUP);
        this.replaceBogusApostrophe = rm.getBoolean(CoreConfigNames.REPLACE_BAD_APOSTROPHE);
        this.replaceControlSequence = rm.getBoolean(CoreConfigNames.REPLACE_CONTROL_SEQUENCE);
        this.replaceUnderscores = rm.getBoolean(CoreConfigNames.REPLACE_UNDERSCORES);
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

    /**
     * identifies xml tags and replaces them with an equal amount of space characters.
     * @param origText text to clean
     * @return cleaned text, same length, same non-whitespace character offsets
     */
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
	protected static boolean keep(List<String> tagNames, List<String> attributeNames, String tagname,
			String attrName) {
		for (int i = 0; i < tagNames.size(); i++) {
			if (tagNames.get(i).equals(tagname))
				if (attributeNames.get(i).equals(attrName))
					return true;
		}
		return false;
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

            xmlTextSt.transformString(start, end, getSubstituteStr(substr));
        }

        return xmlTextSt;
    }

    protected static String getSubstituteStr(String substr) {
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
     * attempts to remove/replace characters likely to cause problems to NLP tools -- output should
     * be ascii only
     */
    public StringTransformation cleanText(StringTransformation origTextSt) {

        StringTransformation st = StringTransformationCleanup.normalizeToAscii(origTextSt);

        String text = st.getTransformedText();
        int start = 0;
        int end = 0;
        int textLen = text.length();

        // warning: regexp can die due to heap exhaustion (recursion problem) for long inputs,
        // so may need to chunk texts. This commented used to break up the input; needs more though
        // to make it work with StringTransformation.
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
