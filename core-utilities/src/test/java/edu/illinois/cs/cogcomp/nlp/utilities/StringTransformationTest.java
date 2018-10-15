/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.utilities;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the StringTransformation class.
 *
 * @author mssammon
 */
public class StringTransformationTest {

    public static final String EXPAND= "The \"only} way";
    public static final String MODEXPAND = "The ``only-RCB- way";
    public static final String REPLACE = "John\"s bad leg_and what a leg";
    public static final String MODREPLACE = "John's bad leg-and what a leg";
    public static final String REDUCE = "http://org.edu.net/killit say it's a leg";
    public static final String MODREDUCE = "WWW say it's a leg";
    public static final String DELETE = "John's leg^@^@^@^@";
    public static final String MODDELETE = "John's leg";
    public static final String OVERLAP = "my_____ why not";
    public static final String MODOVERLAP = "my- why not";
    public static final String SEQUENCE= "The http://theonlyway.org {only}^@^@^@ way___";
    public static final String MODSEQUENCE= "The WWW -LCB-only-RCB- way-";
    public static final String ABUT = "The <emph>only</emph> lonely@^@^man</doc>";
    public static final String MODABUT = "The only lonely man";

    public static final IntPair ONLYORIGOFFSETS = new IntPair(10,14);
    public static final IntPair ONLYNEWOFFSETS = new IntPair(4,8);
    public static final IntPair LONELYORIGOFFSETS = new IntPair(22,28);
    public static final IntPair LONELYNEWOFFSETS = new IntPair(9,15);
    public static final IntPair CTRLORIGOFFSETS = new IntPair(28,32);
    public static final IntPair CTRLNEWOFFSETS = new IntPair(15,16);
    public static final IntPair MANORIGOFFSETS = new IntPair(32,35);
    public static final IntPair MANNEWOFFSETS = new IntPair(16,19);

    StringTransformation st;

//    @Before
//    public void before() {
//    }

    /**
     * sequence of edits AND accesses of transformed string -- make sure
     *    second changes use correct offsets
     * second edit makes first redundant; verify correct output
     */


    @Test
    public void testDelete() {
        //John's leg^@^@^@^@
        StringTransformation st = new StringTransformation(DELETE);
        st.transformString(10, 18, "");
        String modifiedStr = st.getTransformedText();

        assertEquals(DELETE, st.getOrigText());
        assertEquals(DELETE.length() - 8, modifiedStr.length());
        assertEquals(MODDELETE, modifiedStr);
    }


    @Test
    public void testOverlap() {
        //my_____ why not
        StringTransformation st = new StringTransformation(OVERLAP);
        st.transformString(2, 7, "_");
        String modifiedStr = st.getTransformedText();

        st.transformString(2, 3, "-");

        modifiedStr = st.getTransformedText();

        assertEquals(OVERLAP, st.getOrigText());
        assertEquals(OVERLAP.length() - 4, modifiedStr.length());
        assertEquals(MODOVERLAP, modifiedStr);

        IntPair origOffsets = st.getOriginalOffsets(2, 3);

        assertEquals(2, origOffsets.getFirst());
        assertEquals(7, origOffsets.getSecond());
    }

    @Test
    public void testExpand() {
        StringTransformation st = new StringTransformation(EXPAND);
        st.transformString(4, 5, "``");
        st.transformString(9, 10, "-RCB-");

        String modifiedStr = st.getTransformedText();

        assertEquals(EXPAND, st.getOrigText());
        assertEquals(EXPAND.length() + 5, modifiedStr.length());
        assertEquals(MODEXPAND, modifiedStr);

        IntPair origOffsets = st.getOriginalOffsets(4, 6);
        assertEquals(4, origOffsets.getFirst());
        assertEquals(5, origOffsets.getSecond());

        origOffsets = st.getOriginalOffsets(10, 15);
        assertEquals(9, origOffsets.getFirst());
        assertEquals(10, origOffsets.getSecond());

        int modStart = st.computeModifiedOffsetFromOriginal(9);
        int modEnd = st.computeModifiedOffsetFromOriginal(10);
        assertEquals(10, modStart);
        assertEquals(15, modEnd);
    }


