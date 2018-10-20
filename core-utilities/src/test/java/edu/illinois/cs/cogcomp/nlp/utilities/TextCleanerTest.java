/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.utilities;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.TextCleaner;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class TextCleanerTest {
    private static Logger logger = LoggerFactory.getLogger(TextCleanerTest.class);

    private static final String CONFIG = "src/test/resources/testCleanerConfig.txt";
    private static final String LONG_BAD_TEXT_FILE = "src/test/resources/testCleaner.txt";

    private TextCleaner textCleaner;

    @Before
    public void setUp() throws Exception {
        ResourceManager rm = new ResourceManager(CONFIG);
        textCleaner = new TextCleaner(rm);
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public void testCleanText() {
        String badText =
                "The Turkish Parliament will vote Wednesday on whether to extend a mandate permitting military action against Kurdish "
                        + "militants in northern Iraq amid mounting opposition criticism of government policies on the Kurdish conflict."
                        + " The vote will be preceded by a debate on the government motion, which is asking Parliament to authorize another "
                        + "year of cross-border operations against the outlawed Kurdistan Workers’ Party, or PKK, in northern Iraq. The "
                        + "current mandate expires Oct. 17. The motion is expected to be easily approved. The two largest opposition groups, "
                        + "the Republican People’s Party, or CHP, and the Nationalist Movement Party, or MHP, have signaled that they will "
                        + "support the move, leaving the Peace and Democracy Party, or BDP, Turkey’s main Kurdish political movement, as "
                        + "the sole opponent. However, at least two CHP deputies of Kurdish origin, deputy chairman Sezgin Tanrıkulu and "
                        + "Hüseyin Aygün, are expected to withhold support for the motion. The CHP and the MHP stepped up criticism Tuesday "
                        + "of Prime Minister Recep Tayyip Erdoğan’s suggestions that talks with the PKK could resume, accusing him of "
                        + "emboldening PKK violence. MHP chairman Devlet Bahçeli called for an immediate ground operation against PKK hideouts "
                        + "in northern Iraq “to erect the Turkish flag in the Kandil [Mountains] once and for all” in a speech at his party’s "
                        + "first parliamentary group meeting.\n "
                        + "I do not think this will work out. Considering the pressure on Syria it will come to terms with Kurds. If Syria "
                        + "actually comes to terms with Israel, Turkey will most be disappointed with its' southern orientation.\n";

        String cleanText = textCleaner.cleanText(badText);

        boolean isGood = !(cleanText.contains("Erdoğan")) && cleanText.contains("Erdogan");
        assertEquals(true, isGood);
    }

    @Test
    public void testCleanText2() {
        String badText =
                "Do you really think we have a different kind of muslim in American .\n"
                        + "Do you think they follow the Quran and hadith ?\n"
                        + "Infidelity seems rampant in our culture and during these times. Just recently, "
                        + "Arnold Schwarzenegger is alleged to have had a love child with his housekeeper. "
                        + "So whats the solution to this problem? Shamci Rafani of Visalia, CA has a "
                        + "proposal: stone the sluts responsible. In a letter to the editor that appeared "
                        + "in yesterdays Visalia Times-Delta, Rafani  herself the victim of infidelity  "
                        + "angrily calls for the death of woman who knowingly engage in an affair. And she "
                        + "didnt hold back.";


        String cleanText = textCleaner.cleanText(badText);

        boolean isGood = cleanText.contains("didnt");
        assertEquals(true, isGood);
    }

    @Test
    public void replaceMisusedApostropheSymbol() {
        String badText =
                "Someone\"s string used a weird apostrophe that ended up as a quotation mark.";
        String cleanText = TextCleaner.replaceMisusedApostropheSymbol(badText);

        boolean isGood = !(cleanText.contains("\"")) && cleanText.contains("'");
        assertEquals(true, isGood);
    }

    @Test
    public void replaceUnderscores() {
        String badText =
                "I think _ on balance _ that people should use dashes. ___________ Never use underscores to separate text.";
        String cleanText = TextCleaner.replaceUnderscores(badText);

        boolean isGood = !(cleanText.contains("_"));
        logger.info(cleanText);
        assertEquals(true, isGood);
    }

    @Test
    public void replaceTildes() {
        String badText =
                "Some people use tildes to separate things. ~ I hate it. ~~~ it messes up NLP.";
        String cleanText = TextCleaner.replaceTildesAndStars(badText);

        boolean isGood = !(cleanText.contains("~"));
        logger.info(cleanText);
        assertEquals(true, isGood);
    }


    @Test
    public void replaceDuplicatePunctuation() {
        String badText =
                "My father,, the duplicate, has three sons!!! and he never -- that is, never!!! --- uses ellipses.... So THere??";
        String cleanText = TextCleaner.replaceDuplicatePunctuation(badText);

        boolean isGood =
                !(cleanText.contains(",,")) && !(cleanText.contains("--"))
                        && !(cleanText.contains("..")) && cleanText.contains(",")
                        && cleanText.contains("!") && cleanText.contains("-");

        logger.error(cleanText);

        assertEquals(true, isGood);
    }


    @Test
    public void replaceControlSequences() {
        String badText = "The leader of the ^@^@^@^@^@@^@^@ is never **** far away.";
        String cleanText = TextCleaner.replaceControlSequence(badText);

        boolean isGood = !(cleanText.contains("^@"));

        logger.error(cleanText);

        assertEquals(true, isGood);
    }


    @Test
    public void testCleanLongText() {
        ArrayList<String> badTextLines = null;

        try {
            badTextLines = LineIO.read(LONG_BAD_TEXT_FILE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        StringBuilder badText = new StringBuilder();

        for (String line : badTextLines)
            badText.append(line).append("\n");

        String cleanText = TextCleaner.replaceControlSequence(badText.toString());

        boolean isGood = !(cleanText.contains("^@"));

        logger.info(cleanText);

        assertEquals(true, isGood);
    }
}
