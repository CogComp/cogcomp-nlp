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
import edu.illinois.cs.cogcomp.nlp.utilities.StringCleanup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class has methods for conversion of non-UTF-8 characters to UTF-8, and from UTF-8 to Ascii.
 * These attempt to preserve character offsets, and to make intuitive replacements (see the
 * StringCleanup class from coreUtilities).
 * <p>
 * Other methods try to remove problem character sequences from text to avoid known problems with
 * NLP components for such sequences, but don't preserve character offsets.
 *
 * @author mssammon
 */

public class TextCleaner {
    private static final String NAME = TextCleaner.class.getCanonicalName();
    private static final int REGEX_TEXT_LIMIT = 10000;
    private static final String xmlQuot = "&quot;";
    private static final String xmlAmp = "&amp;";
    private static final String xmlApos = "&apos;";
    private static final String xmlLt = "&lt;";
    private static final String xmlGt = "&gt;";
    private static Logger logger = LoggerFactory.getLogger(TextCleaner.class);
    private static Pattern repeatPunctuationPattern = Pattern
            .compile("[\\p{P}\\*@<>=\\+#~_&\\p{P}]+");
    private static Pattern xmlTagPattern = Pattern.compile("(<[^>\\r\\n]+>)");
    /** used to extract the name of the tag, so we can match tag and attribute name. */
    private static Pattern xmlTagNamePattern = Pattern.compile("<([^\\s>]+)");
    /** find attributes in an xml tag instance. Match whitespace then word. Group one is the
     * attribute name, group 2 is the quote mark used, three is the value. */
    private static Pattern tagAttributePatter2 = Pattern.compile("[\\s]+([^\\s>=]+)[\\s]*=[\\s\"']*([^\\s\"'>]+)");
    private static Pattern tagAttributePattern = Pattern.compile("[\\s]+([^\\s>=]+)[\\s]*=[\\s]*[\"']([^\"']+)");
    private static Pattern xmlEscapeCharPattern = Pattern.compile(xmlQuot + "|" +xmlAmp + "|" + xmlApos + "|" +
        xmlLt + "|" + xmlGt);
    private static Map<String, String> escXlmToChar;

