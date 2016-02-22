package edu.illinois.cs.cogcomp.ner;


import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.ExpressiveFeaturesAnnotator;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.FlatGazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.Gazetteers;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjTagger.*;
import edu.illinois.cs.cogcomp.ner.ParsingProcessingData.PlainTextReader;
import edu.illinois.cs.cogcomp.ner.config.NerBaseConfigurator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.fail;

/**
 * test the gazetteers, tree and flat.
 * 
 * @author redman
 *
 */
public class TestGazetteers {

    /** the text used for this test. */
    private final static String TT = "Friday as Leicestershire beat Somerset.";
    private final static String TEST_INPUT =
            " For researchers, like Larry Smarr, with big data but little access to the IT tools needed to analyze it, "
                    + "the Extreme Science and Engineering Discovery Environment (XSEDE) Campus Bridging team is a bit like a superhero squad. Based at Indiana "
                    + "University, the team makes it easier to connect researchers' analyses to the national cyberinfrastructure - including "
                    + "computing and data storage systems, advanced instruments and data repositories, visualization environments and people - "
                    + "smoothing the way for discovery and breakthroughs. Campuses around the US are finding that installing the XSEDE Compatible "
                    + "Basic Cluster (XCBC) software suite quickly improves computing. Designed to help researchers ranging from big data scientists "
                    + "to people running small campus clusters, XCBC lets a local campus create a high performance computing (HPC) cluster from "
                    + "scratch. The open source software tools match the software of the most commonly used systems within XSEDE, and Campus "
                    + "Bridging team members even offer on-site installation help. The first such visit took place in April at Marshall University "
                    + "in Huntington, West Virginia. The project involved increasing the cluster's capabilities, including integrating eight GPUs "
                    + "and enhancing system management. XCBC makes computing more accessible for everyone, said Jeremy Fischer, IU senior systems analyst and XCBC engineer. "
                    + "Many times researchers don't have the knowledge or the capability to set up a high performance cluster on their own. By working with the "
                    + "Campus Bridging team on an on-site XCBC installation, these folks can get their science up and running again. Jack Smith can "
                    + "attest to that. As cyberinfrastructure coordinator in the Division of Science and Research of the West Virginia Higher "
                    + "Education Policy Commission, Smith worked closely with Fischer and colleague Eric Coulter when they came to Marshall.";

    /** the text used for this test. */

    @Before
    public void setUp() throws Exception {
        try {
            String path = "/data/Models/CoNLL/finalSystemBILOU.model.level1";
            System.out.println("--" + NETaggerLevel1.class.getResource(path));
            System.out.println("--" + NETaggerLevel1.class.getResource(path + ".lex"));

            Parameters.readConfigAndLoadExternalData(new NerBaseConfigurator().getDefaultConfig());
            ParametersForLbjCode.currentParameters.forceNewSentenceOnLineBreaks = false;
        } catch (Exception e) {
            System.out.println("Cannot initialise the test, the exception was: ");
            e.printStackTrace();
            fail();
        }
    }