    @Test
    public void testSequentialExpand() {
        StringTransformation st = new StringTransformation(EXPAND);
        st.transformString(4, 5, "``");

        // force edits to be applied
        String modifiedStr = st.getTransformedText();
        assertEquals(EXPAND.length() + 1, modifiedStr.length());

        // subsequent transformation must work w.r.t. modified string
        st.transformString(10, 11, "-RCB-");

        modifiedStr = st.getTransformedText();

        assertEquals(EXPAND, st.getOrigText());
        assertEquals(EXPAND.length() + 5, modifiedStr.length());
        assertEquals(MODEXPAND, modifiedStr);

        int modStart = st.computeModifiedOffsetFromOriginal(9);
        int modEnd = st.computeModifiedOffsetFromOriginal(10);
        assertEquals(10, modStart);
        assertEquals(15, modEnd);


        IntPair origOffsets = st.getOriginalOffsets(4, 6);
        assertEquals(4, origOffsets.getFirst());
        assertEquals(5, origOffsets.getSecond());

        origOffsets = st.getOriginalOffsets(10, 15);
        assertEquals(9, origOffsets.getFirst());
        assertEquals(10, origOffsets.getSecond());
    }


    @Test
    public void testReplace() {
        StringTransformation st = new StringTransformation(REPLACE);
        st.transformString(4, 5, "'");
        st.transformString(14, 15, "-");

        String modifiedStr = st.getTransformedText();

        assertEquals(REPLACE, st.getOrigText());
        assertEquals(REPLACE.length(), modifiedStr.length());
        assertEquals(MODREPLACE, modifiedStr);

        int modStart = st.computeModifiedOffsetFromOriginal(14);
        int modEnd = st.computeModifiedOffsetFromOriginal(15);
        assertEquals(14, modStart);
        assertEquals(15, modEnd);

        IntPair origOffsets = st.getOriginalOffsets(4, 5);
        assertEquals(4, origOffsets.getFirst());
        assertEquals(5, origOffsets.getSecond());

        origOffsets = st.getOriginalOffsets(14, 15);
        assertEquals(14, origOffsets.getFirst());
        assertEquals(15, origOffsets.getSecond());
    }



    @Test
    public void testReduce() {
        StringTransformation st = new StringTransformation(REDUCE);
        // "http://org.edu.net/killit say it's a leg";
        st.transformString(0, 25, "WWW");

        String modifiedStr = st.getTransformedText();

        assertEquals(REDUCE, st.getOrigText());
        assertEquals(REDUCE.length() - 22, modifiedStr.length());
        assertEquals(MODREDUCE, modifiedStr);

        int modStart = st.computeModifiedOffsetFromOriginal(0);
        int modEnd = st.computeModifiedOffsetFromOriginal(25);
        assertEquals(0, modStart);
        assertEquals(3, modEnd);

        /*
         * what happens if we query a char in the middle of a deleted sequence?
         * -- should map to beginning of that modification
         */
        int modMid = st.computeModifiedOffsetFromOriginal(20);
        assertEquals(3, modMid);

        IntPair origOffsets = st.getOriginalOffsets(0,3);
        assertEquals(0, origOffsets.getFirst());
        assertEquals(25, origOffsets.getSecond());

        // intermediate edit chars map to same offsets, treated like replacements
        origOffsets = st.getOriginalOffsets(1,2);
        assertEquals(1, origOffsets.getFirst());
        assertEquals(2, origOffsets.getSecond());

        origOffsets = st.getOriginalOffsets(1, 4); // 1 past the end of the edit
        assertEquals(26, origOffsets.getSecond());
    }

