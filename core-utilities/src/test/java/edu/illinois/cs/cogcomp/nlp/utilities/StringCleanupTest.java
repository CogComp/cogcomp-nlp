/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.utilities;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringCleanupTest extends TestCase {
    private static Logger logger = LoggerFactory.getLogger(StringCleanupTest.class);

    private static final String utf8RefStr = "A𝔊BC ﾁｮｺﾚｰﾄ —interet”";
    private static final String latin1RefStr = "-interet\"";
    private static final String asciiRefStr = "-interet\"";
    private static final String ctrlRefStr = "TestString";
    private static String suppSample = "A" + "\uD835\uDD0A" + "B" + "C";
    private static String halfWidthKatanaSample = "\uff81\uff6e\uff7a\uff9a\uff70\uff84";
    private static String diacriticSample = "—intérêt”";
    private static String ctrlSample = "Test" + String.valueOf((char) 3) + "String";
    private static String combinedStr = suppSample + " " + halfWidthKatanaSample + " "
            + diacriticSample;

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public static void testStringCleanup() {
        String inStr = combinedStr;
        String utf8Str = StringCleanup.normalizeToUtf8(inStr);

        logger.info("Normalized to UTF-8:");
        logger.info(utf8Str);

        assertEquals(utf8RefStr, utf8Str);

        String latin1Str = StringCleanup.normalizeToLatin1(diacriticSample);

        logger.info("Normalized to Latin1:");
        logger.info(latin1Str);

        assertEquals(latin1RefStr, latin1Str);

        String asciiStr = StringCleanup.normalizeToLatin1(diacriticSample);

        logger.info("Normalized to ascii:");
        logger.info(asciiStr);

        assertEquals(asciiRefStr, asciiStr);

        String withoutCtrlCharStr = StringCleanup.removeControlCharacters(ctrlSample);

        logger.info("Removed Control Characters:");
        logger.info(withoutCtrlCharStr);

        assertEquals(ctrlRefStr, withoutCtrlCharStr);

    }
}
