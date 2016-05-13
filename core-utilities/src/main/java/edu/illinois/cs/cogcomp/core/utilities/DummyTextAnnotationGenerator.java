/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CoNLLColumnFormatReader;
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

    static String basicString = "To annotate or not ; that is the question .";

    static String annotatedString1 = "The construction of the John Smith library finished on time .";
    static String annotatedString2 = "The $10M building was designed in 2016 .";
    static String annotatedString3 = "The paving commenced Monday and will finish in June .";

    static List<String[]> annotatedTokenizedStringArray;
    static{
        annotatedTokenizedStringArray = new ArrayList<>();
        annotatedTokenizedStringArray.add(annotatedString1.split(" "));
        annotatedTokenizedStringArray.add(annotatedString2.split(" "));
        annotatedTokenizedStringArray.add(annotatedString3.split(" "));
    };

    static String[] pos = {"DT", "NN", "IN", "DT", "NNP", "NNP", "NN", "VBD", "IN", "NN", ".",
                            "DT", "NN", "NN", "VBD", "VBN", "IN", "CD", ".",
                            "DT", "VBG", "VBD", "NNP", "CC", "MD", "VB", "IN", "NNP", "." };
    static String[] pos_noisy = {"DT", "NNA", "DT", "DT", "NNP", "NN", "NN", "VB", "IN", "NN", ".",
                                "DT", "NNS", "NN", "VB", "VBN", "IN", "NN", ".",
                                "DT", "VBG", "VBD", "NNP", "CC", "MD", "VB", "IN", "NN", "." };
    static String[] lemmas =
            {"the", "construction", "of", "the", "John", "Smith", "library", "finish", "on", "time", ".",
                    "The", "$10M", "building", "be", "design", "in", "2016", "." +
                            "The", "paving", "commence", "Monday", "and", "will", "finish", "in", "June", "."
            };
    static String[] lemmas_noisy =
            {"the", "construct", "of", "the", "John", "Smith", "library", "fin", "on", "time", ".",
            "The", "$10M", "build", "be", "design", "in", "2016", "." +
            "The", "pave", "commence", "Monday", "and", "will", "finish", "in", "June", "." };

    static String tree =
            "(S1 (S (NP (NP (DT The) (NN construction)) (PP (IN of) (NP (DT the) (NNP John) (NNP Smith) (NN library)))) "
                    + "(VP (VBD finished) (PP (IN on) (NP (NN time)))) (. .)))";
    static String tree2 = "(S1 (S (NP (DT The) (JJ $10M) (NN building)) (VP (AUX was) (VP (VBN designed) (PP (IN in) " +
            "(NP (CD 2016)))))(. .)))";
    static String tree3 = "(S1 (S (NP (DT The)(NN paving))(VP (VP (VBD commenced)(NP (NNP Monday)))(CC and)(VP (MD will)(VP (VB finish)(PP (IN in)(NP (NNP June))))))(. .)))";

    static String tree_noisy =
            "(S1 (S (NP (NP (DT The) (NNA construction) (IN of)) (PP (NP (DT the) (NNP John) (NN Smith) (NN library)))) "
                    + "(VP (VB finished) (PP (IN on) (NP (NN time)))) (. .)))";
    static String tree_noisy2 = "(S1 (S (NP (DT The) (NN $10M) (NN building)) (VP (AUX was) (VP (VBN designed) (PP (IN in) " +
            "(NP (CD 2016)))))(. .)))";
    static String tree_noisy3 = "(S1 (S (NP (DT The) (JJ paving)) (VP (VP (VBD commenced) (NP (NNP Monday))) (CC and)" +
            "(VP (MD will) (VP (VB finish) (PP (IN in) (NP (NNP June)))))) (. .)))";

    static Map<IntPair, String> chunks = new HashMap<>();
    static Map<IntPair, String> ner = new HashMap<>();

    static {
        chunks.put(new IntPair(0, 2), "NP");
        chunks.put(new IntPair(2, 3), "PP");
        chunks.put(new IntPair(3, 7), "NP");
        chunks.put(new IntPair(7, 8), "VP");
        chunks.put(new IntPair(8, 9), "PP");
        chunks.put(new IntPair(9, 10), "NP");

        chunks.put(new IntPair(11, 14), "NP");
        chunks.put(new IntPair(14, 16), "VP");
        chunks.put(new IntPair(16, 17), "PP");
        chunks.put(new IntPair(17, 18), "NP");

        chunks.put(new IntPair(19, 21), "NP");
        chunks.put(new IntPair(21, 22), "VP");
        chunks.put(new IntPair(22, 23), "NP");
        chunks.put(new IntPair(24, 26), "VP");
        chunks.put(new IntPair(26, 27), "PP");
        chunks.put(new IntPair(27, 28), "NP");

        ner.put(new IntPair(4, 6), "PER" );
    }

    static Map<IntPair, String> chunks_noisy = new HashMap<>();
    static Map<IntPair, String> ner_noisy = new HashMap<>();

    static {
        chunks_noisy.put(new IntPair(0, 2), "NP");
        chunks_noisy.put(new IntPair(2, 3), "PP");
        chunks_noisy.put(new IntPair(3, 8), "NP");
        chunks_noisy.put(new IntPair(8, 9), "PP");
        chunks_noisy.put(new IntPair(9, 10), "NP");

        chunks_noisy.put(new IntPair(11, 14), "NP");
        chunks_noisy.put(new IntPair(14, 16), "VP");
        chunks_noisy.put(new IntPair(16, 17), "PP");
        chunks_noisy.put(new IntPair(17, 18), "ADJP");

        chunks_noisy.put(new IntPair(19, 21), "NP");
        chunks_noisy.put(new IntPair(21, 22), "VP");
        chunks_noisy.put(new IntPair(22, 23), "ADJP");
        chunks_noisy.put(new IntPair(24, 26), "VP");
        chunks_noisy.put(new IntPair(26, 28), "NP" );


        ner_noisy.put( new IntPair(4, 6), "ORG" );
        ner_noisy.put( new IntPair(27, 28), "PER" );
    }

    static IntPair verbSRLPredicate = new IntPair(7, 8);
    static String verbSRLPredicateSense = "01";
    static String verbSRLPredicateSense_noisy = "02";
    static Map<IntPair, String> verbSRLArgs = new HashMap<>();
    static Map<IntPair, String> verbSRLArgs2 = new HashMap<>();
    static Map<IntPair, String> verbSRLArgs3 = new HashMap<>();
    static Map<IntPair, String> verbSRLArgs4 = new HashMap<>();


    static IntPair verbSRLPredicate2 = new IntPair(15, 16);
    static IntPair verbSRLPredicate3 = new IntPair(21, 22);
    static IntPair verbSRLPredicate4 = new IntPair(25, 26);

    static {
        verbSRLArgs.put(new IntPair(0, 7), "A0");
        verbSRLArgs.put(new IntPair(8, 10), "AM-TMP");

        verbSRLArgs2.put(new IntPair(11, 14), "A1");
        verbSRLArgs2.put(new IntPair(16, 18), "AM-TMP");

        verbSRLArgs3.put(new IntPair(19, 21), "A0");
        verbSRLArgs3.put(new IntPair(22, 23), "AM-TMP");

        verbSRLArgs4.put(new IntPair(19, 21), "A0");
        verbSRLArgs4.put(new IntPair(24, 25), "AM-MOD");
        verbSRLArgs4.put(new IntPair(26, 28), "AM-TMP");

    }

    static Map<IntPair, String> verbSRLArgs_noisy = new HashMap<>();
    static Map<IntPair, String> verbSRLArgs_noisy2 = new HashMap<>();
    static Map<IntPair, String> verbSRLArgs_noisy3 = new HashMap<>();
    static Map<IntPair, String> verbSRLArgs_noisy4 = new HashMap<>();

    static {
        verbSRLArgs_noisy.put(new IntPair(0, 7), "A0");
        verbSRLArgs_noisy.put(new IntPair(8, 10), "A2");

        verbSRLArgs_noisy2.put(new IntPair(11, 14), "A1");
        verbSRLArgs_noisy2.put(new IntPair(16, 18), "AM-TMP");

        verbSRLArgs_noisy3.put(new IntPair(19, 21), "A0");
        verbSRLArgs_noisy3.put(new IntPair(22, 23), "AM-TMP");

        verbSRLArgs_noisy4.put(new IntPair(24, 25), "AM-MOD");
        verbSRLArgs_noisy4.put(new IntPair(26, 28), "AM-MNR");

    }

    /**
     * Generate a basic {@link TextAnnotation} (containing just SENTENCE and TOKENS {@link View}s)
     * with the same dummy text repeated over a number of sentences.
     *
     * @param numSentences The number of sentences that the {@link TextAnnotation} will contain
     * @return A dummy basic {@link TextAnnotation}
     */
    public static TextAnnotation generateBasicTextAnnotation(int numSentences) {
        int i = 0;
        List<String[]> docs = new ArrayList<>();
        while (i < numSentences) {
            docs.add(basicString.split(" "));
            i++;
        }
        return BasicTextAnnotationBuilder.createTextAnnotationFromTokens(docs);
    }

    private static String[] allPossibleViews = new String[] {ViewNames.POS, ViewNames.LEMMA,
            ViewNames.SHALLOW_PARSE, ViewNames.PARSE_GOLD, ViewNames.SRL_VERB, ViewNames.NER_CONLL,
            ViewNames.PSEUDO_PARSE_STANFORD };

    public static TextAnnotation generateAnnotatedTextAnnotation(boolean withNoisyLabels) {
        return generateAnnotatedTextAnnotation(allPossibleViews, withNoisyLabels);
    }

    public static TextAnnotation generateAnnotatedTextAnnotation(String[] viewsToAdd,
            boolean withNoisyLabels) {
        TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(annotatedTokenizedStringArray);

        for (String viewName : viewsToAdd) {
            switch (viewName) {
                case ViewNames.POS:
                    TokenLabelView posView = new TokenLabelView(viewName, ta);
                    for (int i = 0; i < pos.length; i++)
                        if (withNoisyLabels)
                            posView.addTokenLabel(i, pos_noisy[i], 1.0);
                        else
                            posView.addTokenLabel(i, pos[i], 1.0);
                    ta.addView(viewName, posView);
                    break;
                case ViewNames.LEMMA:
                    TokenLabelView lemmaView = new TokenLabelView(ViewNames.LEMMA, ta);
                    for (int i = 0; i < lemmas.length; i++)
                        if (withNoisyLabels)
                            lemmaView.addTokenLabel(i, lemmas_noisy[i], 1.0);
                        else
                            lemmaView.addTokenLabel(i, lemmas[i], 1.0);
                    ta.addView(viewName, lemmaView);
                    break;
                case ViewNames.SHALLOW_PARSE:
                    SpanLabelView chunkView = new SpanLabelView(ViewNames.SHALLOW_PARSE, ta);
                    if (withNoisyLabels)
                        for (IntPair span : chunks_noisy.keySet())
                            chunkView.addSpanLabel(span.getFirst(), span.getSecond(),
                                    chunks_noisy.get(span), 1.0);
                    else
                        for (IntPair span : chunks.keySet())
                            chunkView.addSpanLabel(span.getFirst(), span.getSecond(),
                                    chunks.get(span), 1.0);
                    ta.addView(ViewNames.SHALLOW_PARSE, chunkView);
                    break;
                case ViewNames.NER_CONLL:
                    SpanLabelView nerView = new SpanLabelView(ViewNames.NER_CONLL, ta );
                    Map<IntPair, String> nerSource = (withNoisyLabels ? ner_noisy : ner );
                    for (IntPair span : nerSource.keySet() )
                        nerView.addSpanLabel(span.getFirst(), span.getSecond(), ner_noisy.get( span ), 1.0 );
                    ta.addView( ViewNames.NER_CONLL, nerView );
                    break;
                case ViewNames.PARSE_GOLD:
                case ViewNames.PARSE_STANFORD:
                case ViewNames.PARSE_BERKELEY:
                case ViewNames.PSEUDO_PARSE_STANFORD:
                case ViewNames.PARSE_CHARNIAK:
                    TreeView parseView = new TreeView(viewName, ta);
                    if (withNoisyLabels) {
                        parseView.setParseTree(0,
                                TreeParserFactory.getStringTreeParser().parse(tree_noisy));
                        parseView.setParseTree(1,
                                TreeParserFactory.getStringTreeParser().parse(tree_noisy2));
                        parseView.setParseTree(2,
                                TreeParserFactory.getStringTreeParser().parse(tree_noisy3));
                    }
                    else {
                        parseView.setParseTree(0,
                                TreeParserFactory.getStringTreeParser().parse(tree));
                        parseView.setParseTree(1,
                                TreeParserFactory.getStringTreeParser().parse(tree2));
                        parseView.setParseTree(2,
                                TreeParserFactory.getStringTreeParser().parse(tree3));
                    }
                    ta.addView(viewName, parseView);
                    break;
                case ViewNames.SRL_VERB:
                    PredicateArgumentView verbSRLView = new PredicateArgumentView(viewName, ta);

                    addSrlFrame( verbSRLView,
                            viewName,
                            ta,
                            verbSRLPredicate,
                            ( withNoisyLabels ? verbSRLPredicateSense_noisy : verbSRLPredicateSense),
                            ( withNoisyLabels ? verbSRLArgs_noisy : verbSRLArgs)
                        );

                    addSrlFrame( verbSRLView,
                            viewName,
                            ta,
                            verbSRLPredicate2,
                            ( withNoisyLabels ? verbSRLPredicateSense_noisy : verbSRLPredicateSense),
                            ( withNoisyLabels ? verbSRLArgs_noisy2 : verbSRLArgs)
                    );

                    addSrlFrame( verbSRLView,
                            viewName,
                            ta,
                            verbSRLPredicate3,
                            ( withNoisyLabels ? verbSRLPredicateSense_noisy : verbSRLPredicateSense),
                            ( withNoisyLabels ? verbSRLArgs_noisy3 : verbSRLArgs)
                    );

                    addSrlFrame( verbSRLView,
                            viewName,
                            ta,
                            verbSRLPredicate4,
                            ( withNoisyLabels ? verbSRLPredicateSense_noisy : verbSRLPredicateSense),
                            ( withNoisyLabels ? verbSRLArgs_noisy4 : verbSRLArgs)
                    );

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

    private static void addSrlFrame(PredicateArgumentView srlView,
                                    String viewName,
                                    TextAnnotation ta,
                                    IntPair verbSRLPredicate,
                                    String sense,
                                    Map<IntPair, String> srlArgs) {
        Constituent predicate =
                new Constituent("predicate", viewName, ta, verbSRLPredicate.getFirst(),
                        verbSRLPredicate.getSecond());
        predicate.addAttribute(CoNLLColumnFormatReader.LemmaIdentifier,
                lemmas[verbSRLPredicate.getFirst()]);
            predicate.addAttribute(CoNLLColumnFormatReader.SenseIdentifer,
                    sense);
        List<Constituent> args = new ArrayList<>();
        List<String> tempArgLabels = new ArrayList<>();
        for (IntPair span : srlArgs.keySet()) {
            args.add(new Constituent("argument", viewName, ta, span.getFirst(), span
                    .getSecond()));
                tempArgLabels.add(srlArgs.get(span));
        }
        String[] argLabels = tempArgLabels.toArray(new String[args.size()]);
        double[] scores = new double[args.size()];
        srlView.addPredicateArguments(predicate, args, argLabels, scores);

    }

}