    @Test
    public void testSequence() {
//        SEQUENCE= "The http://theonlyway.org {only}^@^@^@ way___";
//        MODSEQUENCE= "The WWW -LCB-only-RCB- way-";
        StringTransformation st = new StringTransformation(SEQUENCE);

        st.transformString(4, 25, "WWW");
        st.transformString(26, 27, "-LCB-");
        st.transformString(31, 32, "-RCB-");
        st.transformString(32, 38, "");
        st.transformString(42, 45, "-");

        String modifiedStr = st.getTransformedText();

        assertEquals(SEQUENCE, st.getOrigText());
        assertEquals(SEQUENCE.length() - 18, modifiedStr.length());
        assertEquals(MODSEQUENCE, modifiedStr);

        int modStart = st.computeModifiedOffsetFromOriginal(4);
        int modEnd = st.computeModifiedOffsetFromOriginal(25);
        assertEquals(4, modStart);
        assertEquals(7, modEnd);

        String transfSeq = modifiedStr.substring(4, 7);
        String origSeq = st.getOrigText().substring(4, 25);

        assertEquals(transfSeq, "WWW");
        assertEquals(origSeq, "http://theonlyway.org");

        /*
         * what happens if we query a char in the middle of a deleted sequence?
         * -- should map to beginning of that modification
         */
        int modMid = st.computeModifiedOffsetFromOriginal(20);
        assertEquals(7, modMid);

        IntPair origOffsets = st.getOriginalOffsets(4,7);
        assertEquals(4, origOffsets.getFirst());
        assertEquals(25, origOffsets.getSecond());

        // intermediate edit chars map to same offsets, treated like replacements
        origOffsets = st.getOriginalOffsets(1,2);
        assertEquals(1, origOffsets.getFirst());
        assertEquals(2, origOffsets.getSecond());

        origOffsets = st.getOriginalOffsets(1, 6); // in the middle of the replaced
        assertEquals(6, origOffsets.getSecond());


        // check expand edit
        origOffsets = st.getOriginalOffsets(17,22);
        assertEquals(31, origOffsets.getFirst());
        assertEquals(38, origOffsets.getSecond()); // expansion + deletion


        transfSeq = modifiedStr.substring(17, 22);
        origSeq = st.getOrigText().substring(31, 38);

        assertEquals("-RCB-", transfSeq);
        assertEquals("}^@^@^@", origSeq); // combines expand + delete for contiguous spans

        // intermediate edit chars map to same offsets, treated like replacements.
        // note that this could be weird in case of multiple edits at same index
        //   (e.g. insertion, then deletion)
        // Note that these don't really make sense as substrings, and nor are the mapped substrings likely to make sense
        origOffsets = st.getOriginalOffsets(19,20);
        assertEquals(35, origOffsets.getFirst());
        assertEquals(36, origOffsets.getSecond());


        modStart = st.computeModifiedOffsetFromOriginal(31); // in the middle of the replaced
        modEnd = st.computeModifiedOffsetFromOriginal(32);
        assertEquals(17, modStart);
        assertEquals(18, modEnd);

    }

    @Test
    public void testContiguousEdits() {
        String text = "He spoke with Paul <ENAMEX TYPE=\"PERSON\"><ENAMEX TYPE=\"PERSON\" E_OFF=\"1\">Paula</ENAMEX> Zahn</ENAMEX> .";
        StringTransformation st = new StringTransformation(text);
        st.transformString(19, 41, "");
        st.transformString(41, 73, "");
        st.transformString(78, 87, "");
        st.transformString(92,101, "");

        String modifiedStr = st.getTransformedText();

        assertEquals(text, st.getOrigText());
        assertEquals(31, modifiedStr.length());
        assertEquals("He spoke with Paul Paula Zahn .", modifiedStr);
    }


    /**
     * runs the same set of ops as testSequence, but applies edits after each transformation.
     * Ensures that the behavior is the same whether edits are done in a single pass, or over multiple passes.
     */
    @Test
    public void testSequentialSequence() {
//        SEQUENCE= "The http://theonlyway.org {only}^@^@^@ way___";
//        MODSEQUENCE= "The WWW -LCB-only-RCB- way-";
        StringTransformation st = new StringTransformation(SEQUENCE);

        st.transformString(4, 25, "WWW");
        // force edits to be flushed
        st.getTransformedText();

        st.transformString(8, 9, "-LCB-");
        st.getTransformedText();

        st.transformString(17, 18, "-RCB-");
        st.getTransformedText();

        st.transformString(22, 28, "");
        st.getTransformedText();

        st.transformString(26, 29, "-");
        st.getTransformedText();

        String modifiedStr = st.getTransformedText();

        assertEquals(SEQUENCE, st.getOrigText());
        assertEquals(SEQUENCE.length() - 18, modifiedStr.length());
        assertEquals(MODSEQUENCE, modifiedStr);

        int modStart = st.computeModifiedOffsetFromOriginal(4);
        int modEnd = st.computeModifiedOffsetFromOriginal(25);
        assertEquals(4, modStart);
        assertEquals(7, modEnd);

        String transfSeq = modifiedStr.substring(4, 7);
        String origSeq = st.getOrigText().substring(4, 25);

        assertEquals(transfSeq, "WWW");
        assertEquals(origSeq, "http://theonlyway.org");

        /*
         * what happens if we query a char in the middle of a deleted sequence?
         * -- should map to beginning of that modification
         */
        int modMid = st.computeModifiedOffsetFromOriginal(20);
        assertEquals(7, modMid);

        IntPair origOffsets = st.getOriginalOffsets(4,7);
        assertEquals(4, origOffsets.getFirst());
        assertEquals(25, origOffsets.getSecond());

        // intermediate edit chars map to same offsets, treated like replacements
        origOffsets = st.getOriginalOffsets(1,2);
        assertEquals(1, origOffsets.getFirst());
        assertEquals(2, origOffsets.getSecond());

        origOffsets = st.getOriginalOffsets(1, 6); // in the middle of the replaced
        assertEquals(6, origOffsets.getSecond());


        // check expand edit
        origOffsets = st.getOriginalOffsets(17,22);
        assertEquals(31, origOffsets.getFirst());
        assertEquals(32, origOffsets.getSecond());


        transfSeq = modifiedStr.substring(17, 22);
        origSeq = st.getOrigText().substring(31, 32);

        assertEquals("-RCB-", transfSeq);
        assertEquals("}", origSeq); // combines expand + delete for contiguous spans

        // intermediate edit chars map to same offsets, treated like replacements.
        // note that this could be weird in case of multiple edits at same index
        //   (e.g. insertion, then deletion)
        // Note that these don't really make sense as substrings, and nor are the mapped substrings likely to make sense
        origOffsets = st.getOriginalOffsets(19,20);
        assertEquals(29, origOffsets.getFirst());
        assertEquals(30, origOffsets.getSecond());


        modStart = st.computeModifiedOffsetFromOriginal(31); // in the middle of the replaced
        modEnd = st.computeModifiedOffsetFromOriginal(32);
        assertEquals(17, modStart);
        assertEquals(22, modEnd);

    }


