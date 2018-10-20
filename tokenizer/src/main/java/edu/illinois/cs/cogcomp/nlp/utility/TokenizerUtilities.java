/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.utility;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbjava.nlp.Sentence;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Srikumar
 */
public class TokenizerUtilities {

    public static SpanLabelView addTokenView(TextAnnotation input, Tokenizer tokenizer,
            String source) {
        SentenceSplitter splitter = new SentenceSplitter(new String[] {input.getText()});

        Sentence[] sentences = splitter.splitAll();
        List<String> tokens = new ArrayList<>();
        List<IntPair> charOffsets = new ArrayList<>();

        List<IntPair> sentenceSpans = new ArrayList<>();

        int start = 0;

        for (Sentence s : sentences) {

            Pair<String[], IntPair[]> toks = tokenizer.tokenizeSentence(s.text);

            for (int i = 0; i < toks.getFirst().length; i++) {
                tokens.add(toks.getFirst()[i]);
                IntPair charOffset = toks.getSecond()[i];

                IntPair translatedCharOffset =
                        new IntPair(charOffset.getFirst() + s.start, charOffset.getSecond()
                                + s.start);
                charOffsets.add(translatedCharOffset);

            }

            sentenceSpans.add(new IntPair(start, tokens.size()));

            start = tokens.size();
        }

        if (tokens.size() != charOffsets.size())
            throw new IllegalArgumentException("tokens (" + tokens.size()
                    + ") must equal charOffsets (" + charOffsets.size() + "), but does not.");

        SpanLabelView tokView = new SpanLabelView(ViewNames.TOKENS, source, input, 1.0);
        SpanLabelView view = new SpanLabelView(ViewNames.SENTENCE, source, input, 1.0);
        for (int i = 0; i < tokens.size(); ++i) {
            tokView.addSpanLabel(i, i + 1, tokens.get(i), 1d);
        }
        for (IntPair span : sentenceSpans) {
            view.addSpanLabel(span.getFirst(), span.getSecond(), ViewNames.SENTENCE, 1d);
        }

        return tokView;
    }
}