    static {
        escXlmToChar = new HashMap<>();
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


    public TextCleaner(ResourceManager rm_) throws SAXException, IOException {
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
    public static String replaceMisusedApostropheSymbol(String origText_) {
        return origText_.replaceAll("\"s(\\s+)", "'s$1");
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
     * This class removes XML markup, for the most part, but for the provided set of attribute names,
     * it will leave their values in place and in the same position. Content within <code>quote</code>
     * tags is also replaced with white space (for the purpose of cleaning up the ERE code only).
     * @param origText the original text markup.
     * @param tagNames the names of tags containing the attributes leave, MUST BE LOWERCASE.
     * @param attributeNames the names of attributes to leave, MUST BE LOWERCASE.
     * @return the string.
     */
    public static String removeXmlLeaveAttributes(String origText, List<String>tagNames, List<String>attributeNames) {
    	if (tagNames.size() != attributeNames.size()) {
    		throw new RuntimeException("TextCleaner.removeXmlLeaveAttributes requires same number of tags and attributes.");
    	}
        Matcher xmlMatcher = xmlTagPattern.matcher(origText);
        char[] cleanTextBldr = origText.toCharArray();
        int last = 0;

        // match mark-up
        while (xmlMatcher.find()) {
        	String substr = xmlMatcher.group(0);
            int start = xmlMatcher.start();
            int end = xmlMatcher.end();

            for (; last < start; ++last)
    			cleanTextBldr[last] = origText.charAt(last);

            // identify any attribute values we need to retain.
            substr = substr.toLowerCase();
            Matcher tagMatcher = xmlTagNamePattern.matcher(substr);
            if (tagMatcher.find()) {
            	String tagname = tagMatcher.group(1);
            	if (tagname.equals("quote")) {
            	    // skip the entire quote
            	    end = origText.indexOf("</quote>", end);
            	    if (end == -1)
            	        throw new IllegalArgumentException("No end quote");
            	    end += 8;
            	} else {
                	// substring beyond the tag name.
                	int substrstart = start + tagMatcher.end();
                	substr = substr.substring(tagMatcher.end());
                	Matcher attrMatcher = tagAttributePattern.matcher(substr);
                	while (attrMatcher.find()) {
                		String attrName = attrMatcher.group(1);
                		String attrVal = attrMatcher.group(2);
                		if (keep(tagNames, attributeNames, tagname, attrName)) {
    	            		int attrend = substrstart + (attrMatcher.end() - attrVal.length());
    	            		String value = origText.substring(attrend, attrend+attrVal.length());
    	    	            for (; start < attrend; start++)
    	    	                cleanTextBldr[start] = ' ';
    	    	            for (int i = 0 ; i < value.length(); i++)
    							cleanTextBldr[start+i] = value.charAt(i);
    		                start += value.length();
    	            	}
                	}
            	}
            }
            for (; start < end; ++start)
                cleanTextBldr[start] = ' ';

            last = end;
        }
        for (; last < cleanTextBldr.length; ++last)
			cleanTextBldr[last] = origText.charAt(last);
        return new String(cleanTextBldr);
    }

    /**
     * This class removes XML markup, for the most part. For specified tags that denote spans of text other than
     *    body text (e.g. quotes, headlines), the text value and offsets are reported. For specified tags and attributes,
     *    the attribute values and their offsets are reported. Content within <code>quote</code>
     * tags is left in place (though quote tags are removed) and the offsets are reported with the
     * other specified attributes.
     * @param xmlText the original xml text.
     * @param tagsWithText the names of tags containing text other than body text (e.g. headlines, quotes)
     * @param tagsWithAtts the names of tags containing the attributes leave, MUST BE LOWERCASE.
     * @param attributeNames the names of attributes to leave, MUST BE LOWERCASE.
     * @return String comprising text.
     */
    public static String cleanDiscussionForumXml(StringTransformation xmlText, List<String> tagsWithText, List<String>tagsWithAtts, List<String>attributeNames) {

        StringTransformation normalizedTextSt = replaceXmlEscapedChars(xmlText);
        String normalizedText = normalizedTextSt.getTransformedText();
        Matcher xmlMatcher = xmlTagPattern.matcher(normalizedText);
        char[] cleanTextBldr = normalizedText.toCharArray();
        int last = 0;
        // match mark-up
        while (xmlMatcher.find()) {
            String substr = xmlMatcher.group(0);
            int start = xmlMatcher.start();
            int end = xmlMatcher.end();

            for (; last < start; ++last)
                cleanTextBldr[last] = normalizedText.charAt(last);

            // identify any attribute values we need to retain.
            substr = substr.toLowerCase();
            Matcher tagMatcher = xmlTagNamePattern.matcher(substr);
            if (tagMatcher.find()) {
                String tagname = tagMatcher.group(1);
                if (tagsWithText.contains(tagname)) {
                    // skip the entire quote
                    end = normalizedText.indexOf("</quote>", end);
                    if (end == -1)
                        throw new IllegalArgumentException("No end quote");
                    end += 8;
                } else {
                    // substring beyond the tag name.
                    int substrstart = start + tagMatcher.end();
                    substr = substr.substring(tagMatcher.end());
                    Matcher attrMatcher = tagAttributePattern.matcher(substr);
                    while (attrMatcher.find()) {
                        String attrName = attrMatcher.group(1);
                        String attrVal = attrMatcher.group(2);
                        if (tagsWithAtts.contains(tagname) && attributeNames.contains(attrName)) {
                            int attrend = substrstart + (attrMatcher.end() - attrVal.length());
                            String value = normalizedText.substring(attrend, attrend+attrVal.length());
                            for (; start < attrend; start++)
                                cleanTextBldr[start] = ' ';
                            for (int i = 0 ; i < value.length(); i++)
                                cleanTextBldr[start+i] = value.charAt(i);
                            start += value.length();
                        }
                    }
                }
            }
            for (; start < end; ++start)
                cleanTextBldr[start] = ' ';

            last = end;
        }
        for (; last < cleanTextBldr.length; ++last)
            cleanTextBldr[last] = normalizedText.charAt(last);
        return new String(cleanTextBldr);
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
     * replaces underscores with dashes (many crawled news articles seem to have substituted em- or
     * en-dashes with underscores)
     */
    public static String replaceUnderscores(String origText_) {
        return origText_.replaceAll("_", "-");
    }

    /**
     * web documents sometimes use tildes and stars either for page section breaks or as bullets for
     * bullet points; these may cause problems to NLP components
     * <p>
     * this method strips these characters completely
     */
    public static String replaceTildesAndStars(String origText_) {
        String cleanText = origText_.replaceAll("~", "");
        cleanText = cleanText.replaceAll("\\*", "");
        cleanText = cleanText.replaceAll("\\^", "");

        return cleanText;
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
    public static String replaceDuplicatePunctuation(String origText_) {
        Matcher matcher = TextCleaner.repeatPunctuationPattern.matcher(origText_);
        StringBuilder bldr = new StringBuilder();

        int pos = 0;
        int lastPos = 0;
        while (matcher.find(pos)) {
            pos = matcher.start();
            int end = matcher.end();
            bldr.append(origText_.substring(lastPos, pos + 1));
            lastPos = end;
            pos = matcher.end();
        }
        if (lastPos < origText_.length())
            bldr.append(origText_.substring(lastPos));

        return bldr.toString();
    }

    /**
     * noisy character sequences seen in some crawled text look like rendered control sequences;
     * this method strips them all.
     */
    public static String replaceControlSequence(String origText_) {
        String cleanText = origText_.replaceAll("@\\^", "");
        cleanText = cleanText.replaceAll("\\^@", "");
        cleanText = cleanText.replaceAll("\\^", "");
        cleanText = cleanText.replaceAll("@ ", " ");
        return cleanText;
    }

    /**
     * Test here.
     * @param args not used.
     * @throws Exception
     */
    static public void main (String[] args) throws Exception {
        String origText = "<post author='John Marston' toop='1'>Hi, how do you do?</post>";
        ArrayList<String> tagNames = new ArrayList<String>();
        tagNames.add("post");
        ArrayList<String> attributeNames = new ArrayList<String>();
        attributeNames.add("author");
        System.out.println(origText);
        String nt = removeXmlLeaveAttributes(origText,tagNames,attributeNames);
        System.out.println("\n"+nt);
        System.out.println("John is at "+origText.indexOf("John Marston"));
        System.out.println("John is at "+nt.indexOf("John Marston"));

    }

    /**
     * attempts to remove/replace characters likely to cause problems to NLP tools -- output should
     * be ascii only
     */
    public String cleanText(String text_) {
        int start = 0;
        int end = 0;
        int textLen = text_.length();
        StringBuilder finalCleanText = new StringBuilder();

        // regexp can die due to heap exhaustion (recursion problem) for long inputs,
        // so the text is chunked

        while (end < textLen) {
            end = Math.min(start + REGEX_TEXT_LIMIT, textLen);

            String chunk = text_.substring(start, end);

            String cleanText = StringCleanup.normalizeToAscii(chunk);

            if (this.removeRepeatPunctuation)
                cleanText = replaceDuplicatePunctuation(cleanText);

            if (this.replaceUnderscores)
                cleanText = replaceUnderscores(cleanText);

            if (this.replaceControlSequence)
                cleanText = replaceControlSequence(cleanText);

            if (this.replaceAdHocMarkup)
                cleanText = replaceTildesAndStars(cleanText);

            if (this.replaceBogusApostrophe)
                cleanText = replaceMisusedApostropheSymbol(cleanText);

            finalCleanText.append(cleanText);
            start = end;
        }


        logger.debug(NAME + ".cleanText(): original text: \n'" + text_ + "';\n new text: \n'"
                + finalCleanText + "'.");
        return (finalCleanText.toString()).trim();
    }

}
