/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Generator of dummy {@link TextAnnotation}s and their {@link View}s, used for testing.
 * 
 * @author Daniel Khashabi
 * @author Christos Christodoulopoulos
 */
public class DummyTextAnnotationGenerator {
    static Logger logger = LoggerFactory.getLogger(DummyTextAnnotationGenerator.class);

    static String annotatedString1 =
            "The construction of the John Smith library finished on time .";
    static String annotatedString2 = "The $10M building was designed in 2016 .";
    static String annotatedString3 = "The paving commenced Monday and will finish in June .";

    static List<String[]> annotatedTokenizedStringArray1 = new ArrayList<>();
    static List<String[]> annotatedTokenizedStringArray2 = new ArrayList<>();
    static List<String[]> annotatedTokenizedStringArray3 = new ArrayList<>();
    static {
        annotatedTokenizedStringArray1.add(annotatedString1.split(" "));
        annotatedTokenizedStringArray2.add(annotatedString2.split(" "));
        annotatedTokenizedStringArray3.add(annotatedString3.split(" "));
    };

    static String[] pos1 = {"DT", "NN", "IN", "DT", "NNP", "NNP", "NN", "VBD", "IN", "NN", "."};
    static String[] pos2 = {"DT", "NN", "NN", "VBD", "VBN", "IN", "CD", "."};
    static String[] pos3 = {"DT", "VBG", "VBD", "NNP", "CC", "MD", "VB", "IN", "NNP", "."};

    static String[] pos_noisy1 =
            {"DT", "NNA", "DT", "DT", "NNP", "NN", "NN", "VB", "IN", "NN", "."};
    static String[] pos_noisy2 = {"DT", "NNS", "NN", "VB", "VBN", "IN", "NN", "."};
    static String[] pos_noisy3 = {"DT", "VBG", "VBD", "NNP", "CC", "MD", "VB", "IN", "NN", "."};

    static String[] lemmas1 = {"the", "construction", "of", "the", "John", "Smith", "library",
            "finish", "on", "time", "."};
    static String[] lemmas2 = {"The", "$10M", "building", "be", "design", "in", "2016", "."};
    static String[] lemmas3 = {"The", "paving", "commence", "Monday", "and", "will", "finish",
            "in", "June", "."};
    static String[] lemmasAll = new String[lemmas1.length + lemmas2.length + lemmas3.length];
    static {
        System.arraycopy(lemmas1, 0, lemmasAll, 0, lemmas1.length);
        System.arraycopy(lemmas2, 0, lemmasAll, lemmas1.length, lemmas2.length);
        System.arraycopy(lemmas3, 0, lemmasAll, lemmas1.length + lemmas2.length, lemmas3.length);
    }

    static String[] lemmas_noisy1 = {"the", "construct", "of", "the", "John", "Smith", "library",
            "fin", "on", "time", "."};
    static String[] lemmas_noisy2 = {"The", "$10M", "build", "be", "design", "in", "2016", "."};
    static String[] lemmas_noisy3 = {"The", "pave", "commence", "Monday", "and", "will", "finish",
            "in", "June", "."};

    static String tree1 =
            "(S1 (S (NP (NP (DT The) (NN construction)) (PP (IN of) (NP (DT the) (NNP John) (NNP Smith) (NN library)))) "
                    + "(VP (VBD finished) (PP (IN on) (NP (NN time)))) (. .)))";
    static String tree2 =
            "(S1 (S (NP (DT The) (JJ $10M) (NN building)) (VP (AUX was) (VP (VBN designed) (PP (IN in) "
                    + "(NP (CD 2016)))))(. .)))";
    static String tree3 =
            "(S1 (S (NP (DT The)(NN paving))(VP (VP (VBD commenced)(NP (NNP Monday)))(CC and)(VP (MD will)(VP (VB finish)(PP (IN in)(NP (NNP June))))))(. .)))";

    static String tree_noisy1 =
            "(S1 (S (NP (NP (DT The) (NNA construction) (IN of)) (PP (NP (DT the) (NNP John) (NN Smith) (NN library)))) "
                    + "(VP (VB finished) (PP (IN on) (NP (NN time)))) (. .)))";
    static String tree_noisy2 =
            "(S1 (S (NP (DT The) (NN $10M) (NN building)) (VP (AUX was) (VP (VBN designed) (PP (IN in) "
                    + "(NP (CD 2016)))))(. .)))";
    static String tree_noisy3 =
            "(S1 (S (NP (DT The) (JJ paving)) (VP (VP (VBD commenced) (NP (NNP Monday))) (CC and)"
                    + "(VP (MD will) (VP (VB finish) (PP (IN in) (NP (NNP June)))))) (. .)))";