    private String construct(NEWord w, LinkedVector sentence) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < sentence.size(); j++) {
            NEWord word1 = (NEWord) sentence.get(j);
            if (j > 0)
                sb.append(' ');
            if (w == word1) {
                sb.append('{');
                sb.append(word1.form);
                sb.append('}');
            } else
                sb.append(word1.form);
        }
        return sb.toString();
    }

    @Test
    public void testColumnFiles() throws IOException {
        Gazetteers fg = new FlatGazetteers("gazetteers");
        System.out.println("Starting ");

        try {
            final String dir =
                    "/Users/redman/Projects/IllinoisNER/GoldData/Reuters/ColumnFormatDocumentsSplit/TrainPlusDev";
            String[] files = new File(dir).list();
            for (String file1 : files) {
                String file = dir + File.separator + file1;
                String message = "Processing " + file;
                Data data1 = new Data(file, file, "-c", new String[] {}, new String[] {});
                ExpressiveFeaturesAnnotator.annotate(data1);
                ArrayList<LinkedVector> sentences1 = data1.documents.get(0).sentences;

                Data data2 = new Data(file, file, "-c", new String[] {}, new String[] {});
                ExpressiveFeaturesAnnotator.oldannotate(data2, fg);
                ArrayList<LinkedVector> sentences2 = data2.documents.get(0).sentences;
                for (int i = 0; i < sentences1.size(); i++) {
                    LinkedVector lv1 = sentences1.get(i);
                    LinkedVector lv2 = sentences2.get(i);
                    for (int j = 0; j < lv1.size(); j++) {
                        NEWord word1 = (NEWord) lv1.get(j);
                        NEWord word2 = (NEWord) lv2.get(j);
                        ArrayList<String> g1 = word1.gazetteers;
                        ArrayList<String> g2 = word2.gazetteers;
                        ArrayList<String> diffs = new ArrayList<>();

                        // compile a list of hits tree got that flat missed
                        for (String hit1 : g1) {
                            if (!g2.contains(hit1)) {
                                diffs.add(hit1);
                            }
                        }

                        // if we missed some, report them.
                        if (diffs.size() > 0) {
                            if (message != null) {
                                System.out.println("----- " + message);
                                message = null;
                            }
                            System.out.println("Old way missed on \"" + this.construct(word1, lv1)
                                    + "\"");
                            for (String t : diffs)
                                System.out.print("," + t);
                            System.out.println();
                            diffs.clear();
                        }

                        // see if tree missed any
                        for (String hit2 : g2) {
                            if (!g1.contains(hit2)) {
                                diffs.add(hit2);
                            }
                        }

                        // if we missed some, report them.
                        if (diffs.size() > 0) {
                            if (message != null) {
                                System.out.println("----- " + message);
                                message = null;
                            }
                            System.out.println("New way missed on \"" + this.construct(word1, lv1)
                                    + "\"");
                            for (String t : diffs)
                                System.out.print(", " + t);
                            System.out.println();
                            diffs.clear();
                        }
                    }
                }
            }
            System.out.println("Done");

            // compare data1 hits and data2 hits.
        } catch (Exception e) {
            System.out.println("Cannot annotate the test, the exception was: ");
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testTinyText() throws IOException {
        Gazetteers fg = new FlatGazetteers("gazetteers");
        System.out.println("Starting ");
        try {
            ArrayList<LinkedVector> sentences1 = PlainTextReader.parseText(TT);
            Data data1 = new Data(new NERDocument(sentences1, "input1"));
            ExpressiveFeaturesAnnotator.annotate(data1);
            ArrayList<LinkedVector> sentences2 = PlainTextReader.parseText(TT);
            Data data2 = new Data(new NERDocument(sentences2, "input2"));
            ExpressiveFeaturesAnnotator.oldannotate(data2, fg);
            String message = "poopy";
            for (int i = 0; i < sentences1.size(); i++) {
                LinkedVector lv1 = sentences1.get(i);
                LinkedVector lv2 = sentences2.get(i);
                for (int j = 0; j < lv1.size(); j++) {
                    NEWord word1 = (NEWord) lv1.get(j);
                    NEWord word2 = (NEWord) lv2.get(j);
                    ArrayList<String> g1 = word1.gazetteers;
                    ArrayList<String> g2 = word2.gazetteers;
                    Collections.sort(g1);
                    Collections.sort(g2);
                    if (g1.size() != g2.size()) {
                        if (message != null) {
                            System.out.println("----- " + message);
                            message = null;
                        }
                        System.out.println("differing sizes:" + this.construct(word1, lv1));
                        System.out.print("old:");
                        for (String h2 : g2)
                            System.out.print(" " + h2);
                        System.out.print("\nnew:");
                        for (String h1 : g1)
                            System.out.print(" " + h1);
                        System.out.println("\n");
                    } else {
                        for (int x = 0; x < g1.size(); x++) {
                            if (!g1.get(x).equals(g2.get(x))) {
                                if (message != null) {
                                    System.out.println("----- " + message);
                                    message = null;
                                }
                                System.out
                                        .println("differing values:" + this.construct(word1, lv1));
                                System.out.print("old:");
                                for (String h2 : g2)
                                    System.out.print(" " + h2);
                                System.out.print("\nnew:");
                                for (String h1 : g1)
                                    System.out.print(" " + h1);
                                System.out.println("\n");
                                break;
                            }
                        }
                    }
                }
            }
            System.out.println("Done");

            // compare data1 hits and data2 hits.
        } catch (Exception e) {
            System.out.println("Cannot annotate the test, the exception was: ");
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testOrder() throws IOException {
        Gazetteers fg = new FlatGazetteers("gazetteers");
        System.out.println("Starting ");

        try {
            final String dir =
                    "/Users/redman/Projects/IllinoisNER/GoldData/Reuters/ColumnFormatDocumentsSplit/TrainPlusDev";
            String[] files = new File(dir).list();
            for (String file1 : files) {
                String file = dir + File.separator + file1;
                String message = "Processing " + file;
                Data data1 = new Data(file, file, "-c", new String[] {}, new String[] {});
                ExpressiveFeaturesAnnotator.annotate(data1);
                ArrayList<LinkedVector> sentences1 = data1.documents.get(0).sentences;

                Data data2 = new Data(file, file, "-c", new String[] {}, new String[] {});
                ExpressiveFeaturesAnnotator.oldannotate(data2, fg);
                ArrayList<LinkedVector> sentences2 = data2.documents.get(0).sentences;
                for (int i = 0; i < sentences1.size(); i++) {
                    LinkedVector lv1 = sentences1.get(i);
                    LinkedVector lv2 = sentences2.get(i);
                    for (int j = 0; j < lv1.size(); j++) {
                        NEWord word1 = (NEWord) lv1.get(j);
                        NEWord word2 = (NEWord) lv2.get(j);
                        ArrayList<String> g1 = word1.gazetteers;
                        ArrayList<String> g2 = word2.gazetteers;
                        Collections.sort(g1);
                        Collections.sort(g2);
                        if (g1.size() != g2.size()) {
                            if (message != null) {
                                System.out.println("----- " + message);
                                message = null;
                            }
                            System.out.println("differing sizes:" + this.construct(word1, lv1));
                            System.out.print("old:");
                            for (String h2 : g2)
                                System.out.print(" " + h2);
                            System.out.print("\nnew:");
                            for (String h1 : g1)
                                System.out.print(" " + h1);
                            System.out.println("\n");
                        } else {
                            for (int x = 0; x < g1.size(); x++) {
                                if (!g1.get(x).equals(g2.get(x))) {
                                    if (message != null) {
                                        System.out.println("----- " + message);
                                        message = null;
                                    }
                                    System.out.println("differing values:"
                                            + this.construct(word1, lv1));
                                    System.out.print("old:");
                                    for (String h2 : g2)
                                        System.out.print(" " + h2);
                                    System.out.print("\nnew:");
                                    for (String h1 : g1)
                                        System.out.print(" " + h1);
                                    System.out.println("\n");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("Done");

            // compare data1 hits and data2 hits.
        } catch (Exception e) {
            System.out.println("Cannot annotate the test, the exception was: ");
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testAnnotators() {
        try {
            System.out.println("Start processing new way...");
            ArrayList<LinkedVector> sentences1 = PlainTextReader.parseText(TEST_INPUT);
            Data data1 = new Data(new NERDocument(sentences1, "input"));
            ExpressiveFeaturesAnnotator.annotate(data1);
            Gazetteers fg = new FlatGazetteers("gazetteers");
            System.out.println("Start processing old way...");
            ArrayList<LinkedVector> sentences2 = PlainTextReader.parseText(TEST_INPUT);
            Data data2 = new Data(new NERDocument(sentences2, "input"));
            ExpressiveFeaturesAnnotator.oldannotate(data2, fg);

            System.out.println("Unordered Compare");
            for (int i = 0; i < sentences1.size(); i++) {
                LinkedVector lv1 = sentences1.get(i);
                LinkedVector lv2 = sentences2.get(i);
                for (int j = 0; j < lv1.size(); j++) {
                    NEWord word1 = (NEWord) lv1.get(j);
                    NEWord word2 = (NEWord) lv2.get(j);
                    ArrayList<String> g1 = word1.gazetteers;
                    ArrayList<String> g2 = word2.gazetteers;
                    if (g1.size() != g2.size()) {
                        System.out.println("differing sizes");
                        System.out.print("old:");
                        for (String h2 : g2)
                            System.out.print(" " + h2);
                        System.out.print("\nnew:");
                        for (String h1 : g1)
                            System.out.print(" " + h1);
                        System.out.println("\n");

                    }
                    for (String hit1 : g1) {
                        if (!g2.contains(hit1)) {
                            System.out.println("Old way did not contain " + hit1 + " on word \""
                                    + word1.form + "\"");
                            System.out.print("old:");
                            for (String h2 : g2)
                                System.out.print(" " + h2);
                            System.out.print("\nnew:");
                            for (String h1 : g1)
                                System.out.print(" " + h1);
                            System.out.println("\n");
                        }
                    }
                    for (String hit2 : g2) {
                        if (!g1.contains(hit2)) {
                            System.out.println("New way did not contain " + hit2 + " on word \""
                                    + word2.form + "\"");
                            System.out.print("old:");
                            for (String h2 : g2)
                                System.out.print(" " + h2);
                            System.out.print("\nnew:");
                            for (String h1 : g1)
                                System.out.print(" " + h1);
                            System.out.println("\n");
                        }
                    }

                }
            }

            System.out.println("Ordered Compare");
            for (int i = 0; i < sentences1.size(); i++) {
                LinkedVector lv1 = sentences1.get(i);
                LinkedVector lv2 = sentences2.get(i);
                for (int j = 0; j < lv1.size(); j++) {
                    NEWord word1 = (NEWord) lv1.get(j);
                    NEWord word2 = (NEWord) lv2.get(j);
                    ArrayList<String> g1 = word1.gazetteers;
                    ArrayList<String> g2 = word2.gazetteers;
                    if (g1.size() != g2.size()) {
                        System.out.println("differing sizes");
                        System.out.print("old:");
                        for (String h2 : g2)
                            System.out.print(" " + h2);
                        System.out.print("\nnew:");
                        for (String h1 : g1)
                            System.out.print(" " + h1);
                        System.out.println("\n");

                    }
                    for (int k = 0; k < g1.size(); k++) {
                        String hit1 = g1.get(k);
                        String hit2 = g2.get(k);
                        if (!hit1.equals(hit2)) {
                            System.out.println("Old way did not contain " + hit1 + " on word \""
                                    + word1.form + "\"");
                            System.out.print("old:");
                            for (String h2 : g2)
                                System.out.print(" " + h2);
                            System.out.print("\nnew:");
                            for (String h1 : g1)
                                System.out.print(" " + h1);
                            System.out.println("\n");
                        }
                    }
                    for (int k = 0; k < g2.size(); k++) {
                        String hit2 = g2.get(k);
                        String hit1 = g1.get(k);
                        if (!hit1.equals(hit2)) {
                            System.out.println("Old way did not contain " + hit1 + " on word \""
                                    + word1.form + "\"");
                            System.out.print("old:");
                            for (String h2 : g2)
                                System.out.print(" " + h2);
                            System.out.print("\nnew:");
                            for (String h1 : g1)
                                System.out.print(" " + h1);
                            System.out.println("\n");
                        }
                    }
                }
            }

            System.out.println("Done");

            // compare data1 hits and data2 hits.
        } catch (Exception e) {
            System.out.println("Cannot annotate the test, the exception was: ");
            e.printStackTrace();
            fail();
        }
    }

    static private void print(NEWord word, String label) {
        System.out.print(label + " " + word.form + " : ");
        for (String gazhit : word.gazetteers) {
            System.out.print(" " + gazhit);
        }
        System.out.println();
    }

    static public void main(String[] args) {
        try {
            Parameters.readConfigAndLoadExternalData(new NerBaseConfigurator().getDefaultConfig());
            ParametersForLbjCode.currentParameters.forceNewSentenceOnLineBreaks = false;
        } catch (Exception e) {
            System.out.println("Cannot initialise the test, the exception was: ");
            e.printStackTrace();
            fail();
        }

        String file = "0072.txt";
        try {
            System.out.println("Start processing new way...");

            // do the tree gaz
            Data data1 = new Data(file, file, "-c", new String[] {}, new String[] {});
            ArrayList<LinkedVector> sentences1 = data1.documents.get(0).sentences;
            ExpressiveFeaturesAnnotator.annotate(data1);

            // flat gaz
            Gazetteers fg = new FlatGazetteers("gazetteers");
            System.out.println("Start processing old way...");
            Data data2 = new Data(file, file, "-c", new String[] {}, new String[] {});
            ArrayList<LinkedVector> sentences2 = data2.documents.get(0).sentences;
            ExpressiveFeaturesAnnotator.oldannotate(data2, fg);

            System.out.println("Compare");
            for (int i = 0; i < sentences1.size(); i++) {
                LinkedVector lv1 = sentences1.get(i);
                LinkedVector lv2 = sentences2.get(i);
                for (int j = 0; j < lv1.size(); j++) {
                    NEWord word1 = (NEWord) lv1.get(j);
                    NEWord word2 = (NEWord) lv2.get(j);
                    print(word1, "treegaz ");
                    print(word2, "flatgaz ");
                }
            }
            System.out.println("Done");

            // compare data1 hits and data2 hits.
        } catch (Exception e) {
            System.out.println("Cannot annotate the test, the exception was: ");
            e.printStackTrace();
            fail();
        }
    }
}