    /**
     * when you delete a span next to a retained span and later try to retrieve original offsets for the retained
     *    span, StringTransformation must return the span offsets without the deleted span. However, if the edit
     *    reduced a span, the original offsets must include the deleted content -- i.e. the edit type matters.
     * This test assesses this difference in behavior, both before and after a span.
     */
    @Test
    public void testAbuttingEdits(){

        // "The <emph>only</emph> lonely@^@^man</doc>"
        // "The only man";

        StringTransformation st = new StringTransformation(ABUT);

        st.transformString(4, 10, "");
        st.transformString(14, 21, "");
        st.transformString(CTRLORIGOFFSETS.getFirst(), CTRLORIGOFFSETS.getSecond(), " ");
        st.transformString(35, 41, "");

        String transformedStr = st.getTransformedText();
        assertEquals(MODABUT, transformedStr);

        IntPair onlyOrig = st.getOriginalOffsets(ONLYNEWOFFSETS.getFirst(), ONLYNEWOFFSETS.getSecond());
        assertEquals(ONLYORIGOFFSETS, onlyOrig);

        IntPair lonelyOrig = st.getOriginalOffsets(LONELYNEWOFFSETS.getFirst(), LONELYNEWOFFSETS.getSecond());
        String origStr = ABUT.substring(LONELYORIGOFFSETS.getFirst(), LONELYORIGOFFSETS.getSecond());
        String newStr = transformedStr.substring(LONELYNEWOFFSETS.getFirst(), LONELYNEWOFFSETS.getSecond());
        assertEquals(origStr, newStr);
        assertEquals(LONELYORIGOFFSETS, lonelyOrig);

        int onlyNewStart = st.computeModifiedOffsetFromOriginal(ONLYORIGOFFSETS.getFirst());
        int onlyNewEnd = st.computeModifiedOffsetFromOriginal(ONLYORIGOFFSETS.getSecond());

        assertEquals(ONLYNEWOFFSETS.getFirst(), onlyNewStart);
        assertEquals(ONLYNEWOFFSETS.getSecond(), onlyNewEnd);

        IntPair ctrlOrig = st.getOriginalOffsets(CTRLNEWOFFSETS.getFirst(), CTRLNEWOFFSETS.getSecond());
        assertEquals(CTRLORIGOFFSETS.getFirst(), ctrlOrig.getFirst());
        assertEquals(CTRLORIGOFFSETS.getSecond(), ctrlOrig.getSecond());

        IntPair manOrig = st.getOriginalOffsets(MANNEWOFFSETS.getFirst(), MANNEWOFFSETS.getSecond());
        String manNewStr = MODABUT.substring(MANNEWOFFSETS.getFirst(), MANNEWOFFSETS.getSecond());
        String manOrigStr = ABUT.substring(MANORIGOFFSETS.getFirst(), MANORIGOFFSETS.getSecond());

        assertEquals(manNewStr, manOrigStr);
        assertEquals(MANORIGOFFSETS, manOrig);
    }

}