    static Map<IntPair, String> chunks1 = new HashMap<>();
    static Map<IntPair, String> chunks2 = new HashMap<>();
    static Map<IntPair, String> chunks3 = new HashMap<>();
    static Map<IntPair, String> ner1 = new HashMap<>();
    static Map<IntPair, String> ner2 = new HashMap<>();
    static Map<IntPair, String> ner3 = new HashMap<>();

    static {
        chunks1.put(new IntPair(0, 2), "NP");
        chunks1.put(new IntPair(2, 3), "PP");
        chunks1.put(new IntPair(3, 7), "NP");
        chunks1.put(new IntPair(7, 8), "VP");
        chunks1.put(new IntPair(8, 9), "PP");
        chunks1.put(new IntPair(9, 10), "NP");

        chunks2.put(new IntPair(11, 14), "NP");
        chunks2.put(new IntPair(14, 16), "VP");
        chunks2.put(new IntPair(16, 17), "PP");
        chunks2.put(new IntPair(17, 18), "NP");

        chunks3.put(new IntPair(19, 21), "NP");
        chunks3.put(new IntPair(21, 22), "VP");
        chunks3.put(new IntPair(22, 23), "NP");
        chunks3.put(new IntPair(24, 26), "VP");
        chunks3.put(new IntPair(26, 27), "PP");
        chunks3.put(new IntPair(27, 28), "NP");

        ner1.put(new IntPair(4, 6), "PER");
    }

    static Map<IntPair, String> chunks_noisy1 = new HashMap<>();
    static Map<IntPair, String> chunks_noisy2 = new HashMap<>();
    static Map<IntPair, String> chunks_noisy3 = new HashMap<>();
    static Map<IntPair, String> ner_noisy1 = new HashMap<>();
    static Map<IntPair, String> ner_noisy2 = new HashMap<>();
    static Map<IntPair, String> ner_noisy3 = new HashMap<>();

    static {
        chunks_noisy1.put(new IntPair(0, 2), "NP");
        chunks_noisy1.put(new IntPair(2, 3), "PP");
        chunks_noisy1.put(new IntPair(3, 8), "NP");
        chunks_noisy1.put(new IntPair(8, 9), "PP");
        chunks_noisy1.put(new IntPair(9, 10), "NP");

        chunks_noisy2.put(new IntPair(11, 14), "NP");
        chunks_noisy2.put(new IntPair(14, 16), "VP");
        chunks_noisy2.put(new IntPair(16, 17), "PP");
        chunks_noisy2.put(new IntPair(17, 18), "ADJP");

        chunks_noisy3.put(new IntPair(19, 21), "NP");
        chunks_noisy3.put(new IntPair(21, 22), "VP");
        chunks_noisy3.put(new IntPair(22, 23), "ADJP");
        chunks_noisy3.put(new IntPair(24, 26), "VP");
        chunks_noisy3.put(new IntPair(26, 28), "NP");

        ner_noisy1.put(new IntPair(4, 6), "ORG");
        ner_noisy3.put(new IntPair(27, 28), "PER");
    }

    static IntPair verbSRLPredicate1 = new IntPair(7, 8);
    static IntPair verbSRLPredicate2 = new IntPair(15, 16);
    static IntPair verbSRLPredicate3 = new IntPair(21, 22);
    static IntPair verbSRLPredicate4 = new IntPair(25, 26);
    static Map<IntPair, String> verbSRLArgs1 = new HashMap<>();
    static Map<IntPair, String> verbSRLArgs2 = new HashMap<>();
    static Map<IntPair, String> verbSRLArgs3 = new HashMap<>();
    static Map<IntPair, String> verbSRLArgs4 = new HashMap<>();

    static String verbSRLPredicateSense = "01";
    static String verbSRLPredicateSense_noisy = "02";

