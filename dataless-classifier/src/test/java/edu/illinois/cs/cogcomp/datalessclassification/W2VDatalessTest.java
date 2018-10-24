/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification;

import org.junit.Test;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.datalessclassification.config.W2VDatalessConfigurator;
import edu.illinois.cs.cogcomp.datalessclassification.ta.ADatalessAnnotator;
import edu.illinois.cs.cogcomp.datalessclassification.ta.W2VDatalessAnnotator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author shashank
 */
public class W2VDatalessTest {
    private String configFile;
    private W2VDatalessAnnotator dataless;

    private List<String> documents;
    private List<Set<String>> docLabels;

    @Test
    public void testPredictions() {
        try {
            configFile = "config/project.properties";

            ResourceManager nonDefaultRm = new ResourceManager(configFile);
            ResourceManager rm = new W2VDatalessConfigurator().getConfig(nonDefaultRm);
            dataless = new W2VDatalessAnnotator(rm);

            documents = new ArrayList<>();
            String doc1 =
                    "i m looking for some recommendations for screen capture programs a couple" +
                            " of issues ago pc mag listed as editor s choices both conversion artist" +
                            " and hijaak for windows anyone have any experience with those or some others" +
                            " i m trying to get an alpha manual in the next few days and i m not making much" +
                            " progress with the screen shots i m currently using dodot and i m about to burn it" +
                            " and the disks it rode it on it s got a lot of freaky bugs and oversights that are " +
                            "driving me crazy tonight it decided that for any graphic it writes out as a tiff " +
                            "file that s under a certain arbitrary size it will swap the left and right sides of" +
                            " the picture usually it confines itself to not copying things to the clipboard so i " +
                            "have to save and load pix for editing in paintbrush or crashing every hour or so the " +
                            "one nice thing it has though is it s dither option you d think that this would turn " +
                            "colors into dots which it does if you go from say colors to colors but if you go " +
                            "from or colors to b w you can set a threshold level for which colors turn to black " +
                            "and which turn to white for me this is useful because i can turn light grays on buttons" +
                            " to white and the dark grays to black and thereby preserve the d effect on buttons and " +
                            "other parts of the window if you understood my description can you tell me if another " +
                            "less buggy program can do this as well much thanks for any help signature david delgreco " +
                            "what lies behind us and what lies technically a writer before us are tiny matters compared " +
                            "delgreco rahul net to what lies within us oliver wendell holmes david f delgreco delgreco rahul " +
                            "net recommendation for screen capture program";
            documents.add(doc1);

            String doc2 =
                    "yes i know it s nowhere near christmas time but i m gonna loose net access in a few days maybe " +
                            "a week or if i m lucky and wanted to post this for interested people to save till xmas " +
                            "note bell labs is a good place if you have a phd and a good boss i have neither subject " +
                            "xmas light set with levels of brightness another version of a variable brightness xmas " +
                            "light set this set starts with a blinker bulb string diagram orginal way set 0v b b " +
                            "0rtn modified set for level brightness string 0v 0k w string b 0v rtn note no mods to " +
                            "wiring to the right of this point only one blinker is used note that the blinker " +
                            "would not have as much current thru it as the string bulbs because of the second " +
                            "string of bulbs in parallel with it that s why the use of the 0k w resistor here to " +
                            "add extra current thru the blinker to make up for the current shunted thru the second " +
                            "string while the blinker is glowing and the second string is not glowing when the " +
                            "blinker goes open this resistor has only a slight effect on the brightness of the " +
                            "strings s slightly dimmer s slightly brighter or use a w 0v bulb in place of the 0k " +
                            "resistor if you can get one caution do not replace with a standard c bulb as these " +
                            "draw too much current and burn out the blinker c approx w what you ll see when it s " +
                            "working powerup string will light at full brightness and b will be lit bypassing most " +
                            "of the current from the second string making them not light b will open placing both " +
                            "strings in series making the string that was out to glow at a low brightness and the " +
                            "other string that was on before to glow at reduced brightness be sure to wire and insulate" +
                            " the splices resistor leads and cut wires in a safe manner level brightness xmas light " +
                            "set for easter";
            documents.add(doc2);

            docLabels = new ArrayList<>();
            Set<String> docLabels1 =
                    new HashSet<>(Arrays.asList("computer", "comp.os.ms.windows.misc"));
            docLabels.add(docLabels1);

            Set<String> docLabels2 = new HashSet<>(Arrays.asList("computer", "comp.windows.x"));
            docLabels.add(docLabels2);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO Error while initializing the annotator .. " + e.getMessage());
            fail("IO Error while initializing the annotator .. " + e.getMessage());
        }

        try {
            for (int i = 0; i < documents.size(); i++) {
                // String docText = getDocumentText(docPaths.get(i));
                String docText = documents.get(i);
                Set<String> docPredictions = getPredictions(getTextAnnotation(docText), dataless);

                System.out.println("Doc" + i + ": Gold LabelIDs:");
                for (String goldLabel : docLabels.get(i)) {
                    System.out.println(goldLabel);
                }

                System.out.println("Doc" + i + ": Predicted LabelIDs:");

                for (String predictedLabel : docPredictions) {
                    System.out.println(predictedLabel);
                }

                System.out.println();
                assertTrue(checkSetEquality(docLabels.get(i), docPredictions));
            }
        } catch (AnnotatorException e) {
            e.printStackTrace();
            System.out.println("Error annotating the document .. " + e.getMessage());
            fail("Error annotating the document .. " + e.getMessage());
        }
    }

    private boolean checkSetEquality(Set<String> goldLabels, Set<String> predictedLabels) {
        if (goldLabels.size() != predictedLabels.size())
            return false;

        for (String goldLabel : goldLabels) {
            if (predictedLabels.contains(goldLabel) == false)
                return false;
        }

        return true;
    }

    private String getDocumentText(String testFile) {
        try(BufferedReader br = new BufferedReader(new FileReader(new File(testFile)))) {

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(" ");
            }

            String text = sb.toString().trim();

            return text;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IO Error while reading the test file from " + testFile + " .. " + e.getMessage());
            throw new RuntimeException("IO Error while reading the test file from " + testFile + " .. " + e.getMessage());
        }
    }

    private TextAnnotation getTextAnnotation(String text) {
        TokenizerTextAnnotationBuilder taBuilder =
                new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
        TextAnnotation ta = taBuilder.createTextAnnotation(text);

        return ta;
    }

    private Set<String> getPredictions(TextAnnotation ta, ADatalessAnnotator annotator)
            throws AnnotatorException {
        List<Constituent> annots = annotator.getView(ta).getConstituents();

        Set<String> predictedLabels = new HashSet<>();

        for (Constituent annot : annots) {
            String label = annot.getLabel();
            predictedLabels.add(label);
        }

        return predictedLabels;
    }
}
