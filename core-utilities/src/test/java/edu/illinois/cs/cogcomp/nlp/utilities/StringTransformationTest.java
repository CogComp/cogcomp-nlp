package edu.illinois.cs.cogcomp.nlp.utilities;

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

    public static final String EXPAND= "The \"only\" way";
    public static final String REPLACE = "John\"s bad leg_and what a leg";
    public static final String REDUCE = "http://org.edu.net/killit say it's a leg";
    public static final String DELETE = "John's leg^@^@^@^@";
    public static final String OVERLAP = "my_____ why not";
    public static final String MODEXPAND = "The ``only'' way";
    public static final String MODREPLACE = "John's bad leg-and what a leg";
    public static final String MODREDUCE = "WWW say it's a leg";
    public static final String MODDELETE = "John's leg";
    public static final String MODOVERLAP = "my- why not";

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
        st.transformString(9, 10, "''");

        String modifiedStr = st.getTransformedText();

        assertEquals(EXPAND, st.getOrigText());
        assertEquals(EXPAND.length() + 2, modifiedStr.length());
        assertEquals(MODEXPAND, modifiedStr);

        IntPair origOffsets = st.getOriginalOffsets(4, 6);
        assertEquals(4, origOffsets.getFirst());
        assertEquals(5, origOffsets.getSecond());

        origOffsets = st.getOriginalOffsets(10, 12);
        assertEquals(9, origOffsets.getFirst());
        assertEquals(10, origOffsets.getSecond());
    }


    @Test
    public void testSequentialExpand() {
        StringTransformation st = new StringTransformation(EXPAND);
        st.transformString(4, 5, "``");

        // force edits to be applied
        String modifiedStr = st.getTransformedText();
        assertEquals(EXPAND.length() + 1, modifiedStr.length());

        // subsequent transformation must work w.r.t. modified string
        st.transformString(10, 11, "''");

        modifiedStr = st.getTransformedText();

        assertEquals(EXPAND, st.getOrigText());
        assertEquals(EXPAND.length() + 2, modifiedStr.length());
        assertEquals(MODEXPAND, modifiedStr);

        IntPair origOffsets = st.getOriginalOffsets(4, 6);
        assertEquals(4, origOffsets.getFirst());
        assertEquals(5, origOffsets.getSecond());

        origOffsets = st.getOriginalOffsets(10, 12);
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
        // http://org.edu.net/killit say it's a leg";
        st.transformString(0, 25, "WWW");

        String modifiedStr = st.getTransformedText();

        assertEquals(REDUCE, st.getOrigText());
        assertEquals(REDUCE.length() - 22, modifiedStr.length());
        assertEquals(MODREDUCE, modifiedStr);

        IntPair origOffsets = st.getOriginalOffsets(0,3);
        assertEquals(0, origOffsets.getFirst());
        assertEquals(25, origOffsets.getSecond());

        // intermediate edit chars map to same offsets, treated like replacements
        origOffsets = st.getOriginalOffsets(1,2);
        assertEquals(23, origOffsets.getFirst());
        assertEquals(24, origOffsets.getSecond());

        origOffsets = st.getOriginalOffsets(1, 4); // 1 past the end of the edit
        assertEquals(26, origOffsets.getSecond());


    }

}