    static {
        verbSRLArgs1.put(new IntPair(0, 7), "A0");
        verbSRLArgs1.put(new IntPair(8, 10), "AM-TMP");

        verbSRLArgs2.put(new IntPair(11, 14), "A1");
        verbSRLArgs2.put(new IntPair(16, 18), "AM-TMP");

        verbSRLArgs3.put(new IntPair(19, 21), "A0");
        verbSRLArgs3.put(new IntPair(22, 23), "AM-TMP");

        verbSRLArgs4.put(new IntPair(19, 21), "A0");
        verbSRLArgs4.put(new IntPair(24, 25), "AM-MOD");
        verbSRLArgs4.put(new IntPair(26, 28), "AM-TMP");
    }

    static Map<IntPair, String> verbSRLArgs_noisy1 = new HashMap<>();
    static Map<IntPair, String> verbSRLArgs_noisy2 = new HashMap<>();
    static Map<IntPair, String> verbSRLArgs_noisy3 = new HashMap<>();
    static Map<IntPair, String> verbSRLArgs_noisy4 = new HashMap<>();

    static {
        verbSRLArgs_noisy1.put(new IntPair(0, 7), "A0");
        verbSRLArgs_noisy1.put(new IntPair(8, 10), "A2");

        verbSRLArgs_noisy2.put(new IntPair(11, 14), "A1");
        verbSRLArgs_noisy2.put(new IntPair(16, 18), "AM-TMP");

        verbSRLArgs_noisy3.put(new IntPair(19, 21), "A0");
        verbSRLArgs_noisy3.put(new IntPair(22, 23), "AM-TMP");

        verbSRLArgs_noisy4.put(new IntPair(24, 25), "AM-MOD");
        verbSRLArgs_noisy4.put(new IntPair(26, 28), "AM-MNR");
    }

    private static String[] allPossibleViews = new String[] {ViewNames.POS, ViewNames.LEMMA,
            ViewNames.SHALLOW_PARSE, ViewNames.PARSE_GOLD, ViewNames.SRL_VERB, ViewNames.NER_CONLL,
            ViewNames.PSEUDO_PARSE_STANFORD, ViewNames.SENTENCE};

    /**
     * Generate a {@link TextAnnotation} (containing a set of {@link View}s specified in the input)
     * with the some dummy text repeated over a number of sentences.
     *
     * @param withNoisyLabels whether to nosify the annotation or not
     * @param sentenceNum The number of sentences that the {@link TextAnnotation} will contain
     * @return An annotated {@link TextAnnotation}
     */
    public static TextAnnotation generateAnnotatedTextAnnotation(boolean withNoisyLabels,
            int sentenceNum) {
        return generateAnnotatedTextAnnotation(allPossibleViews, withNoisyLabels, sentenceNum);
    }

