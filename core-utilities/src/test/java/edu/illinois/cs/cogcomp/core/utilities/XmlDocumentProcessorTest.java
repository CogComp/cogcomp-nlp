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
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor.SPAN_INFO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test(s) for XmlDocumentProcessor.
 * @author mssammon
 */
public class XmlDocumentProcessorTest {


    private static final String ORIG_TEXT = "<doc>\n<headline>&quot;No way. Really?&quot;</headline>\n" +
            "<distraction>don&apos;t print me. Don&apos;t &quot;save&quot; me.</distraction>\n<post author='John Marston' toop='1'>\n" +
            "<quote orig_author=\"him\">According to Garp:\n<quote orig_author=\"Garp\">Whassup?</quote>\nWhat's up with that?</quote>\n" +
            "Hi, &amp; how do you <img href=\"www.madeup.org/picture\">do?</post>\n</doc>\n";

    private static final String CLEAN_TEXT = "\"No way. Really?\"\nHi, & how do you do?\n";
    private static final IntPair AUTHOR_OFFSETS = new IntPair(149, 161);
    private static final String AUTHOR = "author";
    private static final String NAME = "John Marston";
    private static final IntPair DISTR_OFFSETS = new IntPair(68, 120);
    private static final String DISTR_SUBSTR = "don&apos;t print me. Don&apos;t &quot;save&quot; me.";
    private static final String INNER_QUOT_STR = "Whassup?";
    private static final IntPair IQ_OFFSETS = new IntPair(243, 251);


    @Test
    public void testXmlDocumentProcessor() {

        /*
        <doc id="ENG_DF_001241_20150407_F0000007T">
<headline>
cuba
</headline>
<post id="p1" author="chatmasta" datetime="2015-04-07T14:42:00">

         */
        Map<String, Set<String>> tagsWithAtts = new HashMap<>();
        Set<String> attributeNames = new HashSet<>();
        attributeNames.add("author");
        attributeNames.add("id");
        attributeNames.add("datetime");
        tagsWithAtts.put("post", attributeNames);
        attributeNames = new HashSet<>();
        attributeNames.add("id");
        tagsWithAtts.put("doc", attributeNames);
        Set<String> deletableSpanTags = new HashSet<>();
        deletableSpanTags.add("quote");
        deletableSpanTags.add("distraction");
        Set<String> tagsToIgnore = new HashSet<>();
        tagsToIgnore.add("img");
        tagsToIgnore.add("snip");

//        StringTransformation origTextSt = new StringTransformation(ORIG_TEXT);
        boolean throwExceptionOnXmlTagMiss = true;
        XmlDocumentProcessor proc = new XmlDocumentProcessor(deletableSpanTags, tagsWithAtts, tagsToIgnore, throwExceptionOnXmlTagMiss);

        Pair<StringTransformation, Map<IntPair, Map<String, String>>> nt = proc.processXml(ORIG_TEXT);

        // check that we retained the right attributes, cleaned up the text, generated a sensible cleaned text, and can
        // recover the offsets of strings in the original text.
        StringTransformation st = nt.getFirst();
        Map<IntPair, Map<String, String>> retainedTagInfo = nt.getSecond();

        String cleanText = st.getTransformedText();

        assertEquals(ORIG_TEXT, st.getOrigText());
        assertEquals(CLEAN_TEXT, cleanText);

        assertTrue(retainedTagInfo.containsKey(AUTHOR_OFFSETS));
        Map<String, String> attInfo = retainedTagInfo.get(AUTHOR_OFFSETS);
        assertTrue(attInfo.containsKey(AUTHOR));
        assertEquals(NAME, attInfo.get(AUTHOR));
        String origAuthStr = st.getOrigText().substring(AUTHOR_OFFSETS.getFirst(), AUTHOR_OFFSETS.getSecond());
        assertEquals(NAME, origAuthStr);

        assertTrue(retainedTagInfo.containsKey(DISTR_OFFSETS));
        attInfo = retainedTagInfo.get(DISTR_OFFSETS);
        assertTrue(attInfo.containsKey(SPAN_INFO));
        assertEquals("distraction", attInfo.get(SPAN_INFO));
        assertEquals(DISTR_SUBSTR, ORIG_TEXT.substring(DISTR_OFFSETS.getFirst(), DISTR_OFFSETS.getSecond()));

        assertTrue(retainedTagInfo.containsKey(IQ_OFFSETS));
        int iqStart = st.computeModifiedOffsetFromOriginal(IQ_OFFSETS.getFirst());
        int iqEnd = st.computeModifiedOffsetFromOriginal(IQ_OFFSETS.getSecond());

        assertEquals("", cleanText.substring(iqStart, iqEnd)); // deleted
        assertEquals(ORIG_TEXT.indexOf("Whassup"), IQ_OFFSETS.getFirst());


        int doStart = cleanText.indexOf("do?");
        int doEnd = doStart + 3;

        IntPair origYouOffsets = st.getOriginalOffsets(doStart, doEnd);
        assertEquals("do?", ORIG_TEXT.substring(origYouOffsets.getFirst(), origYouOffsets.getSecond()));
    }

}
