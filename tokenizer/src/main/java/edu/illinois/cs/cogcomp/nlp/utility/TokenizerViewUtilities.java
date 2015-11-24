package edu.illinois.cs.cogcomp.nlp.utility;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.lbjava.nlp.Sentence;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Srikumar
 */
public class TokenizerViewUtilities {

    public static final Tokenizer lbjTokenizer = new IllinoisTokenizer();

    /**
     * The elements of this enumeration generate views called {@code SENTENCE}.
     * Most of the time, you will not have to use this view generator.
     * <p/>
     * Each element of {@code SentenceViewGenerators} implements the
     * {@code getViewGenerator()} function, which creates a view from a
     * {@code TextAnnotation}.
     * <p/>
     * Elements of this enumeration, in addition to generating the view, <i>also
     * tokenize</i> the sentence. Since a {@code TextAnnotation} can be
     * tokenized only once, only elements of this enumeration should be used to
     * tokenize sentences unless you want a custom tokenizer.
     */
    public enum SentenceViewGenerators implements Annotator {

        /**
         * This sentence view generator tokenizes text with the {@link edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer }
         * and uses the LBJ sentence splitter to split sentences.
         */
        LBJSentenceViewGenerator {
            @Override
            public String getViewName() {
                return ViewNames.SENTENCE;
            }

            @Override
            public View getView(TextAnnotation ta) {
                return TokenizerViewUtilities.addTokenView(ta, lbjTokenizer, "LBJ");
			}

            @Override
            public String[] getRequiredViews() {
                return new String[0];
            }
        }
    }

    public static SpanLabelView addTokenView(TextAnnotation input, Tokenizer tokenizer, String source) {
        SentenceSplitter splitter = new SentenceSplitter(new String[]{input.getText()});

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

                IntPair translatedCharOffset = new IntPair(
                        charOffset.getFirst() + s.start, charOffset.getSecond() + s.start);
                charOffsets.add(translatedCharOffset);

            }

            sentenceSpans.add(new IntPair(start, tokens.size()));

            start = tokens.size();
        }

        if ( tokens.size() != charOffsets.size() )
            throw new IllegalArgumentException( "tokens (" + tokens.size() + ") must equal charOffsets (" +
            charOffsets.size() + "), but does not.");

        SpanLabelView tokView = new SpanLabelView(ViewNames.TOKENS, source, input, 1.0 );
        SpanLabelView view = new SpanLabelView(ViewNames.SENTENCE, source, input, 1.0);
        for ( int i = 0; i < tokens.size(); ++i )
        {
            tokView.addSpanLabel(i, i+1, tokens.get( i ), 1d );
        }
        for (IntPair span : sentenceSpans) {
            view.addSpanLabel(span.getFirst(), span.getSecond(), ViewNames.SENTENCE, 1d);
        }

        return tokView;
    }
}