    public static TextAnnotation generateAnnotatedTextAnnotation(String[] viewsToAdd,
            boolean withNoisyLabels, int sentenceNum) {
        // we can do at-most 3 sentences, for now
        if (sentenceNum > 3) {
            logger.error("Currently this function supports at most 3 sentences per TextAnnotation. If you need more, "
                    + "you have to augment this function");
            throw new RuntimeException();
        }

        // at least one sentence
        if (sentenceNum < 1) {
            logger.error("The requested TextAnnotation has to have at least one sentence. ");
            throw new RuntimeException();
        }

        List<String[]> annotatedTokenizedStringArrayAll = new ArrayList<>();
        annotatedTokenizedStringArrayAll.addAll(annotatedTokenizedStringArray1);
        if (sentenceNum > 1)
            annotatedTokenizedStringArrayAll.addAll(annotatedTokenizedStringArray2);
        if (sentenceNum > 2)
            annotatedTokenizedStringArrayAll.addAll(annotatedTokenizedStringArray3);
        TextAnnotation ta =
                BasicTextAnnotationBuilder
                        .createTextAnnotationFromTokens(annotatedTokenizedStringArrayAll);

        for (String viewName : viewsToAdd) {
            switch (viewName) {
                case ViewNames.SENTENCE:
                    SpanLabelView sentView = new SpanLabelView(ViewNames.SENTENCE, ta);
                    sentView.addConstituent(new Constituent("sent1", ViewNames.SENTENCE, ta, 0, pos1.length));
                    if (sentenceNum > 1)
                        sentView.addConstituent(new Constituent("sent2", ViewNames.SENTENCE, ta,
                                pos1.length, pos1.length + pos2.length));
                    if (sentenceNum > 2)
                        sentView.addConstituent(new Constituent("sent3", ViewNames.SENTENCE, ta,
                                pos1.length + pos2.length, pos1.length + pos2.length + pos3.length));
                    ta.addView(ViewNames.SENTENCE, sentView);
                    ta.setSentences();
                    break;
                case ViewNames.POS:
                    TokenLabelView posView = new TokenLabelView(viewName, ta);
                    String[] pos1Overall = withNoisyLabels ? pos_noisy1 : pos1;
                    String[] pos2Overall = withNoisyLabels ? pos_noisy2 : pos2;
                    String[] pos3Overall = withNoisyLabels ? pos_noisy3 : pos3;
                    for (int i = 0; i < pos1Overall.length; i++)
                        posView.addTokenLabel(i, pos1Overall[i], 1.0);
                    if (sentenceNum > 1)
                        for (int i = 0; i < pos2Overall.length; i++)
                            posView.addTokenLabel(pos1Overall.length + i, pos2Overall[i], 1.0);
                    if (sentenceNum > 2)
                        for (int i = 0; i < pos3Overall.length; i++)
                            posView.addTokenLabel(pos1Overall.length + pos2Overall.length + i,
                                    pos3Overall[i], 1.0);
                    ta.addView(viewName, posView);
                    break;
                case ViewNames.LEMMA:
                    TokenLabelView lemmaView = new TokenLabelView(ViewNames.LEMMA, ta);
                    String[] lemmaOveral1 = withNoisyLabels ? lemmas_noisy1 : lemmas1;
                    String[] lemmaOveral2 = withNoisyLabels ? lemmas_noisy2 : lemmas2;
                    String[] lemmaOveral3 = withNoisyLabels ? lemmas_noisy3 : lemmas3;
                    for (int i = 0; i < lemmaOveral1.length; i++)
                        lemmaView.addTokenLabel(i, lemmaOveral1[i], 1.0);
                    if (sentenceNum > 1)
                        for (int i = 0; i < lemmaOveral2.length; i++)
                            lemmaView.addTokenLabel(lemmaOveral1.length + i, lemmaOveral2[i], 1.0);
                    if (sentenceNum > 2)
                        for (int i = 0; i < lemmaOveral3.length; i++)
                            lemmaView.addTokenLabel(lemmaOveral1.length + lemmaOveral2.length + i,
                                    lemmaOveral3[i], 1.0);
                    ta.addView(viewName, lemmaView);
                    break;
                case ViewNames.SHALLOW_PARSE:
                    SpanLabelView chunkView = new SpanLabelView(ViewNames.SHALLOW_PARSE, ta);
                    Map<IntPair, String> chunkOverall1 = withNoisyLabels ? chunks_noisy1 : chunks1;
                    Map<IntPair, String> chunkOverall2 = withNoisyLabels ? chunks_noisy2 : chunks2;
                    Map<IntPair, String> chunkOverall3 = withNoisyLabels ? chunks_noisy3 : chunks3;
                    for (IntPair span : chunkOverall1.keySet())
                        chunkView.addSpanLabel(span.getFirst(), span.getSecond(),
                                chunkOverall1.get(span), 1.0);
                    if (sentenceNum > 1)
                        for (IntPair span : chunkOverall2.keySet())
                            chunkView.addSpanLabel(span.getFirst(), span.getSecond(),
                                    chunkOverall2.get(span), 1.0);
                    if (sentenceNum > 2)
                        for (IntPair span : chunkOverall3.keySet())
                            chunkView.addSpanLabel(span.getFirst(), span.getSecond(),
                                    chunkOverall3.get(span), 1.0);
                    ta.addView(ViewNames.SHALLOW_PARSE, chunkView);
                    break;
                case ViewNames.NER_CONLL:
                    SpanLabelView nerView = new SpanLabelView(ViewNames.NER_CONLL, ta);
                    Map<IntPair, String> nerSource1 = withNoisyLabels ? ner_noisy1 : ner1;
                    Map<IntPair, String> nerSource2 = withNoisyLabels ? ner_noisy2 : ner2;
                    Map<IntPair, String> nerSource3 = withNoisyLabels ? ner_noisy3 : ner3;
                    for (IntPair span : nerSource1.keySet())
                        nerView.addSpanLabel(span.getFirst(), span.getSecond(),
                                nerSource1.get(span), 1.0);
                    if (sentenceNum > 1)
                        for (IntPair span : nerSource2.keySet())
                            nerView.addSpanLabel(span.getFirst(), span.getSecond(),
                                    nerSource2.get(span), 1.0);
                    if (sentenceNum > 2)
                        for (IntPair span : nerSource3.keySet())
                            nerView.addSpanLabel(span.getFirst(), span.getSecond(),
                                    nerSource3.get(span), 1.0);
                    ta.addView(ViewNames.NER_CONLL, nerView);
                    break;
                case ViewNames.PARSE_GOLD:
                case ViewNames.PARSE_STANFORD:
                case ViewNames.PARSE_BERKELEY:
                case ViewNames.PSEUDO_PARSE_STANFORD:
                case ViewNames.PARSE_CHARNIAK:
                    TreeView parseView = new TreeView(viewName, ta);
                    String treeOveral1 = withNoisyLabels ? tree_noisy1 : tree1;
                    String treeOveral2 = withNoisyLabels ? tree_noisy2 : tree2;
                    String treeOveral3 = withNoisyLabels ? tree_noisy3 : tree3;
                    parseView.setParseTree(0,
                            TreeParserFactory.getStringTreeParser().parse(treeOveral1));
                    if (sentenceNum > 1)
                        parseView.setParseTree(1,
                                TreeParserFactory.getStringTreeParser().parse(treeOveral2));
                    if (sentenceNum > 2)
                        parseView.setParseTree(2,
                                TreeParserFactory.getStringTreeParser().parse(treeOveral3));
                    ta.addView(viewName, parseView);
                    break;
                case ViewNames.SRL_VERB:
                    PredicateArgumentView verbSRLView = new PredicateArgumentView(viewName, ta);

                    addSrlFrame(
                            verbSRLView,
                            viewName,
                            ta,
                            verbSRLPredicate1,
                            (withNoisyLabels ? verbSRLPredicateSense_noisy : verbSRLPredicateSense),
                            (withNoisyLabels ? verbSRLArgs_noisy1 : verbSRLArgs1));

                    if (sentenceNum > 1) {
                        addSrlFrame(verbSRLView, viewName, ta, verbSRLPredicate2,
                                (withNoisyLabels ? verbSRLPredicateSense_noisy
                                        : verbSRLPredicateSense),
                                (withNoisyLabels ? verbSRLArgs_noisy2 : verbSRLArgs2));
                    }

                    if (sentenceNum > 2) {
                        addSrlFrame(verbSRLView, viewName, ta, verbSRLPredicate3,
                                (withNoisyLabels ? verbSRLPredicateSense_noisy
                                        : verbSRLPredicateSense),
                                (withNoisyLabels ? verbSRLArgs_noisy3 : verbSRLArgs3));

                        addSrlFrame(verbSRLView, viewName, ta, verbSRLPredicate4,
                                (withNoisyLabels ? verbSRLPredicateSense_noisy
                                        : verbSRLPredicateSense),
                                (withNoisyLabels ? verbSRLArgs_noisy4 : verbSRLArgs4));
                    }

                    ta.addView(viewName, verbSRLView);

                    break;
                case ViewNames.NER_ONTONOTES:
                    // For now the NER views are going to be empty
                    ta.addView(viewName, new SpanLabelView(viewName, ta));
                default:
                    logger.error("Cannot provide annotation for {}", viewName);
            }
        }
        return ta;
    }

    private static void addSrlFrame(PredicateArgumentView srlView, String viewName,
            TextAnnotation ta, IntPair verbSRLPredicate, String sense, Map<IntPair, String> srlArgs) {
        Constituent predicate =
                new Constituent("predicate", viewName, ta, verbSRLPredicate.getFirst(),
                        verbSRLPredicate.getSecond());
        predicate.addAttribute(PredicateArgumentView.LemmaIdentifier,
                lemmasAll[verbSRLPredicate.getFirst()]);
        predicate.addAttribute(PredicateArgumentView.SenseIdentifer, sense);
        List<Constituent> args = new ArrayList<>();
        List<String> tempArgLabels = new ArrayList<>();
        for (IntPair span : srlArgs.keySet()) {
            args.add(new Constituent("argument", viewName, ta, span.getFirst(), span.getSecond()));
            tempArgLabels.add(srlArgs.get(span));
        }
        String[] argLabels = tempArgLabels.toArray(new String[args.size()]);
        double[] scores = new double[args.size()];
        srlView.addPredicateArguments(predicate, args, argLabels, scores);

    }
}
