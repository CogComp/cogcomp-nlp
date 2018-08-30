package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SpanLabelViewTest {
    // test that addConstituent(Constituent) does not allow overlapping spans
    SpanLabelView overlappingSpansView;
    SpanLabelView noOverlappingSpansView;
    TextAnnotation ta;
    Constituent baseConstituent;
    Constituent overlappingConstituent;

    private Tokenizer.Tokenization tokenization;

    String viewName = "VIEWNAME";
    String viewGenerator = "VIEW-GENERATOR";
    String text = "This is a test string; do not pay it any mind.";
    String corpusId = "TEST";
    String textId = "ID";

    double score = 42.0;
    int baseStart = 0;
    int baseEnd = 5;
    int overStart = 2;
    int overEnd = 6;

    private Tokenizer.Tokenization getTokenization(String text) {
        String[] tokens = text.split("\\s");
        List<IntPair> characterOffsets = new ArrayList<>();
        int[] sentenceEndArray = {tokens.length};

        int charOffsetBegin = 0;
        int charOffsetEnd = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                charOffsetEnd = i;
                IntPair tokenOffsets = new IntPair(charOffsetBegin, charOffsetEnd);
                characterOffsets.add(tokenOffsets);
                charOffsetBegin = charOffsetEnd + 1;
            }
        }
        IntPair tokenOffsets = new IntPair(charOffsetBegin, text.length());
        characterOffsets.add(tokenOffsets);

        IntPair[] charOffsetArray = new IntPair[characterOffsets.size()];

        for (int i = 0; i < characterOffsets.size(); i++) {
            charOffsetArray[i] = characterOffsets.get(i);
        }
        Tokenizer.Tokenization tokenization =
                new Tokenizer.Tokenization(tokens, charOffsetArray, sentenceEndArray);
        return tokenization;
    }

    @Before
    public void init(){
        TextAnnotationBuilder taBuilder = new BasicTextAnnotationBuilder();
        ta = taBuilder.createTextAnnotation(this.corpusId, this.textId, this.text, getTokenization(this.text));
        boolean allowOverlappingSpans = true;
        overlappingSpansView = new SpanLabelView(this.viewName, this.viewGenerator,
                ta, this.score, allowOverlappingSpans);
        allowOverlappingSpans = false;
        noOverlappingSpansView = new SpanLabelView(this.viewName, this.viewGenerator,
                ta, this.score, allowOverlappingSpans);

        baseConstituent = new Constituent("BASE", this.score, this.viewName, ta, baseStart, baseEnd);
        overlappingConstituent = new Constituent("OVER", this.score, this.viewName, ta, overStart, overEnd);
    }

    @Test
    public void testOverlappingSpans(){
        overlappingSpansView.addConstituent(baseConstituent);
        overlappingSpansView.addConstituent(overlappingConstituent);
        for(Constituent c : overlappingSpansView.getConstituents()){
            if(c.getLabel().equals("BASE")) {
                assert c.getStartSpan() == this.baseStart;
                assert c.getEndSpan() == this.baseEnd;
            }else {
                assert c.getStartSpan() == this.overStart;
                assert c.getEndSpan() == this.overEnd;
            }
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNoOverlappingSpans(){
        noOverlappingSpansView.addConstituent(baseConstituent);
        noOverlappingSpansView.addConstituent(overlappingConstituent);
    }
}
